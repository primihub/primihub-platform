<script>
import { getExamTaskListRequest, submitExamTaskRequest } from '@/api/preprocessing'
import { deleteTask } from '@/api/task'
import Pagination from '@/components/Pagination'
import StatusIcon from '@/components/StatusIcon'

export default {
  components: {
    Pagination,
    StatusIcon
  },
  data() {
    return {
      statusOptions: [ // 任务状态(0未开始 1成功 2运行中 3失败 4取消)
        {
          label: '查询中',
          value: 2
        }, {
          label: '成功',
          value: 1
        },
        {
          label: '失败',
          value: 3
        }
      ],
      dataList: [],
      runningTaskList: [],
      pageNo: 1,
      pageSize: 10,
      total: 0,
      pageCount: 0,
      timer: null
    }
  },

  async created() {
    // create a timer to fetch data every 15 minutes
    // when there are no tasks currently running, clear the timer.
    this.timer = window.setInterval(() => {
      setTimeout(this.fetchData(), 0)
    }, 1500)
    this.fetchData()
  },

  destroyed() {
    clearInterval(this.timer)
  },

  methods: {
    /** fetch list data */
    fetchData() {
      const params = { pageNo: this.pageNo, pageSize: this.pageSize }
      getExamTaskListRequest(params).then(res => {
        const { result } = res
        this.dataList = result.data
        this.total = result.total
        this.pageCount = result.totalPage
        // filter the running task
        this.runningTaskList = this.dataList.filter(item => item.taskState === 2)
        // No tasks are running
        this.runningTaskList.length === 0 && clearInterval(this.timer)
      }).catch(err => {
        console.log(err)
        clearInterval(this.timer)
      })
    },

    /** submit task */
    async handelSubmitExamTask(taskId) {
      const { code, msg } = await submitExamTaskRequest({ taskId })
      if (code === 0) {
        this.$message.success('操作成功！')
        this.fetchData()
      } else {
        this.$message.error(`error: ${msg}`)
      }
    },

    /** delete row data */
    deleteTask(row) {
      if (row.taskState === 2) return
      this.$confirm('此操作将永久删除该任务, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteTask(row.taskId).then(res => {
          if (res.code === 0) {
            const posIndex = this.dataList.findIndex(item => item.taskId === row.taskId)
            if (posIndex !== -1) {
              this.dataList.splice(posIndex, 1)
            }
            this.$message({
              message: '删除成功',
              type: 'success',
              duration: 1000
            })
            clearInterval(this.timer)
          }
        })
      }).catch(() => {})
    },

    /** Go to task creation page */
    toTaskPage() {
      this.$router.push({ name: 'PreprocessingTask' })
    },

    /** Go to resource detail page */
    toResourceDetail({ resourceId }) {
      this.$router.push({ name: 'UnionResourceDetail', params: { id: resourceId }})
    },

    /** pagination */
    handlePagination(data) {
      this.pageNo = data.page
      this.fetchData()
    }
  }
}
</script>

<template>
  <div class="container">
    <div class="list">
      <el-button class="add-button" icon="el-icon-circle-plus-outline" type="primary" @click="toTaskPage">预处理</el-button>
      <el-table
        :data="dataList"
      >
        <el-table-column
          type="index"
          align="center"
          label="序号"
          width="50"
        />
        <el-table-column
          prop="taskName"
          label="任务名称"
        >
          <template slot-scope="{row}">
            <el-tooltip :content="row.taskName" placement="top"><el-link type="primary" @click="toTaskDetailPage(row.taskId)">{{ row.taskName }}</el-link></el-tooltip>
          </template>
        </el-table-column>
        <el-table-column
          prop="organName"
          label="参与机构"
          align="center"
        />
        <el-table-column
          prop="resourceName"
          label="被查询资源名称"
          min-width="120"
        >
          <template slot-scope="{row}">
            <el-tooltip :content="row.resourceName" placement="top"><span>{{ row.resourceName }}</span></el-tooltip>
          </template>
        </el-table-column>
        <el-table-column
          prop="resourceId"
          label="被查询资源ID"
          min-width="150"
        >
          <template slot-scope="{row}">
            <el-tooltip :content="row.resourceId" placement="top">
              <el-link :disabled="row.available === 1" type="primary" @click="toResourceDetail(row)">{{ row.resourceId }}</el-link>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column
          prop="retrievalId"
          label="查询关键词"
          min-width="100"
        >
          <template slot-scope="{row}">
            <el-tooltip :content="row.retrievalId" placement="top"><span>{{ row.retrievalId }}</span></el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="发起时间" prop="createDate" min-width="120px">
          <template slot-scope="{row}">
            <span>{{ row.createDate && row.createDate.split(' ')[0] }}</span><br>
            <span>{{ row.createDate && row.createDate.split(' ')[1] }}</span><br>
          </template>
        </el-table-column>
        <el-table-column label="任务耗时">
          <template slot-scope="{row}">
            {{ row.consuming | timeFilter }}
          </template>
        </el-table-column>
        <el-table-column
          prop="taskState"
          label="查询状态"
          min-width="120px"
        >
          <template slot-scope="{row}">
            <StatusIcon :status="row.taskState" />
            {{ row.taskState | taskStatusFilter }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="120px" align="center">
          <template slot-scope="{row}">
            <el-link v-if="row.taskState === 0" type="primary" @click="handelSubmitExamTask(row.taskId)">发起</el-link>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.list {
  padding: 25px 48px;
  margin-top: 20px;
  border-top: 1px solid #eee;
  background-color: #fff;
  border-radius: 12px;
}
.add-button{
  margin-bottom: 25px;
}
</style>
