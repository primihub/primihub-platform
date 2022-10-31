<template>
  <el-table
    :data="data"
    v-bind="$attrs"
    empty-text="暂无数据"
  >
    <el-table-column
      prop="resourceName"
      label="资源名称"
      min-width="100"
    >
      <template slot-scope="{row}">
        <el-link :underline="false" size="small" type="primary" @click="toResourceDetailPage(row.resourceId)">{{ row.resourceName }}</el-link>
      </template>
    </el-table-column>
    <el-table-column
      prop="resourceId"
      label="ID"
      align="center"
    >
      <template slot-scope="{row}">
        {{ row.resourceId }}
      </template>
    </el-table-column>
    <el-table-column
      label="资源信息"
      min-width="110"
    >
      <template slot-scope="{row}">
        <div class="info">
          特征量：{{ row.fileColumns }}<br>
          样本量：{{ row.fileRows }} <br>
          正例样本数量：{{ row.fileYRows || 0 }}<br>
          正例样本比例：{{ row.fileYRatio || 0 }}%
        </div>
      </template>
    </el-table-column>
    <el-table-column
      prop="taskIdName"
      label="任务ID"
      min-width="100"
    >
      <template slot-scope="{row}">
        <el-link :underline="false" size="small" type="primary" @click="toModelTaskDetail(row)">{{ row.taskIdName }}</el-link>
      </template>
    </el-table-column>
    <el-table-column
      prop="tag"
      label="衍生数据来源"
    />
    <el-table-column
      prop="createDate"
      label="创建时间"
    >
      <template slot-scope="{row}">
        {{ row.createDate.split(' ')[0] }} <br>
        {{ row.createDate.split(' ')[1] }}
      </template>
    </el-table-column>
    <el-table-column
      label="是否包含Y值"
      min-width="80"
      align="center"
    >
      <template slot-scope="{row}">
        {{ row.fileContainsY ? '是' : '否' }}
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
export default {
  name: '',
  props: {
    data: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      resourceList: this.data
    }
  },
  methods: {
    toModelTaskDetail(row) {
      this.$router.push({
        path: `/project/detail/${row.projectId}/task/${row.taskId}`
      })
    },
    toResourceDetailPage(id) {
      this.$router.push({
        name: 'DerivedDataResourceDetail',
        params: { id }
      })
    }
  }
}
</script>
