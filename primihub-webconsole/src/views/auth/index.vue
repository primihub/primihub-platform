<template>
  <div class="container">
    <div v-if="!isUser" class="body">
      <div class="register-wrap">
        <div class="session-form" data-control="phone|password" style="display: block;">
          <header class="session-form__header center">
            <h3 class="session-form__title">
              <span>继续以完成第三方帐号绑定</span>
            </h3>
          </header>
          <div class="session__auth-info">
            <div class="auth-logos">
              <img class="primihub-logo" alt="primihub logo" src="/images/logo1.png">
              <i class="icon iconfont icon-exchange" />
              <div class="git-other-login-icon">
                <i class="github icon-github iconfont" />
              </div>
            </div>
            <div class="auth-tip">
              你已通过
              <b>GitHub</b>
              授权，完善以下登录信息即可完成绑定
            </div>
          </div>
          <div class="session__flash-error">
            <div class="ui message error flash_error flash_error_box" />
          </div>
          <el-form ref="form" :model="formData" :rules="formRules" auto-complete="on" label-position="left">
            <el-form-item prop="userAccount" class="user-account">
              <el-input
                ref="userAccount"
                v-model="formData.userAccount"
                placeholder="手机号"
                name="userAccount"
                type="text"
                tabindex="1"
              />
            </el-form-item>
            <el-form-item prop="verificationCode">
              <div class="code-box">
                <el-input
                  ref="verificationCode"
                  v-model="formData.verificationCode"
                  placeholder="请输入验证码"
                  name="verificationCode"
                  type="text"
                  tabindex="1"
                />
                <el-button type="primary" class="code-button" plain :disabled="sendCode" @click="sendVerificationCode">
                  <span v-if="!sendCode">获取验证码</span>
                  <span v-else>{{ time }}s后重新获取</span>
                </el-button>
              </div>
            </el-form-item>
            <el-button :loading="loading" type="primary" class="register-button" @click.native.prevent="handleSubmit">绑定并登录</el-button>
            <p class="tips"><span>已有账号？<el-button type="text" @click="toLogin">去登录</el-button></span></p>
          </el-form>

        </div>
      </div>
      <Footer />
    </div>
    <Verify ref="verify" :show-close="false" @success="handleSuccess" />
  </div>
</template>

<script>
import { param2Obj } from '@/utils/index'
import { sendVerificationCode, authRegister } from '@/api/user'
import Footer from '@/components/Footer'
import Verify from '@/components/Verifition'
const phonePattern = /^[1][\d][0-9]{9}$/

export default {
  name: 'Register',
  components: {
    Footer,
    Verify
  },
  data() {
    const validateUserAccount = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入手机号'))
      } else if (!phonePattern.test(value)) {
        callback(new Error('请输入正确的手机号'))
      } else {
        callback()
      }
    }
    return {
      redirect: undefined,
      isUser: false, // 用户是否已绑定过
      formData: {
        userAccount: '',
        verificationCode: '',
        registerType: 3,
        authPublicKey: ''
      },
      formRules: {
        userAccount: [{ required: true, trigger: 'blur', validator: validateUserAccount }],
        verificationCode: [{ required: true, message: '请输入验证码' }]
      },
      sendCode: false,
      loading: false,
      time: 60,
      timer: null
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
    const urlParams = param2Obj(window.location.href)
    this.isUser = urlParams.user === 'true'
    this.formData.authPublicKey = urlParams.authPublicKey || ''
    if (this.isUser) {
      this.$refs.verify.show()
    }
  },
  methods: {
    sendVerificationCode() {
      if (this.formData.userAccount === '') {
        this.$message({
          message: '请输入手机号',
          type: 'error'
        })
        return
      }
      /*
      method:post
      param: codeType 1 注册 2 忘记密码
      result:只有成功和状态码 状态码有以下：
      NOT_IN_THE_WHITE_LIST(113,"不在短信服务白名单中"),
      FIVE_MINUTES_LATER(114,"请在五分钟后重试"),
      ONE_MINUTES_LATER(115,"请在一分钟后重试"),
      OUTNUMBER(116,"超出当天发送条数"),
      SMS_FAILURE(117,"SMS发送失败"),
      VERIFICATION_CODE(118,"验证码失败"),
      */
      const params = {
        codeType: 1,
        cellphone: this.formData.userAccount
      }

      console.log('sendVerificationCode', params)
      sendVerificationCode(params).then(res => {
        if (res.code === 0) {
          this.sendCode = true
          this.countDown()
        } else {
          return
        }
      })
    },
    countDown() {
      this.timer = window.setInterval(() => {
        this.time--
        if (this.time === 0) {
          this.sendCode = false
          this.time = 60
          clearInterval(this.timer)
        }
      }, 1000)
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          authRegister(this.formData).then(res => {
            console.log(res)
            if (res.code === 0) {
              this.$refs.verify.show()
            } else if (res.code === 120) {
              setTimeout(() => {
                this.$router.push({ path: this.redirect || '/' })
              }, 300)
            }
          })
        }
      })
    },
    authLogin() {
      this.$store.dispatch('user/authLogin', {
        captchaVerification: this.formData.captchaVerification,
        authPublicKey: this.formData.authPublicKey
      }).then(() => {
        this.$router.push({ path: this.redirect || '/' })
      })
    },
    handleSuccess(data) {
      if (data !== '') {
        this.formData.captchaVerification = data.captchaVerification
        this.authLogin()
      }
    },
    toLogin() {
      this.$router.push({
        path: '/login',
        replace: true
      })
    }
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
    width: 800px;
  }
  .register-wrap {
    position: relative;
    background-color: #fff;
    border-radius: 30px;
    padding: 64px;
    box-shadow: 0 16px 32px 0 rgb(0 0 0 / 8%);
    margin-top: 30px;
  }
}
.session-sidebox, .session-form {
  margin: 0 auto;
  width: 50%;
}
.session__auth-info{
  margin-bottom: 32px;
}
.auth-logos{
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 50%;
  margin: 0 auto 20px;
}
.primihub-logo{
  width: 50px;
}
.github{
  font-size: 50px;
}
.auth-tip {
  width: 120%;
  margin-left: -10%;
  text-align: center;
  color: #40485b;
  font-size: 14px;
}
.code-box{
  display: flex;
}
.code-button{
  margin-left: 10px;
}
.register-button{
  width: 100%;
}
.tips{
  color: #666;
  margin: 10px 0;
  font-size: 14px;
}
</style>
