<template>
  <div v-if="!isHideFadeBack" class="feedback">
    <el-popover
      placement="left"
      width="100"
      trigger="hover"
    >
      <div style="text-align: center;">
        <img src="../../assets/assitant.png" width="100%">
        <p>原语科技小助手</p>
      </div>
      <div slot="reference" class="contact-icon"><svg-icon icon-class="contact" /></div>
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

<style lang="scss" scoped>
::v-deep .svg-icon{
  width: 25px!important;
  height: 25px!important;
  vertical-align: middle;
  color: #fff;
}
.feedback{
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #fff;
  position: fixed;
  right: 5px;
  z-index: 100;
  bottom: 20%;
  text-align: center;
  box-shadow: 5px 5px 5px rgba($color: #000000, $alpha: 0.1);
  background-color: #1677FF;
  .text{
    font-size: 12px;
    color: #333;
  }
  .contact-icon{
    cursor: pointer;
  }
}
</style>
