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
      <div v-loading="listLoading" class="resource-list">
        <NoData v-if="noData" />
        <template v-else>
          <ResourceItem v-for="resource in resourceList" :key="resource.resourceId" class="resource-item" :has-check-box="true" :resource="resource" :selected="selectedIds && selectedIds.indexOf(resource.resourceId) !== -1" @click="handleResourceClick" />
        </template>
      </div>
    </div>
    <pagination v-show="pageCount>1" :background="false" :limit.sync="pageSize" :page.sync="pageNo" :total="total" layout="total, prev, pager, next, jumper" @pagination="handlePagination" />
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="submit">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { getResourceList } from '@/api/fusionResource'
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
    selectIds: {
      type: Array,
      default: () => {
        return []
      }
    },
    serverAddress: {
      type: String,
      default: '',
      require: true
    },
    organId: {
      type: String,
      default: '',
      require: true
    }
  },
  data() {
    return {
      listLoading: false,
      noData: false,
      resourceList: [],
      total: 0,
      currentPage: 1,
      pageNo: 1,
      pageSize: 6,
      hidePagination: true,
      visible: false,
      selectedResources: [],
      pageCount: 0,
      resourceName: '',
      selectedIds: this.selectIds
    }
  },
  watch: {
    dialogVisible: function(val) {
      if (val) {
        this.visible = val
        this.fetchData()
      } else {
        this.visible = false
      }
    }
  },
  methods: {
    async fetchData() {
      this.resourceList = []
      const params = {
        serverAddress: this.serverAddress,
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        organId: this.organId,
        resourceName: this.resourceName
      }
      console.log(params)
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
    },
    searchResource() {
      this.pageNo = 1
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
      this.resourceName = ''
      this.$emit('close')
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.fetchData()
    }
  }
}
</script>

<style lang="scss" scoped>
.resource-list{
  display: flex;
  flex-wrap: wrap;
  min-height: 80px;
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
  margin: 0 0 10px 5px;
}
.resource{
  margin: 5px;
}
</style>
