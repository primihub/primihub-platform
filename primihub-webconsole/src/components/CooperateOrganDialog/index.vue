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
      <el-table-column
        v-if="selectType === 'checkbox'"
        type="selection"
        width="55"
        :selectable="checkStatus"
      />
      <el-table-column v-else label="选择" width="55">
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
      type: [String, Array],
      default: () => []
    },
    visible: {
      type: Boolean,
      default: false,
      require: true
    },
    selectType: {
      type: String,
      default: 'radio' // checkbox or radio
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
  watch: {
    selectedData(newVal) {
      if (this.selectType === 'checkbox') {
        this.toggleSelection(newVal)
      } else {
        this.setCurrent(newVal)
      }
    }
  },
  created() {
    this.$nextTick(() => {
      if (this.selectType === 'checkbox' && this.selectedData && this.selectedData.length > 0) {
        const selectOrgans = this.selectedData.map(organId => {
          const index = this.data.findIndex(v => v.organId === organId)
          if (index !== -1) {
            return this.data[index]
          }
        })
        this.toggleSelection(selectOrgans)
      } else if (this.selectType === 'radio' && this.selectedData !== '') {
        this.setCurrent(this.selectedData)
      }
    })
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
      if (this.selectType === 'radio') {
        this.$emit('submit', this.currentRow)
      } else {
        this.$emit('submit', this.multipleSelection)
      }
    },
    handleSelectionChange(value) {
      this.multipleSelection = []
      this.multipleSelection = value
    },
    toggleSelection(rows) {
      if (rows) {
        rows.forEach(row => {
          this.$refs.table.toggleRowSelection(row)
        })
      } else {
        this.$refs.table.clearSelection()
      }
    },
    checkStatus(row) {
      if (this.selectType === 'checkbox') {
        const res = !(this.selectedData.length > 0 && this.selectedData.find(organId => row.organId === organId))
        return res
      }
    }
  }

}
</script>

<style lang="scss" scoped>

</style>

