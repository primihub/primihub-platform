<template>
  <div class="navbar">
    <hamburger :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />

    <breadcrumb class="breadcrumb-container" />

    <div class="right-menu">
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
          <!-- <router-link to="/setting/user">
            <el-dropdown-item>用户管理</el-dropdown-item>
          </router-link> -->
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
  </div>
</template>

<script>
import { mapGetters, mapState, mapActions } from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'
import UpdatePwdForm from '@/components/UpdatePwdForm'
export default {
  components: {
    Breadcrumb,
    Hamburger,
    UpdatePwdForm
  },
  data() {
    return {
      userInfo: {},
      organName: '',
      dialogVisible: false
    }
  },
  computed: {
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
    this.getInfo()
  },
  methods: {
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
  }
}
.dialog{
  padding-bottom: 30px!important;
}
</style>
