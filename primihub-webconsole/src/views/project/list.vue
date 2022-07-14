<template>
  <div class="container">
    <el-form :inline="true" :model="searchForm" class="search-area">
      <el-form-item label="项目名称">
        <el-input v-model="searchForm.projectName" />
      </el-form-item>
      <el-form-item label="中心节点">
        <el-select v-model="searchForm.serverAddress" clearable placeholder="请选择" @change="handleServerAddressChange">
          <el-option
            v-for="center in fusionList"
            :key="center.serverAddress"
            :label="center.serverAddress"
            :value="center.serverAddress"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="参与机构">
        <el-select v-model="searchForm.organId" placeholder="请选择" clearable @focus="handleOrganSelect">
          <el-option
            v-for="organ in organList"
            :key="organ.organId"
            :label="organ.organName"
            :value="organ.organId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="参与角色">
        <el-select v-model="searchForm.participationIdentity" placeholder="请选择" clearable>
          <el-option label="发起者" value="1" />
          <el-option label="协作者" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="项目状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable>
          <el-option label="审核中" value="0" />
          <el-option label="可用" value="1" />
          <el-option label="关闭" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="searchForm.createDate"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button class="search-button" type="primary" @click="searchProject">查询</el-button>
      </el-form-item>
    </el-form>
    <div class="tab-container">
      <el-menu :default-active="activeIndex" class="select-menu" mode="horizontal" active-text-color="#4596ff" @select="handleSelect">
        <!-- <el-menu-item index="0"><el-badge :value="totalNum" class="select-item">全部项目</el-badge></el-menu-item> -->
        <!-- <el-menu-item index="1"><el-badge :value="own" class="select-item">我发起的</el-badge></el-menu-item>
        <el-menu-item index="2"><el-badge :value="other" class="select-item">我协作的</el-badge></el-menu-item> -->
        <el-menu-item index="0"><h2>全部项目<span>（{{ totalNum }}）</span></h2></el-menu-item>
        <el-menu-item index="1"><h2>我发起的<span>（{{ own }}）</span></h2></el-menu-item>
        <el-menu-item index="2"><h2>我协作的<span>（{{ other }}）</span></h2></el-menu-item>
      </el-menu>
      <div class="buttons">
        <el-button v-if="hasCreateAuth" class="add-button" icon="el-icon-plus" type="primary" @click="toProjectCreatePage">新建项目</el-button>
        <el-button type="text" class="type" @click="toggleType"><i :class="{'el-icon-set-up':projectType === 'card','el-icon-menu':projectType === 'table' }" /></el-button>
      </div>

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
          <el-row :gutter="15">
            <el-col v-for="item in projectList" :key="item.projectId" :xs="12" :sm="8" :md="8" :lg="6" :xl="5">
              <project-item :project="item" />
            </el-col>
          </el-row>

        </template>
      </template>
      <template v-else>
        <el-table
          :data="projectList"
          border
        >
          <el-table-column
            type="index"
            width="40"
            align="center"
          />
          <el-table-column
            label="项目名称 / ID"
            min-width="200"
          >
            <template slot-scope="{row}">
              <template v-if="hasViewPermission">
                <el-link type="primary" @click="toProjectDetail(row.id)">{{ row.projectName }}</el-link> <br>
              </template>
              <template v-else>
                {{ row.projectName }}<br>
              </template>
              <span>{{ row.projectId }}</span>
            </template>
          </el-table-column>

          <el-table-column
            label="参与机构"
            min-width="150"
          >
            <template slot-scope="{row}">
              <span>发起方: {{ row.createdOrganName }}</span><br>
              <span>协作方: {{ row.providerOrganNames }}</span>
            </template>
          </el-table-column>
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
            label="模型状态"
          >
            <template slot-scope="{row}">
              配置中：{{ row.modelAssembleNum }}<br>
              运行中：{{ row.modelRunNum }} <br>
              成功：{{ row.modelSuccessNum || 0 }}<br>
            </template>
          </el-table-column>
          <el-table-column
            prop="resourceNum"
            label="资源数量"
            align="center"
          />
          <el-table-column
            prop="modelNum"
            label="模型数量"
            align="center"
          />
          <el-table-column
            label="创建时间"
            prop="createDate"
            min-width="160"
          />
          <el-table-column
            v-if="hasViewPermission"
            label="操作"
            min-width="160"
          >
            <template slot-scope="{row}">
              <el-button v-if="hasViewPermission" type="text" icon="el-icon-view" @click="toProjectDetail(row.id)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </div>
    <pagination v-show="pageCount>1" :limit.sync="params.pageSize" :page.sync="params.pageNo" :total="total" layout="total, prev, pager, next, jumper" @pagination="handlePagination" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getProjectList, getListStatistics } from '@/api/project'
import ProjectItem from '@/components/ProjectItem'
import NoData from '@/components/NoData'
import Pagination from '@/components/Pagination'
import { getLocalOrganInfo, findMyGroupOrgan } from '@/api/center'

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
      projectType: 'table',
      projectList: null,
      sysLocalOrganInfo: [],
      fusionList: [],
      organList: [],
      activeIndex: '0',
      listLoading: true,
      params: {
        pageNo: 1,
        pageSize: 10,
        projectName: '',
        serverAddress: '',
        queryType: 0,
        organId: '',
        participationIdentity: '',
        startDate: '',
        endDate: '',
        status: ''
      },
      searchForm: {
        projectName: '',
        serverAddress: '',
        organId: '',
        participationIdentity: '',
        status: '',
        createDate: []
      },
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
    hasCreateAuth() {
      return this.buttonPermissionList.includes('ProjectCreate')
    },
    ...mapGetters([
      'buttonPermissionList'
    ])
  },
  async created() {
    this.projectType = localStorage.getItem('projectType') || this.projectType
    this.params.pageSize = this.projectType === 'table' ? 10 : 12
    await this.getLocalOrganInfo()
    this.fetchData()
    this.getListStatistics()
  },
  methods: {
    toggleType() {
      if (this.projectType === 'table') {
        this.projectType = 'card'
        this.params.pageSize = 12
      } else {
        this.params.pageSize = 10
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
      return status === 0 ? 'status-0 el-icon-refresh' : status === 1 ? 'status-1 el-icon-circle-check' : status === 2 ? 'status-2 el-icon-circle-close' : 'status-0 el-icon-refresh'
    },
    getListStatistics() {
      getListStatistics().then(({ result }) => {
        this.totalNum = result?.total
        this.other = result?.other
        this.own = result?.own
      })
    },
    async getLocalOrganInfo() {
      const { result = {}} = await getLocalOrganInfo()
      this.sysLocalOrganInfo = result.sysLocalOrganInfo
      this.fusionList = this.sysLocalOrganInfo?.fusionList
      if (this.$store.getters.userOrganId === '') {
        console.log(this.$store.getters.permissionList)
        if (this.$store.getters.permissionList.filter(item => item.authCode === 'CenterManage').length > 0) {
          this.$message({
            message: '暂无机构信息，请前往中心管理页面创建机构',
            duration: 2000
          })
          this.$router.push({
            name: 'CenterManage'
          })
        } else {
          this.$message({
            message: '请联系管理员创建机构信息'
          })
        }
      }
      console.log(this.$store.getters.userOrganId)
    },
    toProjectCreatePage() {
      this.$router.push({
        name: 'ProjectCreate'
      })
    },
    handleServerAddressChange(value) {
      console.log(value)
    },
    handleOrganSelect() {
      this.findMyGroupOrgan()
    },
    findMyGroupOrgan() {
      if (this.searchForm.serverAddress === '') {
        this.$message({
          message: '请先选择中心节点',
          type: 'warning'
        })
        return
      }
      findMyGroupOrgan({ serverAddress: this.searchForm.serverAddress }).then(res => {
        this.organList = res.result.dataList.organList && res.result.dataList.organList.map((item) => {
          return {
            organName: item.globalName,
            organId: item.globalId
          }
        })
      })
    },
    searchProject() {
      this.params.projectName = this.searchForm.projectName
      this.params.serverAddress = this.searchForm.serverAddress
      this.params.organId = this.searchForm.organId
      this.params.participationIdentity = this.searchForm.participationIdentity
      this.params.status = this.searchForm.status
      this.params.startDate = this.searchForm.createDate && this.searchForm.createDate[0]
      this.params.endDate = this.searchForm.createDate && this.searchForm.createDate[1]
      this.fetchData()
      this.params.pageNo = 1
    },
    fetchData() {
      this.listLoading = true
      this.projectList = []
      getProjectList(this.params).then((res) => {
        if (res.code === 0) {
          this.listLoading = false
          const { data, totalPage } = res.result
          this.total = res.result.total || 0
          this.pageCount = totalPage
          if (data.length > 0) {
            this.projectList = data
            this.noData = false
          } else {
            this.noData = true
          }
        }
      })
    },
    handleSelect(key) {
      this.params.queryType = parseInt(key)
      this.params.projectName = ''
      this.params.organId = ''
      this.params.participationIdentity = ''
      this.params.status = ''
      this.params.startDate = ''
      this.params.endDate = ''
      this.params.pageNo = 1
      this.fetchData()
    },
    handlePagination(data) {
      this.params.pageNo = data.page
      this.fetchData()
    }
  }
}
</script>
<style lang="scss" scoped>
@import "../../styles/variables.scss";
@import "~@/styles/resource.scss";
h2 {
    display: block;
    font-size: 16px;
    font-weight: normal;
    margin-block-start: 0.2em;
    margin-block-end: 0;
    margin-inline-start: 0px;
    margin-inline-end: 0px;
}
.search-area {
  padding: 30px 30px 20px 20px;
  background-color: #fff;
  display: flex;
  flex-wrap: wrap;
}
.search-button {
  margin-left: auto;
  height: 38px;
  width: 100px;
  border-radius: 4.5px;
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
.tab-container{
  display: flex;
  align-items: center;
  background-color: #fff;
  padding: 0 30px;
}
.buttons{
  margin-left: auto;
  .type{
    font-size: 24px;
    line-height: 1;
    vertical-align: middle;
  }
}
.add-button {
  height: 38px;
  align-self: center;

}
.project-list {
  margin-top: 20px;
  border-radius: $sectionBorderRadius;
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
  justify-content: center;
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
</style>
