<template>
  <div class="wrap">
    <div class="title-container">
      <h3 class="title">
        <span v-if="codeType===1">账号注册</span>
        <span v-if="codeType===2">找回密码</span>
      </h3>
    </div>
    <div class="form-container">

      <el-form ref="form" :model="formData" :rules="formRules" auto-complete="on" label-position="left">
        <el-form-item prop="userAccount" class="user-account">
          <el-row class="flex-between-row">
            <el-col :span="6">
              <el-select v-model="userAccountType" :size="size" placeholder="请选择" class="select" @change="handleUserAccountChange">
                <el-option
                  v-for="item in userAccountOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-col>

            <el-col :span="18">
              <el-input
                ref="userAccount"
                v-model="formData.userAccount"
                :size="size"
                :placeholder="userAccountText"
                name="userAccount"
                type="text"
                tabindex="1"
              />
            </el-col>

          </el-row>
        </el-form-item>

        <el-form-item prop="verificationCode">
          <el-row class="flex-between-row">
            <el-col :span="16">
              <el-input
                ref="verificationCode"
                v-model="formData.verificationCode"
                :size="size"
                placeholder="请输入验证码"
                name="verificationCode"
                type="text"
                tabindex="1"
              />
            </el-col>
            <el-col :span="8">
              <el-button :size="size" type="primary" class="code-button" plain :disabled="sendCode" @click="sendVerificationCode">
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
            :size="size"
            placeholder="请输入昵称"
            name="nickName"
            type="text"
            tabindex="1"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            :key="passwordType"
            ref="password"
            v-model="formData.password"
            :size="size"
            :type="passwordType"
            :placeholder="pwdText"
            name="password"
            tabindex="2"
            auto-complete="off"
            style="ime-mode:disabled"
            onpaste="return false"
            ondragenter="return false"
            oncontextmenu="return false"
          />
          <span class="show-pwd" @click="showPwd">
            <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
          </span>
          <PasswordLevel :value="formData.password" />
        </el-form-item>
        <el-form-item prop="passwordAgain">
          <el-input
            :key="passwordAgainType"
            ref="passwordAgain"
            v-model="formData.passwordAgain"
            :size="size"
            :type="passwordAgainType"
            :placeholder="pwdAgainText"
            name="passwordAgain"
            tabindex="2"
            auto-complete="off"
            style="ime-mode:disabled"
          />
          <span class="show-pwd" @click="showAgainPwd">
            <svg-icon :icon-class="passwordAgainType === 'password' ? 'eye' : 'eye-open'" />
          </span>
        </el-form-item>
        <el-button :size="size" :loading="loading" type="primary" class="register-button" @click.native.prevent="handleSubmit">
          <span v-if="codeType===1">注册</span>
          <span v-if="codeType===2">提交</span>
        </el-button>
      </el-form>
      <div v-if="codeType===1" class="tips">
        <span>已有账号？<el-button type="text" @click="toLogin">去登录</el-button></span>
      </div>
      <div v-if="codeType===2" class="tips">
        <span><el-button :size="size" type="text" @click="toLogin">返回登录</el-button></span>
      </div>
    </div>
  </div>

</template>

<script>
import { sendVerificationCode, register, forgetPassword } from '@/api/user'
import PasswordLevel from '@/components/PasswordLevel'

const emailPattern = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
const phonePattern = /^[1][\d][0-9]{9}$/
const pwdPattern = /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?\d)(?=.*?[^\w]).{8,16}$/

export default {
  name: 'FormData',
  components: { PasswordLevel },
  props: {
    codeType: {
      type: Number,
      default: 1
    },
    size: {
      type: String,
      default: 'small'
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
      } else if (/[\u4E00-\u9FA5]/.test(value)) {
        callback(new Error('密码中不能包含中文'))
      } else if (/\s/.test(value)) {
        callback(new Error('密码中不能包含空格'))
      } else if (value.length < 8 || value.length > 16) {
        callback(new Error('密码长度在8-16个字符以内'))
      } else if (!pwdPattern.test(value)) { // 特殊字符:非字母数字且非下划线的字符
        callback(new Error('密码至少8位且同时包含数字、大小写字母及特殊字符'))
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
        verificationCode: [{ required: true, message: '请输入验证码' }],
        userName: [{ required: true, trigger: 'blur', message: '请输入昵称' }],
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
      dialogVisible: false,
      count: 10,
      countTimer: null
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
  watch: {
    'formData.password'(newVal) {
      if (newVal) {
        this.formData.password = this.formData.password.replace(/[\u4e00-\u9fa5]|(\s+)/ig, '')
      }
    }
  },
  destroyed() {
    clearInterval(this.timer)
    clearInterval(this.countTimer)
  },
  methods: {
    showNotice() {
      const title = this.codeType === 1 ? '注册成功' : '修改成功'
      const message = `<div><p>即将自动跳转至登录页（<span id="countDownNumber" style="font-weight:700;padding: 0 0.01rem">${this.count}</span>s）<p><div style="width:120px;line-height:30px;font-weight:700;background:#1989FA;color:#fff;border-radius:3px;text-align:center;cursor: pointer;margin-top:3px" id="login">立即登录</div></div>`
      const TIME_COUNT = 10
      if (!this.countTimer) {
        this.count = TIME_COUNT
        this.countTimer = setInterval(() => {
          if (this.count > 1 && this.count <= TIME_COUNT) {
            this.count--
            const dom = document.querySelector('#countDownNumber')
            dom.innerText = this.count
          } else {
            clearInterval(this.countTimer)
            this.countTimer = null
          }
        }, 1000)
        const notify = this.$notify({
          title,
          dangerouslyUseHTMLString: true,
          message,
          type: 'success',
          duration: 10000
        })
        setTimeout(() => {
          this.toLogin()
          notify.close()
          clearInterval(this.countTimer)
        }, 10000)
        document.querySelector('#login').onclick = () => {
          notify.close()
          this.toLogin()
          clearInterval(this.countTimer)
        }
      }
    },
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
                this.showNotice()
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
                this.showNotice()
              } else if (res.code === 106) {
                const message = this.formData.registerType === 2 ? '邮箱已存在，请勿重复注册' : '手机号已存在，请勿重复注册'
                this.$message({
                  message,
                  type: 'error'
                })
                this.loading = false
                return
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
<style lang="scss">
::v-deep .el-notification__content p.login-button{
  background-color: #1989FA;
  color: #fff;
  height: 30px;
  line-height: 1;
  padding: 5px 10px;
  margin-top: 3px;

}
</style>
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
  text-align: center;
  float: right;
}
.user-account{
  width: 100%;
}
.select{
  width: 96%;
  float: left;
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
</style>
