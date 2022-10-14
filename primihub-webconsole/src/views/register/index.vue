<template>
  <div class="container">
    <div class="body">
      <div class="poster-wrap">
        <Poster />
      </div>
      <div class="register-wrap">
        <RegisterForm :code-type="1" />
      </div>
      <Footer />
    </div>
  </div>
</template>

<script>
import Poster from '@/components/Poster'
import RegisterForm from '@/components/RegisterForm'
import Footer from '@/components/Footer'

export default {
  name: 'Register',
  components: {
    RegisterForm,
    Poster,
    Footer
  },
  data() {
    return {
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  async mounted() {

  },
  methods: {

  }
}
</script>

<style lang="scss" scoped>
.container {
  width: 100vw;
  height: 100vh;
  background-image: url("/images/login-bg.jpg");
  background-position: center;
  background-size: cover;
  background-repeat: no-repeat;
  text-align: center;
  .body{
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate3d(-50%,-50%,0);
  }
  .register-wrap {
    position: relative;
    background-color: #fff;
    border-radius: 30px;
    padding: 34px 64px 20px 64px;
    box-shadow: 0 16px 32px 0 rgb(0 0 0 / 8%);
    margin-top: 30px;
  }
}
</style>
