<template>
  <el-dialog
    :visible.sync="visible"
    :before-close="closeDialog"
    v-bind="$attrs"
  >
    <el-table
      ref="multipleTable"
      border
      row-key="globalId"
      :data="data"
      @selection-change="handleSelectionChange"
    >
      <el-table-column
        :reserve-selection="true"
        :selectable="checkSelectable"
        type="selection"
        width="55"
      />
      <el-table-column
        prop="globalId"
        label="机构Id"
      />
      <el-table-column
        prop="globalName"
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
      type: Array,
      default: () => []
    },
    serverAddress: {
      type: String,
      default: '',
      require: true
    },
    visible: {
      type: Boolean,
      default: false,
      require: true
    }
  },
  data() {
    return {
      organList: [],
      multipleSelection: []
    }
  },
  methods: {
    closeDialog() {
      this.$emit('close')
    },
    handleSubmit() {
      this.$emit('submit', this.multipleSelection)
    },
    handleSelectionChange(value) {
      this.multipleSelection = value
    },
    checkSelectable(row, index) {
      const res = !(this.selectedData.length > 0 && this.selectedData.filter(item => item.globalId === row.globalId).length > 0)
      return res
    }
  }

}
</script>

<style lang="scss" scoped>

</style>
