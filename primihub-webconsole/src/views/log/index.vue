<template>
  <div v-loading="listLoading" class="container">
    <div class="search-area">
      <el-form :model="query" :inline="true" @keyup.enter.native="search">
        <el-form-item>
          <el-date-picker
            v-model="query.time"
            size="small"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd HH:mm:ss"
            :default-time="['00:00:00', '23:59:59']"
          />

        </el-form-item>
        <el-form-item>
          <el-select v-model="query.taskType" placeholder="请选择任务类型" size="small" clearable @clear="handleClear">
            <el-option
              v-for="item in taskTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="query.taskName" size="small" placeholder="请输入任务名称" clearable @clear="handleClear" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="query.taskId" size="small" placeholder="请输入任务ID" clearable @clear="handleClear" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" @click="search">查询</el-button>
          <el-button icon="el-icon-refresh-right" size="small" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="model-list">
      <el-table
        empty-text="暂无数据"
        :data="taskList"
        :default-sort="{prop: 'createDate',order: 'descending'}"
      >
        <el-table-column
          prop="createDate"
          label="日志时间戳"
          sortable
        />
        <el-table-column
          prop="taskType"
          label="任务类型"
          align="center"
        >
          <template slot-scope="{row}">
            <span>{{ row.taskType | taskTypeFilter }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="taskName"
          label="任务名称"
        />
        <el-table-column
          prop="taskIdName"
          label="任务ID"
        />
        <el-table-column
          label="操作"
          fixed="right"
          align="center"
        >
          <template slot-scope="{row}">
            <div class="buttons">
              <!-- <el-link :disabled="row.status === 2" type="primary" @click="download(row.taskId)">下载</el-link> -->
              <el-link type="primary" @click="openDialog(row)">查看</el-link>
            </div>

          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
      <el-dialog
        width="1000px"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
      >
        <TaskLog v-if="dialogVisible" class="log-wrapper" :task-id="taskId" :task-state="taskState" />
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import { getTaskList } from '@/api/task'
import Pagination from '@/components/Pagination'
import TaskLog from '@/components/Log'

export default {
  name: 'Log',
  components: {
    Pagination,
    TaskLog
  },
  data() {
    return {
      listLoading: false,
      query: {
        taskId: '',
        taskName: '',
        time: '',
        taskType: ''
      },
      taskList: null,
      pageNo: 1,
      pageSize: 10,
      total: 0,
      pageCount: 0,
      taskTypeOptions: [
        {
          value: 1,
          label: '联合建模'
        },
        {
          value: 2,
          label: '安全求交'
        },
        {
          value: 3,
          label: '隐匿查询'
        },
        {
          value: 4,
          label: '联合预测'
        }
      ],
      dialogVisible: false,
      taskId: 0,
      taskState: 0
    }
  },
  computed: {
    hasModelViewPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelView')
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    search() {
      this.pageNo = 1
      this.fetchData()
    },
    reset() {
      console.log('reset')
      this.query.taskId = ''
      this.query.taskName = ''
      this.query.time = ''
      this.query.taskType = ''
      this.pageNo = 1
      this.fetchData()
    },
    handleClear(name) {
      // console.log('清空', name)
      // this.query[name] = ''
      this.fetchData()
    },
    toModelDetail(row) {
      this.$router.push({
        path: `/model/detail/${row.modelId}`,
        query: { taskId: row.taskId }
      })
    },
    toModelTaskDetail(row) {
      this.$router.push({
        path: `/project/detail/${row.projectId}/task/${row.taskId}`
      })
    },
    fetchData() {
      this.taskList = []
      const { taskType, taskName, taskId, time } = this.query
      console.log(time)
      this.listLoading = true
      const params = {
        taskType,
        taskId,
        taskName,
        start: time ? new Date(time[0]).getTime() : '',
        end: time ? new Date(time[1]).getTime() : '',
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }
      console.log('fetchData', params)
      getTaskList(params).then((response) => {
        console.log('response.data', response.result)
        const { result } = response
        this.taskList = result.data
        this.total = result.total
        this.pageCount = result.totalPage
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },
    statusStyle(status) {
      return status === 0 ? 'status-default' : status === 1 ? 'status-processing' : status === 2 ? 'status-end' : 'status-error'
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.fetchData()
    },
    async download(taskId) {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/task/downloadTaskLog?taskId=${taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    },
    handleClose() {
      this.dialogVisible = false
    },
    openDialog({ taskId, taskState }) {
      this.taskId = taskId + ''
      this.taskState = taskState
      this.dialogVisible = true
    }
  }
}
</script>
<style lang="scss" scoped>
.el-date-editor--datetimerange.el-input, .el-date-editor--datetimerange.el-input__inner{
  width: 360px;
  padding: 3px 5px;
}
.search-area {
  padding: 30px 0px 10px 20px;
  background-color: #fff;
  display: flex;
  flex-wrap: wrap;
}
.form-wrap{
  padding-top: 20px;
  background-color: #fff;
}
.model-list {
  margin-top: 20px;
  border-top: 1px solid #eee;
  background-color: #fff;
  padding: 30px;
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
  background-color: #1677FF;
}
.status-end{
  background-color: #909399;
}
.status-processing{
  background-color: #67C23A;
}
.status-error{
  background-color: #F56C6C;
}
.pagination {
  padding: 50px;
  display: flex;
  justify-content: center;
}
.buttons{
  display: flex;
  justify-content: space-evenly;
}
</style>
