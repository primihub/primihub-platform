<template>
  <div>
    <!-- <h3>数据资源预览</h3> -->
    <el-table
      :data="data"
      border
      v-bind="$attrs"
      class="table-list"
      :empty-text="emptyText"
    >
      <el-table-column v-if="data.length >0" align="center" label="序号" type="index" width="50" />
      <el-table-column
        v-for="(item,index) in tableHeader"
        :key="index"
        :prop="item"
        :label="item"
      >
        <template slot-scope="{row}">
          {{ row[item] }}
        </template>
      </el-table-column>

    </el-table>
  </div>

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
    },
    emptyText: {
      type: String,
      default: '暂无数据'
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
  },
  created() {
    console.log(this.data)
  }
}
</script>
