<template>
  <div class="resource-item" @click="handleClick">
    <div class="heading">
      <span>{{ data.modelName }}</span>
      <!-- <span class="icon el-icon-close" /> -->
    </div>
    <div class="main">
      <div class="info-item">
        <span class="label">模板：</span>
        <span class="context">{{ data.modelType | modelTypeFilter }}</span>
      </div>
      <div class="info-item">
        <span class="label">描述：</span>
        <span class="context">{{ data.modelDesc }}</span>
      </div>
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
    }
  },
  methods: {
    handleClick() {
      this.$emit('click', this.data.modelId)
    }
  }
}
</script>

<style lang="scss" scoped>
  .resource-item{
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    border: 1px solid #ccc;
    box-sizing: border-box;
    padding: 10px;
    padding-right: 10px;
    border-radius: 5px;
    font-size: 14px;
    border: 1px solid #e5e5e5;
    width: 200px;
    height: 100px;
    cursor: pointer;
    &:hover {
      box-shadow: 2px 4px 8px rgba(0,0,0,.05);
    }
    .heading {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-shrink: 0;
      width: 100%;
      height: 30px;
      line-height: 30px;
      font-size: 16px;
      font-weight: 500;
      span{
        width: 90%;
        display: block;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
      }
      .icon {
        font-weight: bold;
        color: #bbbbbb;
        &:hover {
          color: #666666;
        }
      }
    }
    .main {
      margin-top: 5px;
      width: 100%;
      flex-grow: 1;
      font-size: 12px;
      .info-item {
        height: 25px;
        line-height: 25px;
        display: flex;
        .label {
          text-align: right;
          width:40px;
          flex-shrink: 0;
        }
        .context {
          flex-grow: 1;
          overflow: hidden;
          // -webkit-line-clamp: 2;
          white-space: nowrap;
          text-overflow: ellipsis;
        }
      }
    }
  }
</style>
