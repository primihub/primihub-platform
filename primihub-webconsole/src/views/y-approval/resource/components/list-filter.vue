<script>
export default {
  name: 'YOwnResourceListFilter',
  data() {
    return {
      resourceSourceList: [],
      query: {
        resourceName: '',
        scenario: '',
        createDate: []
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
  <el-form :model="query" ref="queryForm" :inline="true" @keyup.enter.native="search">
    <el-form-item label="资源名称" prop="resourceName">
      <el-input v-model="query.resourceName" size="small" placeholder="请输入资源名称" />
    </el-form-item>
    <el-form-item label="审批状态" prop="scenario">
      <el-select v-model="query.scenario" size="small" placeholder="请选择" clearable>
        <el-option
          v-for="item in resourceSourceList"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="登记时间" prop="createDate">
      <el-date-picker
        v-model="query.createDate"
        size="small"
        type="daterange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="yyyy-MM-dd HH:mm:ss"
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" icon="el-icon-search" class="search-button" size="small" @click="search">查询</el-button>
      <el-button size="small" icon="el-icon-refresh-right" @click="reset">重置</el-button>
    </el-form-item>
   </el-form>
</template>

<style lang="scss" scoped>
</style>
