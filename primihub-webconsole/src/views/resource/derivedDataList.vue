<template>
  <div class="container">
    <div class="search-area">
      <el-form :model="query" label-width="100px" :inline="true" @keyup.enter.native="search">
        <el-form-item label="数据ID">
          <el-input v-model="query.resourceId" placeholder="请输入资源ID" clearable @clear="handleClear" />
        </el-form-item>
        <el-form-item label="资源名称">
          <el-input v-model="query.resourceName" placeholder="请输入资源名称" clearable @clear="handleClear" />
        </el-form-item>
        <el-form-item label="任务ID">
          <el-input v-model="query.taskIdName" placeholder="请输入任务ID" clearable @clear="handleClear" />
        </el-form-item>
        <el-form-item label="衍生数据来源">
          <el-input v-model="query.tag" placeholder="请输入衍生数据来源" clearable @clear="handleClear" />
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="query.createDate"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd HH:mm:ss"
            clearable
            @change="handleClear"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" class="search-button" @click="search">查询</el-button>
          <el-button class="button" type="primary" icon="el-icon-refresh-right" plain @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="resource">
      <DerivedDataTable :data="resourceList" />
      <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    </div>

  </div>
</template>

<script>
import { getDerivationResourceList } from '@/api/resource'
import Pagination from '@/components/Pagination'
import DerivedDataTable from '@/components/DerivedDataTable'

export default {
  components: { Pagination, DerivedDataTable },
  data() {
    return {
      loading: false,
      tags: [],
      serverAddressList: [],
      query: {
        resourceId: '',
        resourceName: '',
        tag: '',
        createDate: [],
        startDate: '',
        endDate: '',
        taskIdName: ''
      },
      cascaderValue: [],
      resourceList: [],
      total: 0,
      pageCount: 0,
      pageNo: 1,
      pageSize: 10,
      resourceName: '',
      resourceSortType: 0,
      resourceAuthType: 0,
      serverAddress: null,
      groupId: 0,
      organId: 0
    }
  },
  computed: {
    hasViewPermission() {
      return this.$store.getters.buttonPermissionList.includes('UnionResourceDetail')
    }
  },
  async created() {
    await this.fetchData()
  },
  methods: {
    reset() {
      this.query.resourceId = ''
      this.query.resourceName = ''
      this.query.tag = ''
      this.query.createDate = []
      this.query.taskIdName = ''
      this.pageNo = 1
      this.fetchData()
    },
    async search() {
      await this.fetchData()
    },
    async fetchData() {
      this.loading = true
      const resourceId = Number(this.query.resourceId)
      if (resourceId !== '' && isNaN(resourceId)) {
        this.$message({
          message: '资源id为数字',
          type: 'warning'
        })
        return
      }
      // this.resourceList = []
      const createDate = this.query.createDate
      this.query.startDate = createDate && createDate[0]
      this.query.endDate = createDate && createDate[1]
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        resourceName: this.query.resourceName,
        resourceId: this.query.resourceId,
        startDate: createDate && createDate[0],
        endDate: createDate && createDate[1],
        taskIdName: this.query.taskIdName,
        tag: this.query.tag
      }
      const { code, result } = await getDerivationResourceList(params)
      if (code === 0) {
        const { data, total, totalPage } = result
        this.total = total
        this.pageCount = totalPage
        this.resourceList = data
      }
      this.loading = false
    },
    async handlePagination(data) {
      this.pageNo = data.page
      await this.fetchData()
    },
    handleClear() {
      this.fetchData()
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/resource.scss";
</style>
