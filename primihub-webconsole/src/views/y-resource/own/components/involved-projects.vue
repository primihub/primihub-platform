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
    handelToDetail(id) {}
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
    <el-table-column label="发起人" prop="organName" />
    <el-table-column label="创建时间" prop="ctime" />
    <el-table-column align="center" label="操作" width="100">
      <template v-slot="{row}">
        <el-button type="text" @click="handelToDetail(row.id)">详情</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>
