<template>
  <div class="wrap">
    <div class="title-container">
      <h3 class="title">
        <span v-if="codeType===1">账号注册</span>
        <span v-if="codeType===2">找回密码</span>
      </h3>
    </div>
    <div class="form-container">
      <el-select v-model="userAccountType" placeholder="请选择" class="select" @change="handleUserAccountChange">
        <el-option
          v-for="item in userAccountOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-form ref="form" :model="formData" :rules="formRules" auto-complete="on" label-position="left">
        <el-form-item prop="userAccount" class="user-account">
          <div class="flex-end-row">
            <el-col :span="18">
              <el-input
                ref="userAccount"
                v-model="formData.userAccount"
                :placeholder="userAccountText"
                name="userAccount"
                type="text"
                tabindex="1"
              />
            </el-col>
          </div>

        </el-form-item>
        <el-form-item prop="verificationCode">
          <el-row class="flex-between-row">
            <el-col :span="16">
              <el-input
                ref="verificationCode"
                v-model="formData.verificationCode"
                placeholder="请输入验证码"
                name="verificationCode"
                type="text"
                tabindex="1"
              />
            </el-col>
            <el-col :span="8">
              <el-button type="primary" class="code-button" plain :disabled="sendCode" @click="sendVerificationCode">
                <span v-if="!sendCode">获取验证码</span>
                <span v-else>{{ time }}s后重新获取</span>
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item v-if="codeType===1" prop="userName">
          <el-input
            ref="userName"
            v-model="formData.userName"
            placeholder="请输入用户名"
            name="userName"
            type="text"
            tabindex="1"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            :key="passwordType"
            ref="password"
            v-model="formData.password"
            :type="passwordType"
            :placeholder="pwdText"
            name="password"
            tabindex="2"
            auto-complete="off"
          />
          <span class="show-pwd" @click="showPwd">
            <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
          </span>
        </el-form-item>
        <el-form-item prop="passwordAgain">
          <el-input
            :key="passwordAgainType"
            ref="passwordAgain"
            v-model="formData.passwordAgain"
            :type="passwordAgainType"
            :placeholder="pwdAgainText"
            name="passwordAgain"
            tabindex="2"
            auto-complete="off"
          />
          <span class="show-pwd" @click="showAgainPwd">
            <svg-icon :icon-class="passwordAgainType === 'password' ? 'eye' : 'eye-open'" />
          </span>
        </el-form-item>
        <el-button :loading="loading" type="primary" class="register-button" @click.native.prevent="handleSubmit">
          <span v-if="codeType===1">注册</span>
          <span v-if="codeType===2">提交</span>
        </el-button>
      </el-form>
      <div v-if="codeType===1" class="tips">
        <span>已有账号？<el-button type="text" @click="toLogin">去登录</el-button></span>
      </div>
      <div v-if="codeType===2" class="tips">
        <span><el-button type="text" @click="toLogin">返回登录</el-button></span>
      </div>
    </div>
  </div>

</template>

<script>
import { sendVerificationCode, register, forgetPassword } from '@/api/user'

const emailPattern = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
const phonePattern = /^[1][3,4,5,7,8][0-9]{9}$/
const pwdPattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{8,}$/

export default {
  name: 'FormData',
  props: {
    codeType: {
      type: Number,
      default: 1
    }
  },
  data() {
    const validateUserAccount = (rule, value, callback) => {
      if (this.formData.registerType === 3) {
        if (value === '') {
          callback(new Error('请输入手机号'))
        } else if (!phonePattern.test(value)) {
          callback(new Error('请输入正确的手机号'))
        } else {
          callback()
        }
      } else if (this.formData.registerType === 2) {
        if (value === '') {
          callback(new Error('请输入邮箱'))
        } else if (!emailPattern.test(value)) {
          callback(new Error('请输入正确的邮箱'))
        } else {
          callback()
        }
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else if (!pwdPattern.test(value)) {
        callback(new Error('密码至必须8位并且同时包含至少一个字母、一个数字和一个特殊字符'))
      } else {
        callback()
      }
    }
    const validatePasswordAgain = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.formData.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }
    return {
      userAccountType: 3,
      userAccountOptions: [
        {
          value: 3,
          label: '手机号'
        },
        {
          value: 2,
          label: '邮箱'
        }
      ],
      formData: {
        userAccount: '',
        userName: '',
        password: '',
        passwordAgain: '',
        verificationCode: '',
        registerType: 3
      },
      formRules: {
        userAccount: [{ required: true, trigger: 'blur', validator: validateUserAccount }],
        verificationCode: [{ required: true, trigger: 'blur', message: '请输入验证码' }],
        userName: [{ required: true, trigger: 'blur', message: '请输入用户名' }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        passwordAgain: [{ required: true, trigger: 'blur', validator: validatePasswordAgain }]
      },
      loading: false,
      passwordType: 'password',
      passwordAgainType: 'password',
      redirect: undefined,
      time: 60,
      timer: null,
      sendCode: false,
      dialogVisible: false
    }
  },
  computed: {
    pwdText() {
      return this.codeType === 1 ? '请输入密码' : '请输入新密码'
    },
    pwdAgainText() {
      return this.codeType === 1 ? '请确认密码' : '请确认新密码'
    },
    userAccountText() {
      let text = ''
      if (this.formData.registerType === 2) {
        text = this.codeType === 2 ? '请输入注册时的邮箱' : '请输入邮箱'
      } else {
        text = this.codeType === 2 ? '请输入注册时的手机号' : '请输入手机号'
      }
      return text
    }
  },
  methods: {
    handleUserAccountChange(value) {
      this.formData.registerType = value
      this.formData.userAccount = ''
      this.$refs.form.resetFields()
      this.sendCode = false
      clearInterval(this.timer)
    },
    toLogin() {
      this.$notify && this.$notify.closeAll()
      this.$router.push({
        path: '/login',
        replace: true
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
    showAgainPwd() {
      if (this.passwordAgainType === 'password') {
        this.passwordAgainType = ''
      } else {
        this.passwordAgainType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.passwordAgain.focus()
      })
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.loading = true
          const { userAccount, userName, password, passwordAgain, verificationCode } = this.formData
          let params = {
            userAccount,
            password,
            passwordAgain,
            verificationCode
          }
          console.log(params)
          if (this.codeType === 2) {
            forgetPassword(params).then(res => {
              if (res.code === 0) {
                this.loading = false
                this.$notify({
                  title: '密码修改成功',
                  dangerouslyUseHTMLString: true,
                  message: this.$createElement('el-button', { class: 'login-button', on: { click: this.toLogin }, type: 'primary' }, '登录'),
                  type: 'success',
                  duration: 0
                })
              } else {
                this.loading = false
                return
              }
            }).catch(() => {
              this.loading = false
            })
          } else {
            params = Object.assign(params, {
              registerType: this.formData.registerType,
              userName
            })
            register(params).then(res => {
              console.log(res)
              if (res.code === 0) {
                this.loading = false
                this.$notify({
                  title: '注册成功',
                  dangerouslyUseHTMLString: true,
                  message: this.$createElement('el-button', { class: 'login-button', on: { click: this.toLogin }, type: 'primary' }, '立即体验'),
                  type: 'success',
                  duration: 0
                })
              } else {
                this.loading = false
                return
              }
            }).catch(() => {
              this.loading = false
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    sendVerificationCode() {
      if (this.formData.userAccount === '') {
        const message = this.formData.registerType === 3 ? '请输入手机号' : '请输入邮箱'
        this.$message({
          message,
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
      let params = {
        codeType: this.codeType
      }
      console.log(this.formData.registerType)
      if (this.formData.registerType === 3) {
        params = Object.assign(params, {
          cellphone: this.formData.userAccount
        })
      } else if (this.formData.registerType === 2) {
        params = Object.assign(params, {
          email: this.formData.userAccount
        })
      }
      console.log('sendVerificationCode', params)
      sendVerificationCode(params).then(res => {
        console.log(res)
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
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .user-account .el-form-item__error{
  left: 25%;
}
::v-deep .el-dialog__body{
  padding: 10px 0 30px;
}
.el-input {
  input {
    &:-webkit-autofill {
      box-shadow: 0 0 0px 1000px #fff inset !important;
      -webkit-text-fill-color: #000 !important;
    }
  }
}

.title-container {
  position: relative;

  .title {
    font-size: 26px;
    color: #000;
    margin: 0px auto 30px auto;
    text-align: center;
    font-weight: bold;
  }
}
.form-container {
  position: relative;
  width: 400px;
  max-width: 100%;
  /* padding: 160px 35px 0; */
  margin: 0 auto;
  overflow: hidden;
}

.tips {
  font-size: 14px;
  margin-bottom: 10px;
  text-align: right;
}
.flex-between-row{
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.flex-end-row{
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.container{
  width: 400px;
  margin: 0 auto;
}
.code-button{
  font-size: 12px;
  width: 96%;
  padding: 12px 0;
  text-align: center;
  float: right;
}
.user-account{
  width: 100%;
}
.select{
  position: absolute;
  width: 24%;
  left: 0;
  right: 0;
  z-index: 10;
}
.register-button{
  width: 100%;
}

.text-container{
  // display: flex;
  // align-items: center;
  text-align: center;
  font-size: 24px;
  &-icon{
    font-size: 80px;
    color: #67C23A;
  }
  &-text{
    font-size: 24px;
    margin-bottom: 20px;
  }
}
.forgot {
  color: #1989FA;
  font-size: 14px;
  text-align: right;
  padding-bottom:10px;
}
.show-pwd {
  position: absolute;
  right: 10px;
  top: 0px;
  font-size: 16px;
  color: #889aa4;
  cursor: pointer;
  user-select: none;
}
.login-button{
  background-color: #1989FA;
  color: #fff;
  height: 30px;
  line-height: 1;
  padding: 5px 10px;
  margin-top: 3px;
}
</style>
