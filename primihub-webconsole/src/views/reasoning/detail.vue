<template>
  <div class="container">
    <el-descriptions :column="2" class="desc-container">
      <el-descriptions-item label="推理服务名称">{{ dataList.reasoningName }}</el-descriptions-item>
      <el-descriptions-item label="推理服务描述">{{ dataList.reasoningDesc }}</el-descriptions-item>
      <el-descriptions-item label="推理服务ID">{{ dataList.reasoningId }}</el-descriptions-item>
      <el-descriptions-item label="类型">{{ dataList.reasoningType }}方推理</el-descriptions-item>
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
      id: '',
      dataList: []
    }
  },
  async mounted() {
    this.id = this.$route.params.id
    await this.fetchData()
    console.log(this.$route)
  },
  methods: {
    async fetchData() {
      this.listLoading = true
      const res = await getReasoning({ id: this.id })
      if (res.code === 0) {
        this.dataList = res.result
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
</style>
