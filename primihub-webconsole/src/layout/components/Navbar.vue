<template>
  <div class="navbar">
    <hamburger :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />

    <breadcrumb class="breadcrumb-container" />
    <div class="right-menu">
      <div class="feedback">
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
          <el-dropdown-item divided @click.native="openAddPhoneDialog">{{ isBound? '更换手机号': '添加手机号' }}</el-dropdown-item>
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
    <BindPhoneDialog title="添加手机号" append-to-body :visible.sync="phoneDialogVisible" :is-bound="isBound" @close="handlePhoneDialogClose" />
  </div>
</template>

<script>
import { mapGetters, mapState, mapActions } from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'
import UpdatePwdForm from '@/components/UpdatePwdForm'
import BindPhoneDialog from '@/components/BindPhoneDialog'

const phonePattern = /^[1][3,4,5,7,8][0-9]{9}$/

export default {
  components: {
    Breadcrumb,
    Hamburger,
    UpdatePwdForm,
    BindPhoneDialog
  },
  data() {
    return {
      userInfo: {},
      organName: '',
      dialogVisible: false,
      phoneDialogVisible: false,
      userAccount: '',
      registerType: 0
    }
  },
  computed: {
    isBound() {
      return this.registerType === 4 && phonePattern.test(this.userAccount)
    },
    ...mapState('user', ['organChange']),
    ...mapGetters([
      'sidebar',
      'avatar',
      'userOrganId',
      'userOrganName',
      'userName'
    ])
  },
  watch: {
    organChange(newVal) {
      if (newVal) {
        this.getInfo()
      }
    }
  },
  created() {
    this.getInfo().then(res => {
      this.userInfo = res
      this.registerType = res.registerType
      this.userAccount = res.userAccount
    })
  },
  methods: {
    openAddPhoneDialog() {
      this.phoneDialogVisible = true
    },
    handlePhoneDialogClose() {
      this.phoneDialogVisible = false
    },
    showUpdatePwd() {
      console.log(this.dialogVisible)
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
    ...mapActions('user', ['getInfo'])
    // getUserInfo() {
    //   this.userInfo = JSON.parse(localStorage.getItem('userInfo'))
    //   this.organName = this.userInfo.organIdListDesc
    // }
  }
}
</script>

<style lang="scss" scoped>
p{
  margin-block-start: 0;
    margin-block-end: 0;
}
.navbar {
  height: 50px;
  overflow: hidden;
  top: 0;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background .3s;
    -webkit-tap-highlight-color:transparent;

    &:hover {
      background: rgba(0, 0, 0, .025)
    }
  }

  .breadcrumb-container {
    float: left;
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
