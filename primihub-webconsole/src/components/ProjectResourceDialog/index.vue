<template>
  <el-dialog
    v-model="dialogVisible"
    :visible.sync="visible"
    width="920px"
    top="5vh"
    center
    @close="handleClose"
  >
    <div class="resource-container">
      <search-input class="input-with-search" @click="searchResource" @change="handleSearchNameChange" />
      <div v-loading="listLoading" class="project-list">
        <NoData v-if="noData" />
        <template v-else>
          <ResourceItem v-for="resource in resourceList" :key="resource.resourceId" class="resource-item" :has-check-box="true" :resource="resource" :selected="selectedIds && selectedIds.indexOf(resource.resourceId) !== -1" @click="handleResourceClick" />
        </template>
      </div>
    </div>
    <pagination v-show="pageCount>1" :background="false" :limit.sync="params.pageSize" :page.sync="params.pageNo" :total="total" layout="total, prev, pager, next, jumper" @pagination="handlePagination" />
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="submit">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { getResourceList } from '@/api/resource'
import ResourceItem from '@/components/ResourceItem'
import NoData from '@/components/NoData'
import Pagination from '@/components/Pagination'
import SearchInput from '@/components/SearchInput'
export default {
  name: 'ResourceDialog',
  components: {
    ResourceItem,
    NoData,
    Pagination,
    SearchInput
  },
  props: {
    dialogVisible: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    searchName: {
      type: String,
      default: ''
    },
    selectIds: {
      type: Array,
      default: () => {
        return []
      }
    }
  },
  data() {
    return {
      listLoading: false,
      noData: false,
      resourceList: [],
      total: 0,
      currentPage: 1,
      params: {
        pageNo: 1,
        pageSize: 6,
        resourceName: '',
        resourceAuthType: 0
      },
      hidePagination: true,
      visible: false,
      selectedResources: [],
      pageCount: 0,
      resourceName: this.searchName,
      selectedIds: this.selectIds
    }
  },
  watch: {
    searchName(newVal) {
      if (newVal) {
        this.resourceName = this.searchName
      }
    },
    dialogVisible: function(val) {
      if (val) {
        this.visible = val
        if (this.searchName !== '') {
          this.params.resourceName = this.searchName
        }
        this.fetchData()
      } else {
        this.visible = false
      }
    }
  },
  methods: {
    fetchData() {
      this.listLoading = true
      this.noData = false
      getResourceList(this.params).then((res) => {
        if (res.code === 0) {
          this.listLoading = false
          const { data, total, totalPage } = res.result
          this.total = total
          this.pageCount = totalPage
          if (data.length > 0) {
            this.resourceList = data
          } else {
            this.noData = true
          }
        }
      })
    },
    searchResource() {
      this.params.pageNo = 1
      this.params.resourceName = this.resourceName
      this.fetchData()
    },
    handleSearchNameChange(searchName) {
      this.resourceName = searchName
    },
    handleResourceClick(resource) {
      const { resourceId } = resource
      const index = this.selectedIds.indexOf(resourceId)
      if (index === -1) {
        this.selectedIds.push(resourceId)
        this.selectedResources.push(resource)
      } else {
        this.selectedIds.splice(index, 1)
        const i = this.selectedResources.findIndex(item => item.resourceId === resource.resourceId)
        this.selectedResources.splice(i, 1)
      }
    },
    submit() {
      this.$emit('submit', {
        list: this.selectedResources,
        selectIds: this.selectedIds,
        visible: this.visible
      })
    },
    handleClose() {
      console.log('close')
      this.$emit('close')
    },
    handlePagination(data) {
      this.params.pageNo = data.page
      this.fetchData()
    }
  }
}
</script>

<style lang="scss" scoped>
.project-list{
  display: flex;
  flex-wrap: wrap;
  min-height: 80px;
}
.resource-item{
  margin: 10px;
}
.pagination-container {
  padding: 10px 0 0 0;
  display: flex;
  justify-content: center;
}
::v-deep .el-dialog--center .el-dialog__body{
  padding: 10px 20px 0 20px;
}
.input-with-search{
  width: 300px;
  margin: 0 0 10px 10px;
}
</style>
