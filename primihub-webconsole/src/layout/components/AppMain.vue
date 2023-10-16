<template>
  <section class="app-main" :class="{'padding':!routePath}">
    <div v-if="!routePath" class="flex justify-content-between">
      <div>
        <template>
          <hamburger :is-active="$store.getters.sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
          <breadcrumb class="breadcrumb-container" />
        </template>
      </div>
      <div v-if="!isHideFadeBack" id="guide" class="align-self-center flex">
        <!--        <div class="guide-item">-->
        <!--          <el-dropdown>-->
        <!--            <div type="primary" class="dropdown-title">-->
        <!--              <svg-icon icon-class="problem" class="icon"/>新手指引-->
        <!--            </div>-->
        <!--            <el-dropdown-menu slot="dropdown" >-->
        <!--              <el-dropdown-item><svg-icon icon-class="scene" class="icon" />功能场景</el-dropdown-item>-->
        <!--              <el-dropdown-item> <svg-icon icon-class="achieve" class="icon" />技术实现</el-dropdown-item>-->
        <!--              <el-dropdown-item> <svg-icon icon-class="cases" class="icon" />典型用例</el-dropdown-item>-->
        <!--              <el-dropdown-item> <svg-icon icon-class="extend" class="icon" />扩展阅读</el-dropdown-item>-->
        <!--            </el-dropdown-menu>-->
        <!--          </el-dropdown>-->
        <!--        </div>-->
        <!--        <div class="guide-item"> <svg-icon icon-class="problem" class="icon"/>常见问题</div>-->
        <div class="guide-item">
          <el-dropdown>
            <div type="primary" class="dropdown-title">
              <svg-icon icon-class="server" class="icon" />人工服务
            </div>
            <el-dropdown-menu slot="dropdown" class="drop-menu-item">
              <el-dropdown-item>
                <div class="contact-server">
                  <p>微信扫描二维码</p>
                  <img src="/static/img/assitant.001dc94b.png" alt="原语科技">
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <div class="guide-item"><a href="https://m74hgjmt55.feishu.cn/share/base/form/shrcnVHhIFLb4EJzOf4OXx0drLe" target="_blank"> <svg-icon icon-class="order" class="icon" />提交工单</a></div>
      </div>
    </div>
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
  data() {
    return {
      guideProp: null,
      img: require('../../assets/guide.png')
    }
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
    },
    isHideFadeBack() {
      return this.$store.state.settings.isHideFadeBack
    }
  },
  // watch: {
  //   '$route': {
  //     immediate: true,
  //     handler(newVal) {
  //       if (newVal.path !== '/map/index' && !this.isHideFadeBack) {
  //         this.showGuide()
  //       }
  //     }
  //   }
  // },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    showGuide() {
      const isGuide = window.localStorage.getItem('isShowGuide')
      const guideProp = document.createElement('div')
      this.guideProp = guideProp
      if (!isGuide) {
        this.$nextTick(() => {
          const guide = document.querySelector('#guide')
          guide.classList.add('grid-content')

          guideProp.style.position = 'fixed'
          guideProp.style.inset = 0
          guideProp.style.background = 'rgba(0,0,0, .75)'
          guideProp.style.zIndex = 100000

          const guideImg = document.createElement('img')
          guideImg.src = this.img
          guideImg.style.width = '400px'
          guideImg.style.display = 'block'

          const guideBtn = document.createElement('div')
          guideBtn.innerText = '我知道了'
          guideBtn.style.display = 'inline-block'
          guideBtn.style.border = '1px solid #fff'
          guideBtn.style.borderRadius = '8px'
          guideBtn.style.color = '#fff'
          guideBtn.style.padding = '6px 18px'
          guideBtn.style.cursor = 'pointer'
          guideBtn.style.marginTop = '20px'

          const guideContent = document.createElement('div')
          guideContent.style.position = 'absolute'
          guideContent.style.right = '180px'
          guideContent.style.top = '100px'
          guideContent.style.userSelect = 'none'
          guideContent.append(guideImg)
          guideContent.append(guideBtn)
          guideProp.append(guideContent)
          document.body.append(guideProp)

          guideContent.addEventListener('click', () => {
            guideProp.innerHTML = ''
            guideProp.style.display = 'none'
            this.guideProp = null
            guide.classList.remove('grid-content')
          })
        })
        window.localStorage.setItem('isShowGuide', true)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.app-main {
  /*60 = navbar  */
  min-height: calc(100vh - 60px);
  width: 100%;
  position: relative;
  overflow: hidden;
  background-color: #f0f2f5;
  top: 60px;
  &.padding{
     padding: 0 20px 20px 20px;
  }
  .align-self-center{
    align-self: center;
    font-size: 12px;
    color: #666;
    .guide-item{
      font-size: 12px;
      color: #666;
      cursor: pointer;
      margin: 6px 12px;
      .icon{
        font-size: 14px;
        margin-right: 8px;
      }
      &:first-child{
        .dropdown-title{
          &:after{
            content: '';
            display: inline-block;
            width: 0;
            height: 0;
            border-top: 4px solid #666;
            border-left: 4px solid transparent;
            border-right: 4px solid transparent;
            margin-left: 4px;
            vertical-align: middle;
          }
        }
      }
      .dropdown-title{
        font-size: 12px;
      }
    }
  }
  .grid-content {
    padding: 10px 0;
    background: #fff;
    border-radius: 4px;
    position: relative;
    z-index: 100001;
    &:before{
      content: '';
      display: block;
      position: absolute;
      inset: 0;
      background: transparent;
      z-index: 102;

    }
  }
}
.el-dropdown-menu{
  .el-dropdown-menu__item{
    &:hover:not(.is-disabled):hover{
      color: #666;
      background: #F6F6F6 !important;
    }
    margin: 0 12px;
    &:hover .icon{
      color: #4592ff;
    }
    .icon{
      font-size: 14px;
      margin-right: 8px;
    }
    .contact-server{
      img{
        width: 120px;
      }
      text-align: center;
    }
  }
  &.drop-menu-item{
    .el-dropdown-menu__item{
      &:hover:not(.is-disabled):hover{
        background: transparent !important;
      }
    }
  }
}
</style>

<style lang="scss">
@import "~@/styles/variables.scss";
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
