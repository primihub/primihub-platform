<template>
  <div class="container">
    <el-form :inline="true" :model="searchForm" class="search-area">
      <el-form-item label="参与机构">
        <el-select v-model="searchForm.organId" size="small" placeholder="请选择" clearable>
          <el-option
            v-for="organ in organList"
            :key="organ.globalId"
            :label="organ.globalName"
            :value="organ.globalId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="参与角色">
        <el-select v-model="searchForm.queryType" size="small" placeholder="请选择" clearable>
          <el-option label="发起方" value="1" />
          <el-option label="协作方" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="项目ID">
        <el-input v-model="searchForm.projectId" size="small" placeholder="请输入" />
      </el-form-item>
      <el-form-item label="项目名称">
        <el-input v-model="searchForm.projectName" size="small" placeholder="请输入" />
      </el-form-item>
      <el-form-item label="项目状态">
        <el-select v-model="searchForm.status" size="small" placeholder="请选择" clearable>
          <el-option
            v-for="status in projectStatusList"
            :key="status.value"
            :label="status.label"
            :value="status.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="searchForm.createDate"
          size="small"
          type="daterange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="yyyy-MM-dd HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <div class="buttons">
          <el-button class="button" type="primary" size="small" icon="el-icon-search" @click="searchProject">查询</el-button>
          <el-button class="button" size="small" icon="el-icon-refresh-right" plain @click="reset">重置</el-button>
        </div>
      </el-form-item>
    </el-form>
    <el-button v-if="hasCreateAuth" class="add-button" icon="el-icon-plus" type="primary" @click="toProjectCreatePage">新建项目</el-button>

    <div class="main">
      <div class="tab-container">
        <el-menu :default-active="activeIndex" class="select-menu" mode="horizontal" active-text-color="#1677FF" @select="handleSelect">
          <el-menu-item index=""><h2>全部项目<span>（{{ totalNum }}）</span></h2></el-menu-item>
          <el-menu-item index="1"><h2>我发起的<span>（{{ own }}）</span></h2></el-menu-item>
          <el-menu-item index="2"><h2>我协作的<span>（{{ other }}）</span></h2></el-menu-item>
        </el-menu>
        <el-button type="text" class="type" @click="toggleType"><i :class="{'el-icon-set-up':projectType === 'card','el-icon-menu':projectType === 'table' }" /></el-button>
      </div>

      <div v-loading="listLoading" class="project-list">
        <!-- <div v-if="hasCreateAuth" class="add-card" @click="toProjectCreatePage">
        <div class="icon-wrap"><i class="el-icon-document-add" /></div>
        <div class="text">添加项目</div>
      </div> -->
        <template v-if="projectType=== 'card'">
          <template v-if="noData">
            <no-data />
          </template>
          <template v-else>
            <el-row :gutter="18">
              <el-col v-for="item in projectList" :key="item.projectId" :xs="12" :sm="8" :md="8" :lg="6" :xl="4">
                <project-item :project="item" />
              </el-col>
            </el-row>

          </template>
        </template>
        <template v-else>
          <el-table
            :data="projectList"
            border
            highlight-current-row
            :row-class-name="tableRowDisabled"
          >
            <!-- <el-table-column
              type="index"
              width="40"
              align="center"
            /> -->
            <el-table-column
              label="项目ID"
              min-width="150"
            >
              <template slot-scope="{row}">
                <template v-if="hasViewPermission && row.status !== 2">
                  <el-link type="primary" @click="toProjectDetail(row.id)">{{ row.projectId }}</el-link> <br>
                </template>
                <template v-else>
                  {{ row.projectId }}<br>
                </template>
              </template>
            </el-table-column>
            <el-table-column
              prop="projectName"
              label="项目名称"
              min-width="120"
            />

            <el-table-column
              label="参与机构"
              min-width="180"
            >
              <template slot-scope="{row}">
                <span>发起方: {{ row.createdOrganName }}</span><br>
                <span>协作方: {{ row.providerOrganNames }}</span>
              </template>
            </el-table-column>
            <el-table-column
              label="任务状态"
            >
              <template slot-scope="{row}">
                运行中：{{ row.taskRunNum }} <br>
                成功：{{ row.taskSuccessNum || 0 }}<br>
                失败：{{ row.taskFailNum }}<br>
              </template>
            </el-table-column>
            <el-table-column
              prop="resourceNum"
              label="资源数量"
              align="center"
            />
            <el-table-column
              prop="taskNum"
              label="任务数量"
              align="center"
            />
            <el-table-column
              label="创建时间"
              prop="createDate"
              min-width="160"
            />
            <el-table-column
              label="最后更新时间"
              prop="updateDate"
              min-width="160"
            />
            <el-table-column
              label="项目状态"
              prop="status"
              width="80"
            >
              <template slot-scope="{row}">
                <span :class="statusStyle(row.status)">{{ row.status | projectAuditStatusFilter }}</span>
              </template>
            </el-table-column>
            <el-table-column
              v-if="hasViewPermission"
              label="操作"
              fixed="right"
              min-width="160"
            >
              <template slot-scope="{row}">
                <div class="buttons">
                  <el-link v-if="hasViewPermission" :disabled="row.status === 2" type="primary" @click="toProjectDetail(row.id)">查看</el-link>
                  <el-link v-if="hasDeletePermission && row.status === 1 && row.organId === userOrganId" type="danger" @click="projectActionHandler(row.id, 'close')">禁用</el-link>
                  <el-link v-if="hasOpenPermission && row.status === 2 && row.organId === userOrganId" type="primary" @click="projectActionHandler(row.id, 'open')">启用</el-link>
                </div>

              </template>
            </el-table-column>
          </el-table>
        </template>
        <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getProjectList, getListStatistics, closeProject, openProject } from '@/api/project'
import ProjectItem from '@/components/ProjectItem'
import NoData from '@/components/NoData'
import Pagination from '@/components/Pagination'
import { getAvailableOrganList } from '@/api/center'

export default {
  components: { ProjectItem, NoData, Pagination },
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
      projectId: 0,
      projectType: 'table',
      projectList: null,
      sysLocalOrganInfo: [],
      fusionList: [],
      projectStatusList: [{ // 项目状态 0审核中 1可用 2关闭 11 全部可用 12 部分可用
        value: '0',
        label: '审核中'
      }, {
        value: '1',
        label: '可用'
      }, {
        value: '2',
        label: '关闭'
      }, {
        value: '11',
        label: '全部可用'
      }, {
        value: '12',
        label: '部分可用'
      }],
      organList: [],
      activeIndex: '0',
      listLoading: false,
      searchForm: {
        projectId: '',
        projectName: '',
        organId: '',
        status: '',
        createDate: [],
        queryType: '',
        startDate: '',
        endDate: ''
      },
      queryType: '',
      pageNo: 1,
      pageSize: 10,
      total: 0,
      totalNum: 0,
      other: 0,
      own: 0,
      pageCount: 0,
      noData: false,
      currentPage: 0,
      hidePagination: true
    }
  },
  computed: {
    hasViewPermission() {
      return this.$store.getters.buttonPermissionList.includes('ProjectDetail')
    },
    hasDeletePermission() {
      return this.$store.getters.buttonPermissionList.includes('ProjectDelete')
    },
    hasOpenPermission() {
      return this.$store.getters.buttonPermissionList.includes('openProject')
    },
    hasCreateAuth() {
      return this.buttonPermissionList.includes('ProjectCreate')
    },
    ...mapGetters([
      'buttonPermissionList',
      'userOrganId'
    ])
  },
  async created() {
    this.projectType = localStorage.getItem('projectType') || this.projectType
    this.pageSize = this.projectType === 'table' ? 10 : 12
    this.fetchData()
    this.getListStatistics()
    this.getAvailableOrganList()
  },
  methods: {
    handleProjectCancel() {
      this.dialogVisible = false
    },
    handleProjectAction() {
      if (this.projectAction === 'close') {
        this.closeProject()
      } else if (this.projectAction === 'open') {
        this.openProject()
      }
      this.dialogVisible = false
    },
    tableRowDisabled({ row }) {
      if (row.status === 2) {
        return 'table-row-disabled'
      } else {
        return ''
      }
    },
    projectActionHandler(id, action) {
      this.projectAction = action
      this.dialogVisible = true
      this.projectId = id
      const text = action === 'close' ? '禁用后，数据、任务、模型将均不可用，进行中的任务立即停止，确认禁用么？' : '开启后，项目可正常发起任务，确认开启么？'
      this.$confirm(text, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (action === 'close') {
          this.closeProject()
        } else {
          this.openProject()
        }
        this.dialogVisible = false
      }).catch(() => {
        this.dialogVisible = false
      })
    },
    closeProject() {
      closeProject({ id: this.projectId }).then(res => {
        if (res.code === 0) {
          this.$message({
            message: '禁用成功',
            type: 'success'
          })
          this.fetchData()
        }
      })
    },
    openProject() {
      openProject({ id: this.projectId }).then(res => {
        if (res.code === 0) {
          this.$message({
            message: '启动成功',
            type: 'success'
          })
          this.fetchData()
        }
      })
    },
    toggleType() {
      if (this.projectType === 'table') {
        this.projectType = 'card'
        this.pageSize = 12
      } else {
        this.pageSize = 10
        this.projectType = 'table'
      }
      this.fetchData()
      localStorage.setItem('projectType', this.projectType)
    },
    toProjectDetail(id) {
      this.$router.push({
        name: 'ProjectDetail',
        params: { id }
      })
    },
    statusStyle(status) {
      return status === 0 ? 'status-0 el-icon-refresh' : status === 1 || status === 11 || status === 12 ? 'status-1 el-icon-circle-check' : status === 2 ? 'status-2 el-icon-circle-close' : 'status-0 el-icon-refresh'
    },
    getListStatistics() {
      getListStatistics().then(({ result }) => {
        this.totalNum = result?.total
        this.other = result?.other
        this.own = result?.own
      })
    },
    toProjectCreatePage() {
      this.$router.push({
        name: 'ProjectCreate'
      })
    },
    handleOrganSelect() {
      this.getAvailableOrganList()
    },
    getAvailableOrganList() {
      getAvailableOrganList().then(res => {
        this.organList = res.result
      })
    },
    searchProject() {
      this.pageNo = 1
      this.queryType = this.searchForm.queryType
      this.activeIndex = this.queryType !== '' ? this.queryType : '0'
      this.fetchData()
    },
    reset() {
      this.searchForm.projectId = ''
      this.searchForm.projectName = ''
      this.searchForm.organId = ''
      this.searchForm.queryType = ''
      this.searchForm.status = ''
      this.searchForm.createDate = []
      this.searchForm.startDate = ''
      this.searchForm.endDate = ''
      this.pageNo = 1
      this.fetchData()
    },
    fetchData() {
      this.listLoading = true
      this.projectList = []
      const { projectName, organId, status, createDate, projectId, queryType } = this.searchForm
      const params = {
        projectId,
        projectName,
        organId,
        queryType,
        status,
        startDate: createDate && createDate[0],
        endDate: createDate && createDate[1],
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }
      console.log('params', params)
      getProjectList(params).then((res) => {
        if (res.code === 0) {
          const { data, totalPage } = res.result
          this.total = res.result.total || 0
          this.pageCount = totalPage
          if (data.length > 0) {
            this.projectList = data
            this.noData = false
          } else {
            this.noData = true
          }
          this.listLoading = false
        }
      }).catch(err => {
        console.log(err)
        this.listLoading = false
      })
    },
    handleSelect(key) {
      console.log(key)
      this.queryType = key
      this.searchForm.queryType = this.queryType
      this.pageNo = 1
      this.fetchData()
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.fetchData()
    }
  }
}
</script>
<style lang="scss" scoped>
@import "../../styles/variables.scss";
h2 {
  display: block;
  font-size: 16px;
  font-weight: normal;
  margin-block-start: 0.2em;
  margin-block-end: 0;
  margin-inline-start: 0px;
  margin-inline-end: 0px;
}

::v-deep .el-menu.el-menu--horizontal{
  border-bottom: none;
}

::v-deep .el-form--inline .el-form-item{
  margin: 10px 35px 10px 0;
}
.el-select{
  width: 180px!important;
}
.search-area {
  padding: 30px 20px 20px 20px;
  background-color: #fff;
  display: flex;
  flex-wrap: wrap;
}
.add-button {
  margin-right: auto;
  height: 38px;
  margin-bottom: 20px;
  margin-top: 20px;
}
.button {
  margin: 0 3px;
  height: 38px;
  // width: 100px;
  border-radius: 4.5px;
  justify-content: space-between;
}
.el-menu-item *{
  vertical-align: top;
}
.el-menu--horizontal>.el-menu-item.is-active{
  h2{
    font-weight: bold;
    span{
      color: $dangerColor;
    }
  }

}
.select-item {
  margin: 10px 20px 0 20px;
  font-size: initial;
  font-weight: bold;
}

.main{
  background-color: #fff;
}
.tab-container{
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  padding: 0 30px;
}
.buttons{
  // margin-left: auto;
  display: flex;
  align-items: center;
  .el-link{
    margin: 0 5px;
  }
  // justify-content: space-between;
  .type{
    font-size: 24px;
    line-height: 1;
    vertical-align: middle;
  }
}
.project-list {
  // margin-top: 20px;
  min-height: 200px;
  background-color: #fff;
  margin: 0 30px 0 30px;
  padding-top: 30px;
  border-top: 1px solid #e6e6e6;
}
.add-card {
  width: 285px;
  box-sizing: border-box;
  border-radius: 10px;
  font-size: 14px;
  background: #fff;
  margin: 10px;
  color: rgba(0,0,0,0.85);
  cursor: pointer;
  display: flex;
  justify-content: center;
  flex-direction: column;
  border: 1px dashed $borderColor;
  &:hover {
    box-shadow: 2px 4px 8px rgba(0,0,0,.05);
  }
}
.icon-wrap {
  font-size: 45px;
  text-align: center;
  color: rgba(0,0,0,0.65);
}
.text {
  text-align: center;
}
::v-deep .el-badge__content.is-fixed{
  top: 10px;
  right: 0;
}
.pagination-container {
  padding-top: 50px;
  display: flex;
  justify-content: flex-end;
  // background-color: #fff;
}
.status-0{
  color: $mainColor;
}
.status-1{
  color: #67C23A;
}
.status-2{
  color: #F56C6C;
}
::v-deep .el-table tr.table-row-disabled{
  background-color: #f5f7fa;
  color: #909399;
}
</style>
