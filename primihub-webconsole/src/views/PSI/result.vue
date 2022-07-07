<template>
  <div class="container">
    <div class="organ-container">
      <div class="organ">
        <div class="header">
          <p class="organ-name"><i class="el-icon-office-building" />{{ ownOrganName }}</p>
          <search-input @click="handelSearchA" @change="handleSearchNameChangeA" />
        </div>
        <PSITaskResult v-if="allDataPsiTask.length>0" v-loading="listLoading" :data="allDataPsiTask" @delete="handleDelete" />
        <pagination v-show="totalPage>1" :limit.sync="pageSize" :page.sync="pageNo" :total="total" layout="total, prev, pager, next, jumper" @pagination="handlePagination" />
      </div>
    </div>
    <el-dialog
      :visible.sync="dialogVisible"
      width="70%"
    >
      <PSI-task-detail :data="taskData" />
    </el-dialog>
  </div>
</template>

<script>
import { getOrganPsiTask, getPsiTaskDetails, getPsiTaskList } from '@/api/PSI'
import PSITaskDetail from '@/components/PSITaskDetail'
import Pagination from '@/components/Pagination'
import SearchInput from '@/components/SearchInput'
import PSITaskResult from '@/components/PSITaskResult'

export default {
  name: 'PSIDirectory',
  components: {
    PSITaskDetail,
    Pagination,
    SearchInput,
    PSITaskResult
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
      allDataPsiTask: [],
      tableDataB: [],
      otherOrganId: '',
      otherOrganName: '',
      organList: [],
      pageSize: 10,
      totalPage: 0,
      total: 0,
      pageNo: 1,
      pageSize2: 10,
      totalPage2: 0,
      total2: 0,
      pageNo2: 1,
      dialogVisible: false,
      taskData: [],
      taskId: 0,
      resultNameA: '',
      resultNameB: ''
    }
  },
  beforeRouteLeave(to, from, next) {
    if (to.name === 'PSITask') {
      to.query.targetOrganId = this.otherOrganId
      to.query.organName = this.organName
    }
    next()
  },
  async created() {
    this.getUserInfo()
    this.listLoading = true
    this.getPsiTaskList()
  },
  methods: {
    handleDelete(data) {
      this.getPsiTaskList()
    },
    handleSingleResultDelete() {
      this.dataPsiTask = []
    },
    getPsiTaskList() {
      this.listLoading = true
      getPsiTaskList({
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }).then(res => {
        const { data, totalPage, total } = res.result
        this.allDataPsiTask = data
        this.totalPage = totalPage
        this.total = total
        this.listLoading = false
      })
    },
    getUserInfo() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo'))
      this.userInfo = userInfo
      this.ownOrganId = userInfo.organIdList
      this.ownOrganName = userInfo.organIdListDesc
      console.log('userInfo', this.userInfo)
    },
    openDialog(id) {
      this.taskId = id
      getPsiTaskDetails({ taskId: this.taskId }).then(res => {
        this.taskData = res.result
        this.dialogVisible = true
      })
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.getPsiTaskList()
    },
    async search(searchName, organId, pageSize) {
      console.log('searchName', searchName)
      const res = await getOrganPsiTask({
        organId: organId,
        pageSize: pageSize,
        resultName: searchName
      })
      return res
    },
    async handelSearchA(searchName) {
      this.listLoading = true
      this.pageNo = 1
      this.resultNameA = searchName
      const res = await this.search(searchName, this.ownOrganId, this.pageSize)
      this.listLoading = false
      const { data, totalPage, total } = res.result
      this.allDataPsiTask = data
      this.total = total
      this.totalPage = totalPage
    },
    async handelSearchB(searchName) {
      this.listLoading2 = true
      this.pageNo2 = 1
      this.resultNameB = searchName
      const res = await this.search(searchName, this.otherOrganId, this.pageSize2)
      this.listLoading2 = false
      const { data, totalPage, total } = res.result
      this.tableDataB = data
      this.total2 = total
      this.totalPage2 = totalPage
    },
    handleSearchNameChangeA(value) {
      this.resultNameA = value
    },
    handleSearchNameChangeB(value) {
      this.resultNameB = value
    }
  }

}
</script>
<style lang="scss" scoped>
@import "~@/styles/variables.scss";
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
  // display: flex;
  .organ{
    padding: 25px;
    margin-top: 20px;
    background-color: #fff;
    border-radius: 20px;
    // border: 1px solid #DCDFE6;
    .organ-name{
      color: $mainColor;
      font-weight: bold;
      font-size: 18px;
    }
  }
  .resource-name{
    color: #1989fa;
  }
}
::v-deep .el-descriptions__body{
  background-color: #fafafa;
  padding: 10px 20px 10px 20px;
}
::v-deep  .el-descriptions{
  margin-bottom:20px;
}
::v-deep .el-descriptions-item__container{
  align-items: center;
}
::v-deep .el-descriptions__header{
  margin-bottom: 10px;
}
::v-deep .el-dialog__body{
  padding: 20px 20px 10px 20px;
}
.pagination-container{
  padding-left:0;
  padding-right: 0;
}
.header{
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
</style>
