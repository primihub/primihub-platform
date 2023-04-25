<template>
  <div>
    <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" @change="handleCheckAllChange">全选</el-checkbox>
    <el-checkbox-group v-model="checkedList" @change="handleCheckedChange">
      <el-checkbox v-for="item in options" :key="item" :label="item">{{ item }}</el-checkbox>
    </el-checkbox-group>
  </div>
</template>

<script>
export default {
  props: {
    organId: {
      type: String,
      default: ''
    },
    options: {
      type: Array,
      default: () => []
    },
    checked: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      checkedList: this.checked || [],
      currentOptions: this.options,
      isIndeterminate: true
    }
  },
  computed: {
    checkAll: {
      get() {
        return this.checked && this.options && this.checked.length === this.options.length
      },
      set() {

      }
    }
  },
  methods: {
    handleCheckedChange(value) {
      this.isIndeterminate = value.length > 0 && value.length < this.options.length
      this.$emit('change', {
        organId: this.organId,
        checked: this.checkedList
      })
    },
    handleCheckAllChange(value) {
      this.checkedList = value ? this.options : []
      this.isIndeterminate = false
      this.$emit('change', {
        organId: this.organId,
        checked: this.checkedList
      })
    }
  }
}
</script>
