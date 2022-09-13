<template>
  <div v-loading="listLoading" class="container">
    <div class="search-area">
      <el-form :model="query" :inline="true" @keyup.enter.native="search">
        <el-form-item label="模型推理服务ID">
          <el-input v-model="query.id" size="small" placeholder="请输入" clearable @clear="handleClear('id')" />
        </el-form-item>
        <el-form-item label="模型推理服务名称">
          <el-input v-model="query.reasoningName" size="small" placeholder="请输入" clearable @clear="handleClear('reasoningName')" />
        </el-form-item>
        <!-- <el-form-item label="状态">
          <el-select v-model="query.reasoningState" placeholder="请选择">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item> -->
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" @click="search">查询</el-button>
          <el-button icon="el-icon-search" size="small" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-button class="add-button" icon="el-icon-plus" type="primary" @click="toTaskPage">模型推理</el-button>
    <div class="model-list">
      <el-table
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
          prop="reasoningDesc"
          label="描述"
        />
        <el-table-column
          prop="reasoningType"
          label="推理类型"
          min-width="120"
        >
          <template slot-scope="{row}">
            {{ row.reasoningType }}方推理
          </template>
        </el-table-column>
        <!-- <el-table-column
          prop="resourceNum"
          label="上线日期"
          align="center"
        /> -->
        <!-- <el-table-column
          prop="reasoningState"
          label="状态"
          align="center"
        /> -->
      </el-table>
    </div>
    <pagination v-show="pageCount>1" :limit.sync="pageSize" :page.sync="pageNo" :total="total" layout="total, prev, pager, next, jumper" @pagination="handlePagination" />
  </div>
</template>

<script>
import { getReasoningList } from '@/api/reasoning'
import Pagination from '@/components/Pagination'

export default {
  components: {
    Pagination
  },
  data() {
    return {
      listLoading: false,
      query: {
        id: '',
        reasoningName: '',
        reasoningState: ''
      },
      statusOptions: [{
        label: '',
        value: 0
      }],
      dataList: null,
      pageNo: 1,
      pageSize: 10,
      total: 0,
      pageCount: 0
    }
  },
  created() {
    this.fetchData()
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
      console.log('reset')
      this.query.id = ''
      this.query.reasoningName = ''
      this.pageNo = 1
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
    toModelTaskDetail(row) {
      this.$router.push({
        path: `/project/detail/${row.projectId}/task/${row.taskId}`
      })
    },
    fetchData() {
      this.listLoading = true
      this.dataList = []
      const { id, reasoningName } = this.query
      const params = {
        id,
        reasoningName: reasoningName,
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }
      console.log('fetchData', params)
      getReasoningList(params).then((response) => {
        console.log('response.data', response.result)
        const { result } = response
        this.dataList = result.data
        this.total = result.total
        this.pageCount = result.totalPage
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },
    handlePagination(data) {
      this.pageNo = data.page
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
.model-list {
  margin-top: 20px;
  border-top: 1px solid #eee;
  background-color: #fff;
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
  background-color: #409EFF;
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
.add-button{
  margin-top: 20px;
}
</style>
