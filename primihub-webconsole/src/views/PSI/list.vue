<template>
  <div class="container">
    <organ-select @change="handleOrganSelect" />
    <div class="organ-container">
      <div class="organ">
        <div class="header">
          <p class="organ-name"><i class="el-icon-office-building" /> {{ ownOrganName }}</p>
          <search-input width="200px" size="small" @click="handelSearchA" @change="handleSearchNameChangeA" />
        </div>
        <el-table
          v-loading="listLoading"
          element-loading-spinner="el-icon-loading"
          :data="tableDataA"
          class="table-list"
          :default-sort="{prop: 'resourceName', order: 'descending'}"
        >
          <el-table-column label="表名" prop="resourceName" width="250" sortable>
            <template slot-scope="{row}">
              <span class="resource-name" type="text" icon="el-icon-view" @click="toResourceDetail(row.resourceId)">{{ row.resourceName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="fileHandleStatus" sortable>
            <template>
              <!-- <span v-if="row.fileHandleStatus === 2">
                可用
                <i class="state right el-icon-circle-check" />
              </span>
              <span v-else>
                不可用
                <i class="state error el-icon-circle-close" />
              </span> -->
              <span>可用 <i class="state right el-icon-circle-check" /></span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="列数" prop="fileColumns" sortable />
        </el-table>
        <pagination v-show="totalPage>1" :background="false" :limit.sync="pageSize" :page.sync="pageNo" :total="total" layout="total, prev, pager, next" @pagination="handlePagination" />
      </div>
      <div v-if="otherOrganId" class="organ">
        <div class="header">
          <p class="organ-name"><i class="el-icon-office-building" />{{ otherOrganName }}</p>
          <search-input width="200px" size="small" @click="handelSearchA" @change="handleSearchNameChangeA" />
        </div>
        <el-table
          v-loading="listLoading2"
          element-loading-spinner="el-icon-loading"
          :data="tableDataB"
          class="table-list"
          :default-sort="{prop: 'resourceName', order: 'descending'}"
        >
          <el-table-column label="表名" prop="resourceName" width="250" sortable>
            <template slot-scope="{row}">
              <span class="resource-name" type="text" icon="el-icon-view" @click="toResourceDetail(row.resourceId)">{{ row.resourceName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="fileHandleStatus" sortable>
            <template>
              <span>可用 <i class="state right el-icon-circle-check" /></span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="列数" prop="fileColumns" sortable />
        </el-table>
        <pagination v-show="totalPage2>1" :limit.sync="pageSize2" :page.sync="pageNo2" :total="total2" layout="total, prev, pager, next" @pagination="handlePagination2" />
      </div>

    </div>
    <div v-if="otherOrganId" class="button">
      <el-button type="primary" @click="next()">下一步</el-button>
    </div>
  </div>
</template>

<script>
import { getPsiResourceList } from '@/api/PSI'
import OrganSelect from '@/components/OrganSelect'
import Pagination from '@/components/Pagination'

export default {
  name: 'PSIDirectory',
  components: {
    OrganSelect,
    Pagination
  },
  data() {
    return {
      listLoading: false,
      listLoading2: false,
      loading: false,
      active: 0,
      userId: 0,
      ownOrganId: 0,
      ownOrganName: '原语科技',
      tableDataA: [],
      tableDataB: [],
      otherOrganId: '',
      otherOrganName: '机构B',
      organList: [],
      pageSize: 5,
      pageNo: 1,
      totalPage: '',
      total: 0,
      pageSize2: 5,
      pageNo2: 1,
      totalPage2: '',
      total2: 0,
      searchNameA: '',
      searchNameB: ''
    }
  },
  // if (to.name === 'PSITask' && !this.otherOrganId) {
  //   this.$message({
  //     message: '请选择隐私求交机构',
  //     type: 'warning'
  //   })
  // } else {
  //   to.query.targetOrganId = this.otherOrganId
  //   to.query.organName = this.organName
  //   next()
  // }
  // },
  async created() {
    this.getUserInfo()
    this.listLoading = true
    getPsiResourceList({
      organId: this.ownOrganId,
      pageSize: this.pageSize,
      pageNo: this.pageNo
    }).then(res => {
      this.listLoading = false
      const { data, totalPage, total } = res.result
      console.log('res', res)
      this.tableDataA = data
      this.total = total
      this.totalPage = totalPage
    })
  },
  methods: {
    getUserInfo() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo'))
      this.userInfo = userInfo
      this.ownOrganId = userInfo.organIdList
      this.ownOrganName = userInfo.organIdListDesc
      console.log('userInfo', this.userInfo)
    },
    handleOrganSelect(data) {
      this.otherOrganId = data.organId
      this.otherOrganName = data.organName
      this.listLoading2 = true
      getPsiResourceList({
        organId: this.otherOrganId,
        pageSize: this.pageSize2,
        pageNo: this.pageNo2
      }).then(res => {
        const { data, totalPage, total } = res.result
        this.tableDataB = data
        this.total2 = total
        this.totalPage2 = totalPage
        this.listLoading2 = false
      })
    },
    toResourceDetail(id) {
      this.$router.push({
        name: 'ResourceDetail',
        params: { id }
      })
    },
    next(id) {
      this.$router.push({
        name: 'PSITask',
        query: {
          organId: this.otherOrganId,
          organName: encodeURIComponent(this.otherOrganName)
        }
      })
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.listLoading2 = true
      getPsiResourceList({
        organId: this.ownOrganId,
        pageSize: this.pageSize,
        pageNo: this.pageNo,
        resourceName: this.searchNameA
      }).then(res => {
        this.listLoading2 = false
        const { data, totalPage, total } = res.result
        this.tableDataA = data
        this.total = total
        this.totalPage = totalPage
      })
    },
    handlePagination2(data) {
      this.pageNo2 = data.page
      this.listLoading2 = true
      getPsiResourceList({
        organId: this.otherOrganId,
        pageSize: this.pageSize2,
        pageNo: this.pageNo2,
        resourceName: this.searchNameB
      }).then(res => {
        this.listLoading2 = false
        const { data, totalPage, total } = res.result
        this.tableDataB = data
        this.total2 = total
        this.totalPage2 = totalPage
      })
    },
    async search(searchName, organId, pageSize) {
      console.log('searchName', searchName)
      const res = await getPsiResourceList({
        organId: organId,
        pageSize: pageSize,
        resourceName: searchName
      })
      return res
    },
    async handelSearchA(searchName) {
      this.listLoading = true
      this.pageNo = 1
      this.searchNameA = searchName
      const res = await this.search(searchName, this.ownOrganId, this.pageSize)
      this.listLoading = false
      const { data, totalPage, total } = res.result
      console.log('data', data)
      this.tableDataA = data
      this.total = total
      this.totalPage = totalPage
    },
    async handelSearchB(searchName) {
      this.listLoading2 = true
      this.pageNo2 = 1
      this.searchNameB = searchName
      const res = await this.search(searchName, this.otherOrganId, this.pageSize2)
      this.listLoading2 = false
      const { data, totalPage, total } = res.result
      this.tableDataB = data
      this.total2 = total
      this.totalPage2 = totalPage
    },
    handleSearchNameChangeA(value) {
      this.searchNameA = value
    },
    handleSearchNameChangeB(value) {
      this.searchNameB = value
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/variables.scss";
.header{
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  .organ-name{
    color: $mainColor;
  }
  .search{
    width: 260px;
  }
}
::v-deep .el-table th{
  background: #fafafa;
}
.el-table{
  cursor: pointer;
}
.app-container{
  min-width: 1000px
}
.organ-container{
  display: flex;
  .organ{
    background-color: #fff;
    width: 500px;
    padding: 20px;
    margin-top: 20px;
    border-radius: 20px;
    &:nth-child(1){
      margin-right: 20px;
    }
  }
  .resource-name:hover{
    color: #1989fa;
  }
}
.state{
  margin-left: 5px;
  font-weight: bold;
  &.right{
    color: #67C23A;
  }
  &.error{
    color: #F56C6C;
  }

}
.button{
  text-align: right;
  padding-right: 50px;
  margin: 20px auto;
}
</style>
