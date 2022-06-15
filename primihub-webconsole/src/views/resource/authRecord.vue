<template>
  <div class="app-container">
    <h2>授权申请记录</h2>
    <div v-loading="listLoading" class="table-list">
      <el-table
        :data="tableData"
        border
      >
        <el-table-column align="center" label="序号" width="100" type="index" />
        <el-table-column
          prop="organName"
          label="申请机构"
        />
        <el-table-column
          prop="resourceName"
          label="资源名称"
        />
        <el-table-column
          prop="projectName"
          label="所属项目"
        />
        <el-table-column
          prop="userName"
          label="审批人"
        />
        <el-table-column
          prop="createDate"
          label="申请时间"
          width="180"
        />
        <el-table-column
          prop="recordStatus"
          label="状态"
        >
          <template slot-scope="{row}">
            <el-tag :type="row.recordStatus | statusFilter">
              {{ row.recordStatus | statusTextFilter }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="pageCount>1" :limit.sync="params.pageSize" :page.sync="params.pageNo" :total="total" layout="total, prev, pager, next, jumper" @pagination="handlePagination" />
    </div>
  </div>
</template>

<script>
import { getAuthorizationList } from '@/api/resource'
import Pagination from '@/components/Pagination'

export default {
  components: {
    Pagination
  },
  filters: {
    statusFilter(status) {
      const statusMap = {
        1: 'success',
        0: 'info',
        2: 'danger'
      }
      return statusMap[status]
    },
    statusTextFilter(status) {
      const statusMap = {
        0: '未授权',
        1: '已授权',
        2: '已拒绝'
      }
      return statusMap[status]
    }
  },
  data() {
    return {
      listLoading: false,
      tableData: [],
      total: 0,
      pageCount: 0,
      params: {
        pageNo: 1,
        pageSize: 10
      }
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.listLoading = true
      getAuthorizationList(this.params).then((res) => {
        const { result } = res
        this.tableData = result.data
        this.pageCount = result.totalPage
        this.total = result.total
        this.listLoading = false
      })
    },
    handlePagination(data) {
      this.params.pageNo = data.page
      this.fetchData()
    }
  }
}
</script>
<style lang="scss" scoped>
.table-list {
  margin: 10px 0;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
::v-deep .el-table th{
  background: #fafafa;
}
.pagination {
  padding: 50px 0;
  display: flex;
  justify-content: center;
}
</style>
