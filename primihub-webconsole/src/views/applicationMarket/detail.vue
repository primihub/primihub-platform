<template>
  <div v-if="appName" v-loading="loading">
    <navbar />
    <TopBanner />
    <div class="container">
      <section class="detail">
        <h2>{{ appTitle }}</h2>
        <div style="margin: 30px; padding: 0 50px;">
          <div class="img-container">
            <img max-width="100%" :src="detailImg" alt="" srcset="">
          </div>
          <p>{{ appDescription }}</p>
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
    ...mapState('application', [
      'appDescription',
      'appTitle',
      'detailImg'
    ])
  },
  created() {
    this.appName = this.$route.params.name || ''
    this.loading = true
    this.getAppInfo(this.appName)
    this.loading = false
  },
  methods: {
    // init() {
    //   this.appName = this.$route.params.name || ''
    //   const currentApplication = this.data.find(item => item.appName === this.appName)
    //   this.description = currentApplication?.description
    //   this.appTitle = currentApplication?.appTitle
    //   this.detailImg = require(`../../assets/${currentApplication.detailImg}`)
    // },
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
.container{
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow: hidden;
  background-color: #f0f2f5;
  padding: 0 20px 20px 20px;
  section{
    background-color: #fff;
    width: 1400px;
    margin: 20px auto;
    padding: 30px 30px 70px;
  }
}
.detail{
  .img-container{
    text-align: center;
    background: #fff;
  }
  p{
    font-size: 20px;
    line-height: 1.75;
    text-align: left;
    letter-spacing: .1em;
  }
}
.application-con{
  background-color: #fff;
  width: 1200px;
  padding: 30px;
  margin: 20px auto;
}
.buttons{
  display: flex;
  justify-content: center;
}
</style>
