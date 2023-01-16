<template>
  <div class="navbar">
    <div class="logo">
      <img v-if="sidebarLogo && logoUrl !== ''" :src="logoUrl" class="sidebar-logo">
      <h1 v-if="showLogoTitle" class="logo-title">{{ logoTitle }} </h1>
    </div>
    <div class="right-menu">
      <div v-if="!isHideFadeBack" class="feedback">
        <el-popover
          placement="bottom"
          width="200"
          trigger="click"
        >
          <div style="text-align: center;">
            <img src="https://primihub.com/img/qrcode1.78d466ad.png" alt="" srcset="" width="175px">
            <p>原语科技小助手</p>
          </div>
          <el-link slot="reference" type="primary">建议与反馈</el-link>
        </el-popover>
      </div>
      <el-button v-if="!isHideAppMarket" type="primary" size="small" style="margin-right: 10px;" @click="toApplicationPage">应用市场</el-button>
      <el-dropdown class="avatar-container" trigger="click">
        <div class="avatar-wrapper">
          <!-- <img :src="avatar+'?imageView2/1/w/80/h/80'" class="user-avatar"> -->
          <img src="/images/avatar.png" class="user-avatar">
          <div class="user-info">
            <p>{{ userOrganName }}</p>
            <p>{{ userName }}</p>
          </div>
          <i class="el-icon-arrow-down el-icon--right" />
        </div>
        <el-dropdown-menu slot="dropdown" class="user-dropdown">
          <el-dropdown-item @click.native="showUpdatePwd">修改密码</el-dropdown-item>
          <el-dropdown-item v-if="registerType === 4" divided @click.native="openAddPhoneDialog">{{ isBound ? '更换手机号': '添加手机号' }}</el-dropdown-item>
          <el-dropdown-item divided @click.native="logout">
            <span style="display:block;">退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
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

const phonePattern = /^[1][3,4,5,7,8][0-9]{9}$/

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
      'logoUrl',
      'sidebarLogo',
      'isHideFadeBack',
      'isHideAppMarket',
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
    toApplicationPage() {
      this.$router.push({
        path: '/applicationIndex'
      })
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
  justify-content: space-between;
  align-items: center;
  height: 50px;
  overflow: hidden;
  background: #3e4555;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  color: #838790;
  width: 100%;
  .logo{
    padding: 5px 20px;
    height: 100%;
    flex: 1;
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
