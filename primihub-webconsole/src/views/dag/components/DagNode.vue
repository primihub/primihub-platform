<template>
  <div class="dag-container">
    <div :class="['node', status]">
      <img :src="image.logo">
      <span class="label">{{ label }}</span>
      <span class="status">
        <img v-show="status === 'success'" :src="image.success">
        <img v-show="status === 'failed'" :src="image.failed">
        <!-- <img v-show="status === 'running'" :src="image.running"> -->
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
    status: '',
    label: '23'
  }),
  mounted() {
    const node = this.getNode()
    const { data } = node
    const { label = '', status = '' } = data
    this.label = label
    this.status = status
    // console.log('node', node)
    // 监听数据改变事件
    node.on('change:data', ({ current }) => {
      // console.log({ data })
      const { label, status = 'default' } = current
      this.label = label
      this.status = status
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
}
.node img {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  margin-left: 8px;
}
.node .label {
  display: inline-block;
  flex-shrink: 0;
  width: 104px;
  margin-left: 8px;
  color: #666;
  font-size: 12px;
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

</style>
