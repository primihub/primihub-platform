<template>
  <el-input
    v-model="searchName"
    placeholder="请输入内容"
    class="search"
    :style="{width: width}"
    v-bind="$attrs"
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
    }
  },
  data() {
    return {
      searchName: ''
    }
  },
  watch: {
    searchName(newValue) {
      this.$emit('change', newValue)
    }
  },
  methods: {
    handleSearch() {
      this.$emit('click', this.searchName)
    }
  }
}
</script>
