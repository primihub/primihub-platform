<template>
  <div class="container">
    <div class="search-area">
      <div><el-button v-if="hasCreateAuth" icon="el-icon-plus" type="primary" @click="toProjectCreatePage">新建项目</el-button></div>
      <search-input class="input-with-search" @click="searchProject" @change="handleSearchNameChange" />
    </div>
    <div v-loading="listLoading" class="project-list">
      <template v-if="noData">
        <no-data />
      </template>
      <template v-else>
        <project-item v-for="item in projectList" :key="item.projectId" :project="item" />
      </template>
    </div>
    <pagination v-show="pageCount>1" :limit.sync="params.pageSize" :page.sync="params.pageNo" :total="total" layout="total, prev, pager, next, jumper" @pagination="handlePagination" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getProjectList } from '@/api/project'
import ProjectItem from '@/components/ProjectItem'
import NoData from '@/components/NoData'
import Pagination from '@/components/Pagination'
import SearchInput from '@/components/SearchInput'

export default {
  components: { ProjectItem, NoData, Pagination, SearchInput },
  filters: {
    statusFilter(status) {
      const statusMap = {
        published: 'success',
        draft: 'gray',
        deleted: 'danger'
      }
      return statusMap[status]
    }
  },
  data() {
    return {
      searchName: '',
      projectList: null,
      listLoading: true,
      params: {
        pageNo: 1,
        pageSize: 12,
        projectName: ''
      },
      total: 0,
      pageCount: 0,
      noData: false,
      currentPage: 0,
      hidePagination: true
    }
  },
  computed: {
    ...mapGetters([
      'buttonPermissionList'
    ]),
    hasCreateAuth() {
      return this.buttonPermissionList.includes('ProjectCreate')
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    toProjectCreatePage() {
      this.$router.push({
        name: 'ProjectCreate'
      })
    },
    searchProject() {
      this.params.projectName = this.searchName
      this.fetchData()
      this.params.pageNo = 1
    },
    handleSearchNameChange(searchName) {
      this.searchName = searchName
    },
    fetchData() {
      this.listLoading = true
      this.noData = false
      this.projectList = []
      getProjectList(this.params).then((res) => {
        if (res.code === 0) {
          this.listLoading = false
          const { data, total, totalPage } = res.result
          this.total = total
          this.pageCount = totalPage
          if (data.length > 0) {
            this.projectList = data
          } else {
            this.noData = true
          }
        }
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
@import "~@/styles/variables.scss";
.search-area {
  padding: 0 10px;
  border-radius: $sectionBorderRadius;
}
.input-with-search {
  width: 300px;
  margin-top: 10px;
}
.project-list {
  margin-top: 40px;
  display: flex;
  flex-wrap: wrap;
  border-radius: $sectionBorderRadius;
}
.pagination-container {
  padding-top: 50px;
  display: flex;
  justify-content: center;
  // background-color: #fff;
}
</style>
