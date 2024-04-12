<script>
import List from './components/list.vue'
import listFilter from './components/list-filter.vue'
import ViewBox from '@/components/ViewBox'
import { getResourceList } from '@/api/fusionResource'
import Pagination from '@/components/Pagination'

export default {
  name: 'YPartnerResource',
  components: {
    List,
    listFilter,
    ViewBox,
    Pagination
  },
  data() {
    return {
      resourceList: [],
      queryParams: {},
      total: 0,
      pageCount: 0,
      pageNo: 1,
      pageSize: 10
    }
  },
  mounted() {
    this.getResourceData()
  },
  methods: {
    /** query resource list */
    async getResourceData(){
      const params = {
        ...this.queryParams,
        pageNo: this.pageNo,
        pageSize: this.pageSize,
      }
      const { code, result } = await getResourceList(params)
      if (code === -1) this.$message.warning('资源同步中')
      if (code === 0) {
        const { data, total, totalPage } = result
        this.total = total
        this.pageCount = totalPage
        this.resourceList = data
      }
    },

    /** search */
    searchResourceData(data){
      this.queryParams = { ...data }
      this.getResourceData()
    },

    /** page or limit change */
    handlePagination({ page }) {
      this.pageNo = page
      this.getResourceData()
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
      <List :data="resourceList" @refresh="getResourceData()"/>
      <pagination :page-count="pageCount" :limit.sync="pageSize" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    </template>
  </ViewBox>
</template>

<style lang="scss" scoped>
</style>
