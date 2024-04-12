<script>
export default {
  name: 'YPartnerResourceListFilter',
  data() {
    return {
      resourceSourceList: [],
      query: {
        resourceName: '',
        scenario: ''
      }
    }
  },
  mounted() {},
  methods: {
    search() {
      this.$emit('search', this.query)
    },
    reset() {
      this.$refs.queryForm.resetFields()
      this.$emit('search', this.query)
    }
  }
}
</script>

<template>
  <el-form ref="queryForm" :model="query" :inline="true" @keyup.enter.native="search">
    <el-form-item label="资源名称" prop="resourceName">
      <el-input v-model="query.resourceName" size="small" placeholder="请输入资源名称" />
    </el-form-item>
    <el-form-item label="应用场景" prop="scenario">
      <el-select v-model="query.scenario" size="small" placeholder="请选择" clearable>
        <el-option
          v-for="item in resourceSourceList"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" icon="el-icon-search" class="search-button" size="small" @click="search">查询</el-button>
      <el-button size="small" icon="el-icon-refresh-right" @click="reset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style lang="scss" scoped>
</style>
