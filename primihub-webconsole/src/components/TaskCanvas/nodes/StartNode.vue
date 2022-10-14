<template>
  <div class="dag-container">
    <div class="start-node">
      <span class="start-node-icon"><svg t="1665382885868" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3518" width="24" height="24"><path d="M508.5 66.7C260.4 66.7 59.2 267.8 59.2 516s201.1 449.3 449.3 449.3S957.8 764.2 957.8 516 756.6 66.7 508.5 66.7z m268.2 717.4c-34.9 34.9-75.4 62.2-120.6 81.3-46.7 19.8-96.4 29.8-147.6 29.8s-100.9-10-147.6-29.8c-45.2-19.1-85.7-46.5-120.6-81.3-34.9-34.9-62.2-75.4-81.3-120.6-19.8-46.7-29.8-96.4-29.8-147.6s10-100.9 29.8-147.6c19.1-45.2 46.5-85.7 81.3-120.6 34.9-34.9 75.4-62.2 120.6-81.3 46.7-19.8 96.4-29.8 147.6-29.8s100.9 10 147.6 29.8c45.2 19.1 85.7 46.5 120.6 81.3 34.9 34.9 62.2 75.4 81.3 120.6 19.8 46.7 29.8 96.4 29.8 147.6s-10 100.9-29.8 147.6c-19.1 45.2-46.5 85.8-81.3 120.6z" p-id="3519" fill="#00a387" /><path d="M686 484.8L469.8 308.6c-10.5-8.5-24.9-10.3-37.1-4.5-12.2 5.8-20 18.1-20 31.6v352.4c0 13.5 7.8 25.8 20 31.6 4.8 2.3 9.9 3.4 15 3.4 7.9 0 15.7-2.7 22.1-7.9L686 539c8.2-6.6 12.9-16.6 12.9-27.1s-4.7-20.5-12.9-27.1zM482.7 614.4v-205l125.8 102.5-125.8 102.5z" p-id="3520" fill="#00a387" /></svg></span>
      <span class="start-node-label">{{ labelText }}</span>
    </div>
    <div v-if="showTime" class="time"><i class="icon el-icon-time" /><span class="time-text">耗时：{{ timeConsuming | timeFilter }}</span></div>
  </div>
</template>

<script>
export default {
  inject: ['getGraph', 'getNode'],
  data: () => ({
    labelText: '',
    timeConsuming: 0, // 组件耗时
    showTime: false
  }),
  mounted() {
    const node = this.getNode()
    const { data } = node
    this.labelText = data.componentName
    this.showTime = data.showTime
  }

}
</script>

<style lang="scss" scoped>
.dag-container {
  height: 40px;
}
.start-node {
  color: #00a387;
  font-weight: bold;
  display: flex;
  padding: 8px 0 0 30px;
  width: 120px;
  height: 40px;
  line-height: 1;
  border-radius: 30px;
  box-shadow: 0 2px 5px 1px rgba(0, 0, 0, 0.06);
}
.start-node-icon{
  margin-right: 5px;
  vertical-align: middle;
}
.start-node-label{
  height: 24px;
  vertical-align: middle;
  line-height: 24px;
}
.time{
  position: absolute;
  font-size: 12px;
  width: 120px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  left: 170px;
  top: 16px;
  background-color: #fff;
  .icon{
    color: #1890ff;
    margin-right: 5px;
  }
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
