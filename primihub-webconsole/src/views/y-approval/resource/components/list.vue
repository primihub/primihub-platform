<script>
import { mapGetters } from "vuex"
import { resourceStatusChange } from '@/api/resource'

export default {
  name: "YOwnResource",
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
      return this.buttonPermissionList.indexOf("ResourceDetail") !== -1
    },
    hasEditPermission() {
      // edit resource permission
      return this.buttonPermissionList.includes("ResourceEdit")
    },
    ...mapGetters(["buttonPermissionList"]),
  },
  mounted() {},
  methods: {
    handelAgree(id) {},

    handelCancel(id) {},

    handelRefuse(id) {},
  },
}
</script>

<template>
  <el-table :data="data" empty-text="暂无数据" border>
    <el-table-column prop="resourceId" label="资源ID">
      <template slot-scope="{ row }">
        <template v-if="hasViewPermission && row.resourceState === 0">
          <el-link
            type="primary"
            @click="toResourceDetailPage(row.resourceId)"
            >{{ row.resourceId }}</el-link
          ><br />
        </template>
        <template v-else> {{ row.resourceId }}<br /> </template>
      </template>
    </el-table-column>
    <el-table-column prop="resourceName" label="资源名称" min-width="200"/>
    <el-table-column prop="resourceName" label="合作方" min-width="140"/>
    <el-table-column prop="tags" label="项目类型" min-width="100">
      <template slot-scope="{ row }">
        <el-tag
          v-for="tag in row.tags"
          :key="tag.tagId"
          type="success"
          size="mini"
          class="tag"
          >{{ tag.tagName }}</el-tag
        >
      </template>
    </el-table-column>
    <el-table-column label="数据信息" min-width="200">
      <template slot-scope="{ row }">
        样本数:{{ row.fileRows }}  特征数:{{ row.fileColumns }}
      </template>
    </el-table-column>
    <el-table-column prop="createDate" label="申请时间" min-width="160" />
    <el-table-column prop="createDate" label="持续时间" min-width="160" />
    <el-table-column prop="resourceDesc" label="描述信息" min-width="120" >
      <template slot-scope="{ row }">{{row.resourceDesc || '-'}}</template>
    </el-table-column>
    <el-table-column label="操作" fixed="right" width="160" align="center">
      <template slot-scope="{ row }">
        <el-button  v-if="hasEditPermission && row.resourceState === 0" type="text" @click="handelAgree(row.resourceId)"
          >同意授权</el-button
        >
        <el-button v-if="false" type="text" @click="handelCancel(row.resourceId)"
          >取消授权</el-button
        >
        <el-button type="text" @click="handelRefuse(row.resourceId)"
          >拒绝授权</el-button
        >
      </template>
    </el-table-column>
  </el-table>
</template>

<style lang="scss" scoped>
@import "~@/styles/resource.scss";
</style>
