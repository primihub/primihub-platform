<template>
  <div>
    <el-select
      ref="selectTree"
      v-model="selectValue"
      :placeholder="placeholder"
      clearable
      filterable
      remote
      :remote-method="searchResource"
      v-bind="$attrs"
      @focus="handleFocus"
      @clear="handleSearchNameClear"
      @change="handleResourceChange"
    >
      <el-option
        v-for="item in options"
        :key="item.resourceId"
        :label="item.resourceName"
        :value="item.resourceId"
      />

    </el-select>
  </div>
</template>

<script>

export default {
  name: 'ResourceSelect',
  props: {
    value: {
      type: String,
      default: ''
    },
    options: {
      type: Array,
      default: () => []
    },
    role: { // own or other
      type: String,
      default: 'own'
    },
    placeholder: {
      type: String,
      default: '请选择'
    }
  },
  data() {
    return {
      selectValue: this.value
    }
  },
  watch: {
    value(newVal) {
      if (newVal) {
        this.selectValue = newVal
      } else {
        this.selectValue = ''
      }
    }
  },
  created() {
    this.$emit('search', '')
  },
  methods: {
    handleFocus(e) {
      this.$emit('focus', this.role)
    },
    handleSearchNameClear() {
      this.$emit('clear', this.role)
    },
    async searchResource(resourceName) {
      if (resourceName !== '') {
        this.$emit('search', resourceName)
      }
    },
    handleResourceChange(resourceId) {
      this.selectValue = resourceId
      this.$emit('change', resourceId)
    }
  }

}
</script>
