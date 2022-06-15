<template>
  <div class="container">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane label="创建求交任务" name="create">
        <PSITaskCreate v-loading="listLoading" :is-reset="isReset" @change="handelTaskChange" @submit="handleSubmit" @cancel="handelCancel" />
        <PSITaskResult v-if="dataPsiTask.length>0" v-loading="resultLoading" :data="dataPsiTask" @delete="handleSingleResultDelete" />
      </el-tab-pane>
      <el-tab-pane label="查看求交任务" name="view" class="panel">
        <PSITaskResult v-loading="taskLoading" :data="allDataPsiTask" :is-clickable="true" @delete="handleDelete" @complete="handelTaskComplete" />
        <pagination v-show="totalPage>1" :background="true" :limit.sync="pageSize" :page.sync="pageNo" :total="total" layout="total, prev, pager, next, jumper" @pagination="handlePagination" @change="handleListChange" />
      </el-tab-pane>
    </el-tabs>

  </div>
</template>

<script>
import { getPsiTaskList } from '@/api/PSI'
import PSITaskCreate from '@/components/PSITaskCreate'
import PSITaskResult from '@/components/PSITaskResult'
import Pagination from '@/components/Pagination'

export default {
  name: 'PSITask',
  components: {
    PSITaskCreate,
    PSITaskResult,
    Pagination
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
    handleSingleResultDelete() {
      this.dataPsiTask = []
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
