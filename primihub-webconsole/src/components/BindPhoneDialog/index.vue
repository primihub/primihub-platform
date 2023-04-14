<template>
  <el-dialog
    :title="title"
    v-bind="$attrs"
    :before-close="handleClose"
  >
    <div v-if="showBound" class="form-container">
      <p>已绑定手机</p>
      <h2>{{ $store.getters.userAccount }}</h2>
      <p>绑定手机可关联账号且可优先体验部分产品功能，建议您绑定。</p>
    </div>
    <el-form v-else ref="form" class="form-container" :model="formData" :rules="formRules" auto-complete="on" label-position="left">
      <el-form-item prop="userAccount" class="user-account" label="手机号">
        <el-input
          ref="userAccount"
          v-model="formData.userAccount"
          placeholder="手机号"
          name="userAccount"
          type="text"
          tabindex="1"
        />
        <p class="tips">建议绑定手机，可用于监控报警等消息的接收，不绑定不影响其他功能使用。</p>
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
    </el-form>
    <span v-if="showBound" slot="footer" class="dialog-footer">
      <el-button type="primary" @click.native.prevent="changeBindPhone">更换手机号</el-button>
      <el-button type="info" @click.native.prevent="unBindPhone">手机号解除绑定</el-button>
    </span>
    <span v-else slot="footer" class="dialog-footer">
      <el-button type="primary" @click.native.prevent="handleSubmit">确定</el-button>
      <el-button @click="handleClose">取 消</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { sendVerificationCode, changeUserAccount, relieveUserAccount } from '@/api/user'
const phonePattern = /^[1][\d][0-9]{9}$/

export default {
  name: 'PersonalInfo',
  props: {
    isBound: {
      type: Boolean,
      default: false
    }
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
      formData: {
        userAccount: '',
        verificationCode: ''
      },
      formRules: {
        userAccount: [{ required: true, trigger: 'blur', validator: validateUserAccount }],
        verificationCode: [{ required: true, message: '请输入验证码' }]
      },
      sendCode: false,
      time: 60,
      timer: null,
      showBound: this.isBound,
      userId: ''
    }
  },
  computed: {
    title: {
      get() {
        return this.isBound ? '更换手机号' : '添加手机号'
      },
      set() {}
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    },
    isBound(newVal) {
      this.showBound = newVal
      this.title = newVal ? '更换手机号' : this.title
    }
  },
  mounted() {
    this.$store.dispatch('user/getInfo').then(data => {
      this.userId = data.userId
    })
  },
  methods: {
    changeBindPhone() {
      this.title = '更换手机号'
      this.formData.userAccount = ''
      this.formData.verificationCode = ''
      this.showBound = false
    },
    unBindPhone() {
      this.$confirm('解绑手机后，可能会影响部分新功能的体验，确认解除绑定么？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        relieveUserAccount({ userId: this.userId }).then(res => {
          if (res.code === 0) {
            this.$message.success('手机号解除绑定成功')
            this.$store.commit('user/SET_USER_ACCOUNT', res.result.user.userAccount)
            this.handleClose()
          }
        })
      }).catch(err => {
        console.log(err)
      })
    },
    sendVerificationCode() {
      if (this.formData.userAccount === '') {
        this.$message({
          message: '请输入手机号',
          type: 'error'
        })
        return
      }
      this.countDown()
      /*
      method:post
      param: codeType 1 注册 2 忘记密码 3 绑定手机号
      result:只有成功和状态码 状态码有以下：
      NOT_IN_THE_WHITE_LIST(113,"不在短信服务白名单中"),
      FIVE_MINUTES_LATER(114,"请在五分钟后重试"),
      ONE_MINUTES_LATER(115,"请在一分钟后重试"),
      OUTNUMBER(116,"超出当天发送条数"),
      SMS_FAILURE(117,"SMS发送失败"),
      VERIFICATION_CODE(118,"验证码失败"),
      */
      const params = {
        codeType: 3,
        cellphone: this.formData.userAccount
      }

      sendVerificationCode(params).then(res => {
        if (res.code === 0) {
          this.sendCode = true
        } else {
          clearInterval(this.timer)
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
          this.changeUserAccount()
        }
      })
    },
    changeUserAccount() {
      const params = {
        userAccount: this.formData.userAccount,
        verificationCode: this.formData.verificationCode
      }
      changeUserAccount(params).then(res => {
        console.log(res)
        if (res.code === 0) {
          this.$message({
            message: this.isBound ? '更换成功' : '添加成功',
            type: 'success'
          })
          this.showBound = true
          clearInterval(this.timer)
          this.$store.commit('user/SET_USER_ACCOUNT', this.formData.userAccount)
          this.$store.commit('user/SET_REGISTER_TYPE', 4)
          this.$emit('success')
        }
      })
    },
    handleClose() {
      this.formData.userAccount = ''
      this.formData.verificationCode = ''
      this.showBound = this.isBound
      this.$refs.form && this.$refs.form.resetFields()
      clearInterval(this.timer)
      this.$emit('close')
    }
  }
}
</script>
<style lang="scss">
.app-main,.app-container{
  height: 100%;
}
</style>

<style lang="scss" scoped>
.form-container{
  padding: 0 20px 20px 20px;
}
.dialog-footer{
  display: flex;
  justify-content: center;
  padding-bottom: 30px;
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
  color: #999;
  font-size: 12px;
  height: 15px;
  line-height: 2;
}
</style>
