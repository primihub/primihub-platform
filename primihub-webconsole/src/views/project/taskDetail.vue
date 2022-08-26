<template>
  <div v-loading="listLoading" class="container">
    <section class="infos">
      <el-descriptions :column="3" label-class-name="detail-title" title="基本信息">
        <el-descriptions-item label="任务ID">
          {{ task.taskId }}
        </el-descriptions-item>
        <el-descriptions-item label="任务类型">
          模型
        </el-descriptions-item>
        <el-descriptions-item v-if="task.taskState === 1" label="模型ID">
          {{ model.modelId }}
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
          {{ task.modelDesc }}
        </el-descriptions-item>
      </el-descriptions>
    </section>
    <section>
      <el-tabs v-model="tabName">
        <el-tab-pane label="所用数据" name="resource">
          <el-table
            :data="modelResources"
            border
          >
            <el-table-column
              prop="resourceId"
              label="资源ID"
            >
              <template slot-scope="{row}">
                <template v-if="hasViewPermission">
                  <el-button :disabled="project.status === 2" size="mini" type="text" @click="toResourceDetailPage(row)">{{ row.resourceId }}</el-button>
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
                {{ row.participationIdentity === 1? '发起方': '协作方' }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane v-if="task.taskState === 1" label="任务模型" name="model">
          <TaskModel :state="task.taskState" :project-status="project.status" />
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<script>
import { getModelDetail } from '@/api/model'
import TaskModel from '@/components/TaskModel'

export default {
  name: 'TaskDetail',
  components: {
    TaskModel
  },
  filters: {
    quotaTypeFilter(type) {
      const typeMap = {
        1: '训练样本集',
        2: '测试样本集'
      }
      return typeMap[type]
    },
    timeFilter(time) {
      const hour = parseInt(time / 3600) < 10 ? '0' + parseInt(time / 3600) : parseInt(time / 3600)
      const min = parseInt(time % 3600 / 60) < 10 ? '0' + parseInt(time % 3600 / 60) : parseInt(time % 3600 / 60)
      const sec = parseInt(time % 3600 % 60) < 10 ? '0' + parseInt(time % 3600 % 60) : parseInt(time % 3600 % 60)
      return hour + ':' + min + ':' + sec
    },
    taskStatusFilter(status) {
      // 任务状态(0未开始 1成功 2运行中 3失败 4取消)
      status = status || 0
      const statusMap = {
        0: '未开始',
        1: '已完成',
        2: '执行中',
        3: '任务失败',
        4: '取消'
      }
      return statusMap[status]
    }
  },
  data() {
    return {
      taskId: null,
      tabName: 'resource',
      listLoading: false,
      model: {},
      modelQuotas: [],
      modelResources: [],
      modelId: 0,
      modelComponent: [],
      anotherQuotas: [],
      taskState: null,
      task: {},
      project: {}
    }
  },
  computed: {
    hasViewPermission() {
      return this.$store.getters.buttonPermissionList.includes('UnionResourceDetail')
    }
  },
  created() {
    this.taskId = this.$route.params.id
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.listLoading = true
      this.taskId = this.$route.params.taskId
      this.modelId = this.$route.query.modelId || 0
      getModelDetail({ taskId: this.taskId }).then((response) => {
        this.listLoading = false
        console.log('response.data', response.result)
        const { task, model, modelQuotas, modelResources, modelComponent, anotherQuotas, taskState, project } = response.result
        this.task = task
        this.project = project
        this.model = model
        this.anotherQuotas = anotherQuotas
        this.modelQuotas = modelQuotas
        this.modelResources = modelResources.sort(function(a, b) { return a.participationIdentity - b.participationIdentity })
        this.modelComponent = modelComponent
        this.taskState = taskState
      })
    },
    statusStyle(status) {
      return status === 0 ? 'status-default el-icon-error' : status === 1 ? 'status-end el-icon-success' : status === 2 ? 'status-processing' : status === 3 ? 'status-error el-icon-error' : 'status-default  el-icon-error'
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
.container {
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
    border-radius: 20px;
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
  background-color: #409EFF;
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
</style>
