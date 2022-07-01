<template>
  <div class="container">
    <PSITaskCreate v-loading="listLoading" :is-reset="isReset" @change="handelTaskChange" @submit="handleSubmit" @cancel="handelCancel" />
    <PSITaskResult v-if="dataPsiTask.length>0" v-loading="resultLoading" :data="dataPsiTask" @delete="handleSingleResultDelete" />
  </div>
</template>

<script>
import { getPsiTaskList } from '@/api/PSI'
import PSITaskCreate from '@/components/PSITaskCreate'
import PSITaskResult from '@/components/PSITaskResult'

export default {
  name: 'PSITask',
  components: {
    PSITaskCreate,
    PSITaskResult
  },
  data() {
    return {
      listLoading: false,
      taskLoading: false,
      activeName: 'create',
      buttonText: '确定',
      pageSize: 10,
      totalPage: 0,
      total: 0,
      pageNo: 1,
      isRun: false, // 任务是否在运行中
      dataPsiTask: [], // 任务运行结果
      allDataPsiTask: [], // 查看任务结果
      isReset: false,
      resultLoading: false,
      taskId: 0
    }
  },
  methods: {
    handelTaskChange(result) {
      this.dataPsiTask = [result]
      this.resultLoading = false
    },
    handleTabClick(tab) {
      if (tab.index === '1') {
        this.getPsiTaskList()
      }
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.getPsiTaskList()
    },
    getPsiTaskList() {
      this.taskLoading = true
      getPsiTaskList({
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }).then(res => {
        const { data, totalPage, total } = res.result
        this.allDataPsiTask = data
        this.totalPage = totalPage
        this.total = total
        this.taskLoading = false
      })
    },
    handleDelete(data) {
      if (this.taskId === data.taskId) {
        this.taskId = 0
      }
      console.log(data)
      this.getPsiTaskList()
    },
    async handleRetry(data) {
      this.resultLoading = true
    },
    handleListRetry(data) {
      this.taskLoading = true
    },
    handelTaskComplete(data) {
      this.taskLoading = false
    },
    handleListChange(state) {
    },
    handleSubmit(data) {
      this.resultLoading = true
      this.dataPsiTask = [data]
    },
    handelCancel() {
      this.resultLoading = false
      this.dataPsiTask[0].taskState = 4
    },
    handleSingleResultDelete() {
      this.dataPsiTask = []
    }
  }
}
</script>
<style lang="scss" scoped>
::v-deep .el-tabs__item{
  font-size: 15px;
}
.panel{
  background-color: #fff;
  padding: 20px;
  border-radius: 20px;
}
</style>
