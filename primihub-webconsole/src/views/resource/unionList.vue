<template>
  <div class="container">
    <div class="search-area">
      <el-form :model="query" label-width="100px" :inline="true" @keyup.enter.native="search">
        <el-form-item label="机构">
          <el-select v-model="query.organId" size="small" placeholder="请选择" clearable @change="handleOrganChange" @clear="handleClear">
            <el-option
              v-for="item in organList"
              :key="item.globalId"
              :label="item.globalName"
              :value="item.globalId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资源ID">
          <el-input v-model="query.resourceId" size="small" placeholder="请输入资源ID" clearable @clear="handleClear" />
        </el-form-item>
        <el-form-item label="资源名称">
          <el-input v-model="query.resourceName" size="small" placeholder="请输入资源名称" clearable @clear="handleClear" />
        </el-form-item>
        <el-form-item label="标签">
          <TagsSelect :data="tags" :reset="isReset" size="small" :remote="false" @filter="searchResource" @change="handleTagChange" />
        </el-form-item>
        <el-form-item label="资源类型">
          <el-select v-model="query.resourceSource" size="small" placeholder="请选择" clearable @clear="handleClear">
            <el-option
              v-for="item in resourceSourceList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Y值">
          <el-select v-model="query.fileContainsY" size="small" placeholder="请选择" clearable>
            <el-option
              v-for="item in YValueOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" class="search-button" size="small" @click="search">查询</el-button>
          <el-button icon="el-icon-refresh-right" size="small" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="resource">
      <el-table
        v-loading="loading"
        :data="resourceList"
        border
      >
        <el-table-column
          prop="organId"
          label="机构ID"
          min-width="120"
        />
        <el-table-column
          prop="organName"
          label="机构名称"
        />
        <el-table-column
          label="资源ID"
          min-width="160"
        >
          <template slot-scope="{row}">
            <template v-if="hasViewPermission">
              <el-link type="primary" @click="toResourceDetailPage(row.resourceId)">{{ row.resourceId }}</el-link><br>
            </template>
            <template v-else>
              {{ row.resourceId }}<br>
            </template>
          </template>
        </el-table-column>
        <el-table-column
          prop="resourceName"
          label="资源名称"
        />
        <el-table-column
          prop="tags"
          label="标签"
        >
          <template slot-scope="{row}">
            <el-tag v-for="(tag,index) in row.resourceTag" :key="index" type="success" size="mini" class="tag">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="数据信息"
          min-width="200"
        >
          <template slot-scope="{row}">
            特征量：{{ row.resourceColumnCount }}<br>
            样本量：{{ row.resourceRowsCount }} <br>
            正例样本数量：{{ row.resourceYRowsCount || 0 }}<br>
            正例样本比例：{{ row.resourceYRatio || 0 }}%<br>
            <el-tag v-if="row.resourceContainsY" class="containsy-tag" type="primary" size="mini">包含Y值</el-tag>
            <el-tag v-else class="containsy-tag" type="danger" size="mini">不包含Y值</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="resourceSource"
          label="资源类型"
          align="center"
        >
          <template slot-scope="{row}">
            {{ row.resourceType | sourceFilter }}
          </template>
        </el-table-column>
        <el-table-column
          prop="resourceAuthType"
          label="可见性"
          align="center"
        >
          <template slot-scope="{row}">
            {{ row.resourceAuthType | authTypeFilter }}
          </template>
        </el-table-column>
        <el-table-column
          label="上传者"
          min-width="160"
        >
          <template slot-scope="{row}">
            {{ row.userName }} <br>
            {{ row.createDate }}
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    </div>

  </div>
</template>

<script>
import { getResourceList, getResourceTagList } from '@/api/fusionResource'
import { getAvailableOrganList } from '@/api/center'
import Pagination from '@/components/Pagination'
import TagsSelect from '@/components/TagsSelect'

export default {
  components: { Pagination, TagsSelect },
  data() {
    return {
      loading: false,
      tags: [],
      cascaderOptions: [],
      sysLocalOrganInfo: null,
      fusionList: [],
      groupList: [],
      organList: [],
      query: {
        resourceId: '',
        resourceName: '',
        tagName: '',
        userName: '',
        resourceSource: '',
        groupId: 0,
        organId: '',
        resourceAuthType: '',
        fileContainsY: ''
      },
      resourceSourceList: [{
        label: '文件上传',
        value: 1
      }, {
        label: '数据库导入',
        value: 2
      }],
      YValueOptions: [{
        label: '包含',
        value: 1
      }, {
        label: '不包含',
        value: 0
      }],
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
      isReset: false
    }
  },
  computed: {
    hasViewPermission() {
      return this.$store.getters.buttonPermissionList.includes('UnionResourceDetail')
    }
  },
  async created() {
    await this.getAvailableOrganList()
    await this.getResourceTagList()
    await this.fetchData()
  },
  methods: {
    handleClear() {
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
    async search() {
      this.pageNo = 1
      await this.fetchData()
    },
    async getResourceTagList() {
      const { result } = await getResourceTagList()
      this.tags = result && result.map((item, index) => {
        return {
          value: index,
          label: item
        }
      })
    },
    handleTagChange(tagName) {
      this.query.tagName = tagName
      this.query.selectTag = 0
      this.fetchData()
    },
    searchResource(tagName) {
      this.pageNo = 1
      if (tagName !== '') {
        this.query.tagName = tagName
        this.query.selectTag = 1
        this.fetchData()
      }
    },
    handleChange(value) {
      console.log(value, this.$refs.connectRef.getCheckedNodes())
    },
    async fetchData() {
      this.loading = true
      this.resourceList = []
      const { resourceId, resourceName, tagName, organId, resourceSource, fileContainsY } = this.query
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        resourceId,
        resourceName,
        tagName,
        resourceSource,
        organId,
        fileContainsY
      }
      const { code, result } = await getResourceList(params)
      if (code === -1) {
        this.$message({
          message: '资源同步中',
          type: 'warning'
        })
      }
      if (code === 0) {
        const { data, total, totalPage } = result
        this.total = total
        this.pageCount = totalPage
        if (data.length > 0) {
          this.resourceList = data
        }
      }
      this.loading = false
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.fetchData()
    },
    toResourceDetailPage(id) {
      this.$router.push({
        name: 'UnionResourceDetail',
        params: {
          id
        }
      })
    },
    async getAvailableOrganList() {
      const { result } = await getAvailableOrganList()
      this.organList = result
    },

    async handleOrganChange(value) {
      this.query.organId = value
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/resource.scss";
</style>
