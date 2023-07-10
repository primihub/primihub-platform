<template>
  <el-dialog
    :before-close="closeDialog"
    v-bind="$attrs"
  >
    <div class="dialog-body">
      <!-- <search-input class="input-with-search" @click="searchResource" @change="handleSearchNameChange" /> -->
      <ResourceTableSingleSelect max-height="530" :data="tableData" :show-status="showStatus" :selected-data="selectedResourceId" @change="handleChange" />
    </div>

    <span slot="footer" class="dialog-footer">
      <pagination v-show="paginationOptions.pageCount>1" small :limit.sync="paginationOptions.pageSize" :page.sync="paginationOptions.pageNo" :total="paginationOptions.total" layout="total, prev, pager, next" @pagination="handlePagination" />
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
// import SearchInput from '@/components/SearchInput'
export default {
  name: 'ResourceDialog',
  components: {
    ResourceTableSingleSelect,
    Pagination
    // SearchInput
  },
  props: {
    tableData: {
      type: Array,
      default: () => []
    },
    selectedData: {
      type: String,
      default: ''
    },
    showStatus: {
      type: Boolean,
      default: true
    },
    paginationOptions: {
      type: Object,
      default: () => {
        return {
          pageCount: 1,
          pageSize: 50,
          total: 1
        }
      }
    }
  },
  data() {
    return {
      resourceList: [],
      resourceName: '',
      listLoading: false,
      pageCount: 1,
      total: 0,
      pageSize: 5,
      pageNo: 1,
      selectedResource: null
    }
  },
  computed: {
    selectedResourceId: {
      get() {
        return this.selectedData
      },
      set() {}
    }
  },
  watch: {
    selectedData(newVal) {
      if (newVal) {
        this.selectedResourceId = newVal
      }
    }
  },
  methods: {
    handleChange(data) {
      this.selectedResourceId = data.resourceId
      this.selectedResource = data
    },
    searchResource() {
      this.$emit('request')
      // this.fetchData()
    },
    handleSearchNameChange(searchName) {
      this.resourceName = searchName
    },
    closeDialog() {
      this.$emit('close')
    },
    handleSubmit() {
      this.resourceName = ''
      if (!this.selectedResource) {
        this.$message({
          message: '请选择资源',
          type: 'warning'
        })
        return
      } else {
        this.$emit('submit', this.selectedResource)
      }
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.$emit('pagination', this.pageNo)
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
.dialog-body{
  min-height: 200px;
}
.pagination-container {
  padding: 10px 0 0 0;
  display: flex;
  justify-content: flex-start;
}
.dialog-footer{
  display: flex;
  justify-content: space-around;
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
  padding: 0px 0 0 0;
  display: flex;
  justify-content: center;
}
</style>
