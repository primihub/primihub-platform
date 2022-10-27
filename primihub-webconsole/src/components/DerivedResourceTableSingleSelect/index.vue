<template>
  <el-table
    ref="table"
    class="table"
    border
    :data="data"
    v-bind="$attrs"
    highlight-current-row
  >
    <el-table-column label="选择" width="55">
      <template slot-scope="{row}">
        <el-radio v-model="radioSelect" :label="row.resourceId" @change="handleRadioChange(row)"><i /></el-radio>
      </template>
    </el-table-column>
    <el-table-column
      prop="resourceName"
      label="资源名称"
    >
      <template slot-scope="{row}">
        <el-tooltip :content="row.resourceName" placement="top">
          <el-link size="mini" type="primary" @click="toResourceDetailPage(row.resourceId)">{{ row.resourceName }}</el-link>
        </el-tooltip>
      </template>
    </el-table-column>
    <el-table-column
      prop="resourceId"
      label="资源ID"
      min-width="120"
    >
      <template slot-scope="{row}">
        {{ row.resourceId }}
        <el-tooltip :content="row.resourceId" placement="top">
          {{ row.resourceId }}
        </el-tooltip>
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
      prop="resourceSource"
      label="衍生数据来源"
      align="center"
    />
    <el-table-column
      prop="createDate"
      label="创建时间"
      min-width="110"
      align="center"
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
  name: 'ResourceTable',
  props: {
    data: {
      type: Array,
      default: () => [
        {
          'projectId': '141',
          'resourceId': '2b598a7e3298-4f62b21f-665e-4426-9068-4fc8f8186966',
          'resourceName': 'test2-的新资源',
          'taskId': '290',
          'taskIdName': '2cad8338-2e8c-4768-904d-2b598a7e3298',
          'resourceSource': 1,
          'fileRows': 50,
          'fileColumns': 7,
          'fileHandleStatus': 0,
          'fileContainsY': null,
          'fileYRows': null,
          'fileYRatio': null,
          'createDate': '2022-10-19 14:11:26'
        }
      ]
    },
    selectedData: {
      type: String,
      default: ''
    },
    showStatus: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      currentRow: null,
      radioSelect: null
    }
  },
  watch: {
    selectedData(newVal) {
      this.setCurrent(newVal)
    }
  },
  mounted() {
    if (this.selectedData) {
      this.setCurrent(this.selectedData)
    }
  },
  methods: {
    handleRadioChange(row) {
      this.currentRow = row
      this.setCurrent(row.resourceId)
      this.$emit('change', this.currentRow)
    },
    setCurrent(resourceId) {
      this.radioSelect = resourceId || ''
    },
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
<style lang="scss" scoped>
.table{
  margin: 15px 0;
}
::v-deep .el-button{
  margin: 2px 5px;
}

</style>

