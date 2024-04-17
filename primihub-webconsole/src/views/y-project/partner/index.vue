<script>
import List from './components/list.vue'
import listFilter from './components/list-filter.vue'
import ViewBox from '@/components/ViewBox'
import Pagination from '@/components/Pagination'
import { mapGetters } from 'vuex'
import { getProjectList} from '@/api/project'

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
    hasCreateAuth() {
      return this.buttonPermissionList.includes('ProjectCreate')
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
    async getTableData() {
      const params = {
        ...this.queryParams,
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }
      const { code, result } = await getProjectList(params)
      if (code === 0) {
        const { data, total, totalPage } = result
        this.total = total
        this.pageCount = totalPage
        this.dataList = data
      }
    },

    /** search */
    searchData(data) {
      this.queryParams = { ...data }
      this.getTableData()
    },

    /** page or limit change */
    handlePagination({ page }) {
      this.pageNo = page
      this.getTableData()
    },

    /** go to create resource page */
    toCreatePage() {
      this.$router.push({
        name: 'YResourceCreate'
      })
    }
  }
}
</script>

<template>
  <ViewBox>
    <template v-slot:filter>
      <listFilter @search="searchData" />
    </template>
    <template v-slot:list>
      <List :data="dataList" @refresh="getTableData()" />
      <pagination :page-count="pageCount" :limit.sync="pageSize" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    </template>
  </ViewBox>
</template>
