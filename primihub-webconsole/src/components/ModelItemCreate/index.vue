<template>
  <div class="model-item">
    <div class="btn-box"><span class="el-icon-circle-plus-outline" /> <span>添加模型</span></div>
  </div>
</template>

<script>
export default {
  name: 'ModelItemCreate',
  props: {
    type: {
      type: String,
      default: ''
    }
  },
  methods: {
  }
}
</script>

<style lang="scss" scoped>
.model-item {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ccc;
  box-sizing: border-box;
  border-radius: 5px;
  font-size: 14px;
  border: 1px solid #e5e5e5;
  width: 200px;
  height: 100px;
  &:hover {
    box-shadow: 2px 4px 8px rgba(0,0,0,.05);
  }
  .btn-box {
    font-size: 18px;
    text-align: center;
  }
}
</style>
