<script>
export default {
  name: 'OwnResourceDetail',
  data() {
    return {
      data: []
    }
  },
  created() {

  },
  methods: {
    handelViewLog(id) {}
  }
}
</script>

<template>
  <el-table
    :data="data"
    class="table-list"
    v-bind="$attrs"
  >
    <el-table-column label="项目编号" prop="organName" />
    <el-table-column label="项目名称" prop="organName" />
    <el-table-column label="合作机构" prop="organName" />
    <el-table-column label="任务ID" prop="organName" />
    <el-table-column label="调用时间" prop="ctime" />
    <el-table-column label="运算类型" prop="ctime" />
    <el-table-column align="center" label="操作" width="100">
      <template v-slot="{row}">
        <el-button type="text" @click="handelViewLog(row.id)">查看日志</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>
