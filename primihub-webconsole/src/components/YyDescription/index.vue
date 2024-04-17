<script>
export default {
  name: 'YYText',
  props: {
    label: {
      type: String,
      default: ''
    },
    width: {
      type: String,
      default: ''
    }
  }
}
</script>

<template>
  <div class="yy-text">
    <label class="yy-text--label" :style="{width: `${width}px` || '110px'}">{{ label }}</label>
    <p class="yy-text--value" :style="{width: `calc(100% - ${width}px)` || 'calc(100% - 120px)'}"><slot /></p>
  </div>
</template>

<style lang="scss" scoped>
.yy-text{
  display: flex;
  align-items: baseline;
  width: 100%;
  margin-bottom: 26px;
  font-size: 14px;
}
.yy-text--label{
  width: 110px;
  margin-right: 5px;
  text-align: right;
  color: #666;
}
.yy-text--value{
  width: calc(100% - 120px);
  word-break: break-all;
  color: #333;
}
</style>
