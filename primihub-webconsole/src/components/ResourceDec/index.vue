<template>
  <el-descriptions :column="1" border label-class-name="detail-title" class="resource-data">
    <el-descriptions-item label="资源名称">{{ data.resourceName }}</el-descriptions-item>
    <el-descriptions-item label="资源id">{{ data.resourceId }}</el-descriptions-item>
    <el-descriptions-item label="特征量">{{ data.resourceRowsCount }}</el-descriptions-item>
    <el-descriptions-item label="样本量">{{ data.resourceColumnCount }}</el-descriptions-item>
    <el-descriptions-item label="是否包含Y值">{{ data.resourceContainsY === 1? '是': '否' }}</el-descriptions-item>
  </el-descriptions>
</template>

<script>
export default {
  props: {
    data: {
      type: Object,
      default: () => {}
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .detail-title{
  width: 100px;
}
::v-deep .el-descriptions .is-bordered .el-descriptions-item__cell{
  padding: 5px!important;
}
.resource-data{
  font-size: 12px;
  margin-top: 10px;
}
</style>
