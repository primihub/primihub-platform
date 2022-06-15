<template>
  <div class="task-result">
    <el-table
      :data="data"
      border
      class="table-list"
    >
      <el-table-column label="求交结果名称" prop="resultName" width="150">
        <template slot-scope="{row}">
          <span class="result-name" type="text" icon="el-icon-view" @click="openDialog(row.taskId)">{{ row.resultName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="任务ID" prop="taskIdName" width="320" />
      <el-table-column label="状态" prop="taskState" width="100">
        <template slot-scope="{row}">
          <i :class="statusStyle(row.taskState)" />
          <span>{{ row.taskState | taskStateFilter }}</span>
        </template>
      </el-table-column>
      <el-table-column label="求交结果归属" prop="ascription" />
      <el-table-column label="时间" prop="createDate" />
      <el-table-column label="操作">
        <template slot-scope="{row}">
          <div>
            <el-button v-if="isClickable" type="text" icon="el-icon-view" @click="openDialog(row.taskId)">查看</el-button>
            <!-- <el-button v-if="data.length > 1 && (row.taskState === 3 || row.taskState === 4)" type="text" icon="el-icon-refresh" @click="retry(row.taskId)">重试</el-button> -->
            <el-button type="text" icon="el-icon-delete" @click="delPsiTask(row.taskId)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog
      :visible.sync="dialogVisible"
      width="70%"
    >
      <PSI-task-detail :data="taskData" />
    </el-dialog>
  </div>
</template>

<script>
import { getPsiTaskDetails, delPsiTask, retryPsiTask } from '@/api/PSI'
import PSITaskDetail from '@/components/PSITaskDetail'

export default {
  name: 'PSITaskCreate',
  components: {
    PSITaskDetail
  },
  filters: {
    // 运行状态 0未运行 1完成 2运行中 3失败 默认0
    taskStateFilter(state) {
      const stateMap = {
        0: '未运行',
        1: '完成',
        2: '运行中',
        3: '失败',
        4: '已取消'
      }
      return stateMap[state]
    }
  },
  props: {
    data: {
      type: Array,
      required: true,
      default() {
        return []
      }
    },
    isClickable: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialogVisible: false,
      result: {
        resultName: ''
      },
      taskData: {},
      taskId: -1,
      timmer: null
    }
  },
  methods: {
    openDialog(id) {
      if (!this.isClickable) return
      this.taskId = id
      getPsiTaskDetails({ taskId: this.taskId }).then(res => {
        this.taskData = res.result
        this.dialogVisible = true
      })
    },
    handleClose() {
      console.log('关闭')
    },
    statusStyle(state) {
      return state === 0 ? 'state-default' : state === 1 ? 'state-end' : state === 2 ? 'state-running' : state === 4 ? 'state-default' : 'state-error'
    },
    delPsiTask(taskId) {
      delPsiTask({ taskId }).then(res => {
        if (res.code === 0) {
          this.$message({
            message: '删除成功',
            type: 'success',
            duration: 1000
          })
          this.$emit('delete', {
            taskId
          })
        }
      })
    },
    retry(taskId) {
      this.taskId = taskId
      retryPsiTask({ taskId }).then(res => {
        this.$message({
          message: '请求成功',
          type: 'success',
          duration: 1000
        })
      })
      this.timmer = window.setInterval(() => {
        setTimeout(this.getPsiTaskDetails(), 0)
      }, 1500)
      this.$emit('retry', this.data)
    },
    getPsiTaskDetails() {
      const index = this.data.findIndex(item => item.taskId === this.taskId)
      getPsiTaskDetails({ taskId: this.taskId }).then(res => {
        const { taskState, taskId } = res.result
        this.taskData = res.result
        this.taskId = taskId
        clearInterval(this.timmer)

        switch (taskState) {
          case 1:
            this.$notify({
              message: '请求成功',
              type: 'success',
              duration: 1000
            })
            break
          case 3:
            this.$notify({
              message: '请求失败',
              type: 'warning',
              duration: 1000
            })
            break
          default:
            break
        }
        this.data[index].taskState = taskState
        this.$emit('complete')
      })
    }
  }
}
</script>
<style lang="scss" scoped>
::v-deep .el-table th{
  background: #fafafa;
}
::v-deep .el-descriptions__body{
  background-color: #fafafa;
  padding: 10px 20px 10px 20px;
}
::v-deep  .el-descriptions{
  margin-bottom:20px;
}
::v-deep .el-descriptions-item__container{
  align-items: center;
}
::v-deep .el-descriptions__header{
  margin-bottom: 10px;
}
::v-deep .el-dialog__body{
  padding: 20px 20px 10px 20px;
}
.result-name{
  cursor: pointer;
  &:hover{
    color: #1989fa;
  }
}
.state-default,.state-running,.state-error,.state-end{
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
  vertical-align: middle;
  margin-right: 5px;
}
.state-default{
  background-color: #909399;

}
.state-end{
  background-color: #67C23A;

}
.state-running{
  background-color: #409EFF;
}
.state-error{
  background-color: #F56C6C;
}
</style>
