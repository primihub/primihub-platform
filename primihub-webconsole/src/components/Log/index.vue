<template>
  <div class="log">
    <el-button :disabled="errorLog.length === 0" size="small" type="danger" plain style="margin-bottom: 10px" @click="showErrorLog">ERROR ({{ errorLog.length }})</el-button>
    <div class="log-container">
      <template v-if="logData.length>0">
        <p v-for="(item,index) in logData" :id="(logData.length === index+1)?'scrollLog':''" :key="index" class="item">
          <span v-if="logValueType === 'object'">
            {{ item.log }}
          </span>
          <span v-else>
            {{ item }}
          </span>
        </p>
      </template>
      <template v-else>
        <p>
          {{ text }}
          <i v-if="logData.length === 0 && text === '日志加载中'" class="el-icon-loading" />
        </p>
      </template>
    </div>
  </div>

</template>

<script>
import { getTaskLogInfo } from '@/api/task'

export default {
  name: 'Log',
  props: {
    taskState: {
      type: Number,
      default: 0
    },
    taskId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      text: '日志加载中', // 暂无日志数据
      ws: null,
      logData: [],
      query: '',
      start: '',
      errorLog: [],
      logType: '',
      logValueType: 'string',
      lockReconnect: false,
      connectTimer: null,
      serverTimeout: null,
      heartbeatTimer: null,
      timeout: 5 * 1000, // 5秒一次心跳
      destroyed: false,
      connectCount: 0
    }
  },
  async created() {
    console.log('显示')
    await this.getTaskLogInfo()
    this.socketInit()
  },
  beforeDestroy() {
    this.ws && this.ws.close()
    this.destroyed = true
    // 清除时间
    clearInterval(this.heartbeatTimer)
    clearInterval(this.connectTimer)
    clearTimeout(this.serverTimeout)
  },
  methods: {
    socketInit() {
      console.log('init connectCount', this.connectCount)
      if (!this.address) {
        this.text = '暂无数据'
        return
      }
      if (this.connectCount === 10) {
        this.text = '暂无数据'
        this.reset()
        return
      }
      this.connectCount += 1
      this.text = '日志加载中'
      const protocol = document.location.protocol === 'https:' ? 'wss' : 'ws'
      const url = `${protocol}://${this.address}/loki/api/v1/tail?start=${this.start}&query=${this.query}&limit=1000`
      this.ws = new WebSocket(url)
      this.ws.onopen = this.open
      this.ws.onerror = this.error
      this.ws.onmessage = this.getMessage
      this.ws.onsend = this.send
      this.ws.onclose = this.close
    },
    open: function() {
      console.log('socket连接成功', this.connectCount)
      this.heartbeatStart()
      setTimeout(() => {
        if (this.ws.readyState === 1 && this.logData.length === 0) {
          this.reconnect()
        }
      }, 2000)
    },
    error: function() {
      console.log('连接错误')
      // 关闭心跳定时器
      clearInterval(this.heartbeatTimer)
      // 关闭重连定时器
      clearInterval(this.connectTimer)
      clearTimeout(this.serverTimeout)
      this.text = '暂无数据'
    },
    close: function(e) {
      this.ws.close()
      console.log('socket已经关闭', e)
      if (!this.destroyed) {
        this.reconnect()
      }
    },
    reconnect() {
      if (this.lockReconnect) {
        return
      }
      this.lockReconnect = true
      // 没连接上会一直重连，设置延迟避免请求过多
      this.connectTimer && clearInterval(this.connectTimer)
      this.connectTimer = setInterval(() => {
        this.socketInit()// 新连接
        this.lockReconnect = false
      }, 5000)
    },
    reset() {
      // 关闭心跳定时器
      clearInterval(this.heartbeatTimer)
      // 关闭重连定时器
      clearInterval(this.connectTimer)
      clearTimeout(this.serverTimeout)
    },
    heartbeatStart() { // 开启心跳
      this.heartbeatTimer && clearInterval(this.heartbeatTimer)
      this.serverTimeout && clearTimeout(this.serverTimeout)
      this.heartbeatTimer = setInterval(() => {
        if (this.ws.readyState === 1) {
          this.ws.send(JSON.stringify({
            data: {
              messageType: 'heartCheck'
            }
          }))
        } else {
          this.reconnect()
        }
        this.serverTimeout = setTimeout(() => {
          // 超时关闭
          this.ws.close()
          this.destroyed = true
        }, 3000)
      }, this.timeout)
    },
    getMessage: function(msg) {
      if (msg.data.length > 0) {
        this.reset()// 收到服务器信息，心跳重置
        const data = JSON.parse(msg.data).streams
        const formatData = data.map(item => {
          this.logValueType = typeof item.values[0][1]
          if (this.logValueType === 'object') {
            const value = JSON.parse(item.values[0][1])
            if (value.log !== '\n') {
              value.log = value.time.split('T')[0] + ' ' + value.log
              return value
            }
            return item.values
          } else {
            let log = JSON.parse(item.values[0][1]).log
            log = log.replace(/\[\d+m/ig, '')
            log = log.replace(/\\+/ig, '')
            return log
          }
        })
        this.logData = this.logData.concat(formatData)
        this.$nextTick(() => {
          this.scrollToTarget('scrollLog')
        })
        this.filterErrorLog()
      }
    },
    filterErrorLog() {
      if (this.logValueType === 'string') {
        this.errorLog = this.logData.filter(item => item.indexOf('ERROR') !== -1)
      } else {
        this.errorLog = this.logData.filter(item => item.log.indexOf('ERROR') !== -1)
      }
      this.$emit('error', this.errorLog)
    },
    send: function(order) {
      this.ws.send(order)
    },
    async getTaskLogInfo() {
      const taskId = this.taskId || this.$route.params.taskId
      const res = await getTaskLogInfo(taskId)
      if (res.code === 0) {
        const { container, job, taskIdName, start, address } = res.result
        this.start = start
        this.address = address
        if (job === 'false') {
          this.query = `{container_name="${container}"}|="${taskIdName}"`
        } else {
          this.query = `{job ="${job}", container="${container}"}|="${taskIdName}"`
        }
      }
    },
    scrollToTarget(target, block = 'start') {
      const element = document.getElementById(target)
      element && element.scrollIntoView({ behavior: 'smooth', block })
    },
    showErrorLog() {
      this.logType = 'ERROR'
      this.query += `|="ERROR"`
      this.logData = []
      this.reset()
      this.socketInit()
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .table,::v-deep .table  tr{
  background: transparent;
  color: #fff;
}
::v-deep .el-table--enable-row-hover .el-table__body tr:hover>td.el-table__cell{
  background-color: rgba($color: #000000, $alpha: 0.1);
}
::v-deep .el-table::before{
  height: 0;
}
::v-deep .el-table td.el-table__cell, .el-table th.el-table__cell.is-leaf{
  border: none;
}
.log-container{
  background-color: #112435;
  color: #fff;
  font-size: 14px;
  padding: 20px;
  overflow-y: scroll;
  overflow-x: hidden;
  width: 100%;
  height: 500px;
  .item{
    display: inline-block;
    margin: 10px 0;
    line-height: 1.5;
    word-break:break-all;
    .state-error{
      color: rgba(245, 108, 108);
    }
  }
}
</style>
