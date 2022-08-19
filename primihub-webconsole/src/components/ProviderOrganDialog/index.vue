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
      // true:选中，0或者false:取消选中
      // 取消选中需判断是否在选择过的列表里
      if (!selected && posIndex !== -1) {
        this.$confirm('删除后，不可再使用此协作者数据创建任务，且进行中任务将会失败，是否删除？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          const id = this.selectedData.filter(item => item.globalId === row.globalId)[0].id
          this.$emit('delete', id)
        }).catch(() => {
          rows.push(row)
          this.toggleSelection(rows)
        })
      }
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
      this.$nextTick(() => {
        if (rows) {
          this.$refs.table.clearSelection()
          rows.forEach(row => {
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
