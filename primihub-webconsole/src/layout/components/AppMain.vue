<template>
  <section class="app-main">
    <template v-if="!routePath">
      <hamburger :is-active="$store.getters.sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
      <breadcrumb class="breadcrumb-container" />
    </template>
    <transition name="fade-transform" mode="out-in">
      <router-view :key="key" />
    </transition>
  </section>
</template>

<script>
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'

export default {
  name: 'AppMain',
  components: {
    Breadcrumb,
    Hamburger
  },
  computed: {
    key() {
      return this.$route.path
    },
    routePath() {
      const path = this.$store.state.watchRouter.currentPath
      if (path.search('/map/index') > -1) {
        return true
      }
      return false
    }
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    }
  }
}
</script>

<style scoped>
.app-main {
  /*50 = navbar  */
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow: hidden;
  background-color: #f0f2f5;
  padding: 0 20px 20px 20px;
  top: 50px;
}
/* .fixed-header+.app-main {
  padding-top: 30px;
} */
</style>

<style lang="scss">
@import "~@/styles/variables.scss";
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 15px;
  }
}
.hamburger-container {
  line-height: 46px;
  height: 100%;
  float: left;
  cursor: pointer;
  transition: background .3s;
  -webkit-tap-highlight-color:transparent;
  padding: 0!important;
}
</style>
