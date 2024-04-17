<script>
export default {
  name: '',
  components: {},
  data() {
    return {
      tableData: []
    }
  },
  created() {},
  methods: {}
}
</script>
<template>
  <div>
    <el-table :data="tableData" empty-text="暂无数据" border>
      <el-table-column prop="resourceId" label="模型编码" />
      <el-table-column prop="resourceId" label="任务编码" />
      <el-table-column prop="resourceId" label="关联任务流名称" />
      <el-table-column prop="resourceId" label="更新时间" />
      <el-table-column prop="resourceId" label="操作" width="160" align="center">
        <template v-slot="{ row }">
          <el-button type="text">下载结果</el-button>
          <el-button type="text">部署服务</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
