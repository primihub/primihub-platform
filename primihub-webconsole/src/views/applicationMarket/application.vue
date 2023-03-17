<template>
  <div v-if="appName">
    <navbar />
    <TopBanner />
    <div class="container">
      <section>
        <h2><span>{{ appTitle }}</span></h2>
        <component :is="appName" />
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
    PIR: () => import('./components/PIR.vue'),
    PSI: () => import('./components/PSI.vue'),
    reasoning: () => import('./components/reasoning.vue'),
    ADPrediction: () => import('./components/ADPrediction.vue'),
    UserPortrait: () => import('./components/UserPortrait.vue'),
    TopBanner
  },
  data() {
    return {
    }
  },
  computed: {
    ...mapState('application', [
      'appTitle'
    ])
  },
  created() {
    this.appName = this.$route.params.name || ''
    this.getAppInfo(this.appName)
  },
  methods: {
    init() {
      this.appName = this.$route.params.name || ''
      const currentApplication = this.data.find(item => item.appName === this.appName)
      this.appTitle = currentApplication?.appTitle
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
    width: 1000px;
    margin: 20px auto;
    padding: 50px 30px;
  }
  .icon{
    width: 91px;
    height: 100px;
    background: url(data:image/png;base64,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) no-repeat;
    background-size: contain;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
