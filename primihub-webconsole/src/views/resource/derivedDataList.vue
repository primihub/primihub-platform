<template>
  <div class="container">
    <div class="search-area">
      <el-form :model="query" label-width="100px" :inline="true" @keyup.enter.native="search">
        <el-form-item label="数据ID">
          <el-input v-model="query.resourceId" size="small" placeholder="请输入资源ID" clearable @clear="handleClear" />
        </el-form-item>
        <el-form-item label="资源名称">
          <el-input v-model="query.resourceName" size="small" placeholder="请输入资源名称" clearable @clear="handleClear" />
        </el-form-item>
        <el-form-item label="任务ID">
          <el-input v-model="query.taskIdName" size="small" placeholder="请输入任务ID" clearable @clear="handleClear" />
        </el-form-item>
        <el-form-item label="衍生数据来源">
          <el-select v-model="query.tag" size="small" placeholder="请选择" clearable @clear="handleClear" @change="handleResourceSourceChange">
            <el-option
              v-for="item in resourceSourceList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="query.createDate"
            size="small"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd HH:mm:ss"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" class="search-button" @click="search">查询</el-button>
          <el-button class="button" type="primary" size="small" icon="el-icon-refresh-right" plain @click="reset">重置</el-button>
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
      groupId: 0,
      organId: 0,
      resourceSourceList: [{
        label: '数据对齐',
        value: 1
      }, {
        label: '缺失值处理',
        value: 2
      }, {
        label: '异常值处理',
        value: 3
      }]
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
      this.pageNo = 1
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
    },
    handleResourceSourceChange(val) {
      this.query.tag = this.resourceSourceList.filter(item => item.value === val)[0]?.label
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/resource.scss";
</style>
