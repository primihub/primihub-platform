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
    /** go to resource detail */
    toResourceDetailPage(id) {
      this.$router.push({ name: 'YResourceDetail', params: { id } })
    },

    /** go to resource edit page  */
    toResourceEditPage(id) {},

    /** change resource status */
    changeResourceStatus({ resourceId, resourceState }) {
      resourceState = resourceState === 0 ? 1 : 0
      resourceStatusChange({ resourceId, resourceState }).then(res => {
        if (res.code === 0) {
          this.$emit('refresh')
          this.$message({
            message: resourceState === 0 ? '上线成功' : '下线成功',
            type: 'success'
          })
        }
      })
    },

    /** when the resource state changes, modify the resource style. */
    tableRowDisabled({ row }) {
      return row.resourceState ? 'resource-disabled' : ''
    },
  },
};
</script>

<template>
  <el-table
    :data="data"
    :row-class-name="tableRowDisabled"
    empty-text="暂无数据"
    border
  >
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
    <el-table-column prop="resourceName" label="应用场景" min-width="140"/>
    <el-table-column prop="tags" label="数据集标签" min-width="100">
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
    <el-table-column label="上传者" min-width="160">
      <template slot-scope="{ row }">
        {{ row.userName }} <br />
        {{ row.createDate }}
      </template>
    </el-table-column>
    <el-table-column prop="resourceDesc" label="描述信息" min-width="120" >
      <template slot-scope="{ row }">{{row.resourceDesc || '-'}}</template>
    </el-table-column>
    <el-table-column label="操作" fixed="right" width="160" align="center">
      <template slot-scope="{ row }">
        <el-button type="text" @click="toResourceDetailPage(row.resourceId)"
          >查看</el-button
        >
        <el-button
          v-if="hasEditPermission && row.resourceState === 0"
          type="text"
          @click="toResourceEditPage(row.resourceId)"
          >编辑</el-button
        >
        <el-button type="text" @click="changeResourceStatus(row)">{{
          row.resourceState === 0 ? "下线" : "上线"
        }}</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<style lang="scss" scoped>
@import "~@/styles/resource.scss";
</style>
