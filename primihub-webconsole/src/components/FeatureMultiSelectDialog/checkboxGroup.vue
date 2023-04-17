<template>
  <div>
    <el-checkbox v-model="currentOptions.checkAll" :indeterminate="currentOptions.isIndeterminate" @change="handleCheckAllChange">全选</el-checkbox>
    <el-checkbox-group v-model="checkedList" @change="handleCheckedChange">
      <el-checkbox v-for="item in currentOptions.calculationField" :key="item" :label="item">{{ item }}</el-checkbox>
    </el-checkbox-group>
  </div>
</template>

<script>
export default {
  props: {
    options: {
      type: Object,
      default: () => {}
    },
    checked: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      checkedList: this.checked,
      currentOptions: this.options
    }
  },
  methods: {
    handleCheckedChange(value) {
      const checkedCount = value.length
      this.$emit('change', Object.assign({}, this.options, {
        checked: this.checkedList,
        checkAll: checkedCount === this.options.calculationField.length,
        isIndeterminate: checkedCount > 0 && checkedCount < this.options.calculationField.length
      }))
    },
    handleCheckAllChange(value) {
      const checkedCount = value.length
      this.checkedList = value ? this.options.calculationField : []
      this.$emit('change', Object.assign({}, this.options, {
        checked: this.checkedList,
        checkAll: checkedCount === this.options.calculationField.length,
        isIndeterminate: checkedCount > 0 && checkedCount < this.options.calculationField.length
      }))
    }
  }
}
</script>
