<template>
  <div class="container">
    <div class="desc-container">
      <el-descriptions :column="2">
        <el-descriptions-item label="推理服务ID">{{ dataList.reasoningId }}</el-descriptions-item>
        <el-descriptions-item label="推理服务名称">{{ dataList.reasoningName }}</el-descriptions-item>
        <el-descriptions-item label="推理类型">{{ dataList.reasoningType }}方推理</el-descriptions-item>
        <el-descriptions-item label="上线时间">{{ dataList.releaseDate }}</el-descriptions-item>
        <el-descriptions-item label="状态"><StatusIcon :status="dataList.reasoningState" /> {{ dataList.reasoningState | reasoningStateFilter }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ dataList.reasoningDesc }}</el-descriptions-item>
      </el-descriptions>
      <div class="buttons">
        <el-button v-if="dataList.reasoningState === 1" type="primary" icon="el-icon-download" @click="download">下载结果</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getReasoning } from '@/api/reasoning'
import { getToken } from '@/utils/auth'
import StatusIcon from '@/components/StatusIcon'

export default {
  name: 'InferenceDetail',
  components: {
    StatusIcon
  },
  filters: {
    reasoningStateFilter(status) {
      status = status || 0
      const statusMap = {
        0: '未开始',
        1: '完成',
        2: '运行中',
        3: '运行失败',
        4: '取消',
        5: '已删除'
      }
      return statusMap[status]
    }
  },
  data() {
    return {
      id: '',
      dataList: {},
      timer: null,
      reasoningState: ''
    }
  },
  async mounted() {
    this.id = this.$route.params.id
    await this.fetchData()
  },
  destroyed() {
    clearInterval(this.timer)
  },
  methods: {
    async download() {
      const taskId = this.dataList.runTaskId || ''
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/task/downloadTaskFile?taskId=${taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    },
    async fetchData() {
      this.listLoading = true
      const res = await getReasoning({ id: this.id })
      if (res.code === 0) {
        this.dataList = res.result
        this.reasoningState = this.dataList.reasoningState
        this.listLoading = false
        clearInterval(this.timer)
        if (this.reasoningState === 2) {
          this.timer = setInterval(async() => {
            await this.fetchData()
          }, 2000)
        }
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.desc-container{
  background-color: #ffffff;
  padding: 30px;
  margin-bottom: 30px;
}
.buttons{
  display: flex;
  justify-content: flex-end;
}
</style>
