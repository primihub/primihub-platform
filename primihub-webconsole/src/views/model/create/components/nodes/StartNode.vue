<template>
  <div class="dag-container">
    <div class="node">
      <i class="el-icon-video-play node-icon" />
      <span>{{ labelText }}</span>
    </div>
  </div>
</template>

<script>
export default {
  inject: ['getGraph', 'getNode'],
  data: () => ({
    labelText: ''
  }),
  mounted() {
    const node = this.getNode()
    const { data } = node
    this.labelText = data.componentName
  }

}
</script>

<style lang="scss" scoped>
.dag-container {
  height: 100%;
}
.node {
  color: #00a387;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  border: 2px solid #34e2c4;
  background: #e4fffa;
  // background-image: linear-gradient(45deg, #8ce33b,#00e3ae);
  border-radius: 30px;
  box-shadow: 0 2px 5px 1px rgba(0, 0, 0, 0.06);
}
.node-icon{
  font-size: 24px;
  margin-right: 5px;
}
@keyframes spin {
  from {
      transform: rotate(0deg);
  }
  to {
      transform: rotate(360deg);
  }
}
</style>
