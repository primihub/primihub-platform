<template>
  <div>
    <div class="model-list">
      <el-table
        v-loading="listLoading"
        :data="modelList"
        empty-text="暂无任务"
        border
      >
        <el-table-column
          label="任务ID"
          min-width="100"
        >
          <template slot-scope="{row}">
            <template v-if="hasModelViewPermission">
              <el-button :disabled="projectStatus === 2" type="text" size="mini" @click="toModelDetail(row.latestTaskId)">{{ row.latestTaskIdName }}</el-button>
            </template>
            <template v-else>
              <span>{{ row.latestTaskId }}</span>
            </template>
          </template>
        </el-table-column>
        <el-table-column
          prop="latestTaskName"
          label="任务名称"
          min-width="100"
        />
        <el-table-column
          align="center"
          label="任务类型"
        >
          <template slot-scope="{row}">
            <span><el-tag type="primary" size="mini">{{ row.taskType | taskTypeFilter }}</el-tag></span>
          </template>
        </el-table-column>
        <el-table-column
          label="开始时间"
          min-width="150"
        >
          <template slot-scope="{row}">
            <span>{{ row.latestTaskStartDate?row.latestTaskStartDate:'未开始' }}</span> <br>
          </template>
        </el-table-column>
        <el-table-column
          label="结束时间"
          min-width="150"
        >
          <template slot-scope="{row}">
            <span>{{ row.taskEndDate? row.taskEndDate:'未结束' }}</span> <br>
          </template>
        </el-table-column>
        <el-table-column
          prop="timeConsuming"
          label="耗时"
          width="100"
        >
          <template slot-scope="{row}">
            <span>{{ (row.timeConsuming) | timeFilter }}</span> <br>
          </template>
        </el-table-column>
        <el-table-column
          prop="latestTaskStatus"
          label="任务状态"
          sortable
        >
          <template slot-scope="{row}">
            <i :class="statusStyle(row.latestTaskStatus)" />
            {{ row.latestTaskStatus | taskStatusFilter }}
            <span v-if="row.latestTaskStatus === 3" class="error-tips">{{ row.taskErrorMsg }}</span>
          </template>
        </el-table-column>
        <!-- if not have permissions, hide the column -->
        <el-table-column
          v-if="isCreator && !(!hasModelTaskHistoryPermission && !hasModelRunPermission && !hasModelEditPermission && !hasCopyModelTaskPermission && !hasModelDownloadPermission && !hasDeleteModelTaskPermission)"
          label="操作"
          width="130"
          fixed="right"
        >
          <template slot-scope="{row}">
            <div class="buttons">
              <el-link v-if="hasCopyModelTaskPermission && isCreator" :disabled="projectStatus === 2" type="primary" size="mini" @click.stop="copyTask(row)">复制</el-link>
              <el-link v-if="hasModelRunPermission && row.latestTaskStatus === 3 && isCreator" :disabled="projectStatus === 2" type="primary" size="mini" @click.stop="restartTaskModel(row.latestTaskId)">重启</el-link>
              <el-link v-if="row.latestTaskStatus === 2 && isCreator" type="danger" @click.stop="cancelTaskModel(row.latestTaskIdName)">取消</el-link>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="pageCount>1" :limit.sync="params.pageSize" :page-count="pageCount" :page.sync="params.pageNo" :total="total" @pagination="handlePagination" />
  </div>
</template>

<script>
import { getModelList, runTaskModel, getTaskModelComponent, restartTaskModel } from '@/api/model'
import { deleteTask, cancelTask } from '@/api/task'
import Pagination from '@/components/Pagination'
import { getToken } from '@/utils/auth'

export default {
  components: {
    Pagination
  },
  props: {
    isCreator: {
      type: Boolean,
      default: false
    },
    projectStatus: {
      type: Number,
      default: -1
    }
  },
  data() {
    return {
      fullscreenLoading: false,
      listLoading: false,
      query: {
        modelName: '',
        projectName: '',
        taskStatus: ''
      },
      projectNamesList: [],
      modelList: null,
      params: {
        pageNo: 1,
        pageSize: 10,
        projectId: ''
      },
      total: 0,
      pageCount: 0,
      hidePagination: true,
      projectId: this.$route.params.id,
      timer: null
    }
  },
  computed: {
    hasModelTaskHistoryPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelTaskHistory')
    },
    hasModelRunPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelRun')
    },
    hasModelEditPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelEdit')
    },
    hasModelViewPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelView')
    },
    hasModelDownloadPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelResultDownload')
    },
    hasDeleteModelTaskPermission() {
      return this.$store.getters.buttonPermissionList.includes('deleteModelTask')
    },
    hasCopyModelTaskPermission() {
      return this.$store.getters.buttonPermissionList.includes('copyModelTask')
    }
  },
  created() {
    this.fetchData()
    this.timer = window.setInterval(() => {
      setTimeout(this.fetchData(), 0)
    }, 5000)
  },
  destroyed() {
    clearTimeout(this.timer)
    console.log('destroyed model')
  },
  methods: {
    copyTask(row) {
      const modelId = row.modelId
      this.$router.push({
        name: 'ModelCreate',
        query: {
          modelId,
          projectId: this.projectId,
          isCopy: 1
        }
      })
    },
    restartTaskModel(taskId) {
      this.listLoading = true
      restartTaskModel({ taskId }).then(res => {
        if (res.code === 0) {
          this.listLoading = false
          const posIndex = this.modelList.findIndex(item => item.latestTaskId === taskId)
          this.modelList[posIndex].latestTaskStatus = 2
          this.timer = window.setInterval(() => {
            setTimeout(this.fetchData(), 0)
          }, 5000)
        } else {
          this.listLoading = false
          this.$message({
            message: res.msg,
            type: 'error'
          })
        }
      }).catch(err => {
        this.listLoading = false
        console.log(err)
      })
    },
    cancelTaskModel(taskId) {
      cancelTask(taskId).then(res => {
        if (res.code === 0) {
          this.fetchData()
          this.$message({
            message: '取消成功',
            type: 'success'
          })
        }
      })
    },
    deleteTask(taskId) {
      this.$confirm('此操作将永久删除该任务, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.listLoading = true
        deleteTask(taskId).then(res => {
          if (res.code === 0) {
            // last page && all deleted, to the first page
            this.modelList.splice(this.modelList.findIndex(item => item.taskId === taskId), 1)
            if (this.modelList.length === 0 && (this.params.pageNo === this.pageCount)) {
              this.params.pageNo = 1
            }
            this.fetchData()
            this.$message({
              message: '删除成功',
              type: 'success',
              duration: 1000
            })
          }
          this.listLoading = false
        }).catch(() => {
          this.listLoading = false
        })
      })
    },
    async download(taskId) {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/task/downloadTaskFile?taskId=${taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    },
    toModelDetail(taskId) {
      this.$router.push({
        name: `ModelTaskDetail`,
        params: { taskId }
      })
    },
    search() {
      this.params.pageNo = 1
      this.params.modelName = this.query.modelName
      this.params.projectName = this.query.projectName
      this.params.taskStatus = this.query.taskStatus
      this.fetchData()
    },
    toModelHistory(id) {
      this.$router.push({
        path: `/model/history/${id}`
      })
    },
    searchModel() {
      console.log('searchModel', this.searchName)
    },
    fetchData() {
      this.params.projectId = this.projectId
      getModelList(this.params).then((response) => {
        const { result } = response
        this.modelList = result.data
        this.total = result.total
        this.pageCount = result.totalPage
        // filter the running task
        const res = this.modelList.filter(item => item.latestTaskStatus === 2)
        // No tasks are running
        if (res.length === 0) {
          clearInterval(this.timer)
        }
      }).catch(() => {
        clearInterval(this.taskTimer)
      })
    },
    statusStyle(status) {
      return status === 0 ? 'status-default el-icon-error' : status === 1 ? 'status-end el-icon-success' : status === 2 ? 'status-processing el-icon-loading' : status === 3 ? 'status-error el-icon-error' : 'status-default  el-icon-error'
    },
    handlePagination(data) {
      this.params.pageNo = data.page
      this.fetchData()
    },
    toEditPage(row) {
      const modelId = row.modelId
      this.$router.push({
        name: 'ModelCreate',
        query: {
          modelId,
          projectId: this.projectId
        }
      })
    },
    runTaskModel(row, loading) {
      const modelId = row.modelId
      this.fullscreenLoading = true
      runTaskModel({ modelId }).then(res => {
        if (res.code !== 0) {
          this.$message({
            message: res.msg,
            type: 'error'
          })
          return
        } else {
          const taskId = res.result.taskId
          this.taskTimer = window.setInterval(() => {
            setTimeout(this.getTaskModelComponent(row, taskId, loading), 0)
          }, 1500)
        }
      }).catch(err => {
        console.log(err)
        this.fullscreenLoading = false
      })
    },
    getTaskModelComponent(row, taskId, loading) {
      getTaskModelComponent({ taskId }).then(res => {
        const result = res.result
        const taskResult = []
        result && result.forEach((item) => {
          const { componentCode, complete } = item
          if (complete) {
            taskResult.push(componentCode)
          }
        })
        if (taskResult.length === result.length) { // 所有任务运行完成，停止轮询
          this.fullscreenLoading = false
          clearInterval(this.taskTimer)
          this.$message({
            message: `${row.modelName} 运行成功`,
            type: 'success',
            duration: 3000
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-form--inline .el-form-item {
  margin: 5px 10px!important;
}
::v-deep .el-button {
    white-space: normal;
    text-align: left;
    line-height: 1.5;
}
.form-wrap{
  padding: 20px 0;
}
.status-default,.status-processing,.status-error,.status-end{
  margin-right: 3px;
  font-size: 12px;
}
.status-default{
  color: #909399;
}
.status-end{
  color: #67C23A;
}
.status-processing{
  color: #909399;
}
.status-error{
  color: #F56C6C;
}
.error-tips{
  display: block;
  white-space:normal;
  color: #F56C6C;
  font-size: 12px;
  line-height: 1.2;
}
.buttons{
  display: flex;
  justify-content: space-evenly;
}
</style>
