<template>
  <el-descriptions :column="1" border label-class-name="detail-title" class="resource-data">
    <el-descriptions-item label="资源名称">{{ data.resourceName }}</el-descriptions-item>
    <el-descriptions-item label="资源id">{{ data.resourceId }}</el-descriptions-item>
    <el-descriptions-item label="特征量">{{ data.resourceColumnCount || data.fileColumns }}</el-descriptions-item>
    <el-descriptions-item label="样本量">{{ data.resourceRowsCount || data.fileRows }}</el-descriptions-item>
    <el-descriptions-item label="是否包含Y值">{{ data.resourceContainsY === 1? '是': '否' }}</el-descriptions-item>
    <!-- <el-descriptions-item label="选择特征">
      <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" @change="handleCheckAllChange">全选</el-checkbox>
      <el-checkbox-group v-model="data.calculationField" @change="handleCheckedChange">
        <el-checkbox v-for="(item,index) in data.fileHandleField" :key="index" :label="item" />
      </el-checkbox-group>
    </el-descriptions-item> -->
    <el-descriptions-item label="选择标签列Y">
      <el-radio-group v-model="data.calculationField" @change="handleCheckedChange">
        <el-radio v-for="(item,index) in data.fileHandleField" :key="index" :disabled="disabled" :label="item">{{ item }}</el-radio>
      </el-radio-group>
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
      calculationField: this.data.calculationField,
      checkAll: false,
      isIndeterminate: false
    }
  },
  // watch: {
  //   data(val) {
  //     if (!val.calculationField) {
  //       this.data.calculationField = this.data.fileHandleField[0]
  //     }
  //   }
  // },
  created() {
    // this.init()
  },
  methods: {
    init() {
      if (!this.data.calculationField) {
        this.data.calculationField = this.data.fileHandleField[0]
      }
    },
    // handleCheckAllChange(val) {
    //   this.data.calculationField = val ? this.data.fileHandleField : []
    //   this.isIndeterminate = false
    //   this.$emit('change', this.data)
    // },
    // handleCheckedChange(value) {
    //   console.log(value)
    //   const checkedCount = value.length
    //   this.checkAll = checkedCount === this.data.fileHandleField.length
    //   this.isIndeterminate = checkedCount > 0 && checkedCount < this.data.fileHandleField.length
    //   this.$emit('change', this.data)
    // },
    handleCheckedChange(value) {
      this.data.calculationField = value
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
