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
            {{ organName }}
            <br>
            {{ userInfo.userName }}
          </div>
          <i class="el-icon-arrow-down el-icon--right" />
        </div>
        <el-dropdown-menu slot="dropdown" class="user-dropdown">
          <!-- <router-link to="/setting/role">
            <el-dropdown-item>角色管理</el-dropdown-item>
          </router-link>
          <router-link to="/setting/user">
            <el-dropdown-item>用户管理</el-dropdown-item>
          </router-link> -->
          <el-dropdown-item divided @click.native="logout">
            <span style="display:block;">退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapState } from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'

export default {
  components: {
    Breadcrumb,
    Hamburger
  },
  data() {
    return {
      userInfo: {},
      organName: ''
    }
  },
  computed: {
    ...mapState('user', ['organChange']),
    ...mapGetters([
      'sidebar',
      'avatar'
    ])
  },
  watch: {
    organChange(newVal) {
      if (newVal) {
        this.getUserInfo()
      }
    }
  },
  created() {
    this.getUserInfo()
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    async logout() {
      await this.$store.dispatch('user/logout')
      // location.reload()
      this.$router.push(`/login?redirect=${this.$route.fullPath}`)
    },
    getUserInfo() {
      this.userInfo = JSON.parse(localStorage.getItem('userInfo'))
      this.organName = this.userInfo.organIdListDesc
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  top: 0;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  z-index: 999;

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

      .avatar-wrapper {
        position: relative;
        .user-info{
          display: inline-block;
          vertical-align: middle;
          line-height: 1;
          margin: 0 3px;
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
</style>
