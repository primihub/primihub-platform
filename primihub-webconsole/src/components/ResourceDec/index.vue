<template>
  <el-descriptions :column="1" border label-class-name="detail-title" class="resource-data">
    <el-descriptions-item label="资源名称">{{ data.resourceName }}</el-descriptions-item>
    <el-descriptions-item label="资源id">{{ data.resourceId }}</el-descriptions-item>
    <el-descriptions-item label="特征量">{{ data.resourceColumnCount || data.fileColumns }}</el-descriptions-item>
    <el-descriptions-item label="样本量">{{ data.resourceRowsCount || data.fileRows }}</el-descriptions-item>
    <el-descriptions-item label="是否包含Y值">{{ data.resourceContainsY === 1? '是': '否' }}</el-descriptions-item>
    <el-descriptions-item label="选择特征">
      <el-checkbox-group v-model="calculationField" @change="handleCheckedChange">
        <el-checkbox v-for="(item,index) in data.fileHandleField" :key="index" :disabled="item === 'y'" :label="item" />
      </el-checkbox-group>
    </el-descriptions-item>
  </el-descriptions>
</template>

<script>
export default {
  props: {
    data: {
      type: Object,
      default: () => {}
    },
    disabled: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      calculationField: this.data.calculationField ? this.data.calculationField : []
    }
  },
  watch: {
    'data.resourceId'(newVal) {
      if (newVal) {
        this.calculationField = this.data.fileHandleField
        if (this.data.fileHandleField.includes('y')) {
          this.addY()
        }
      }
    }
  },
  created() {
    if (this.data.fileHandleField.includes('y')) {
      this.addY()
    }
  },
  methods: {
    addY() {
      if (this.data.fileHandleField.includes('y') && !this.calculationField.includes('y')) {
        this.calculationField.push('y')
      }
    },
    handleCheckedChange(value) {
      this.data.calculationField = this.calculationField
      this.$emit('change', this.data)
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .detail-title{
  width: 80px!important;
}
::v-deep .el-radio{
  margin: 2px 6px;
}
::v-deep .el-radio__inner{
  width: 10px;
  height: 10px;
}
::v-deep .el-radio__label{
  font-size: 12px!important;
}
::v-deep .el-descriptions .is-bordered .el-descriptions-item__cell{
  padding:2px 5px!important;
}
.resource-data{
  font-size: 12px;
  margin-top: 10px;
}
</style>
