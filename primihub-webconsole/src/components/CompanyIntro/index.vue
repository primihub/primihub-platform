<template>
  <div class="intro">
    <h1><img v-if="loginLogoUrl" class="logo" :src="loginLogoUrl" alt="logo"></h1>
    <div v-if="loginDescription !== ''" v-html="loginDescription" />
    <div v-else>
      <p>PrimiHub是基于多方安全计算、联邦学习、同态加密等主流隐私计算技术自主研发的分布式隐私计算平台。</p><p>平台涵盖隐私安全求交、联合建模、联合推理、联合统计、匿踪查询、数据资源管理、算法容器管理等能力，致力打造适配全业务场景且安全高效的隐私计算平台。</p>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  computed: {
    ...mapState('settings', ['loginDescription', 'loginLogoUrl'])
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
  .logo{
    width: 200px;
  }
}
</style>
