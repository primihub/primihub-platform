<template>
  <div v-if="logo" class="intro">
    <h1><img class="logo" :src="logo" alt="logo"></h1>
    <div v-html="introduction" />
  </div>
</template>

<script>
import defaultSetting from '@/settings'
import { mapState } from 'vuex'

export default {
  computed: {
    introduction() {
      return this.loginDescription ? this.loginDescription : defaultSetting.introduction
    },
    logo() {
      return this.loginLogoUrl ? this.loginLogoUrl : '/images/logo-white.png'
    },
    ...mapState('settings', ['logoUrl', 'loginDescription', 'loginLogoUrl'])
  }
}
</script>
<style lang="scss" scoped>
.intro{
  width: 50%;
  color: #fff;
  padding: 70px 90px 30px 90px;
  text-align: left;
  line-height: 1.5;
  border-top-left-radius: 10px;
  border-bottom-left-radius: 10px;
  background: #1a69f9 url('../../assets/login-bg.png') left bottom no-repeat;
  p{
    font-size: 14px;
  }
  .logo{
    width: 200px;
  }
}
</style>
