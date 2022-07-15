<template>
  <el-dialog
    :before-close="closeDialog"
    v-bind="$attrs"
  >
    <search-input class="input-with-search" @click="searchResource" @change="handleSearchNameChange" />
    <ResourceTableSingleSelect :selected-data="selectedData" row-key="resourceId" :data="tableData" @change="handleChange" />
    <span slot="footer" class="dialog-footer">
      <pagination v-show="pageCount>1" small :limit.sync="pageSize" :page.sync="pageNo" :total="total" layout="total, prev, pager, next" @pagination="handlePagination" />
      <div class="buttons">
        <el-button size="medium" @click="closeDialog">取 消</el-button>
        <el-button size="medium" type="primary" @click="handleSubmit">确 定</el-button>
      </div>
    </span>
  </el-dialog>
</template>

<script>
import ResourceTableSingleSelect from '@/components/ResourceTableSingleSelect'
import Pagination from '@/components/Pagination'
import SearchInput from '@/components/SearchInput'
export default {
  name: 'ResourceDialog',
  components: {
    ResourceTableSingleSelect,
    Pagination,
    SearchInput
  },
  props: {
    selectedData: {
      type: Array,
      default: () => []
    },
    tableData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      resourceList: [],
      total: 0,
      currentPage: 1,
      pageNo: 1,
      pageSize: 5,
      selectedResources: [],
      pageCount: 0,
      resourceName: '',
      listLoading: false
    }
  },
  watch: {
    visible: async function(val) {
      if (val) {
        await this.fetchData()
      }
    }
  },
  methods: {
    handleChange(data) {
      console.log('111', data)
      this.selectedResources = data
    },
    searchResource() {
      this.pageNo = 1
      this.fetchData()
    },
    handleSearchNameChange(searchName) {
      this.resourceName = searchName
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.fetchData()
    },
    closeDialog() {
      this.resourceName = ''
      this.$emit('close')
    },
    handleSubmit() {
      this.resourceName = ''
      this.$emit('submit', this.selectedResources)
    },
    handleSearch() {

    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-table,::v-deep .el-divider__text, .el-link{
  font-size: 12px!important;
}
::v-deep .el-table td.el-table__cell div{
  line-height: 1.5;
}
::v-deep .table.el-table .el-table__cell{
  padding: 5px 0;
}
.pagination-container {
  padding: 10px 0 0 0;
  display: flex;
  justify-content: flex-start;
}
.dialog-footer{
  display: flex;
  justify-content: flex-end;
  padding-bottom: 30px;
  align-items: center;
}
::v-deep .el-dialog__body{
  padding: 10px 20px 0 20px;
}
.input-with-search{
  width: 300px;
  margin: 0 0 10px 0px;
}
.pagination-container {
  padding: 10px 0 0 0;
  display: flex;
  justify-content: center;
}
</style>
