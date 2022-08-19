<template>
  <el-dialog
    :before-close="closeDialog"
    v-bind="$attrs"
  >
    <!-- <search-input class="input-with-search" @click="searchResource" @change="handleSearchNameChange" /> -->
    <ResourceTableSingleSelect max-height="530" :data="tableData" :selected-data="selectedData" @change="handleChange" />
    <span slot="footer" class="dialog-footer">
      <div class="buttons">
        <el-button size="medium" @click="closeDialog">取 消</el-button>
        <el-button size="medium" type="primary" @click="handleSubmit">确 定</el-button>
      </div>
    </span>
  </el-dialog>
</template>

<script>
import ResourceTableSingleSelect from '@/components/ResourceTableSingleSelect'
// import SearchInput from '@/components/SearchInput'
export default {
  name: 'ResourceDialog',
  components: {
    ResourceTableSingleSelect
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
    }
  },
  data() {
    return {
      resourceList: [],
      selectedResource: {},
      resourceName: '',
      listLoading: false,
      selectedResourceId: ''
    }
  },
  methods: {
    handleChange(data) {
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
      this.resourceName = ''
      this.$emit('close')
    },
    handleSubmit() {
      this.resourceName = ''
      this.$emit('submit', this.selectedResource)
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
