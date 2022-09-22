<template>
  <div class="container">
    <div class="search-area">
      <el-form :model="query" :inline="true" @keyup.enter.native="search">
        <el-form-item label="中心节点">
          <el-select v-model="query.serverAddress" v-loading="serverAddressLoading" size="small" clearable placeholder="请选择" @change="handleServerAddressChange" @focus="handleServerAddressFocus" @clear="handleClear('serverAddress')">
            <el-option
              v-for="item in serverAddressList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="参与机构">
          <el-select v-model="query.organName" v-loading="organLoading" size="small" placeholder="请选择" clearable @change="handleOrganCascaderChange" @focus="handleOrganFocus" @clear="handleClear('organName')">
            <el-option
              v-for="item in organOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资源名称">
          <el-input v-model="query.resourceName" size="small" placeholder="请输入" clearable @clear="handleClear('resourceName')" />
        </el-form-item>
        <el-form-item label="资源Id">
          <el-input v-model="query.resourceId" size="small" placeholder="请输入" clearable @clear="handleClear('resourceId')" />
        </el-form-item>
        <el-form-item label="检索ID">
          <el-input v-model="query.retrievalId" size="small" placeholder="请输入" clearable @clear="handleClear('retrievalId')" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.taskState" size="small" placeholder="请选择" clearable @clear="handleClear('taskState')">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" @click="search">查询</el-button>
          <el-button icon="el-icon-refresh-right" size="small" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-button class="add-button" icon="el-icon-plus" type="primary" @click="toTaskPage">匿踪查询</el-button>
    <div class="list">
      <el-table
        v-loading="listLoading"
        :data="dataList"
      >
        <el-table-column
          prop="serverAddress"
          label="中心节点"
        >
          <template slot-scope="{row}">
            <el-tooltip :content="row.serverAddress" placement="top"><span>{{ row.serverAddress }}</span></el-tooltip>
          </template>
        </el-table-column>
        <el-table-column
          prop="organName"
          label="参与机构"
          align="center"
        >
          <template slot-scope="{row}">
            <el-tooltip :content="row.organName" placement="top"><span>{{ row.organName }}</span></el-tooltip>
          </template>
        </el-table-column>
        <el-table-column
          prop="resourceName"
          label="资源名称"
          min-width="100"
        >
          <template slot-scope="{row}">
            <el-tooltip :content="row.resourceName" placement="top"><span>{{ row.resourceName }}</span></el-tooltip>
          </template>
        </el-table-column>
        <el-table-column
          prop="resourceId"
          label="资源ID"
          min-width="120"
        >
          <template slot-scope="{row}">
            <el-tooltip :content="row.resourceId" placement="top">
              <el-link size="small" type="primary" @click="toResourceDetail(row)">{{ row.resourceId }}</el-link>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column
          label="数据信息"
          min-width="150"
        >
          <template slot-scope="{row}">
            <div class="info">
              特征量：{{ row.resourceColumnCount }}<br>
              样本量：{{ row.resourceRowsCount }} <br>
              正例样本数量：{{ row.resourceYRowsCount || 0 }}<br>
              正例样本比例：{{ row.resourceYRatio || 0 }}%
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="retrievalId"
          label="检索ID"
        />
        <el-table-column
          prop="taskState"
          label="查询状态"
          min-width="100"
        >
          <template slot-scope="{row}">
            <StatusIcon :status="row.taskState" />
            {{ row.taskState | statusFilter }}
          </template>
        </el-table-column>
        <el-table-column
          prop="createDate"
          label="查询时间"
          min-width="100"
        >
          <template slot-scope="{row}">
            {{ row.createDate.split(' ')[0] }} <br>
            {{ row.createDate.split(' ')[1] }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="120"
          fixed="right"
        >
          <template slot-scope="{row}">
            <div>
              <el-button :disabled="row.taskState !== 1" type="text" @click.stop="download(row.taskId)">导出结果</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    </div>
  </div>
</template>

<script>
import { getLocalOrganInfo, findMyGroupOrgan } from '@/api/center'
import { getPirTaskList } from '@/api/PIR'
import Pagination from '@/components/Pagination'
import StatusIcon from '@/components/StatusIcon'
import { getToken } from '@/utils/auth'

export default {
  components: {
    Pagination,
    StatusIcon
  },
  filters: {
    // 任务状态 任务状态(0未开始 1成功 2运行中 3失败 4取消)
    statusFilter(status) {
      const sourceMap = {
        2: '查询中',
        1: '成功',
        3: '失败'
      }
      return sourceMap[status]
    }
  },
  data() {
    return {
      serverAddressLoading: false,
      organLoading: false,
      listLoading: false,
      organOptions: [],
      serverAddressList: [],
      sysLocalOrganInfo: null,
      fusionList: [],
      organList: [],
      query: {
        serverAddress: '',
        organName: '',
        retrievalId: '',
        resourceName: '',
        resourceId: '',
        taskState: ''
      },
      statusOptions: [ // 任务状态(0未开始 1成功 2运行中 3失败 4取消)
        {
          label: '未开始',
          value: 0
        }, {
          label: '成功',
          value: 1
        },
        {
          label: '查询中',
          value: 2
        },
        {
          label: '失败',
          value: 3
        },
        {
          label: '取消',
          value: 4
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
    if (this.searchList.length > 0) {
      // this.listLoading = false
      this.timer = window.setInterval(async() => {
        setTimeout(await this.fetchData(), 0)
      }, 5000)
    }
  },
  methods: {
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
      this.query.serverAddress = ''
      this.query.organName = ''
      this.query.resourceName = ''
      this.query.resourceId = ''
      this.query.retrievalId = ''
      this.query.taskState = ''
      this.pageNo = 1
      this.fetchData()
    },
    handleClear(name) {
      console.log('清空', name)
      this.query[name] = ''
      this.fetchData()
    },
    toResourceDetail({ resourceId, serverAddress }) {
      this.$router.push({
        name: 'UnionResourceDetail',
        params: { id: resourceId },
        query: { serverAddress }
      })
    },
    async fetchData() {
      this.listLoading = true
      const { taskState, serverAddress, organName, resourceName, resourceId, retrievalId } = this.query
      const params = {
        taskState,
        serverAddress,
        organName,
        resourceName,
        resourceId,
        retrievalId,
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }
      const res = await getPirTaskList(params)
      const { result } = res
      this.dataList = result.data
      this.total = result.total
      this.pageCount = result.totalPage
      this.listLoading = false
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
    async handleServerAddressChange(value) {
      this.query.serverAddress = this.serverAddressList.filter(item => item.value === value)[0]?.label
    },
    handleOrganCascaderChange(value) {
      this.query.organName = this.organOptions.filter(item => item.value === value)[0]?.label
    },
    async handleOrganFocus() {
      if (this.query.serverAddress === '') {
        this.$message({
          message: '请先选择中心节点',
          type: 'warning'
        })
        return
      }
      if (this.organList.length < 1) {
        await this.findMyGroupOrgan()
      }
    },
    async handleServerAddressFocus() {
      if (this.serverAddressList.length < 1) {
        this.serverAddressList = []
        await this.getLocalOrganInfo()
      }
    },
    async getLocalOrganInfo() {
      this.serverAddressLoading = true
      const { result } = await getLocalOrganInfo()
      this.sysLocalOrganInfo = result.sysLocalOrganInfo
      if (!result.sysLocalOrganInfo) return
      this.fusionList = result.sysLocalOrganInfo?.fusionList
      this.fusionList && this.fusionList.map((item, index) => {
        this.serverAddressList.push({
          label: item.serverAddress,
          value: index,
          registered: item.registered,
          show: item.show
        })
      })
      this.serverAddressLoading = false
    },
    async findMyGroupOrgan() {
      this.organLoading = true
      const { result } = await findMyGroupOrgan({ serverAddress: this.query.serverAddress })
      this.organList = result.dataList.organList
      this.organOptions = this.organList && this.organList.map((item) => {
        return {
          label: item.globalName,
          value: item.globalId,
          leaf: true
        }
      })
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
::v-deep .el-table .cell{
  margin: 0 5px;
  line-height: 1.5;
  max-height: 65px;
  overflow: hidden;
  cursor: pointer;
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
.list {
  padding: 30px;
  margin-top: 20px;
  border-top: 1px solid #eee;
  background-color: #fff;
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
  margin-top: 20px;
}
</style>
