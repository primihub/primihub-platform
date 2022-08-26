<template>
  <div class="resource-item">
    <span v-if="showClose" class="icon el-icon-close" @click="handleDelete" />
    <div class="heading">
      <span class="heading-title">{{ data.resourceName }}</span>
      <el-tag v-if="data.isAuthed !== undefined" size="mini" :type="data.isAuthed === 1? 'success':data.isAuthed === 2?'danger':'info'">{{ data.isAuthed | authStatusFilter }}</el-tag>
    </div>
    <div class="main">
      <div class="info-item"><span class="label">是否包含Y值：</span><span class="context">{{ data.resourceContainsY === 1? '是' : '否' }}</span></div>
      <div class="info-item"><span class="label">特征量：</span><span class="context">{{ data.resourceColumnCount || 0 }}</span></div>
      <div class="info-item"><span class="label">样本量：</span><span class="context">{{ data.resourceRowsCount || 0 }}</span></div>
      <div class="info-item"><span class="label">正例样本数量：</span><span class="context">{{ data.resourceYRowsCount || 0 }}</span></div>
      <div class="info-item"><span class="label">正例样本比例：</span><span class="context">{{ data.resourceYRatio || 0 }}%</span></div>
      <div class="info-item"><span class="label">创建时间：</span><span class="context">{{ data.createDate }}</span></div>
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
  .resource-item{
    display: flex;
    flex-direction: column;
    border: 1px solid #ccc;
    box-sizing: border-box;
    padding: 10px 15px 12px;
    border-radius: 5px;
    font-size: 14px;
    border: 1px solid #e5e5e5;
    width: 230px;
    // height: 100px;
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
      // flex-grow: 1;
      font-size: 12px;
      .info-item {
        height: 20px;
        line-height: 20px;
        display: flex;
        .label {
          text-align: right;
          flex-shrink: 0;
        }
      }
    }
  }
</style>
