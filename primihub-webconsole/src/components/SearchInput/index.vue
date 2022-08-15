<template>
  <el-input
    v-model="name"
    placeholder="请输入内容"
    class="search"
    :style="{width: width}"
    v-bind="$attrs"
    @change="handleChange"
    @keyup.enter.native="handleSearch"
  >
    <el-button slot="append" icon="el-icon-search" @click="handleSearch" />
  </el-input>
</template>

<script>
export default {
  name: 'SearchInput',
  props: {
    width: {
      type: String,
      default: '300px'
    },
    searchName: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      name: this.searchName
    }
  },
  watch: {
    searchName(newValue) {
      if (newValue) {
        this.name = newValue
      } else {
        this.name = ''
      }
    }
  },
  methods: {
    handleChange(value) {
      this.name = value
      this.$emit('change', value)
    },
    handleSearch() {
      this.$emit('click', this.name)
    }
  }
}
</script>
