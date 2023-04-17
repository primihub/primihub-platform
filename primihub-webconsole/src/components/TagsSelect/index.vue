<template>
  <el-select
    v-model="tagName"
    placeholder="请选择"
    clearable
    filterable
    v-bind="$attrs"
    :remote="remote"
    :remote-method="handleFilter"
    @change="handleTagChange"
    @clear="handleClear"
  >
    <el-option
      v-for="item in data"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    />
  </el-select>
</template>

<script>
export default {
  name: 'TagsSelect',
  props: {
    remote: { // is it a remote search
      type: Boolean,
      default: false
    },
    data: {
      type: Array,
      default: () => []
    },
    reset: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      tagName: ''
    }
  },
  watch: {
    reset(newVal) {
      if (newVal) {
        this.tagName = ''
        this.$emit('change', this.tagName)
      }
    }
  },
  methods: {
    handleClear() {
      this.tagName = ''
      this.$emit('change', this.tagName)
    },
    handleTagChange(val) {
      this.tagName = this.data.filter(item => item.value === val)[0]?.label
      this.$emit('change', this.tagName)
    },
    handleFilter(tagName) {
      if (!this.remote) return
      this.tagName = tagName
      this.$emit('filter', this.tagName)
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
