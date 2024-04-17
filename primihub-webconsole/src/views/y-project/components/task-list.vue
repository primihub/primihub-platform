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
      <el-table-column prop="resourceId" label="任务ID" />
      <el-table-column prop="resourceId" label="关联任务流名称" />
      <el-table-column prop="resourceId" label="运行状态" />
      <el-table-column prop="resourceId" label="创建时间" />
      <el-table-column prop="resourceId" label="结束时间" />
      <el-table-column prop="resourceId" label="操作" width="160" align="center">
        <template v-slot="{ row }">
          <el-button type="text">查看日志</el-button>
          <el-button type="text">任务详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
