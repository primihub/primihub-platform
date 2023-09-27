<template>
  <div class="container">
    <div class="search-area">
      <el-form :model="query" :inline="true" @keyup.enter.native="search">
        <el-form-item>
          <el-select v-model="query.organName" placeholder="请选择参与机构" clearable @change="handleOrganChange" @clear="handleClear('organName')">
            <el-option
              v-for="item in organList"
              :key="item.globalId"
              :label="item.globalName"
              :value="item.globalId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="query.taskName" placeholder="请输入任务名称" clearable @clear="handleClear('taskName')" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="query.retrievalId" placeholder="请输入查询关键词" clearable @clear="handleClear('retrievalId')" />
        </el-form-item>
        <!-- <el-form-item>
          <el-select v-model="query.taskState" placeholder="请选择查询状态" clearable @clear="handleClear('taskState')">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item> -->
        <el-form-item>
          <el-date-picker
            v-model="query.createDate"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            :default-time="['00:00:00', '23:59:59']"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
          <el-button icon="el-icon-refresh-right" @click="reset" />
        </el-form-item>
      </el-form>
    </div>
    <div class="list">
      <el-button class="add-button" icon="el-icon-circle-plus-outline" type="primary" @click="toTaskPage">隐匿查询</el-button>
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
            <span>{{ row.createDate.split(' ')[0] }}</span><br>
            <span>{{ row.createDate.split(' ')[1] }}</span><br>
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
            <p class="tool-buttons">
              <el-link type="primary" @click="toTaskDetailPage(row.taskId)">查看</el-link>
              <el-link v-if="row.taskState === 2" type="warning" @click="cancelTask(row)">取消</el-link>
              <el-link type="danger" :disabled="row.taskState === 2" @click="deleteTask(row)">删除</el-link>
            </p>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    </div>
  </div>
</template>

<script>
import { getAvailableOrganList } from '@/api/center'
import { getPirTaskList } from '@/api/PIR'
import { deleteTask, cancelTask } from '@/api/task'
import Pagination from '@/components/Pagination'
import StatusIcon from '@/components/StatusIcon'
import { getToken } from '@/utils/auth'

export default {
  components: {
    Pagination,
    StatusIcon
  },
  data() {
    return {
      organLoading: false,
      sysLocalOrganInfo: null,
      fusionList: [],
      organList: [],
      query: {
        organName: '',
        retrievalId: '',
        resourceName: '',
        taskState: '',
        taskId: ''
      },
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
      searchList: [],
      pageNo: 1,
      pageSize: 10,
      total: 0,
      pageCount: 0,
      timer: null,
      startInterval: true
    }
  },
  async created() {
    await this.fetchData()
    await this.getAvailableOrganList()
    if (this.searchList.length > 0) {
      this.timer = window.setInterval(async() => {
        setTimeout(await this.fetchData(), 0)
      }, 5000)
    }
  },
  destroyed() {
    clearInterval(this.timer)
  },
  methods: {
    handleDateChange(val) {
      if (!val) {
        this.query.createDate = []
        this.fetchData()
      }
    },
    toTaskDetailPage(id) {
      this.$router.push({
        name: 'PIRDetail',
        params: { id }
      })
    },
    async cancelTask(row) {
      const res = await cancelTask(row.taskId)
      if (res.code === 0) {
        const posIndex = this.dataList.findIndex(item => item.taskId === row.taskId)
        this.$set(this.dataList[posIndex], 'taskState', 4)
        this.$notify({
          message: '取消成功',
          type: 'success',
          duration: 1000
        })
      }
    },
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
    toTaskPage() {
      this.$router.push({
        name: 'PIRTask'
      })
    },
    async search() {
      this.pageNo = 1
      await this.fetchData()
    },
    reset() {
      for (const key in this.query) {
        this.query[key] = ''
      }
      this.pageNo = 1
      this.fetchData()
    },
    handleClear(name) {
      console.log('清空', name)
      this.query[name] = ''
      this.fetchData()
    },
    toResourceDetail({ resourceId }) {
      this.$router.push({
        name: 'UnionResourceDetail',
        params: { id: resourceId }
      })
    },
    async fetchData() {
      const { taskState, organName, resourceName, retrievalId, taskName } = this.query
      let params = {
        taskName,
        taskState,
        organName,
        resourceName,
        retrievalId,
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }
      if (this.query.createDate && this.query.createDate.length > 0) {
        const startDate = this.query.createDate.length > 0 ? this.query.createDate[0] : ''
        const endDate = this.query.createDate.length > 0 ? this.query.createDate[1] : ''
        params = {
          ...params,
          startDate: startDate,
          endDate: endDate }
      }
      const res = await getPirTaskList(params)
      const { result } = res
      this.dataList = result.data
      this.total = result.total
      this.pageCount = result.totalPage
      // filter the running task
      this.searchList = this.dataList.filter(item => item.taskState === 2)
      // No tasks are running
      if (this.searchList.length === 0) {
        this.startInterval = false
        clearInterval(this.timer)
      }
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.fetchData()
    },
    handleOrganChange(value) {
      this.query.organName = this.organList.find(item => item.globalId === value)?.globalName
    },
    async getAvailableOrganList() {
      this.organLoading = true
      const { result } = await getAvailableOrganList()
      this.organList = result
      this.organLoading = false
    },
    async download(taskId) {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/task/downloadTaskFile?taskId=${taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    }

  }
}
</script>
<style lang="scss" scoped>
::v-deep .el-input--suffix .el-input__inner{
  padding-right: 0;
}
.search-area {
  padding: 48px 40px 20px 40px;
  background-color: #fff;
  display: flex;
  flex-wrap: wrap;
  border-radius: 12px;
}
.form-wrap{
  padding-top: 20px;
  background-color: #fff;
}
.list {
  padding: 25px 48px;
  margin-top: 20px;
  border-top: 1px solid #eee;
  background-color: #fff;
  border-radius: 12px;
}
.info{
  font-size: 12px;
  line-height: 1.4;
}
.pagination {
  padding: 50px;
  display: flex;
  justify-content: center;
}
.add-button{
  margin-bottom: 25px;
}
.tool-buttons{
  display: flex;
  justify-content: center;
  .el-link{
    margin: 0 5px;
  }
}
</style>
