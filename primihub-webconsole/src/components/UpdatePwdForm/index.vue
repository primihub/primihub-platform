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
            v-model="formData.password"
            :type="passwordType"
            placeholder="请输入新密码"
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
            placeholder="请确认新密码"
            name="passwordAgain"
            tabindex="2"
            auto-complete="off"
          />
          <span class="show-pwd" @click="showAgainPwd">
            <svg-icon :icon-class="passwordAgainType === 'password' ? 'eye' : 'eye-open'" />
          </span>
        </el-form-item>
        <el-button :loading="loading" type="primary" class="register-button" @click.native.prevent="handleSubmit">提交</el-button>
      </el-form>
    </div>
    <el-dialog
      :visible.sync="dialogVisible"
      width="30%"
      :before-close="handleClose"
      append-to-body
      :modal="false"
    >
      <div class="text-container">
        <p class="text-container-icon"><i class="el-icon-success" /></p>
        <p class="text-container-text">
          <span>密码修改成功</span>
        </p>
        <el-button type="primary" @click="toLogin">重新登录</el-button>
      </div>
    </el-dialog>
  </div>

</template>

<script>
import { getValidatePublicKey, updatePassword } from '@/api/user'
import JSEncrypt from 'jsencrypt'
const pwdPattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{8,}$/

export default {
  name: 'FormData',
  data() {
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
      dialogVisible: false,
      timer: null
    }
  },
  async mounted() {
    await this.getValidatePublicKey()
  },
  methods: {
    toLogin() {
      this.$notify.closeAll()
      this.$router.push({
        path: '/login',
        replace: true
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
              this.$notify({
                title: '密码修改成功',
                dangerouslyUseHTMLString: true,
                message: this.$createElement('el-button', { class: 'login-button', on: { click: this.toLogin }, type: 'primary' }, '重新登录'),
                type: 'success',
                duration: 3000
              })
            } else {
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
    handleClose() {
      this.dialogVisible = false
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
