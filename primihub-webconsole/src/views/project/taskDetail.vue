<template>
  <div v-loading="listLoading" class="container" :class="{'disabled': task.taskState === 5}">
    <section class="infos">
      <el-descriptions :column="3" label-class-name="detail-title" title="基本信息">
        <el-descriptions-item label="任务ID">
          {{ task.taskIdName }}
        </el-descriptions-item>
        <el-descriptions-item label="任务名称">
          {{ task.taskName }}
        </el-descriptions-item>
        <el-descriptions-item label="任务类型">
          模型
        </el-descriptions-item>
        <el-descriptions-item v-if="task.taskState === 1" label="模型ID">
          <el-link v-if="task.isCooperation === 0" type="primary" @click="toModelDetail">{{ model.modelId }}</el-link>
          <span v-else>{{ model.modelId }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">
          {{ task.taskStartDate?task.taskStartDate: '未开始' }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">
          {{ task.taskEndDate?task.taskEndDate: '未开始' }}
        </el-descriptions-item>
        <el-descriptions-item label="耗时">
          {{ task.timeConsuming | timeFilter }}
        </el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <p>
            <i :class="statusStyle(task.taskState)" />
            {{ task.taskState | taskStatusFilter }}
            <span v-if="task.taskState === 3" class="error-tips">{{ task.taskErrorMsg }}</span>
          </p>
        </el-descriptions-item>
        <el-descriptions-item label="任务描述">
          {{ task.taskDesc }}
        </el-descriptions-item>
      </el-descriptions>
      <div class="buttons">
        <el-button v-if="hasModelDownloadPermission && task.taskState === 1" :disabled="project.status === 2 && task.taskState === 5" type="primary" icon="el-icon-download" @click="download">下载结果</el-button>
        <el-button v-if="hasModelRunPermission && task.taskState === 3" :disabled="project.status === 2" type="primary" @click="restartTaskModel(task.taskId)">重启任务</el-button>
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
        <el-tab-pane v-if="task.isCooperation === 0 || (task.isCooperation === 1 && task.taskState === 1)" label="预览图" name="3">
          <div class="canvas-panel">
            <TaskCanvas v-if="tabName === '3' && modelId" :model-id="modelId" :options="taskOptions" :model-data="modelComponent" :state="task.taskState" :restart-run="restartRun" @complete="handleTaskComplete" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import { getModelDetail } from '@/api/model'
import { deleteTask } from '@/api/task'
import TaskModel from '@/components/TaskModel'
import TaskCanvas from '@/components/TaskCanvas'

export default {
  name: 'TaskDetail',
  components: {
    TaskModel,
    TaskCanvas
  },
  filters: {
    taskStatusFilter(status) {
      // 任务状态(0未开始 1成功 2运行中 3失败 4取消)
      status = status || 0
      const statusMap = {
        0: '未开始',
        1: '已完成',
        2: '执行中',
        3: '任务失败',
        4: '取消',
        5: '已删除'
      }
      return statusMap[status]
    }
  },
  data() {
    return {
      taskId: null,
      tabName: '',
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
      }
    }
  },
  computed: {
    hasViewPermission() {
      return this.$store.getters.buttonPermissionList.includes('UnionResourceDetail')
    },
    hasModelDownloadPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelResultDownload')
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
    this.tabName = routerFrom === '0' ? '3' : '1'
    await this.fetchData()
  },
  methods: {
    tableRowClassName({ row }) {
      console.log(row.available)
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
        const { task, model, modelQuotas, modelResources, modelComponent, anotherQuotas, taskState, project } = response.result
        this.task = task
        this.project = project
        this.model = model
        this.modelId = model.modelId ? model.modelId : this.$route.query.modelId
        this.anotherQuotas = anotherQuotas
        this.modelQuotas = modelQuotas
        this.modelResources = modelResources.sort(function(a, b) { return a.participationIdentity - b.participationIdentity })
        if (task.isCooperation === 1) {
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
      this.$router.push({
        name: 'UnionResourceDetail',
        params: {
          id: row.resourceId
        },
        query: {
          serverAddress: row.serverAddress
        }
      })
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
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-tabs__item {
  font-size: 16px;
}
::v-deep .el-button{
  white-space: normal;
  text-align: left;
  line-height: 1.5;
}
::v-deep .el-table .cell{
  font-size: 14px;
  line-height: 1.3;
}
::v-deep .el-table .el-table__cell{
  padding: 8px;
  font-size: 14px;
}
::v-deep .el-tabs__nav{
  transform: none;
}
::v-deep .el-descriptions-item__container{
  margin: 5px 10px 0px 0;
}
.panel{
  display: flex;
}
.canvas-panel{
  flex: 1;
  overflow: hidden;
  display: flex;
  height: 100%;
  position: relative;
}
.container {
  &.disabled{
    filter:progid:DXImageTransform.Microsoft.BasicImage(graysale=1);
    -webkit-filter: grayscale(100%);
  }
  .total-time-label{
    font-size: 18px;
    margin-right: 10px;
  }
  .total-time{
    font-size: 20px;
  }
  .progress-line {
    width: 400px;
    margin-left: 10px;
  }
  .el-icon-time {
    color: #409EFF;
    margin-right: 3px;
  }
  section {
    padding: 30px;
    background-color: #fff;
    margin-bottom: 30px;
  }
  .img-container{
    display: flex;
    img{
      width: 500px;
      max-width: 100%;
    }
  }
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
::v-deep .el-table .disabled {
  color: #C0C4CC;
}

</style>
