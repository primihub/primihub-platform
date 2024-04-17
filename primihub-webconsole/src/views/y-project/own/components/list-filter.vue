<script>
import { getAvailableOrganList } from '@/api/center'
export default {
  name: 'YOwnResourceListFilter',
  data() {
    return {
      organList: [],
      query: {
        projectName: '',
        organId: '',
        createDate: []
      }
    }
  },
  created() {
    this.getAvailableOrganListFunc()
  },
  methods: {
    getAvailableOrganListFunc() {
      getAvailableOrganList().then(res => {
        this.organList = res.result
      })
    },

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
    <el-form-item label="项目名称" prop="projectName">
      <el-input v-model="query.projectName" size="small" placeholder="请输入资源名称" />
    </el-form-item>
    <el-form-item label="合作方" prop="organId">
      <el-select v-model="query.organId" size="small" placeholder="请选择" clearable>
        <el-option
          v-for="item in organList"
          :key="item.globalId"
          :label="item.globalName"
          :value="item.globalId"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="创建时间" prop="createDate">
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
