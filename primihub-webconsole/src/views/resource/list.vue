<template>
  <div class="container">
    <div class="search-area">
      <el-button v-if="hasUploadAuth" type="primary" class="upload-button" @click="toResourceCreatePage"> <i class="el-icon-upload" /> 添加资源</el-button>
      <el-form :model="query" label-width="100px" :inline="true" @keyup.enter.native="search">
        <el-form-item label="数据资源ID">
          <el-input v-model="query.resourceId" placeholder="请输入资源ID" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="query.resourceName" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="关键词">
          <TagsSelect :data="tags" :remote="false" @filter="searchResource" @change="handleTagChange" />
        </el-form-item>
        <el-form-item label="上传者">
          <el-input v-model="query.userName" placeholder="请输入上传者名称" />
        </el-form-item>
        <el-form-item label="资源类型">
          <el-select v-model="query.resourceSource" placeholder="请选择" clearable>
            <el-option
              v-for="item in resourceSourceList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" class="search-button" @click="search">查询</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="resource">
      <el-table
        :data="resourceList"
        border
      >
        <el-table-column
          prop="resourceId"
          label="资源Id"
        />
        <el-table-column
          prop="resourceName"
          label="名称"
        >
          <template slot-scope="{row}">
            <el-link type="primary" @click="toResourceDetailPage(row.resourceId)">{{ row.resourceName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column
          prop="tags"
          label="关键词"
        >
          <template slot-scope="{row}">
            <el-tag v-for="tag in row.tags" :key="tag.tagId" type="success" size="mini" class="tag">{{ tag.tagName }}</el-tag>
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
          prop="resourceSource"
          label="资源类型"
          align="center"
        >
          <template slot-scope="{row}">
            {{ row.resourceSource | sourceFilter }}
          </template>
        </el-table-column>
        <el-table-column
          label="数据信息"
          min-width="200"
        >
          <template slot-scope="{row}">
            特征量：{{ row.fileRows }}<br>
            样本量：{{ row.fileColumns }} <br>
            正例样本数量：{{ row.fileYRows }}<br>
            正例样本比例：{{ row.fileYRatio }}% <br>
            <el-tag v-if="row.fileContainsY" type="primary">包含Y值</el-tag>
            <el-tag v-else type="danger">不包含Y值</el-tag>
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
          label="操作"
          fixed="right"
          width="220"
          align="center"
        >
          <template slot-scope="{row}">
            <el-button v-if="hasEditPermission" icon="el-icon-edit" size="mini" type="primary" @click="toResourceEditPage(row.resourceId)">编辑</el-button>
            <el-button v-if="hasDeletePermission" size="mini" icon="el-icon-delete" type="danger" @click="handleResourceDelete(row.resourceId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="pageCount>1" :limit.sync="pageSize" :page.sync="pageNo" :total="total" layout="total, prev, pager, next, jumper" @pagination="handlePagination" />

  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getResourceList, getResourceTags, deleteResource } from '@/api/resource'
import Pagination from '@/components/Pagination'
import TagsSelect from '@/components/TagsSelect'

export default {
  components: { Pagination, TagsSelect },
  data() {
    return {
      query: {
        resourceId: '', resourceName: '', tag: null, userName: '', resourceSource: '', selectTag: 0
      },
      userList: [],
      tags: [],
      resourceSourceList: [{
        label: '文件上传',
        value: 1
      }],
      resourceList: [],
      currentPage: 1,
      total: 0,
      pageCount: 0,
      pageNo: 1,
      pageSize: 10,
      resourceName: ''
    }
  },
  computed: {
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
    this.fetchData()
    await this.getResourceTags()
  },
  methods: {
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
    fetchData() {
      this.resourceList = []
      const { resourceId, resourceName, tag, userName, resourceSource, selectTag } = this.query
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        resourceName,
        resourceId,
        tag,
        userName,
        resourceSource,
        selectTag
      }
      getResourceList(params).then((res) => {
        if (res.code === 0) {
          const { data, total, totalPage } = res.result
          this.total = total
          this.pageCount = totalPage
          if (data.length > 0) {
            this.resourceList = data
          }
        }
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
@import "~@/styles/resource.scss";
</style>
