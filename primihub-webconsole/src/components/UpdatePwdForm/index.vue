<template>
  <div class="wrap">
    <div class="title-container">
      <h3 class="title">修改密码</h3>
    </div>
    <div class="form-container">
      <el-form ref="form" :model="formData" :rules="formRules" auto-complete="on" label-position="left">
        <el-form-item prop="oldPassword">
          <el-input
            :key="oldPasswordType"
            ref="oldPassword"
            v-model="formData.oldPassword"
            :type="oldPasswordType"
            placeholder="请输入原密码"
            name="oldPassword"
            tabindex="2"
            auto-complete="off"
          />
          <span class="show-pwd" @click="showOldPwd">
            <svg-icon :icon-class="oldPasswordType === 'password' ? 'eye' : 'eye-open'" />
          </span>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            :key="passwordType"
            ref="password"
            v-model.trim="formData.password"
            :type="passwordType"
            placeholder="请输入新密码"
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
            v-model.trim="formData.passwordAgain"
            :type="passwordAgainType"
            placeholder="请确认新密码"
            name="passwordAgain"
            tabindex="2"
            auto-complete="off"
            style="ime-mode:disabled"
          />
          <span class="show-pwd" @click="showAgainPwd">
            <svg-icon :icon-class="passwordAgainType === 'password' ? 'eye' : 'eye-open'" />
          </span>
        </el-form-item>
        <el-button :loading="loading" type="primary" class="register-button" @click.native.prevent="handleSubmit">提交</el-button>
      </el-form>
    </div>
  </div>

</template>

<script>
import { getValidatePublicKey, updatePassword } from '@/api/user'
import JSEncrypt from 'jsencrypt'
import PasswordLevel from '@/components/PasswordLevel'

const pwdPattern = /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?\d)(?=.*?[^\w]).{8,16}$/

export default {
  name: 'FormData',
  components: { PasswordLevel },
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else if (value.length < 8 || value.length > 16) {
        callback(new Error('密码长度在8-16个字符以内'))
      } else if (!pwdPattern.test(value)) {
        callback(new Error('密码至少8位且同时包含数字、大小写字母及特殊字符'))
      } else {
        callback()
      }
    }
    const validatePasswordAgain = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入新密码'))
      } else if (value !== this.formData.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }
    return {
      formData: {
        oldPassword: '',
        password: '',
        passwordAgain: ''
      },
      formRules: {
        oldPassword: [{ required: true, trigger: 'blur', message: '请输入原密码' }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        passwordAgain: [{ required: true, trigger: 'blur', validator: validatePasswordAgain }]
      },
      loading: false,
      oldPasswordType: 'password',
      passwordType: 'password',
      passwordAgainType: 'password',
      publicKeyData: {},
      timer: null,
      count: 3,
      countTimer: null
    }
  },
  watch: {
    'formData.password'(newVal) {
      if (newVal) {
        this.formData.password = this.formData.password.replace(/[\u4e00-\u9fa5]|(\s+)/ig, '')
      }
    }
  },
  async mounted() {
    await this.getValidatePublicKey()
  },
  methods: {
    async toLogin() {
      this.$notify.closeAll()
      this.$store.dispatch('user/logout').then(() => {
        this.$router.push({
          path: '/login',
          replace: true
        })
      })
    },
    async getValidatePublicKey() {
      const { result = {}} = await getValidatePublicKey()
      const { publicKey, publicKeyName } = result
      this.publicKeyData = { publicKey, publicKeyName }
    },
    pwdToggle(typeName, refName) {
      console.log('typeName', typeName)
      if (this[typeName] === 'password') {
        this[typeName] = ''
      } else {
        this[typeName] = 'password'
      }
      this.$nextTick(() => {
        this.$refs[refName].focus()
      })
    },
    showOldPwd() {
      if (this.oldPasswordType === 'password') {
        this.oldPasswordType = ''
      } else {
        this.oldPasswordType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.oldPassword.focus()
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
          const { publicKey, publicKeyName } = this.publicKeyData
          if (!publicKey) {
            this.$message({
              message: '请求异常，请刷新后稍后重试',
              type: 'error'
            })
            return
          }
          const crypt = new JSEncrypt()
          crypt.setKey(publicKey)
          const password = crypt.encrypt(`${this.formData.oldPassword},${this.formData.password}`)
          const params = { password, validateKeyName: publicKeyName }
          console.log(params)
          updatePassword(params).then(res => {
            if (res.code === 0) {
              this.loading = false
              this.showNotice()
            } else {
              this.$emit('submit', false)
              this.loading = false
              return
            }
          }).catch(err => {
            console.log(err)
            this.loading = false
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    showNotice() {
      const message = `<div><p>即将自动跳转至登录页（<span id="countDownNumber" style="font-weight:700;padding: 0 0.01rem">${this.count}</span>s）<p><div style="width:120px;line-height:30px;font-weight:700;background:#1989FA;color:#fff;border-radius:3px;text-align:center;cursor: pointer;margin-top:3px" id="login">立即登录</div></div>`
      const TIME_COUNT = 3
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
          title: '修改成功',
          dangerouslyUseHTMLString: true,
          message,
          customClass: 'notice',
          type: 'success',
          duration: 3000
        })
        setTimeout(() => {
          this.toLogin()
          notify.close()
          this.$emit('submit', true)
          clearInterval(this.countTimer)
        }, 3000)
        document.querySelector('#login').onclick = () => {
          notify.close()
          this.toLogin()
          clearInterval(this.countTimer)
        }
      }
    }
  }
}
</script>
<style lang="scss">
.notice{
  z-index: 2999!important;
}
</style>
<style lang="scss" scoped>
::v-deep .user-account .el-form-item__error{
  left: 25%;
}
::v-deep .el-dialog__body{
  padding: 10px 0 30px;
}
::v-deep .el-dialog{
  border-radius: 10px;
}
::v-deep .el-dialog__headerbtn .el-dialog__close{
  color: #fff;
}
::v-deep .el-dialog, .el-pager li{
  background-color: rgba(0,0,0,.6);
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
  padding-bottom: 30px;
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
    color: #fff;
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
