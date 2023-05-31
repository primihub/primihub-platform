<template>
  <div class="navbar">
    <div class="logo" @click="toPage('/')">
      <template v-if="loaded">
        <img v-if="showLogo && logoUrl !== ''" :src="logoUrl" class="sidebar-logo">
        <h1 v-if="showLogoTitle" class="logo-title">{{ logoTitle }} </h1>
      </template>
    </div>
    <div class="right-menu avatar-container">
      <div class="avatar-wrapper">
        <img src="/images/avatar.png" class="user-avatar">
        <div class="user-info">
          <p>{{ userOrganName }}</p>
          <p>{{ userName }}</p>
        </div>
      </div>
    </div>
    <el-dialog
      class="dialog"
      :visible.sync="dialogVisible"
      :append-to-body="true"
      :close-on-click-modal="false"
      width="500px"
      :before-close="handleClose"
    >
      <UpdatePwdForm @submit="handleSubmit" />
    </el-dialog>
    <!-- add phone dialog -->
    <BindPhoneDialog append-to-body :visible.sync="phoneDialogVisible" :is-bound="isBound" @close="handlePhoneDialogClose" @success="handlePhoneBindSuccess" />
  </div>
</template>

<script>
import { mapGetters, mapState, mapActions } from 'vuex'
import UpdatePwdForm from '@/components/UpdatePwdForm'
import BindPhoneDialog from '@/components/BindPhoneDialog'

const phonePattern = /^[1][\d][0-9]{9}$/

export default {
  components: {
    UpdatePwdForm,
    BindPhoneDialog
  },
  data() {
    return {
      userInfo: {},
      organName: '',
      dialogVisible: false,
      phoneDialogVisible: false
    }
  },
  computed: {
    isBound: {
      set(val) {},
      get() {
        return this.registerType === 4 && phonePattern.test(this.userAccount)
      }
    },
    ...mapState('user', ['organChange']),
    ...mapState('settings', [
      'loaded',
      'logoUrl',
      'showLogo',
      'isHideFadeBack',
      'logoTitle',
      'showLogoTitle',
      'settingChanged'
    ]),
    ...mapGetters([
      'sidebar',
      'avatar',
      'userOrganId',
      'userOrganName',
      'userName',
      'userAccount',
      'registerType'
    ])
  },
  watch: {
    organChange(newVal) {
      if (newVal) {
        this.getInfo()
      }
    },
    userAccount(newVal) {
      if (phonePattern.test(newVal)) {
        this.isBound = true
      }
    },
    async settingChanged(newVal) {
      if (newVal) {
        await this.getHomepage()
      }
    }
  },
  created() {
    this.getInfo()
    this.$nextTick(async() => {
      await this.getHomepage()
    })
  },
  methods: {
    toPage(path) {
      this.$router.push(path)
    },
    openAddPhoneDialog() {
      this.phoneDialogVisible = true
    },
    handlePhoneDialogClose() {
      this.phoneDialogVisible = false
    },
    handlePhoneBindSuccess() {
      this.isBound = true
      this.phoneDialogVisible = false
    },
    showUpdatePwd() {
      this.dialogVisible = true
    },
    handleSubmit(res) {
      if (res) {
        this.dialogVisible = false
      }
    },
    handleClose() {
      this.dialogVisible = false
    },
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    async logout() {
      await this.$store.dispatch('user/logout')
      // location.reload()
      this.$router.push(`/login?redirect=${this.$route.fullPath}`)
    },
    ...mapActions('user', ['getInfo']),
    ...mapActions('settings', ['getHomepage'])
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-icon--right{
  color: #fff;
}
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 50px;
  overflow: hidden;
  background: #3e4555;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  color: #838790;
  width: 100%;
  .middle-container{
    flex: 1;
    color: #fff;
    span{
      margin: 0 10px;
      display: inline-block;
      cursor: pointer;
    }
  }
  .logo{
    padding: 5px 20px;
    height: 100%;
    line-height: 36px;
    cursor: pointer;
    h1.logo-title{
      color: #fff;
      font-size: 16px;
      margin: 0 5px;
      display: inline-block;
      vertical-align: middle;
    }
    img{
      max-height: 100%;
      vertical-align: middle;
      height: 100%;
    }
  }
  .avatar-container {
      margin-right: 30px;
      cursor: pointer;

      .avatar-wrapper {
        position: relative;
        .user-info{
          display: inline-block;
          line-height: 1;
          margin: 0 3px;
          vertical-align: middle;

          p{
            color: #fff;
          }
        }
        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
          vertical-align: middle;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background .3s;

        &:hover {
          background: rgba(0, 0, 0, .025)
        }
      }
    }

    .feedback{
      display: inline-block;
      margin-right: 10px;
    }
  }
}
.dialog{
  padding-bottom: 30px!important;
}
</style>
