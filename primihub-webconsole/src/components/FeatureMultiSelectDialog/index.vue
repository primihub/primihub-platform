<template>
  <el-dialog
    title="选择多方特征列"
    width="50%"
    v-bind="$attrs"
    :before-close="handleClose"
  >
    <el-tabs v-model="activeName">
      <el-tab-pane v-for="(item,index) in selectedData" :key="index" :label="item.organName" :name="item.organId">
        <checkbox :organ-id="item.organId" :options="item.calculationField" :checked="item.checked" @change="handleChange" />
      </el-tab-pane>
    </el-tabs>
    <span slot="footer" class="dialog-footer">
      <el-button size="small" @click="handleClose">取 消</el-button>
      <el-button size="small" type="primary" @click="handleDialogSubmit">确 定</el-button>
    </span>
  </el-dialog>

</template>

<script>
import checkbox from '@/components/BaseCheckbox'

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
      activeName: '',
      selectedData: {}
    }
  },
  created() {
    this.selectedData = JSON.parse(JSON.stringify(this.data))
    this.activeName = this.selectedData && this.selectedData[0].organId
  },
  methods: {
    handleClose() {
      this.$emit('close')
    },
    handleDialogSubmit() {
      this.$emit('submit', this.selectedData)
    },
    handleChange(data) {
      const { organId, checked } = data
      const posIndex = this.selectedData.findIndex(item => item.organId === organId)
      this.selectedData[posIndex].checked = checked
    }
  }
}
</script>
