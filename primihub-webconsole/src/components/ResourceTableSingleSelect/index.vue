<template>
  <div>
    <el-table
      ref="table"
      class="table"
      border
      :data="data"
      v-bind="$attrs"
      highlight-current-row
      :row-class-name="tableRowClassName"
    >
      <el-table-column label="选择" width="55">
        <template slot-scope="{row}">
          <el-radio v-model="radioSelect" :disabled="row.auditStatus !== 1" :label="row.resourceId" @change="handleRadioChange(row)"><i /></el-radio>
          <!-- <el-radio v-model="radioSelect" :label="row.resourceId" :disabled="row.auditStatus !== 1" @change="handleRadioChange(row)"><i /></el-radio> -->
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
        v-if="showStatus"
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
    tableRowClassName({ row }) {
      if (row.auditStatus !== 1) {
        return 'row-disabled'
      } else {
        return ''
      }
    },
    handleRadioChange(row) {
      this.currentRow = row
      this.setCurrent(row.resourceId)
      this.$emit('change', this.currentRow)
    },
    setCurrent(resourceId) {
      this.radioSelect = resourceId || ''
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
::v-deep .el-table tr.row-disabled{
  color: #999;
}
</style>

