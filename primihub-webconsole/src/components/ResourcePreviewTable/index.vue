<template>
  <el-table
    :data="data"
    border
    v-bind="$attrs"
    class="table-list"
  >
    <el-table-column align="center" label="序号" type="index" width="50" />
    <el-table-column
      v-for="(item,index) in tableHeader"
      :key="index"
      :prop="item"
      :label="item"
    />

  </el-table>
</template>

<script>
export default {
  name: 'ResourcePreviewTable',
  props: {
    data: {
      type: Array,
      default() {
        return []
      }
    }
  },
  computed: {
    // get resource table data header field
    tableHeader() {
      const data = []
      if (this.data.length > 0) {
        for (const key in this.data[0]) {
          data.push(key)
        }
      }
      return data
    }
  }
}
</script>
