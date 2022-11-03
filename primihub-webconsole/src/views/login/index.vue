<template>
  <div class="login-container">
    <div class="body">
      <div class="login-wrap">
        <CompanyIntro />
        <div class="login-form">
          <el-form ref="loginForm" :model="loginForm" :rules="loginRules" auto-complete="on" label-position="left">
            <div class="title-container">
              <h3 class="title">登录</h3>
            </div>

            <el-form-item prop="username">
              <span class="svg-container">
                <svg-icon icon-class="user" />
              </span>
              <el-input
                ref="username"
                v-model="loginForm.username"
                placeholder="请输入手机号/邮箱/用户名"
                name="username"
                type="text"
                tabindex="1"
                auto-complete="on"
              />
            </el-form-item>

            <el-form-item prop="password">
              <span class="svg-container">
                <svg-icon icon-class="password" />
              </span>
              <el-input
                :key="passwordType"
                ref="password"
                v-model="loginForm.password"
                :type="passwordType"
                placeholder="请输入密码"
                name="password"
                tabindex="2"
                auto-complete="on"
                @keyup.enter.native="handleLogin"
              />
              <span class="show-pwd" @click="showPwd">
                <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
              </span>
            </el-form-item>
            <el-button type="primary" style="width:100%;margin-bottom:10px;" :disabled="publicKeyData.publicKey === ''" @click.native.prevent="handleLogin">登录</el-button>
          </el-form>
          <div class="forgot">
            <el-link type="primary" @click.stop="toRegister">立即注册</el-link> |
            <el-link type="primary" @click.stop="forgotPwd">忘记密码？</el-link>
          </div>
          <div v-if="authList.length > 0" class="login-type">
            <p>其他登录方式</p>
            <div class="login-icon">
              <span v-for="item in authList" :key="item.name" @click="toLoginPage(item)">
                <span class="icon iconfont" :class="item.icon" />
              </span>
            </div>
          </div>
        </div>
      </div>
      <Footer />
    </div>

    <Verify ref="verify" @success="handleSuccess" @close="handleClose" />
  </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex'
import { getValidatePublicKey, getAuthList } from '@/api/user'
import JSEncrypt from 'jsencrypt'
import Verify from '@/components/Verifition'
import Footer from '@/components/Footer'
import CompanyIntro from '@/components/CompanyIntro'

export default {
  name: 'Login',
  components: {
    Verify,
    Footer,
    CompanyIntro
  },
  data() {
    const validatePassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入密码'))
      } else if (value.length < 6 || value.length > 20) {
        callback(new Error('密码长度在8-20个字符以内'))
      } else {
        callback()
      }
    }
    return {
      publicKeyData: {},
      loginForm: {
        username: '',
        password: '',
        captchaVerification: ''
      },
      loginRules: {
        username: [{ required: true, trigger: 'blur', message: '请输入手机号/邮箱/用户名' }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }]
      },
      passwordType: 'password',
      redirect: undefined,
      showVerify: false,
      authList: []
    }
  },
  computed: {
    ...mapState('user', ['showValidation'])
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    },
    showValidation(newVal) {
      if (newVal) {
        this.showValidationBox()
      }
    }
  },
  async mounted() {
    await this.getAuthList()
  },
  methods: {
    toLoginPage(item) {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      window.open(`${process.env.VUE_APP_BASE_API}/sys/oauth/${item.name}/render?timestamp=${timestamp}&nonce=${nonce}`)
    },
    showValidationBox() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.$refs.verify.show()
        }
      })
    },
    handleSuccess(data) {
      if (data !== '') {
        this.loginForm.captchaVerification = data.captchaVerification
        this.handleLogin()
      }
    },
    handleClose() {
      this.SET_SHOW_VALIDATION(false)
    },
    handleFail() {
      console.log('handleFail')
      this.$refs.verify.closeBox()
    },
    forgotPwd() {
      this.$router.push({
        path: '/forgotPwd',
        replace: true
      })
    },
    toRegister() {
      this.$router.push({
        path: '/register',
        replace: true
      })
    },
    async getValidatePublicKey() {
      const { result = {}} = await getValidatePublicKey()
      const { publicKey, publicKeyName } = result
      this.publicKeyData = { publicKey, publicKeyName }
    },
    async getAuthList() {
      const { result = {}} = await getAuthList()
      this.authList = result.map(item => {
        return {
          name: item,
          icon: 'icon-' + item
        }
      })
    },
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = ''
      } else {
        this.passwordType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.password.focus()
      })
    },
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (valid) {
          await this.getValidatePublicKey()
          const { publicKey, publicKeyName } = this.publicKeyData
          if (!publicKey) {
            this.$message({
              message: '请求异常，请刷新后稍后重试',
              type: 'error'
            })
            return
          }
          // console.log('getValidatePublicKey', result)
          const crypt = new JSEncrypt()
          crypt.setKey(publicKey)
          const { username: userAccount, password, captchaVerification } = this.loginForm
          const userPassword = crypt.encrypt(password)
          const loginForm = { userAccount, userPassword, validateKeyName: publicKeyName, captchaVerification }

          this.$store.dispatch('user/login', loginForm).then(res => {
            console.log('code', res)
            this.$router.push({ path: this.redirect || '/' })
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    ...mapMutations('user', ['SET_SHOW_VALIDATION'])
  }
}
</script>

<style lang="scss" scoped>
$bg:#2d3a4b;
$dark_gray:#889aa4;
$light_gray:#000;
$cursor: #000;

::v-deep .el-input {
  display: inline-block;
  height: 47px;
  width: 85%;

  input {
    background: transparent;
    border: 0px;
    -webkit-appearance: none;
    border-radius: 0px;
    padding: 12px 5px 12px 15px;
    color: $light_gray;
    height: 47px;
    caret-color: $cursor;

    &:-webkit-autofill {
      box-shadow: 0 0 0px 1000px #fff inset !important;
      -webkit-text-fill-color: $cursor !important;
    }
  }
}
.login-container {
  width: 100vw;
  height: 100vh;
  background-image: url("/images/bg.png");
  background-position: center;
  background-size: cover;
  background-repeat: no-repeat;
  text-align: center;
  position: relative;
  overflow: hidden;

  .el-form-item {
    border: 1px solid rgba(0, 0, 0, 0.1);
    /* background: rgba(0, 0, 0, 0.1); */
    border-radius: 5px;
    color: #454545;
  }
  .login-form {
    width: 50%;
    padding: 80px 59px;
    max-width: 100%;
    margin: 0 auto;
    overflow: hidden;
  }
  .body{
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate3d(-50%,-50%,0);
  }
  .tips {
    font-size: 14px;
    color: #fff;
    margin-bottom: 10px;

    span {
      &:first-of-type {
        margin-right: 16px;
      }
    }
  }

  .svg-container {
    // padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
  }

  .title-container {
    position: relative;

    .title {
      font-size: 26px;
      color: $light_gray;
      margin: 0px auto 40px auto;
      text-align: left;
      font-weight: bold;
    }
  }

  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
  }
  .poster-wrap {
    .slogan {
      font-size: 22px;
      color: rgba(0, 0, 0, 0.45);
    }
  }
  .login-wrap {
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
  .login-type{
    color: #999999;
    font-size: 14px;
    margin-top: 20px;
    .icon{
      display: inline-block;
      font-size: 30px;
      margin: 10px;
    }
  }
  .forgot {
    color: #1989FA;
    font-size: 14px;
    text-align: right;
    padding-bottom:10px;
  }
}
</style>
