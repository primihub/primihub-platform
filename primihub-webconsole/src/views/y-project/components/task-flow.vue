<script>
export default {
  name: '',
  props: {
    data: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      id: '',
      taskType: '',
      tableData: [],
      taskTypes: {
        1: 'PIRTaskCreate',
        2: 'PSITaskCreate',
        3: 'TaskCreate'
      }
    }
  },
  watch: {
    data: {
      handler (val) {
        const { id } = val
        this.id = id
        this.taskType = 3
      },
      immediate: true
    },
  },
  methods: {
    toCreatePage() {
      console.log(this.taskTypes[this.taskType])
      this.$router.push({ name: this.taskTypes[this.taskType] })
    }
  }
}
</script>
<template>
  <div>
    <el-button type="primary" plain class="mb15" @click="toCreatePage">新建任务流</el-button>
    <el-table :data="tableData" empty-text="暂无数据" border>
      <el-table-column prop="resourceId" label="任务流名称" />
      <el-table-column prop="resourceId" label="任务流英文名" />
      <el-table-column prop="resourceId" label="任务流描述" />
      <el-table-column prop="resourceId" label="创建时间" />
      <el-table-column prop="resourceId" label="操作" width="120" align="center">
        <template v-slot="{ row }">
          <el-button type="text">编辑</el-button>
          <el-button type="text">运行</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped lang="scss">
.mb15 {
  margin-bottom: 15px;
}
</style>
