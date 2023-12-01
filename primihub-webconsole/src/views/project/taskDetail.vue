<template>
  <div v-loading="listLoading" class="container" :class="{'disabled': task.taskState === 5}">
    <section class="infos">
      <h2 class="infos-title">基本信息</h2>
      <el-row type="flex">
        <el-col class="desc-col" :span="6">
          <div class="desc-label">任务ID:</div>
          <div class="desc-content">{{ task.taskIdName }}</div>
        </el-col>
        <el-col class="desc-col" :span="6">
          <div class="desc-label">任务名称:</div>
          <div class="desc-content">{{ task.taskName }}</div>
        </el-col>
      </el-row>
      <el-row type="flex">
        <el-col class="desc-col" :span="6">
          <div class="desc-label">任务类型:</div>
          <div class="desc-content">{{ task.taskType | taskTypeFilter }}</div>
        </el-col>
        <el-col class="desc-col" :span="6">
          <div class="desc-label">开始时间:</div>
          <div class="desc-content">{{ task.taskStartDate?task.taskStartDate: '未开始' }}</div>
        </el-col>
      </el-row>
      <el-row type="flex">
        <el-col class="desc-col" :span="6">
          <div class="desc-label">结束时间:</div>
          <div class="desc-content">{{ task.taskEndDate? task.taskEndDate:'未结束' }}</div>
        </el-col>
        <el-col class="desc-col" :span="6">
          <div class="desc-label">耗时:</div>
          <div class="desc-content">{{ task.timeConsuming | timeFilter }}</div>
        </el-col>
      </el-row>
      <el-row type="flex">
        <el-col class="desc-col" :span="6">
          <div class="desc-label">任务状态:</div>
          <div class="desc-content">
            <p>
              <i :class="statusStyle(task.taskState)" />
              {{ task.taskState | taskStatusFilter }}
              <span v-if="task.taskState === 3" class="error-tips">{{ task.taskErrorMsg }}</span>
            </p>
          </div>
        </el-col>
      </el-row>
      <el-row>
        <el-col class="desc-col" :span="24">
          <div class="desc-label">任务描述:</div>
          <div class="desc-content">
            <template v-if="task.isCooperation === 0">
              <EditInput style="width:70%;" type="textarea" show-word-limit maxlength="200" :value="taskDesc" @change="handleDescChange" />
            </template>
            <template v-else>
              {{ taskDesc }}
            </template>
          </div>
        </el-col>
      </el-row>
      <div class="buttons">
        <el-button v-if="downLoadPermission" :disabled="project.status === 2 && task.taskState === 5" type="primary" icon="el-icon-download" @click="download">下载结果</el-button>
        <el-button v-if="hasModelRunPermission && task.taskState === 3" :disabled="project.status === 2" type="primary" @click="restartTaskModel(task.taskId)">重启任务</el-button>
        <el-button v-if="task.taskState === 2" :disabled="project.status === 2" type="primary" @click="cancelTaskModel(task.taskId)">取消任务</el-button>
        <el-button v-if="hasDeleteModelTaskPermission && task.taskState !== 5 && task.isCooperation === 0" :disabled="project.status === 2 || task.taskState === 2" type="danger" icon="el-icon-delete" @click="deleteModelTask">删除任务</el-button>
      </div>
    </section>
    <section>
      <el-tabs v-model="tabName" @tab-click="handleTabClick">
        <el-tab-pane label="所用数据" name="1">
          <el-table
            v-if="tabName === '1'"
            :data="modelResources"
            :row-class-name="tableRowClassName"
            border
          >
            <el-table-column
              prop="resourceId"
              label="资源ID"
            >
              <template slot-scope="{row}">
                <!-- available: 0 可用， 1不可用 -->
                <template v-if="hasViewPermission">
                  <el-button :disabled="project.status === 2 || task.taskState === 5 || row.available === 1" size="mini" type="text" @click="toResourceDetailPage(row)">{{ row.resourceId }}</el-button>
                </template>
                <template v-else>
                  <span>{{ row.resourceId }}</span>
                </template>
              </template>
            </el-table-column>
            <el-table-column
              prop="resourceName"
              label="资源名称"
            />
            <el-table-column
              prop="resourceType"
              label="上传方式"
            >
              <template slot-scope="{row}">
                {{ row.resourceType | sourceFilter }}
              </template>
            </el-table-column>
            <el-table-column
              prop="resourceType"
              label="数据类型"
            >
              <template slot-scope="{row}">
                {{ row.resourceType === 3 ? '衍生数据' : '原始数据' }}
              </template>
            </el-table-column>
            <el-table-column
              prop="organName"
              label="上传机构"
            />
            <el-table-column
              prop="participationIdentity"
              label="角色"
            >
              <template slot-scope="{row}">
                {{ row.participationIdentity === 1 ? '发起方': row.organId === userOrganId ? '本机构' : '协作方' }}
              </template>
            </el-table-column>
            <el-table-column
              prop="available"
              label="数据状态"
            >
              <template slot-scope="{row}">
                {{ row.available === 1? '不可用': '可用' }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane v-if="task.taskState === 1 || task.taskState === 5" label="任务模型" name="2">
          <TaskModel v-if="tabName === '2'" :state="task.taskState" :project-status="project.status" />
        </el-tab-pane>
        <el-tab-pane label="预览图" name="3">
          <div class="canvas-panel">
            <TaskCanvas v-if="tabName === '3' || restartRun" :model-id="modelId" :options="taskOptions" :model-data="modelComponent" :state="task.taskState" :restart-run="restartRun" @complete="handleTaskComplete" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="日志" name="4">
          <Log v-if="tabName === '4' " :task-state="task.taskState" />
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import { getModelDetail } from '@/api/model'
import { deleteTask, cancelTask, updateTaskDesc } from '@/api/task'
import TaskModel from '@/components/TaskModel'
import TaskCanvas from '@/components/TaskCanvas'
import Log from '@/components/Log'
import EditInput from '@/components/editInput'

export default {
  name: 'TaskDetail',
  components: {
    TaskModel,
    TaskCanvas,
    Log,
    EditInput
  },
  data() {
    return {
      taskId: null,
      tabName: '1',
      listLoading: false,
      model: {},
      modelQuotas: [],
      modelResources: [],
      modelId: 0,
      modelComponent: [],
      anotherQuotas: [],
      taskState: null,
      task: {},
      project: {},
      restartRun: false,
      taskOptions: {
        showTime: true,
        showMinimap: false,
        isEditable: false,
        isRun: true,
        showDraft: false,
        showComponentsDetails: true,
        center: true,
        toolbarOptions: {
          background: false,
          position: 'right',
          buttons: ['zoomIn', 'zoomOut', 'reset']
        }
      },
      logType: '',
      errorLog: [],
      isCooperation: false,
      taskDesc: ''
    }
  },
  computed: {
    hasViewPermission() {
      return this.$store.getters.buttonPermissionList.includes('UnionResourceDetail')
    },
    downLoadPermission() {
      const sysPermission = this.$store.getters.buttonPermissionList.includes('ModelResultDownload')
      if (this.task.taskState === 1 && sysPermission) {
        // mpc-lr task, only the initiator has the permission
        if (this.model.modelType === 4) {
          return this.task.isCooperation === 0
        }
        return true
      } else {
        return false
      }
    },
    hasDeleteModelTaskPermission() {
      return this.$store.getters.buttonPermissionList.includes('deleteModelTask')
    },
    hasModelRunPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelRun')
    },
    userOrganId() {
      return this.$store.getters.userOrganId
    }
  },
  async created() {
    this.taskId = this.$route.params.id
    const routerFrom = this.$route.query.from
    await this.fetchData()
    this.tabName = routerFrom === '0' ? '3' : this.task.taskState === 2 ? '3' : '1'
  },
  methods: {
    handleDescChange({ change, value }) {
      if (change) {
        this.taskDesc = value
        updateTaskDesc({
          taskId: this.taskId,
          taskDesc: this.taskDesc
        }).then(({ code }) => {
          if (code === 0) {
            this.$message.success('修改成功')
          }
        })
      }
    },
    tableRowClassName({ row }) {
      if (row.available === 1) {
        return 'disabled'
      } else {
        return ''
      }
    },
    toModelDetail() {
      this.$router.push({
        path: `/model/detail/${this.modelId}`,
        query: { taskId: this.task.taskId }
      })
    },
    async handleTaskComplete() {
      this.restartRun = false
      await this.fetchData()
    },
    handleTabClick(tab, event) {
      this.tabName = tab.name
    },
    async fetchData() {
      this.listLoading = true
      this.taskId = this.$route.params.taskId
      const response = await getModelDetail({ taskId: this.taskId })
      if (response.code === 0) {
        this.listLoading = false
        const { task, model, modelResources, modelQuotas, modelComponent, anotherQuotas, taskState, project } = response.result
        this.task = task
        this.project = project
        this.model = model
        this.modelId = model.modelId ? Number(model.modelId) : Number(this.$route.query.modelId)
        this.anotherQuotas = anotherQuotas
        this.modelQuotas = modelQuotas
        this.modelResources = modelResources.sort(function(a, b) { return a.participationIdentity - b.participationIdentity })
        this.taskDesc = this.task.taskDesc
        if (this.task.isCooperation === 1) {
          // provider organ only view own resource data
          this.modelResources = this.modelResources.filter(item => item.organId === this.userOrganId)
        }
        this.modelComponent = modelComponent
        this.taskState = taskState
      }
    },
    statusStyle(status) {
      return status === 0 ? 'status-default el-icon-error' : status === 1 ? 'status-end el-icon-success' : status === 2 ? 'status-processing el-icon-loading' : status === 3 ? 'status-error el-icon-error' : 'status-default  el-icon-error'
    },
    toResourceDetailPage(row) {
      if (row.participationIdentity === 2) {
        this.$router.push({
          name: 'UnionResourceDetail',
          params: {
            id: row.resourceId
          }
        })
      } else {
        this.$router.push({
          name: 'ResourceDetail',
          params: {
            id: row.resourceId
          }
        })
      }
    },
    async download() {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/task/downloadTaskFile?taskId=${this.taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    },
    deleteModelTask() {
      this.$confirm('此操作将永久删除该任务, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.listLoading = true
        deleteTask(this.taskId).then(res => {
          if (res.code === 0) {
            this.$message({
              message: '删除成功',
              type: 'success',
              duration: 1000
            })
            this.fetchData()
          }
          this.listLoading = false
        }).catch(() => {
          this.listLoading = false
        })
      })
    },
    toProjectDetail() {
      this.$router.push({
        name: 'ProjectDetail',
        params: { id: this.project.id }
      })
    },
    restartTaskModel() {
      console.log('重启')
      this.tabName = '3'
      this.task.taskState = 2
      this.task.taskStartDate = this.task.taskEndDate
      this.task.taskEndDate = null
      this.restartRun = true
    },
    cancelTaskModel(taskId) {
      cancelTask(taskId).then(res => {
        if (res.code === 0) {
          this.$message({
            message: '取消成功',
            type: 'success'
          })
          this.fetchData()
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/description.scss";
::v-deep .el-tabs__item {
  font-size: 16px;
}
::v-deep .el-button{
  white-space: normal;
  text-align: left;
}
::v-deep .el-tabs__nav{
  transform: none;
}
.canvas-panel{
  flex: 1;
  overflow: hidden;
  display: flex;
  height: 100%;
  position: relative;
}
 section {
  padding: 20px 30px;
  background-color: #fff;
  margin-bottom: 30px;
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
  justify-content: flex-end;
}

.log-panel{
  height: 600px;
}

</style>
