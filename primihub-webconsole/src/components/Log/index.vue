<template>
  <div class="log">
    <el-button size="small" type="danger" plain style="margin-bottom: 10px" @click="showErrorLog">ERROR ({{ errorLog.length }})</el-button>
    <div class="log-container">
      <template v-if="logData.length>0">
        <p v-for="(item,index) in logData" :id="(logData.length === index+1)?'scrollLog':''" :key="index" class="item">
          {{ item.log }}
        </p>
      </template>
      <template v-else>
        <p>暂无日志数据</p>
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
      ws: null,
      logData: [],
      query: '',
      start: '',
      errorLog: [],
      logType: ''
    }
  },
  async mounted() {
    await this.getTaskLogInfo()
    this.socketInit()
  },
  destroyed() {
    this.ws.close()
  },
  methods: {
    socketInit() {
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
      console.log('socket连接成功')
    },
    error: function() {
      console.log('连接错误')
    },
    close: function(e) {
      this.ws.close()
      if (this.taskState === 2) {
        this.socketInit()
      }
      console.log('socket已经关闭', e)
    },
    getMessage: function(msg) {
      if (msg.data.length > 0) {
        const data = JSON.parse(msg.data).streams
        const formatData = data.map(item => {
          const value = JSON.parse(item.values[0][1])
          if (value.log !== '\n') {
            value.log = value.time.split('T')[0] + ' ' + value.log
            // filter ERROR log
            if (value.log.indexOf('ERROR') !== -1) {
              this.errorLog.push(value)
              this.$emit('error', this.errorLog)
            }
            return value
          }
        })
        this.logData = this.logData.concat(formatData)
        this.$nextTick(() => {
          this.scrollToTarget('scrollLog')
        })
      }
    },
    send: function(order) {
      console.log(order)
      this.ws.send(order)
    },
    async getTaskLogInfo() {
      console.log(this.taskId)
      const taskId = this.taskId || this.$route.params.taskId
      const res = await getTaskLogInfo(taskId)
      if (res.code === 0) {
        const { container, job, taskIdName, start, address } = res.result
        this.start = start
        this.address = address
        this.query = `{job ="${job}", container="${container}"}|="${taskIdName}"`
      }
    },
    scrollToTarget(target, block = 'end') {
      const element = document.getElementById(target)
      element.scrollIntoView({ behavior: 'smooth', block })
    },
    showErrorLog() {
      this.logType = 'error'
      this.query += `|="ERROR"`
      this.logData = []

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
    margin-bottom: 10px;
    line-height: 1.5;
    word-break:break-all;
    .state-error{
      color: rgba(245, 108, 108);
    }
  }
}
</style>
