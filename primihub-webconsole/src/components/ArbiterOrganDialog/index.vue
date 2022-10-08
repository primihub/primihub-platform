<template>
  <el-dialog
    :visible.sync="visible"
    :before-close="closeDialog"
    v-bind="$attrs"
  >
    <el-table
      ref="table"
      v-loading="loading"
      empty-text="暂无审核通过的第三方"
      border
      row-key="organId"
      :data="data"
      @selection-change="handleSelectionChange"
    >
      <!-- :selectable="checkSelectable" 默认选中置灰 -->
      <el-table-column label="选择" width="55">
        <template slot-scope="{row}">
          <el-radio v-model="radioSelect" :label="row.organId" @change="handleRadioChange(row)"><i /></el-radio>
        </template>
      </el-table-column>
      <el-table-column
        prop="organId"
        label="机构Id"
      />
      <el-table-column
        prop="organName"
        label="机构名称"
      />
    </el-table>
    <span slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">取 消</el-button>
      <el-button type="primary" @click="handleSubmit">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  props: {
    data: {
      type: Array,
      default: () => []
    },
    selectedData: {
      type: String,
      default: ''
    },
    visible: {
      type: Boolean,
      default: false,
      require: true
    }
  },
  data() {
    return {
      loading: false,
      organList: [],
      multipleSelection: [],
      currentRow: null,
      selectedRows: [],
      radioSelect: null
    }
  },
  methods: {
    handleRadioChange(row) {
      this.currentRow = row
      this.setCurrent(row.organId)
    },
    setCurrent(organId) {
      this.radioSelect = organId || ''
    },
    closeDialog() {
      this.$emit('close', this.selectedData)
    },
    handleSubmit() {
      this.$emit('submit', this.currentRow)
    },
    handleSelectionChange(value) {
      this.multipleSelection = []
      console.log('handleSelectionChange', value)
      this.multipleSelection = value
    },
    handleSelect(rows, row) {
      const selected = rows.length && rows.indexOf(row) !== -1
      const posIndex = this.selectedData.findIndex(item => item.globalId === row.globalId)
      const id = this.selectedData.filter(item => item.globalId === row.globalId)[0]?.id
      // true:选中，0或者false:取消选中
      // 取消选中需判断是否在选择过的列表里
      if (!selected && posIndex !== -1) {
        this.confirm([id])
      }
    }
  }

}
</script>

<style lang="scss" scoped>

</style>

