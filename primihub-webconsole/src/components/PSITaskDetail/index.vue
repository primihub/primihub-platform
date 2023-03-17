<template>
  <div class="task-detail">
    <el-descriptions title="基本信息" :column="2" label-class-name="detail-title">
      <el-descriptions-item label="任务结果名称">{{ data.resultName }}</el-descriptions-item>
      <el-descriptions-item label="任务ID">{{ data.taskIdName }}</el-descriptions-item>
      <el-descriptions-item label="输出内容">{{ data.outputContent=== 0? '交集': '差集' }}</el-descriptions-item>
      <el-descriptions-item label="输出格式">{{ data.outputFormat === '0'? '资源文件(csv)': '' }}</el-descriptions-item>
      <el-descriptions-item label="实现协议">{{ data.tag === 0? 'ECDH': 'KKRT' }}</el-descriptions-item>
      <el-descriptions-item label="关键键有重复值时">{{ data.outputNoRepeat === 1? '去重': '不去重' }}</el-descriptions-item>
      <el-descriptions-item v-if="data.taskState === 1 && showDownload" label="任务结果"><el-button class="download-button" type="text" @click="downloadPsiTask">{{ data.resultName }}.csv <i class="el-icon-download" /></el-button></el-descriptions-item>
    </el-descriptions>
    <el-descriptions title="数据配置" :column="2" label-class-name="detail-title">
      <el-descriptions-item label="发起方">{{ data.ownOrganName }}</el-descriptions-item>
      <el-descriptions-item label="协作方">{{ data.otherOrganName }}</el-descriptions-item>
      <el-descriptions-item label="资源表">
        <el-link type="primary" @click="toResourceDetailPage(data.ownResourceId)">{{ data.ownResourceName }}</el-link>
      </el-descriptions-item>
      <el-descriptions-item label="资源表">
        <el-link type="primary" @click="toResourceDetailPage(data.otherResourceId)">{{ data.otherResourceName }}</el-link>
      </el-descriptions-item>
      <el-descriptions-item label="关联键">{{ data.ownKeyword }}</el-descriptions-item>
      <el-descriptions-item label="关联键">{{ data.otherKeyword }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'

export default {
  name: 'PSITaskDetail',
  filters: {
    // 运行状态 0未运行 1完成 2运行中 3失败 默认0
    taskStateFilter(state) {
      const stateMap = {
        0: '未运行',
        1: '完成',
        2: '运行中',
        3: '失败'
      }
      return stateMap[state]
    }
  },
  props: {
    showDownload: {
      type: Boolean,
      default: true
    },
    data: {
      type: Object,
      required: true,
      default() {
        return {}
      }
    },
    serverAddress: {
      type: String,
      default: 'http://fusion.primihub.svc.cluster.local:8080/'
    }
  },
  data() {
    return {

    }
  },
  methods: {
    toResourceDetailPage(id) {
      this.$router.push({
        name: 'UnionResourceDetail',
        params: { id },
        query: {
          serverAddress: this.serverAddress
        }
      })
    },
    async downloadPsiTask() {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/psi/downloadPsiTask?taskId=${this.data.taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-descriptions__body{
  background-color: #fafafa;
  padding: 20px 20px 10px 20px;
  font-size: 14px;
  line-height: 35px;
}
::v-deep  .el-descriptions{
  margin-bottom:30px;
  padding: 0 10px;
}
::v-deep .el-descriptions-item{
  padding: 5px 10px;
}
.download-button{
  padding: 0;
  line-height: 20px;
}
</style>
