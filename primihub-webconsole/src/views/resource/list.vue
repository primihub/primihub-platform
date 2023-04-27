<template>
  <div class="container">
    <div class="search-area">
      <el-button v-if="hasUploadAuth" type="primary" class="upload-button" @click="toResourceCreatePage"> <i class="el-icon-upload" /> 添加资源</el-button>
      <el-form :model="query" label-width="100px" :inline="true" @keyup.enter.native="search">
        <el-form-item label="资源ID">
          <el-input v-model.number="query.resourceId" size="small" placeholder="请输入资源ID" />
        </el-form-item>
        <el-form-item label="资源名称">
          <el-input v-model="query.resourceName" size="small" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="标签">
          <TagsSelect :data="tags" :reset="isReset" size="small" :remote="false" @filter="searchResource" @change="handleTagChange" />
        </el-form-item>
        <el-form-item label="资源类型">
          <el-select v-model="query.resourceSource" size="small" placeholder="请选择" clearable>
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
        <el-form-item label="上传者">
          <el-input v-model="query.userName" size="small" placeholder="请输入上传者名称" />
        </el-form-item>

        <el-form-item label="可见性">
          <el-select v-model="query.resourceAuthType" size="small" placeholder="请选择" clearable>
            <el-option
              v-for="item in resourceAuthTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" class="search-button" size="small" @click="search">查询</el-button>
          <el-button size="small" icon="el-icon-refresh-right" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="resource">
      <el-table
        :data="resourceList"
        :row-class-name="tableRowDisabled"
        empty-text="暂无数据"
        border
      >
        <el-table-column
          prop="resourceId"
          label="资源ID"
        >
          <template slot-scope="{row}">
            <template v-if="hasViewPermission && row.resourceState === 0">
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
            <el-tag v-for="tag in row.tags" :key="tag.tagId" type="success" size="mini" class="tag">{{ tag.tagName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="数据信息"
          min-width="200"
        >
          <template slot-scope="{row}">
            特征量：{{ row.fileColumns }}<br>
            样本量：{{ row.fileRows }} <br>
            正例样本数量：{{ row.fileYRows ? row.fileYRows : 0 }}<br>
            正例样本比例：{{ row.fileYRatio? row.fileYRatio : 0 }}% <br>
            <div class="margin-top-5">
              <el-tag v-if="row.fileContainsY" type="primary" size="mini">包含Y值</el-tag>
              <el-tag v-else type="danger" size="mini">不包含Y值</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="resourceSource"
          label="资源类型"
          align="center"
        >
          <template slot-scope="{row}">
            {{ row.resourceSource | sourceFilter }}
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
        <el-table-column
          prop="resourceHashCode"
          label="文件Hash"
          min-width="120"
        />
        <el-table-column
          label="操作"
          fixed="right"
          width="160"
          align="center"
        >
          <template slot-scope="{row}">
            <el-button type="text" @click="toResourceDetailPage(row.resourceId)">查看</el-button>
            <el-button v-if="hasEditPermission && row.resourceState === 0" type="text" @click="toResourceEditPage(row.resourceId)">编辑</el-button>
            <el-button type="text" @click="changeResourceStatus(row)">{{ row.resourceState === 0 ? '下线': '上线' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    </div>

  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getResourceList, getResourceTags, deleteResource, resourceStatusChange } from '@/api/resource'
import Pagination from '@/components/Pagination'
import TagsSelect from '@/components/TagsSelect'

export default {
  components: { Pagination, TagsSelect },
  data() {
    return {
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
      resourceAuthTypeOptions: [{
        label: '公开',
        value: 1
      }, {
        label: '私有',
        value: 2
      }, {
        label: '指定机构可见',
        value: 3
      }],
      YValueOptions: [{
        label: '包含',
        value: 1
      }, {
        label: '不包含',
        value: 0
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
    hasViewPermission() { // view resource permission
      return this.buttonPermissionList.indexOf('ResourceDetail') !== -1
    },
    hasUploadAuth() { // upload resource permission
      return this.buttonPermissionList.indexOf('ResourceUpload') !== -1
    },
    hasEditPermission() { // edit resource permission
      return this.buttonPermissionList.includes('ResourceEdit')
    },
    hasDeletePermission() { // delete resource permission
      return this.buttonPermissionList.includes('ResourceDelete')
    },
    ...mapGetters([
      'buttonPermissionList'
    ])
  },
  async created() {
    await this.fetchData()
    await this.getResourceTags()
  },
  methods: {
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
    handleDelete(id) {
      const index = this.resourceList.findIndex(item => item.resourceId === id)
      this.resourceList.splice(index, 1)
      this.fetchData()
    },
    toResourceEditPage(id) {
      this.$router.push({
        name: 'ResourceEdit',
        params: { id }
      })
    },
    toResourceDetailPage(id) {
      this.$router.push({
        name: 'ResourceDetail',
        params: { id }
      })
    },
    toResourceCreatePage(name) {
      this.$router.push({
        name: 'ResourceUpload'
      })
    },
    search() {
      this.pageNo = 1
      this.fetchData()
    },
    handleSearchNameChange(searchName) {
      this.searchName = searchName
    },
    async fetchData() {
      this.resourceList = []
      const { resourceName, tag, userName, resourceSource, selectTag, resourceAuthType, fileContainsY } = this.query
      const resourceId = Number(this.query.resourceId)
      if (resourceId !== '' && isNaN(resourceId)) {
        this.$message({
          message: '资源id为数字',
          type: 'warning'
        })
        return
      }
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        resourceName,
        resourceId,
        tag,
        userName,
        resourceSource,
        selectTag,
        derivation: 0,
        resourceAuthType,
        fileContainsY
      }
      const res = await getResourceList(params)
      if (res.code === 0) {
        const { data, total, totalPage } = res.result
        this.total = total
        this.pageCount = totalPage
        if (data.length > 0) {
          this.resourceList = data
        }
        this.isReset = false
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
</style>
