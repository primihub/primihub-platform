<template>
  <div v-if="!isHideFadeBack" class="feedback">
    <el-popover
      placement="left"
      width="100"
      trigger="hover"
    >
      <div style="text-align: center;">
        <img src="https://primihub.com/img/qrcode1.78d466ad.png" width="100%">
        <p>原语科技小助手</p>
      </div>
      <el-link slot="reference" :underline="false" class="text"><img src="../../assets/logo2.png" alt="" srcset=""><p>建议与反馈</p></el-link>
    </el-popover>
  </div>
</template>

<script>
export default {
  computed: {
    isHideFadeBack() {
      return this.$store.state.settings.isHideFadeBack
    }

  }
}
</script>

<style lang="scss">
.feedback{
  display: flex;
  align-items: center;
  justify-content: center;
  width: 75px;
  height: 75px;
  border-radius: 50%;
  background-color: #fff;
  position: fixed;
  right: 0;
  z-index: 100;
  bottom: 20%;
  text-align: center;
  box-shadow: 5px 5px 5px rgba($color: #000000, $alpha: 0.1);
  .text{
    font-size: 12px;
    color: #333;
  }
}
</style>
