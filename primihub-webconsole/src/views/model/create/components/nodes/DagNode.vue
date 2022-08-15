<template>
  <div class="dag-container">
    <div :class="['node', status]">
      <img :src="image.logo">
      <span class="require">
        <i v-if="isMandatory === 0"> * </i>
      </span>
      <span class="label">{{ label }}</span>
      <span class="status">
        <img v-show="status === 'success'" :src="image.success">
        <img v-show="status === 'failed'" :src="image.failed">
        <img v-show="status === 'running'" :src="image.running">
      </span>
    </div>
  </div>
</template>

<script>
const image = {
  logo: 'https://gw.alipayobjects.com/mdn/rms_43231b/afts/img/A*evDjT5vjkX0AAAAAAAAAAAAAARQnAQ',
  success:
    'https://gw.alipayobjects.com/mdn/rms_43231b/afts/img/A*6l60T6h8TTQAAAAAAAAAAAAAARQnAQ',
  failed:
    'https://gw.alipayobjects.com/mdn/rms_43231b/afts/img/A*SEISQ6My-HoAAAAAAAAAAAAAARQnAQ',
  running:
    'https://gw.alipayobjects.com/mdn/rms_43231b/afts/img/A*t8fURKfgSOgAAAAAAAAAAAAAARQnAQ'
}

export default {
  inject: ['getGraph', 'getNode'],
  data: () => ({
    image,
    id: '',
    complete: false,
    label: '',
    isMandatory: 0, // 0必选 1非必选
    componentState: 0
  }),
  computed: {
    status() {
      return this.componentState === 1 ? 'success' : this.componentState === 3 ? 'failed' : this.componentState === 2 ? 'running' : 'default'
    }
  },
  mounted() {
    const node = this.getNode()
    const { data } = node
    const { componentName = '', complete = '', isMandatory, componentState = 0 } = data
    this.label = componentName
    this.complete = complete
    this.isMandatory = isMandatory
    this.componentState = componentState
    // console.log('node', node)
    // 监听数据改变事件
    node.on('change:data', ({ current }) => {
      const { componentName, complete = false, isMandatory, componentState = 0 } = current
      this.label = componentName
      this.complete = complete
      this.isMandatory = isMandatory
      this.componentState = componentState
    })
  }

}
</script>

<style lang="scss" scoped>
.dag-container {
  height: 100%;
}
.node {
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #fff;
  border: 1px solid #c2c8d5;
  border-left: 4px solid #5F95FF;
  border-radius: 4px;
  box-shadow: 0 2px 5px 1px rgba(0, 0, 0, 0.06);
  justify-content: flex-start;
}
.node-text{
  width: 95px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}
.node img {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  margin-left: 8px;
}
.node .label {
  width: 90px;
  font-size: 14px;
  text-align: center;
}
.node .status {
  flex-shrink: 0;
}
.node.success {
  border-left: 4px solid #52c41a;
}
.node.failed {
  border-left: 4px solid #ff4d4f;
}
.node.running .status img {
  animation: spin 1s linear infinite;
}
.x6-node-selected .node {
  border-color: #1890ff;
  border-radius: 2px;
  box-shadow: 0 0 0 4px #d4e8fe;
}
.x6-node-selected .node.success {
  border-color: #52c41a;
  border-radius: 2px;
  box-shadow: 0 0 0 4px #ccecc0;
}
.x6-node-selected .node.failed {
  border-color: #ff4d4f;
  border-radius: 2px;
  box-shadow: 0 0 0 4px #fedcdc;
}
.x6-edge:hover path:nth-child(2){
  stroke: #1890ff;
  stroke-width: 1px;
}

.x6-edge-selected path:nth-child(2){
  stroke: #1890ff;
  stroke-width: 1.5px !important;
}
.require{
  color: red;
  margin-left: 10px;
  font-size: 20px;
  line-height: 1;
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
