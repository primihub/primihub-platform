<template>
  <el-dialog
    :before-close="closeDialog"
    v-bind="$attrs"
  >
    <el-tabs v-model="activeName">
      <el-tab-pane label="原始数据资源" name="0">
        <ResourceTableSingleSelect max-height="530" :data="tableData" :show-status="showStatus" :selected-data="selectedResourceId" @change="handleChange" />
      </el-tab-pane>
      <el-tab-pane v-if="showTab" label="衍生数据资源" name="1">
        <DerivedResourceTableSingleSelect v-if="activeName === '1'" max-height="480" :selected-data="selectedResourceId" @change="handleChange" />
      </el-tab-pane>
    </el-tabs>
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
import DerivedResourceTableSingleSelect from '@/components/DerivedResourceTableSingleSelect'

export default {
  name: 'ResourceDialog',
  components: {
    ResourceTableSingleSelect,
    DerivedResourceTableSingleSelect,
    Pagination
    // SearchInput
  },
  props: {
    tableData: {
      type: Array,
      default: () => []
    },
    selectedData: {
      type: String || Number,
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
    },
    showTab: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      resourceList: [],
      selectedResource: {},
      resourceName: '',
      listLoading: false,
      selectedResourceId: this.selectedData,
      pageCount: 1,
      total: 0,
      pageSize: 5,
      pageNo: 1,
      activeName: '0'
    }
  },
  watch: {
    selectedData(newVal) {
      if (newVal) {
        this.selectedResourceId = newVal
      } else {
        this.selectedResourceId = ''
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
      this.selectedResource = null
      this.selectedResourceId = ''
      this.resourceName = ''
      this.activeName = '0'
      this.$emit('close')
    },
    handleSubmit() {
      this.resourceName = ''
      if (!this.selectedResourceId) {
        this.$message({
          message: '请选择资源',
          type: 'warning'
        })
        return
      } else {
        this.selectedResource.derivation = this.activeName
        this.$emit('submit', this.selectedResource)
      }
      this.activeName = '0'
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
