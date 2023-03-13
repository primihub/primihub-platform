<template>
  <div class="app-main">
    <navbar />
    <TopBanner />
    <div class="main">
      <el-row :gutter="20">
        <el-col v-for="(item) in data" :key="item.applicationId" :span="8">
          <el-card class="card" shadow="always" @click.native="toDetail(item.appName)">
            <h3>{{ item.appTitle }}</h3>
            <p>上传者：{{ item.organName }}</p>
            <p>上传时间：{{ item.createTime }}</p>
            <p class="description">{{ item.description }}</p>

            <div class="card-footer"> <el-tag type="primary" plain>阅读数 {{ item.readNumber }} </el-tag></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
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
  methods: {
    toDetail(name) {
      this.$router.push(`/applicationIndex/detail/${name}`)
    }
  }
}
</script>

<style lang="scss" scoped>
.top-container{
  text-align: center;
}
.main{
  background-color: #fff;
  padding: 30px;
}
.card{
  line-height: 1.5;
  cursor: pointer;
  margin: 10px 0;
  h3{
    border-bottom: 1px solid #dee0e3;
    padding-bottom: 10px;
    margin-block-start: 0;
  }
  .description{
    color: #666;
    margin: 10px 0;
    height: 100px;
    overflow: hidden;
  }
  &-footer{
    display: flex;
    justify-content: flex-end;
  }
}
</style>
