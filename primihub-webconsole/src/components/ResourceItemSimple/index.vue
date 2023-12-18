<template>
  <div class="resource-item">
    <div class="heading">
      <span class="heading-title">{{ data.resourceName }}</span>
      <el-tag v-if="data.isAuthed !== undefined" size="mini" :type="data.isAuthed === 1? 'success':data. isAuthed === 2?'danger':'info'">{{ data.isAuthed | authStatusFilter }}</el-tag>
      <span v-if="showClose" class="icon el-icon-close" @click="handleDelete" />
    </div>
    <div class="main">
      <el-descriptions :column="2">
        <!-- <el-descriptions-item label-class-name="label-item" label="是否包含Y值">{{ data.resourceContainsY === 1? '是' : '否' }}</el-descriptions-item> -->
        <el-descriptions-item label="特征量">{{ data.resourceColumnCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="样本量">{{ data.resourceRowsCount || 0 }}</el-descriptions-item>
        <!-- <el-descriptions-item label="正例样本数量">{{ data.resourceYRowsCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="正例样本比例">{{ data.resourceYRatio || 0 }}%</el-descriptions-item> -->
        <el-descriptions-item label="创建时间">{{ data.createDate }}</el-descriptions-item>
        <el-descriptions-item label="资源描述">{{ data.resourceDesc }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ResourceItemSimple',
  props: {
    type: {
      type: String,
      default: ''
    },
    data: {
      type: Object,
      default: () => {
        return {}
      }
    },
    showClose: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    handleDelete() {
      this.$emit('delete', {
        id: this.data.resourceId
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
::v-deep .el-descriptions :not(.is-bordered) .el-descriptions-item__cell{
  padding-bottom: 0px;
}
  .resource-item{
    border: 1px solid #ccc;
    box-sizing: border-box;
    padding: 10px 15px 12px;
    border-radius: 5px;
    font-size: 14px;
    border: 1px solid #e5e5e5;
    cursor: pointer;
    &:hover {
      box-shadow: 2px 4px 8px rgba(0,0,0,.05);
    }
    .icon{
      text-align: right;
      font-weight: bold;
      color: #bbbbbb;
      &:hover {
        color: #666666;
      }
    }
    .heading {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-shrink: 0;
      width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-bottom: 5px;
      line-height: 1;
      height: 30px;
      &-title{
        max-width: 70%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        line-height: 1;
        font-size: 16px;
        font-weight: bold;
        overflow: hidden;
        color: $mainColor;
        }
      font-size: 16px;
      font-weight: 500;
    }
    .main {
      margin-top: 5px;
      width: 100%;
      display: flex;
      flex-grow: 1;
      font-size: 12px;
      .info-item {
        height: 20px;
        line-height: 20px;
        display: flex;
        width: calc((100% - 10px) / 3);
        .label {
          text-align: right;
          flex-shrink: 0;
        }
      }
    }
  }
</style>
