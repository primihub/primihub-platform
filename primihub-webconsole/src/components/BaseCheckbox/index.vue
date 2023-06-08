<template>
  <div>
    <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" @change="handleCheckAllChange">全选</el-checkbox>
    <el-checkbox-group v-model="checkedList" @change="handleCheckedChange">
      <el-checkbox v-for="item in options" :key="item" :label="item" :disabled="filterStatus(item)">{{ item }}</el-checkbox>
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
    },
    selectData: {
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
    filterStatus(item) {
      const currentData = this.selectData.find(data => data === item)
      return !!currentData
    },
    handleCheckedChange(value) {
      this.isIndeterminate = value.length > 0 && value.length < this.options.length
      this.$emit('change', {
        organId: this.organId,
        checked: this.checkedList
      })
    },
    handleCheckAllChange(value) {
      const options = [...this.options]
      if (value) {
        for (let i = 0; i < options.length; i++) {
          const item = options[i]
          if (!this.selectData.includes(item)) {
            this.checkedList.push(item)
          }
        }
      } else {
        this.checkedList = []
      }
      this.isIndeterminate = true
      this.$emit('change', {
        organId: this.organId,
        checked: this.checkedList
      })
    }
  }
}
</script>
