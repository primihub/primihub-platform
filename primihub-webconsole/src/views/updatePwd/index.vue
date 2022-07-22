<template>
  <div class="container">
    <div class="body">
      <div class="poster-wrap">
        <Poster />
      </div>
      <div class="register-wrap">
        <UpdatePwdForm />
      </div>
    </div>
  </div>
</template>

<script>
import Poster from '@/components/Poster'
import UpdatePwdForm from '@/components/UpdatePwdForm'

export default {
  name: 'Register',
  components: {
    UpdatePwdForm,
    Poster
  },
  data() {
    return {
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-notification__content{
  display: block;
  width: 300px;
  background-color: #1989FA;
  color: #fff;
}
.container {
  width: 100vw;
  height: 100vh;
  background-image: url("/images/login-bg.jpg");
  background-position: center;
  background-size: cover;
  background-repeat: no-repeat;
  .body{
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate3d(-50%,-50%,0);
  }
  .poster-wrap {
    flex-grow: 1;
    flex-shrink: 0;
    // width: 50vw;
    // height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .register-wrap {
    flex-grow: 1;
    flex-shrink: 0;
    // width: 50vw;
    margin-top: 30px;
    position: relative;
    background-color: #fff;
    border-radius: 30px;
    padding: 64px;
    box-shadow: 0 16px 32px 0 rgb(0 0 0 / 8%);
  }
}
</style>
