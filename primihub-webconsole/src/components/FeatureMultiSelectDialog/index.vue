<template>
  <el-dialog
    title="选择多方特征列"
    width="50%"
    v-bind="$attrs"
    :before-close="handleClose"
  >
    <el-tabs v-model="activeName">
      <el-tab-pane v-for="(item,index) in currentData" :key="index" :label="item.organName" :name="item.organId">
        <checkbox :options="item" :checked="item.checked" @change="handleChange" />
      </el-tab-pane>
    </el-tabs>
    <span slot="footer" class="dialog-footer">
      <el-button size="small" @click="handleClose">取 消</el-button>
      <el-button size="small" type="primary" @click="handleDialogSubmit">确 定</el-button>
    </span>
  </el-dialog>

</template>

<script>
import checkbox from './checkboxGroup.vue'

export default {
  name: 'FeatureMultiSelectDialog',
  components: {
    checkbox
  },
  props: {
    data: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      dialogVisible: false,
      checkAll: false,
      isIndeterminate: true,
      activeName: '',
      currentData: [],
      selectedData: {}
    }
  },
  created() {
    this.currentData = this.data.map(item => {
      item.isIndeterminate = true
      item.checkAll = false
      item.checked = !item.checked ? [] : item.checked
      return item
    })
    this.activeName = this.data && this.data[0].organId
  },
  methods: {
    handleClose() {
      this.$emit('close')
    },
    handleDialogSubmit() {
      this.$emit('submit', this.currentData)
    },
    handleChange(data) {
      this.selectedData = data
      const posIndex = this.currentData.findIndex(item => item.organId === this.selectedData.organId)
      this.currentData[posIndex] = this.selectedData
    }

  }
}
</script>
