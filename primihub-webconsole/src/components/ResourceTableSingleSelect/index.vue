<template>
  <div>
    <el-table
      ref="table"
      class="table"
      border
      :data="data"
      v-bind="$attrs"
      highlight-current-row
      @selection-change="handleCurrentChange"
    >
      <el-table-column label="选择" width="55">
        <template slot-scope="{row}">
          <el-radio v-model="radioSelect" :label="row" :disabled="row.auditStatus !== 1" @change="handleRadioChange"><i /></el-radio>
        </template>
      </el-table-column>
      <el-table-column
        label="资源 / Id"
        min-width="120"
      >
        <template slot-scope="{row}">
          {{ row.resourceName }}<br>
          {{ row.resourceId }}
        </template>
      </el-table-column>
      <el-table-column
        label="资源信息"
        min-width="120"
      >
        <template slot-scope="{row}">
          特征量：{{ row.resourceColumnCount }}<br>
          样本量：{{ row.resourceRowsCount }} <br>
          正例样本数量：{{ row.resourceYRowsCount || 0 }}<br>
          正例样本比例：{{ row.resourceYRatio || 0 }}%<br>
        </template>
      </el-table-column>
      <el-table-column
        prop="auditStatus"
        label="审核状态"
        align="center"
      >
        <template slot-scope="{row}">
          {{ row.auditStatus | resourceAuditStatusFilter }}
        </template>
      </el-table-column>
      <el-table-column
        label="是否包含Y值"
        min-width="80"
        align="center"
      >
        <template slot-scope="{row}">
          {{ row.resourceContainsY? '是' : '否' }}
        </template>
      </el-table-column>
    </el-table>
  </div>

</template>

<script>
export default {
  name: 'ResourceTable',
  props: {
    data: {
      type: Array,
      default: () => []
    },
    selectedData: {
      type: Array,
      default: () => []
    }

  },
  data() {
    return {
      currentRow: null,
      radioSelect: null
    }
  },
  mounted() {
    this.setCurrent(this.selectedData)
  },
  methods: {
    handleRadioChange(val) {
      this.currentRow = val
      console.log(this.currentRow)
      this.$emit('change', this.currentRow)
    },
    setCurrent(row) {
      this.$refs.table.setCurrentRow(row)
    },
    handleCurrentChange(val) {
      console.log('handleCurrentChange', val)
      this.currentRow = val
      this.$emit('change', this.currentRow)
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

