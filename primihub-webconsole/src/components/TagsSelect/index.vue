<template>
  <el-select
    v-model="tagName"
    placeholder="请选择"
    clearable
    filterable
    :remote="remote"
    :remote-method="handleFilter"
    @change="handleTagChange"
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
    }
  },
  data() {
    return {
      tagName: ''
    }
  },
  methods: {
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
