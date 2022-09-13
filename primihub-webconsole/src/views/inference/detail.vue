<template>
  <div class="container">
    <el-descriptions>
      <el-descriptions-item label="推理服务名称">{{ dataList.reasoningName }}</el-descriptions-item>
      <el-descriptions-item label="推理服务描述">{{ dataList.reasoningDesc }}</el-descriptions-item>
      <el-descriptions-item label="推理服务ID">{{ dataList.reasoningId }}</el-descriptions-item>
      <el-descriptions-item label="类型">{{ dataList.reasoningType }}方</el-descriptions-item>
      <!-- <el-descriptions-item label="状态">{{ reasoningState }}</el-descriptions-item> -->
    </el-descriptions>
  </div>
</template>

<script>
import { getReasoning } from '@/api/reasoning'
export default {
  name: 'InferenceDetail',
  data() {
    return {
      id: ''
    }
  },
  created() {
    this.id = this.$route.params.id
  },
  methods: {
    fetchData() {
      this.listLoading = true
      getReasoning({ id: this.id }).then((res) => {
        console.log('response.data', res)
        if (res.code === 0) {
          const { result } = res.result
          this.dataList = result.data
          this.total = result.total
          this.pageCount = result.totalPage
        }

        setTimeout(() => {
          this.listLoading = false
        }, 200)
      }).catch(() => {
        this.listLoading = false
      })
    }
  }
}
</script>
