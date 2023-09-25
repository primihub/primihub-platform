<template>
  <div class="task-result">
    <el-table
      :data="tableData"
      border
      class="table-list"
    >
      <el-table-column
        type="index"
        align="center"
        width="50"
      />
      <el-table-column label="任务ID" min-width="120px">
        <template slot-scope="{row}">
          <span class="result-name" type="text" icon="el-icon-view" @click="openDialog(row.taskId)">{{ row.taskIdName }}</span> <br>
        </template>
      </el-table-column>
      <el-table-column label="任务名称" min-width="120px">
        <template slot-scope="{row}">
          <p class="result-id">{{ row.resultName }}</p>
        </template>
      </el-table-column>
      <el-table-column label="任务类型" prop="ascription" />
      <el-table-column label="任务发起时间" prop="createDate" min-width="120px" />
      <el-table-column label="任务耗时" min-width="120px">
        <template slot-scope="{row}">
          {{ row.consuming | timeFilter }}
        </template>
      </el-table-column>
      <el-table-column label="任务状态" prop="taskState">
        <template slot-scope="{row}">
          <i :class="statusStyle(row.taskState)" />
          <span>{{ row.taskState | taskStateFilter }}</span>
          <span v-if="row.taskState === 2"> <i class="el-icon-loading" /></span>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="120px" align="center">
        <template slot-scope="{row}">
          <p class="tool-buttons">
            <el-link type="primary" @click="openDialog(row.taskId)">查看</el-link>
            <el-link v-if="row.taskState === 2" type="warning" @click="cancelTask(row)">取消</el-link>
            <el-link type="danger" :disabled="row.taskState === 2" @click="delPsiTask(row)">删除</el-link>
          </p>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog
      :visible.sync="dialogVisible"
      :append-to-body="true"
      top="5vh"
      width="750px"
    >
      <PSI-task-detail :data="taskData" @change="handleEditChange" />
    </el-dialog>
  </div>
</template>

<script>
import { getPsiTaskDetails, delPsiTask, retryPsiTask, cancelTask } from '@/api/PSI'
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
      // tableData: [],
      dialogVisible: false,
      result: {
        resultName: ''
      },
      taskData: {},
      taskId: -1,
      timer: null
    }
  },
  computed: {
    tableData: {
      get() {
        return this.data
      },
      set() {}
    }
  },
  methods: {
    openDialog(id) {
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
    delPsiTask(row) {
      if (row.taskState === 2) return
      this.$confirm('此操作将永久删除该任务, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delPsiTask({ taskId: row.taskId }).then(res => {
          if (res.code === 0) {
            const posIndex = this.data.findIndex(item => item.taskId === row.taskId)
            if (posIndex !== -1) {
              this.tableData.splice(posIndex, 1)
            }
            this.$message({
              message: '删除成功',
              type: 'success',
              duration: 1000
            })
            clearInterval(this.timer)
            this.$emit('delete', this.tableData)
          }
        })
      }).catch(() => {})
    },
    async cancelTask(row) {
      const res = await cancelTask(row.taskId)
      if (res.code === 0) {
        const posIndex = this.tableData.findIndex(item => item.taskId === row.taskId)
        this.tableData[posIndex].taskState === 4
        this.$notify({
          message: '取消成功',
          type: 'success',
          duration: 1000
        })
        this.$emit('cancel', { taskId: row.taskId })
      }
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
      this.timer = window.setInterval(() => {
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
        clearInterval(this.timer)

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
    },
    handleEditChange({ taskId, resultName }) {
      const posIndex = this.tableData.findIndex(item => item.taskId === taskId)
      if (posIndex !== -1) {
        this.tableData[posIndex].resultName = resultName
      }
    }
  }
}
</script>
<style lang="scss" scoped>
::v-deep .el-table th{
  background: #fafafa;
  font-size: 14px;
}
.result-id{
  line-height: 17px!important;
}
.result-name{
  cursor: pointer;
  color: #1989fa;
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
  background-color: #1677FF;
}
.state-error{
  background-color: #F56C6C;
}
.tool-buttons{
  .el-link{
    margin: 0 3px;
  }
}
</style>
