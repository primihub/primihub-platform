<template>
  <el-dialog
    :visible.sync="visible"
    :before-close="closeDialog"
    v-bind="$attrs"
  >
    <search-input class="input-with-search" :search-name="resourceName" @click="searchResource" @change="handleSearchNameChange" />
    <ResourceTable
      v-if="resourceList"
      v-loading="listLoading"
      :show-delete-button="showDeleteButton"
      :show-preview-button="showPreviewButton"
      :show-status="false"
      :multiple="true"
      :organ-id="organId"
      :selected-data="selectedData"
      row-key="resourceId"
      :data="resourceList"
      min-height="300"
      @change="handleChange"
      @preview="handlePreview"
    />
    <span slot="footer" class="dialog-footer">
      <pagination v-show="pageCount>1" small :limit.sync="pageSize" :page.sync="pageNo" :page-count="pageCount" :total="total" layout="total, prev, pager, next" @pagination="handlePagination" />
      <div class="buttons">
        <el-button size="medium" @click="closeDialog">取 消</el-button>
        <el-button size="medium" type="primary" @click="handleSubmit">确 定</el-button>
      </div>
    </span>
  </el-dialog>
</template>

<script>
import { getResourceList } from '@/api/fusionResource'
import ResourceTable from '@/components/ResourceTable'
import Pagination from '@/components/Pagination'
import SearchInput from '@/components/SearchInput'
export default {
  name: 'ResourceDialog',
  components: {
    ResourceTable,
    Pagination,
    SearchInput
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    organId: {
      type: String,
      default: '',
      require: true
    },
    selectedData: {
      type: Array,
      default: () => []
    },
    showPreviewButton: {
      type: Boolean,
      default: true
    },
    showDeleteButton: {
      type: Boolean,
      default: false
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
        this.pageNo = 1
        await this.fetchData()
      }
    }
  },
  methods: {
    async fetchData() {
      this.listLoading = true
      this.resourceList = []
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        organId: this.organId,
        resourceName: this.resourceName
      }
      const { code, result } = await getResourceList(params)
      if (code === -1) {
        this.$message({
          message: '资源同步中',
          type: 'warning'
        })
      }
      if (code === 0) {
        const { data, total, totalPage } = result
        this.total = total
        this.pageCount = totalPage
        if (data.length > 0) {
          this.noData = false
          this.resourceList = data
        } else {
          this.noData = true
        }
      }
      this.listLoading = false
    },
    handleChange(data) {
      this.selectedResources = data
    },
    searchResource(name) {
      this.resourceName = name
      this.pageNo = 1
      this.fetchData()
    },
    handleSearchNameChange(searchName) {
      console.log(searchName)
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
    handlePreview(row) {
      console.log('handlePreview', row)
      this.$emit('preview', row)
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
  justify-content: space-between;
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
