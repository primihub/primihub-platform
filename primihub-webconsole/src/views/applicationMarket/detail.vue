<template>
  <div v-if="appName" v-loading="loading">
    <navbar />
    <TopBanner />
    <div class="container">
      <section class="detail">
        <div class="app-name">
          <h2><img class="icon-yingyong" src="../../assets/icon-yingyong.png" alt="" srcset="">{{ appTitle }}</h2>
          <p>Application introduction</p>
        </div>
        <div class="wrapper" :class="{'flex': layout === 'flex'}">
          <div class="img-container">
            <img :src="detailImg" alt="" srcset="">
          </div>
          <div class="desc">
            <ul>
              <li v-for="(item,index) in description" :key="index" class="desc-item">
                {{ item }}
              </li>
            </ul>
          </div>
        </div>
        <div class="buttons">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="toApplication">开始体验</el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import Navbar from './components/ApplicationNavBar'
import TopBanner from './components/TopBanner.vue'

export default {
  name: 'ApplicationDetail',
  components: {
    Navbar,
    TopBanner
  },
  data() {
    return {
      loading: false
    }
  },
  computed: {
    description() {
      return this.appDescription.split('<br/>')
    },
    ...mapState('application', [
      'appDescription',
      'appTitle',
      'detailImg',
      'layout'
    ])
  },
  created() {
    this.appName = this.$route.params.name || ''
    this.loading = true
    this.getAppInfo(this.appName)
    this.loading = false

    console.log(this.appDescription.split('<br/>'))
  },
  methods: {
    toApplication() {
      this.$router.push({
        name: 'Application',
        params: { name: this.appName }
      })
    },
    goBack() {
      this.$router.back()
    },
    ...mapActions('application', [
      'getAppInfo'
    ])
  }
}
</script>

<style lang="scss" scoped>
h2{
  margin-block-end: 0;
  margin-block-start: 10px;
}
ul{
  padding-inline-start: 0px;
}
.container{
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow: hidden;
  background-color: #f0f2f5;
  padding: 0 20px 20px 20px;
  section{
    background-color: #fff;
    max-width: 1200px;
    margin: 20px auto;
    padding: 30px 30px 70px;
  }
}
.wrapper{
  margin-bottom: 30px;
  padding: 0 50px;
  &.flex{
    display: flex;
    align-items: center;
    .img-container{
      width: 500px;
    }
    .desc{
      width: 700px;
      padding: 0 20px;
      .desc-item{
        font-size: 18px;
        line-height: 1.75;
        // letter-spacing: 0;
      }
    }
  }
}
.detail{
  .img-container{
    text-align: center;
    background: #fff;
    padding: 0 50px;
    img{
      width: 100%;
    }
  }
  .desc-item{
    font-size: 18px;
    line-height: 1.75;
    text-align: left;
    letter-spacing: .1em;
    color: #333b4a;
    margin: 10px 0;
    &::marker{
      color: #66b1ff;
    }
  }
}
.application-con{
  background-color: #fff;
  max-width: 1200px;
  padding: 30px;
  margin: 20px auto;
}
.buttons{
  display: flex;
  justify-content: center;
}
.icon-yingyong{
  width: 25px;
  margin-right: 6px;
}
.app-name{
  position: relative;
  height:80px;
  p{
    color: #999;
    font-size: 16px;
    margin-left: 31px;
  }
  h2{
    z-index: 2;
    display: flex;
    align-items: center;
  }
}

</style>
