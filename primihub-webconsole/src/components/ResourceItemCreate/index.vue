<template>
  <div class="resource-item" @click="handleClick">
    <div class="btn-box"><span class="el-icon-search" /> <span>选择资源</span></div>
  </div>
</template>

<script>
export default {
  name: 'ResourceItemCreate',
  props: {
    type: {
      type: String,
      default: ''
    }
  },
  methods: {
    handleClick() {
      this.$emit('click')
    }

  }
}
</script>

<style lang="scss" scoped>
.resource-item {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ccc;
  box-sizing: border-box;
  border-radius: 5px;
  font-size: 14px;
  border: 1px solid #e5e5e5;
  width: 230px;
  height: 186px;
  &:hover {
    box-shadow: 2px 4px 8px rgba(0,0,0,.05);
  }
  .btn-box {
    font-size: 20px;
    text-align: center;
  }
}
</style>
