<template>
  <Log :task-state="task.taskState" @error="getErrorLogData" />
</template>

<script>
import Log from '@/components/Log'
import { getModelDetail } from '@/api/model'
export default {
  name: 'LogDetail',
  components: {
    Log
  },
  async created() {
    await this.fetchData()
  },
  methods: {
    async fetchData() {
      this.listLoading = true
      this.taskId = this.$route.params.taskId
      const response = await getModelDetail({ taskId: this.taskId })
      if (response.code === 0) {
        this.listLoading = false
        this.task = response.result.task
      }
    }
  }

}
</script>
