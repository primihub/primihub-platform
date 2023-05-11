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
      @select="handleSelect"
      @select-all="handleSelectAll"
      @selection-change="handleSelectionChange"
    >
      <!-- :selectable="checkSelectable" 默认选中置灰 -->
      <el-table-column
        :reserve-selection="true"
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
import { getAvailableOrganList } from '@/api/center'

export default {
  props: {
    selectedData: {
      type: Array,
      default: () => []
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
      selectedRows: []
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.getAvailableOrganList()
      } else {
        this.organList = []
      }
    }
  },
  methods: {
    closeDialog() {
      this.$emit('close', this.selectedData)
    },
    handleSubmit() {
      this.$emit('submit', this.multipleSelection)
    },
    handleSelectionChange(value) {
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
    },
    handleSelectAll(selection) {
      if (selection.length === 0 && this.selectedData.length > 0) {
        const ids = this.selectedData.map(item => item.id)
        this.confirm(ids)
      }
    },
    confirm(data) {
      this.$confirm('删除后，不可再使用此协作者数据创建任务，且进行中任务将会失败，是否删除？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$emit('delete', data)
      }).catch(() => {
        this.$nextTick(() => {
          this.toggleSelection(this.selectedRows)
        })
      })
    },
    checkSelectable(row, index) {
      const res = !(this.selectedData.length > 0 && this.selectedData.filter(item => item.globalId === row.globalId).length > 0)
      return res
    },
    async getAvailableOrganList() {
      this.loading = true
      const { result } = await getAvailableOrganList()
      this.organList = result
      console.log('selectedData', this.selectedData)
      this.selectedRows = this.selectedData.map(item => {
        return this.organList.filter(o => o.globalId === item.globalId)[0]
      })
      console.log('selectedRows', this.selectedRows)
      this.$nextTick(() => {
        this.toggleSelection(this.selectedRows)
      })
      this.loading = false
    },
    toggleSelection(rows) {
      if (rows) {
        this.$refs.table.clearSelection()
        rows.forEach(row => {
          console.log(row)
          this.$refs.table.toggleRowSelection(row, true)
        })
      } else {
        this.$refs.table.clearSelection()
      }
    }
  }

}
</script>

<style lang="scss" scoped>

</style>
