<template>
  <div class="app-container">
    <h2>授权审批</h2>
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
        <!-- <el-table-column
          prop="userName"
          label="审批人"
        /> -->
        <el-table-column
          prop="createDate"
          label="申请时间"
        />
        <el-table-column
          v-if="hasApporvalAuth"
          prop="recordStatus"
          label="操作"
        >
          <template slot-scope="{row}">
            <el-button size="small" type="primary" @click="handleResourceApproval(row.recordId,1)">通过</el-button>
            <el-button size="small" type="danger" @click="handleResourceApproval(row.recordId,2)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="pageCount>1" :total="total" :limit.sync="params.pageSize" :page.sync="params.pageNo" layout="total, prev, pager, next, jumper" @pagination="handlePagination" />
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getAuthorizationList, resourceApproval } from '@/api/resource'
import Pagination from '@/components/Pagination'

export default {
  components: {
    Pagination
  },
  data() {
    return {
      listLoading: false,
      tableData: [],
      total: 0,
      pageCount: 0,
      params: {
        pageNo: 1,
        pageSize: 5,
        status: 0
      }
    }
  },
  computed: {
    ...mapGetters([
      'buttonPermissionList'
    ]),
    hasApporvalAuth() { // 按钮上传权限
      return this.buttonPermissionList.indexOf('ResourceApprovalButton') !== -1
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    handleResourceApproval(recordId, status) {
      this.listLoading = true
      resourceApproval({ recordId, status }).then(res => {
        this.listLoading = false
        if (res.code === 0) {
          this.$message({
            message: '操作成功！',
            type: 'success'
          })
          this.params.pageNo = 1
          this.fetchData()
        } else {
          this.$message({
            message: '操作失败，请重试！',
            type: 'warning'
          })
        }
      })
    },
    handlePagination(data) {
      this.params.pageNo = data.page
      this.fetchData()
    },
    fetchData() {
      this.listLoading = true
      getAuthorizationList(this.params).then((res) => {
        const { result } = res
        this.tableData = result.data
        this.pageCount = result.totalPage
        this.total = result.total
        setTimeout(() => {
          this.listLoading = false
        }, 200)
      })
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
  padding-top: 50px;
  display: flex;
  justify-content: flex-end;
}
</style>
