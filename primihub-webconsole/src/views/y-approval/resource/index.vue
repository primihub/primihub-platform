<script>
import List from './components/list.vue'
import listFilter from './components/list-filter.vue'
import ViewBox from '@/components/ViewBox'
import { getResourceList } from '@/api/resource'
import Pagination from '@/components/Pagination'
import { mapGetters } from 'vuex'

export default {
  name: 'YOwnResource',
  components: {
    List,
    listFilter,
    ViewBox,
    Pagination
  },
  data() {
    return {
      dataList: [],
      queryParams: {},
      total: 0,
      pageCount: 0,
      pageNo: 1,
      pageSize: 10
    }
  },
  computed: {
    hasUploadAuth() { // upload resource permission
      return this.buttonPermissionList.indexOf('ResourceUpload') !== -1
    },
    ...mapGetters([
      'buttonPermissionList'
    ])
  },
  mounted() {
    this.getTableData()
  },
  methods: {
    /** query resource list */
    async getTableData(){
      const params = {
        ...this.queryParams,
        pageNo: this.pageNo,
        pageSize: this.pageSize,
      }
      const { code, result } = await getResourceList(params)
      if (code === 0) {
        const { data, total, totalPage } = result
        this.total = total
        this.pageCount = totalPage
        this.dataList = data
      }
    },

    /** search */
    searchResourceData(data){
      this.queryParams = { ...data }
      this.getTableData()
    },

    /** page or limit change */
    handlePagination({ page }) {
      this.pageNo = page
      this.getTableData()
    }
  }
}
</script>

<template>
  <ViewBox>
    <template v-slot:filter>
      <listFilter @search="searchResourceData"/>
    </template>
    <template v-slot:list>
      <List :data="dataList" @refresh="getTableData()"/>
      <pagination :page-count="pageCount" :limit.sync="pageSize" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    </template>
  </ViewBox>
</template>

<style lang="scss" scoped>
</style>
