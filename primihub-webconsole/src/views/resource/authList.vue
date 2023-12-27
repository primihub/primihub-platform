<template>
  <div class="container">
    <div class="resource">
      <div class="resource-info flex margin-bottom-5">
        <div>资源ID: {{ resourceId }}
          资源名称: {{ resourceName }}</div>
        <el-button v-if="isOrganAdmin" class="add-button" icon="el-icon-circle-plus-outline" type="primary" @click="addAuthorization">添加授权</el-button>
      </div>
      <el-table
        v-loading="loading"
        :data="resourceList"
        :row-class-name="tableRowDisabled"
        empty-text="暂无数据"
        border
      >
        <el-table-column
          prop="resourceId"
          label="被授权人"
        />
        <el-table-column
          prop="organName"
          label="被授权人所在机构"
        />
        <el-table-column
          prop="applyTime"
          label="申请授权时间"
        />
        <!-- 0申请中，1审核通过，2审核拒绝 -->
        <el-table-column
          prop="auditStatus"
          label="授权状态"
        >
          <template slot-scope="{row}">
            {{ row.auditStatus === 1 ? '已授权' : row.auditStatus === 2 ? '已拒绝' : '申请待确认' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="assignTime"
          label="授权时间"
        />
        <el-table-column
          label="操作"
          fixed="right"
          width="160"
          align="center"
        >
          <template slot-scope="{row}">
            <!-- <el-button type="text" @click="changeResourceStatus(row)">{{ row.resourceState === 0 ? '下线': '上线' }}</el-button> -->
            <el-button type="text" @click="changeResourceStatus(row)">{{ row.resourceState === 0 ? '取消授权': '确认授权' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <authorizationUserDialog :visible.sync="dialogVisible" :resource-id="resourceId" @close="closeAuthDialog" @cancel="closeAuthDialog" @submit="handleAuthConfirm" />
    <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getDataResourceAssignmentDetail, getResourceTags, deleteResource, resourceStatusChange } from '@/api/resource'
import Pagination from '@/components/Pagination'
import authorizationUserDialog from '@/components/authorizationUserDialog'

export default {
  components: { Pagination, authorizationUserDialog },
  data() {
    return {
      userValue: [],
      resourceId: 0,
      dialogVisible: false,
      loading: false,
      activeName: '2',
      query: {
        fileContainsY: '', resourceId: '', resourceName: '', tag: null, userName: '', resourceSource: '', selectTag: 0, resourceAuthType: ''
      },
      tags: [],
      resourceSourceList: [{
        label: '文件上传',
        value: 1
      }, {
        label: '数据库导入',
        value: 2
      }],
      resourceList: [],
      currentPage: 1,
      total: 0,
      pageCount: 0,
      pageNo: 1,
      pageSize: 10,
      resourceName: '',
      isReset: false
    }
  },
  computed: {
    ...mapGetters([
      'buttonPermissionList',
      'isOrganAdmin'
    ])
  },
  async created() {
    console.log(this.$route.query)
    this.resourceId = this.$route.params.id || ''
    this.resourceName = decodeURIComponent(this.$route.query.resourceName)
    console.log('resourceId', this.resourceId)
    await this.fetchData()
    await this.getResourceTags()
  },
  methods: {
    closeAuthDialog() {
      this.dialogVisible = false
    },
    handleAuthConfirm(data) {
      this.userValue = data
      this.dialogVisible = false
    },
    addAuthorization() {
      this.dialogVisible = true
    },
    handleTabClick() {
      this.fetchData()
    },
    reset() {
      this.isReset = true
      for (const key in this.query) {
        this.query[key] = ''
      }
      this.pageNo = 1
      this.fetchData()
    },
    tableRowDisabled({ row }) {
      if (row.resourceState === 1) {
        return 'resource-disabled'
      } else {
        return ''
      }
    },
    changeResourceStatus({ resourceId, resourceState }) {
      resourceState = resourceState === 0 ? 1 : 0
      resourceStatusChange({ resourceId, resourceState }).then(res => {
        if (res.code === 0) {
          this.$message({
            message: resourceState === 0 ? '上线成功' : '下线成功',
            type: 'success'
          })
          const posIndex = this.resourceList.findIndex(item => item.resourceId === resourceId)
          this.resourceList[posIndex].resourceState = resourceState
        }
      })
    },
    async getResourceTags() {
      const { result } = await getResourceTags()
      this.tags = result && result.map((item, index) => {
        return {
          value: index,
          label: item
        }
      })
    },
    handleTagChange(tagName) {
      this.query.tag = tagName
      this.query.selectTag = 0
    },
    searchResource(tagName) {
      this.pageNo = 1
      if (tagName !== '') {
        this.query.tag = tagName
        this.query.selectTag = 1
        this.fetchData()
      }
    },
    handleResourceDelete(resourceId) {
      this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.confirmDelete(resourceId)
        this.fetchData()
      }).catch(() => {})
    },
    confirmDelete(resourceId) {
      deleteResource(resourceId).then(res => {
        if (res.code === 0) {
          this.$message({
            message: '删除成功',
            type: 'success'
          })
        } else {
          this.$message({
            message: res.msg,
            type: 'error'
          })
        }
      })
    },
    async fetchData() {
      this.loading = true
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        resourceFusionId: this.resourceId,
        queryType: 1
      }
      try {
        const res = await getDataResourceAssignmentDetail(params)
        if (res.code === 0) {
          const { data, total, totalPage } = res.result
          this.total = total
          this.pageCount = totalPage
          if (data.length > 0) {
            this.resourceList = data
          }
          this.isReset = false
        }
        this.loading = false
      } catch (e) {
        this.loading = false
      }
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.fetchData()
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/resource.scss";
.resource{
  position: relative;
  .resource-info{
    padding: 10px 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .add-button{
    position: relative;
  }
  .resource-info{
    margin-bottom: 10px;
  }
}
</style>
