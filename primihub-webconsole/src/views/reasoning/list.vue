<template>
  <div class="container">
    <div class="search-area">
      <el-form :model="query" :inline="true">
        <el-form-item label="推理服务ID">
          <el-input v-model="query.id" size="small" placeholder="请输入" clearable @clear="handleClear('id')" />
        </el-form-item>
        <el-form-item label="推理服务名称">
          <el-input v-model="query.reasoningName" size="small" placeholder="请输入" clearable @clear="handleClear('reasoningName')" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.reasoningState" size="small" placeholder="请选择">
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
    <el-button class="add-button" icon="el-icon-plus" type="primary" @click="toTaskPage">推理服务</el-button>
    <div class="list">
      <el-table
        v-loading="listLoading"
        :data="dataList"
      >
        <el-table-column
          prop="reasoningId"
          label="推理服务ID"
          align="center"
        >
          <template slot-scope="{row}">
            <el-link type="primary" @click="toTaskDetail(row.id)">{{ row.id }}</el-link>
          </template>
        </el-table-column>
        <el-table-column
          prop="reasoningName"
          label="推理服务名称"
        />
        <el-table-column
          prop="reasoningType"
          label="推理类型"
        >
          <template slot-scope="{row}">
            {{ row.reasoningType }}方推理
          </template>
        </el-table-column>
        <el-table-column
          label="模型名称"
        >
          <template slot-scope="{row}">
            <el-link type="primary" @click="toModelDetail(row)">{{ row.modelName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column
          prop="releaseDate"
          label="上线时间"
        />
        <el-table-column
          prop="reasoningDesc"
          label="描述"
        />
        <el-table-column
          prop="reasoningState"
          label="状态"
          min-width="60"
        >
          <template slot-scope="{row}">
            <StatusIcon :status="row.reasoningState" />
            {{ row.reasoningState | reasoningStateFilter }}
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    </div>
  </div>
</template>

<script>
import { getReasoningList } from '@/api/reasoning'
import Pagination from '@/components/Pagination'
import StatusIcon from '@/components/StatusIcon'

export default {
  components: {
    Pagination,
    StatusIcon
  },
  filters: {
    reasoningStateFilter(status) {
      status = status || 0
      const statusMap = {
        0: '未开始',
        1: '完成',
        2: '运行中',
        3: '运行失败',
        4: '取消',
        5: '已删除'
      }
      return statusMap[status]
    }
  },
  data() {
    return {
      listLoading: false,
      query: {
        id: '',
        reasoningName: '',
        reasoningState: ''
      },
      statusOptions: [ // 任务状态(0未开始 1成功 2运行中 3失败 4取消)
        {
          label: '运行中',
          value: 2
        }, {
          label: '完成',
          value: 1
        },
        {
          label: '运行失败',
          value: 3
        }
      ],
      dataList: null,
      pageNo: 1,
      pageSize: 10,
      total: 0,
      pageCount: 0,
      searchList: [],
      timer: null
    }
  },
  async created() {
    await this.fetchData()
    console.log(this.searchList)
    if (this.searchList.length > 0) {
      this.timer = window.setInterval(async() => {
        setTimeout(await this.fetchData(), 0)
      }, 3000)
    } else {
      clearInterval(this.timer)
    }
  },
  destroyed() {
    clearInterval(this.timer)
  },
  methods: {
    toTaskPage() {
      this.$router.push({
        name: 'ModelReasoningTask'
      })
    },
    search() {
      this.pageNo = 1
      this.fetchData()
    },
    reset() {
      this.query.id = ''
      this.query.reasoningName = ''
      this.pageNo = 1
      this.query.reasoningState = ''
      this.fetchData()
    },
    handleClear(name) {
      console.log('清空', name)
      this.query[name] = ''
      this.fetchData()
    },
    toTaskDetail(id) {
      this.$router.push({
        name: 'ModelReasoningDetail',
        params: { id }
      })
    },
    toModelDetail({ runTaskId, taskId }) {
      this.$router.push({
        path: `/model/detail/${runTaskId}`,
        query: { taskId }
      })
    },
    toModelTaskDetail(row) {
      this.$router.push({
        path: `/project/detail/${row.projectId}/task/${row.taskId}`
      })
    },
    async fetchData() {
      this.listLoading = true
      const { id, reasoningName, reasoningState } = this.query
      const params = {
        id,
        reasoningState,
        reasoningName,
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }
      const { result } = await getReasoningList(params)
      this.dataList = result.data
      this.total = result.total
      this.pageCount = result.totalPage
      // filter the running task
      this.searchList = this.dataList.filter(item => item.reasoningState === 2)
      this.listLoading = false
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.pageSize = data.limit
      this.fetchData()
    }
  }
}
</script>
<style lang="scss" scoped>
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
.pagination {
  padding: 50px;
  display: flex;
  justify-content: center;
}
.add-button{
  margin-top: 20px;
}
</style>
