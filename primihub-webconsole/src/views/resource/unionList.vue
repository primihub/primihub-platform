<template>
  <div class="container">
    <div class="search-area">
      <el-form :model="query" label-width="100px" :inline="true" @keyup.enter.native="search">
        <el-form-item v-if="isOrganAdmin" label="机构">
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
      <el-tabs v-if="!isOrganAdmin" v-model="activeName" @tab-click="handleClick">
        <el-tab-pane label="机构内资源" name="0" />
        <el-tab-pane label="其他机构资源" name="1" />
      </el-tabs>
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
          align="center"
          label="资源所属机构"
        />
        <el-table-column
          prop="userName"
          label="被授权人"
        />
        <el-table-column
          label="资源ID"
          min-width="160"
        >
          <template slot-scope="{row}">
            <template>
              <el-link type="primary" @click="toResourceDetailPage(row.resourceId)">{{ row.resourceId }}</el-link><br>
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
            <template v-if="row.tags">
              <el-tag v-for="tag in row.tags" :key="tag.tagId" type="success" size="mini" class="tag">{{ tag.tagName }}</el-tag>
            </template>
            <template v-else>
              <el-tag v-for="(tag,index) in row.resourceTag" :key="index" type="success" size="mini" class="tag">{{ tag }}</el-tag>
            </template>
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
            {{ row.resourceSource ? row.resourceSource : row.resourceType | sourceFilter }}
          </template>
        </el-table-column>
        <!-- <el-table-column
          prop="resourceAuthType"
          label="可见性"
          align="center"
          min-width="100"
        >
          <template slot-scope="{row}">
            {{ row.resourceAuthType | authTypeFilter }}
          </template>
        </el-table-column> -->
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
          width="160"
          align="center"
        >
          <template slot-scope="{row}">
            <el-button type="text" @click="toResourceDetailPage(row.resourceId)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

    </div>
    <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    <!-- <authorizationUserDialog :visible.sync="dialogVisible" :resource-id="resourceId" @close="closeAuthDialog" @cancel="closeAuthDialog" @submit="handleAuthConfirm" /> -->
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getFusionDataResourceAssignedToMe, getResourceTagList } from '@/api/fusionResource'
import { getDataResourceAssignedToMe } from '@/api/resource'
import { getAvailableOrganList } from '@/api/center'
import Pagination from '@/components/Pagination'
import TagsSelect from '@/components/TagsSelect'
// import authorizationUserDialog from '@/components/authorizationUserDialog'

export default {
  components: { Pagination, TagsSelect },
  data() {
    return {
      params: {},
      activeName: '0',
      dialogVisible: false,
      resourceId: 0,
      userValue: [],
      loading: false,
      tags: [],
      organList: [],
      query: {
        resourceId: '',
        resourceName: '',
        tagName: '',
        userName: '',
        resourceSource: '',
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
      resourceList: [],
      total: 0,
      pageCount: 0,
      pageNo: 1,
      pageSize: 10,
      resourceName: '',
      resourceSortType: 0,
      resourceAuthType: 0,
      organId: 0,
      isReset: false
    }
  },
  computed: {
    ...mapGetters([
      'isOrganAdmin'
    ])
  },
  async created() {
    await this.getAvailableOrganList()
    await this.getResourceTagList()
    await this.firstFetchData()
    this.activeName = this.isOrganAdmin ? '1' : '0'
  },
  methods: {
    handleClick() {
      this.reset()
      console.log(this.activeName)
      if (this.activeName === '1') {
        this.getFusionDataResourceAssignedToMe()
      } else {
        this.getDataResourceAssignedToMe()
      }
    },
    closeAuthDialog() {
      this.dialogVisible = false
    },
    handleAuthConfirm(data) {
      this.userValue = data
      this.dialogVisible = false
    },
    addAuthorization({ resourceId }) {
      this.resourceId = resourceId
      this.dialogVisible = true
    },
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
      this.fetchData()
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
      this.params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        resourceId,
        resourceName,
        tagName,
        resourceSource,
        organId,
        fileContainsY,
        queryType: this.isOrganAdmin ? '1' : '0'
      }
      console.log(this.activeName)
      debugger
      if (this.activeName === '1') {
        this.getFusionDataResourceAssignedToMe()
      } else {
        this.getDataResourceAssignedToMe()
      }
    },
    async firstFetchData() {
      this.loading = true
      this.resourceList = []
      const { resourceId, resourceName, tagName, organId, resourceSource, fileContainsY } = this.query
      this.params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        resourceId,
        resourceName,
        tagName,
        resourceSource,
        organId,
        fileContainsY,
        queryType: this.isOrganAdmin ? '1' : '0'
      }
      if (this.isOrganAdmin) {
        this.getFusionDataResourceAssignedToMe()
      } else {
        this.getDataResourceAssignedToMe()
      }
    },
    getFusionDataResourceAssignedToMe() {
      console.log(this.params)
      getFusionDataResourceAssignedToMe(this.params).then(res => {
        const { code, result } = res
        if (code === 0) {
          const { data, total, totalPage } = result
          this.total = total
          this.pageCount = totalPage
          if (data.length > 0) {
            this.resourceList = data
          }
        }
        this.loading = false
      })
    },
    getDataResourceAssignedToMe() {
      console.log(this.params)
      getDataResourceAssignedToMe(this.params).then(res => {
        const { code, result } = res
        if (code === 0) {
          const { data, total, totalPage } = result
          this.total = total
          this.pageCount = totalPage
          if (data.length > 0) {
            this.resourceList = data
          }
        }
        this.loading = false
      })
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.params.pageNo = this.pageNo
      console.log(this.pageNo)
      console.log(this.activeName)
      if (this.activeName === '1') {
        this.getFusionDataResourceAssignedToMe()
      } else {
        this.getDataResourceAssignedToMe()
      }
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
