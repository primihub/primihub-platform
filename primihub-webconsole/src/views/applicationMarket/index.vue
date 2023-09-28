<template>
  <div class="app-main">
    <navbar />
    <TopBanner />
    <div class="main">
      <el-row :gutter="20">
        <el-col v-for="(item) in data" :key="item.applicationId" :span="8">
          <el-card class="card" shadow="always" @click.native="toDetail(item.appName)">
            <h3><img class="icon-yingyong" src="../../assets/icon-yingyong.png" alt="" srcset="">{{ item.appTitle }}</h3>
            <p>上传者：{{ item.organName }}</p>
            <p>上传时间：{{ item.createTime }}</p>
            <p class="description">{{ item.description }}</p>

            <div class="card-footer">
              <span class="read-num">阅读数 {{ item.readNumber }}</span>
              <el-button type="primary" plain size="small">去体验 >></el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import Navbar from './components/ApplicationNavBar'
import TopBanner from './components/TopBanner.vue'

export default {
  name: 'ApplicationMarket',
  components: {
    Navbar,
    TopBanner
  },
  data() {
    return {
    }
  },
  computed: {
    ...mapState('application', ['data'])
  },
  created() {
    this.getReadNumber().then(res => {
      if (!res) {
        const type = []
        this.data.forEach(item => {
          type.push(item.appName)
        })
        this.getReadNumber({
          type: type.join(','),
          operation: 2
        })
      }
    })
  },
  methods: {
    toDetail(name) {
      this.getReadNumber({ type: name, operation: 1 })
      this.$router.push(`/applicationIndex/detail/${name}`)
    },
    ...mapActions('application', ['getReadNumber'])
  }
}
</script>

<style lang="scss" scoped>
.main{
  background-color: #fff;
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
}
.card{
  line-height: 1.5;
  cursor: pointer;
  margin: 10px 0;
  h3{
    border-bottom: 1px solid #dee0e3;
    padding-bottom: 10px;
    margin-block-start: 0;
    display: flex;
    align-items: center;
    .icon-yingyong{
      width: 20px;
      margin-right: 6px;
    }
  }
  .description{
    color: #666;
    margin: 10px 0 20px;
    height: 100px;
    overflow: hidden;
    font-size: 14px;
  }
  &-footer{
    display: flex;
    justify-content: space-between;
    align-items: center;
    .read-num{
      color: #1677FF;
      font-size: 14px;
    }
  }
}
</style>
