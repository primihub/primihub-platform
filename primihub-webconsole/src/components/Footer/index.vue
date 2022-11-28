<template>
  <div v-if="!$store.state.settings.isHideFooterVersion" class="footer">
    <p>{{ $store.state.settings.footerText }}</p>
  </div>
</template>

<script>
export default {
  name: 'PageFooter',
  async created() {
    await this.$store.dispatch('settings/getHomepage')
  }
}
</script>

<style lang="scss" scoped>
.footer{
  font-size: 14px;
  color: #999;
  position: absolute;
  bottom: -50px;
  left: 0;
  right: 0;
  text-align: center;
}
</style>
