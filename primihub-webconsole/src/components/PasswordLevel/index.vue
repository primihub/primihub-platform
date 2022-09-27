<template>
  <div v-if="value" class="pwd-level">密码强度:
    <span class="level" :class="{ level0: userRegPwdLevel >= 1 }" />
    <span class="level" :class="{ level1: userRegPwdLevel >= 2 }" />
    <span class="level" :class="{ level2: userRegPwdLevel >= 3 }" />
    {{ levelText }}
  </div>
</template>

<script>
const pwdPattern = /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?\d)(?=.*?[^\w]).{8,16}$/

export default {
  name: 'PasswordLevel',
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      userRegPwdLevel: 1,
      levelText: '弱'
    }
  },
  watch: {
    value(newVal) {
      if (newVal) {
        this.passwordCheck(newVal)
      }
    }
  },
  methods: {
    passwordCheck(value) {
      const numReg = /\d/
      const letterReg = /[a-zA-z]/
      const specialReg = /\W/

      const weakReg = value.match(numReg) || value.match(letterReg) || value.match(specialReg)
      const mediumReg = (value.match(numReg) && value.match(letterReg)) || (value.match(numReg) && value.match(specialReg)) || (value.match(letterReg) && value.match(specialReg))
      const strongReg = value.match(pwdPattern)

      if (value.length < 8) { // 最初级别
        this.userRegPwdLevel = 1
        this.levelText = '弱'
        return false
      }
      if (strongReg) {
        this.userRegPwdLevel = 3
        this.levelText = '强'
      } else if (mediumReg) {
        this.userRegPwdLevel = 2
        this.levelText = '中'
      } else if (weakReg) {
        this.userRegPwdLevel = 1
        this.levelText = '弱'
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.pwd-level{
  display: flex;
  align-items: center;
  font-size: 12px;
  padding-top: 8px;
  height: 20px;
}
.level{
  display: inline-block;
  width: 28px;
  height: 4px;
  background-color: #ddd;
  margin: 0 3px;
  text-align: center;
}
.level0{
  background-color: #F56C6C;
}
.level1{
  background-color: #E6A23C;
}
.level2{
  background-color: #67C23A;
}
</style>
