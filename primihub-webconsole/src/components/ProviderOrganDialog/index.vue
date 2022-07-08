<template>
  <el-dialog
    :visible.sync="visible"
    :before-close="closeDialog"
    v-bind="$attrs"
  >
    <el-table
      ref="table"
      v-loading="loading"
      border
      row-key="globalId"
      :data="organList"
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
import { findMyGroupOrgan } from '@/api/center'

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
      loading: false,
      organList: [],
      multipleSelection: []
    }
  },
  watch: {
    visible(newVal) {
      console.log(newVal)
      if (newVal) {
        this.findMyGroupOrgan()
        this.toggleSelection(this.selectedData)
      } else {
        this.organList = []
      }
    }
  },
  // mounted() {
  //   this.toggleSelection(this.selectedData)
  // },
  methods: {
    closeDialog() {
      console.log('close', this.selectedData)
      this.$emit('close', this.selectedData)
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
    },
    async findMyGroupOrgan() {
      this.loading = true
      const { result } = await findMyGroupOrgan({ serverAddress: this.serverAddress })
      this.organList = result.dataList.organList
      this.loading = false
    },
    toggleSelection(rows) {
      console.log('toggleSelection', rows)
      console.log('selectedData', this.selectedData)
      this.$nextTick(() => {
        if (rows) {
          this.$refs.table.clearSelection()
          rows.forEach(row => {
            console.log(row.globalId)
            this.$refs.table.toggleRowSelection(row, true)
          })
        } else {
          this.$refs.table.clearSelection()
        }
      })
    }
  }

}
</script>

<style lang="scss" scoped>

</style>
