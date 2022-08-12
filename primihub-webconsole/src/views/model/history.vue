<template>
  <div>
    <div class="model-list">
      <el-table
        :data="taskList"
        border
      >
        <!-- <el-table-column
          prop="taskName"
          label="任务名称"
        /> -->
        <el-table-column
          type="index"
          align="center"
        />
        <el-table-column
          prop="taskIdName"
          label="任务ID"
        />
        <el-table-column
          prop="taskType"
          align="center"
          label="任务类型"
          width="80"
        >
          <template slot-scope="{row}">
            <span><el-tag type="primary" size="mini">{{ row.taskType | taskTypeFilter }}</el-tag></span>
          </template>
        </el-table-column>
        <el-table-column
          prop="taskState"
          label="任务状态"
          width="100"
        >
          <template slot-scope="{row}">
            <i :class="statusStyle(row.taskState)" />
            {{ row.taskState | taskStatusFilter }}
            <span v-if="row.taskState === 3" class="error-tips">{{ row.taskErrorMsg }}</span>
            <span v-if="row.taskState === 2"> <i class="el-icon-loading" /></span>
          </template>
        </el-table-column>
        <el-table-column
          label="时间"
          min-width="100"
        >
          <template slot-scope="{row}">
            <span>开始时间：{{ row.taskStartDate }}</span> <br>
            <span v-if="row.taskState !== (0 || 2)">结束时间：{{ row.taskEndDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="timeConsuming"
          label="耗时"
          width="100"
        >
          <template slot-scope="{row}">
            <span>{{ row.timeConsuming | timeFilter }}</span> <br>
          </template>
        </el-table-column>
        <el-table-column
          v-if="hasModelViewPermission"
          label="操作"
        >
          <template slot-scope="{row}">
            <div class="buttons">
              <el-button v-if="hasModelViewPermission" type="text" size="mini" icon="el-icon-view" @click="toModelDetail(row.taskId)">查看详情</el-button>
              <el-button v-if="hasModelDownloadPermission && row.taskState === 1" type="text" size="mini" icon="el-icon-download" @click="download(row.taskId)">下载结果</el-button>
              <el-button type="text" size="mini" icon="el-icon-delete" :disabled="row.taskState === 2" @click="deleteTask(row.taskId)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="pageCount>1" :limit.sync="pageSize" :page.sync="pageNo" :total="total" layout="total, prev, pager, next, jumper" @pagination="handlePagination" />
  </div>
</template>

<script>
import { getModelTaskList } from '@/api/model'
import { deleteTask } from '@/api/task'
import Pagination from '@/components/Pagination'
import { getToken } from '@/utils/auth'

export default {
  components: {
    Pagination
  },
  filters: {
    taskStatusFilter(status) {
      // 任务状态(0未开始 1成功 2运行中 3失败 4取消)
      status = status || 0
      const statusMap = {
        0: '未开始',
        1: '成功',
        2: '运行中',
        3: '失败',
        4: '取消'
      }
      return statusMap[status]
    },
    taskTypeFilter(status) {
      // 任务类型 1、模型 2、PSI 3、PIR
      status = status || 0
      const statusMap = {
        1: '模型',
        2: 'PSI',
        3: 'PIR'
      }
      return statusMap[status]
    },
    timeFilter(time) {
      const hour = parseInt(time / 3600) < 10 ? '0' + parseInt(time / 3600) : parseInt(time / 3600)
      const min = parseInt(time % 3600 / 60) < 10 ? '0' + parseInt(time % 3600 / 60) : parseInt(time % 3600 / 60)
      const sec = parseInt(time % 3600 % 60) < 10 ? '0' + parseInt(time % 3600 % 60) : parseInt(time % 3600 % 60)
      return hour + ':' + min + ':' + sec
    }
  },
  data() {
    return {
      listLoading: false,
      statusList: [{ label: '未开始', value: 0 }, { label: '运行中', value: 1 }, { label: '已完成', value: 2 }, { label: '异常', value: -1 }],
      taskList: null,
      modelId: this.$route.params.id,
      pageNo: 1,
      pageSize: 10,
      total: 0,
      pageCount: 0,
      hidePagination: true,
      emptyText: '',
      timer: null
    }
  },
  computed: {
    hasModelViewPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelView')
    },
    hasModelDownloadPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelResultDownload')
    }
  },
  destroyed() {
    clearInterval(this.timer)
  },
  created() {
    this.fetchData()
    this.timer = window.setInterval(() => {
      setTimeout(this.fetchData(), 0)
    }, 5000)
  },
  methods: {
    deleteTask(taskId) {
      this.$confirm('此操作将永久删除该任务, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteTask(taskId).then(res => {
          if (res.code === 0) {
            const posIndex = this.taskList.findIndex(item => item.taskId === taskId)
            if (posIndex !== -1) {
              this.taskList.splice(posIndex, 1)
            }
            this.$message({
              message: '删除成功',
              type: 'success',
              duration: 1000
            })
          }
        })
      }).catch(() => {})
    },
    async download(taskId) {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/task/downloadTaskFile?taskId=${taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    },
    toModelDetail(taskId) {
      this.$router.push({
        path: `/model/detail/${taskId}`,
        query: { modelId: this.modelId }
      })
    },
    searchModel() {
      console.log('searchModel', this.searchName)
    },
    fetchData() {
      // this.listLoading = true
      this.taskList = []
      const params = {
        modelId: this.modelId,
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }
      getModelTaskList(params).then((response) => {
        const { result } = response
        this.taskList = result.data
        this.total = result.total
        this.pageCount = result.totalPage
        // this.listLoading = false
        // filter the running task
        const res = this.taskList.filter(item => item.taskState === 2)
        // No tasks are running
        if (res.length === 0) {
          clearInterval(this.timer)
        }
      })
    },
    statusStyle(status) {
      return status === 0 ? 'status-default' : status === 1 ? 'status-end' : status === 2 ? 'status-processing' : status === 3 ? 'status-error' : 'status-default'
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.fetchData()
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-form--inline .el-form-item {
  margin: 5px 10px!important;
}
.model-list{
  background: #fff;
  border-radius: 20px;
  padding: 30px;
}
.form-wrap{
  padding: 20px 0;
}
.status-default,.status-processing,.status-error,.status-end{
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
  vertical-align: middle;
  margin-right: 3px;
}
.status-default{
  background-color: #909399;
}
.status-end{
  background-color: #67C23A;
}
.status-processing{
  background-color: #409EFF;
}
.status-error{
  background-color: #F56C6C;
}
::v-deep .el-table,::v-deep .el-divider__text, .el-link{
  font-size: 12px;
}
::v-deep .el-table .el-table__cell{
  padding: 5px 0;
}
.error-tips{
  display: block;
  white-space:normal;
  color: #F56C6C;
  font-size: 12px;
  line-height: 1.2;
}
</style>
