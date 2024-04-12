<script>
import { mapGetters } from 'vuex'

export default {
  name: 'YPartnerResource',
  props: {
    data: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {}
  },
  computed: {
    hasViewPermission() {
      // view resource permission
      return this.buttonPermissionList.indexOf('ResourceDetail') !== -1
    },
    ...mapGetters(['buttonPermissionList'])
  },
  mounted() {},
  methods: {
    /** go to resource detail */
    toResourceDetailPage(id) {
      this.$router.push({ name: 'YPartnerResourceDetail', params: { id }})
    }
  }
}
</script>

<template>
  <el-table
    :data="data"
    empty-text="暂无数据"
    border
  >
    <el-table-column prop="resourceId" label="资源ID" min-width="300" fixed="left">
      <template slot-scope="{ row }">
        <template v-if="hasViewPermission && row.resourceState === 0">
          <el-link
            type="primary"
            @click="toResourceDetailPage(row.resourceId)"
          >{{ row.resourceId }}</el-link><br>
        </template>
        <template v-else> {{ row.resourceId }}<br> </template>
      </template>
    </el-table-column>
    <el-table-column prop="resourceName" label="资源名称" min-width="200" />
    <el-table-column prop="resourceName" label="应用场景" min-width="140" />
    <el-table-column prop="tags" label="数据集标签" min-width="100">
      <template slot-scope="{ row }">
        <el-tag
          v-for="(tag, index) in row.resourceTag"
          :key="index"
          type="success"
          size="mini"
          class="tag"
        >{{ tag }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="数据信息" min-width="200">
      <template slot-scope="{ row }">
        样本数:{{ row.resourceRowsCount }}  特征数:{{ row.resourceColumnCount }}
      </template>
    </el-table-column>
    <el-table-column label="授权状态" min-width="160">
      <template slot-scope="{ row }">
        {{ row.userName }} <br>
        {{ row.createDate }}
      </template>
    </el-table-column>
    <el-table-column prop="resourceDesc" label="描述信息" min-width="180">
      <template slot-scope="{ row }">{{ row.resourceDesc || '-' }}</template>
    </el-table-column>
    <el-table-column prop="organName" label="所属组织" min-width="120" />
  </el-table>
</template>

<style lang="scss" scoped>
@import "~@/styles/resource.scss";
</style>
