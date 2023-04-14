<template>
  <div class="container">
    <div class="body">
      <div class="bd-wrap">
        <CompanyIntro />
        <div class="register-wrap">
          <RegisterForm size="mini" :code-type="1" />
        </div>
      </div>
      <Footer />
    </div>
  </div>
</template>

<script>
import RegisterForm from '@/components/RegisterForm'
import Footer from '@/components/Footer'
import CompanyIntro from '@/components/CompanyIntro'

export default {
  name: 'Register',
  components: {
    RegisterForm,
    Footer,
    CompanyIntro
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
::v-deep .el-form-item{
  margin-bottom: 14px;
}
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
    width: 50%;
    padding: 60px 59px;
    max-width: 100%;
    margin: 0 auto;
    overflow: hidden;
  }
  .bd-wrap {
    display: flex;
    flex-grow: 1;
    flex-shrink: 0;
    position: relative;
    background-color: #fff;
    border-radius: 10px;
    // padding: 64px;
    box-shadow: 0 16px 32px 0 rgb(0 0 0 / 8%);
    margin-top: 30px;
    width: 1000px;
    height: 500px;
  }
}
</style>
