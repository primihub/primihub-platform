<template>
  <div v-loading="listLoading" class="container">
    <!-- <h2>模型详情</h2> -->
    <div class="section">
      <h3>模型信息</h3>
      <el-descriptions>
        <el-descriptions-item label="模型名称">{{ model.modelName }}</el-descriptions-item>
        <el-descriptions-item label="模型描述">{{ model.modelDesc }}</el-descriptions-item>
        <el-descriptions-item label="模型模版">{{ model.modelType | modelTypeFilter }}</el-descriptions-item>
        <el-descriptions-item label="是否包含Y值"><el-tag type="mini" size="mini">{{ model.yValueColumn }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ model.createDate }}</el-descriptions-item>
        <el-descriptions-item v-if="hasModelDownloadPermission && taskState === 1" label="下载结果">
          <el-button class="download-button" type="text" size="mini" icon="el-icon-download" @click="download">下载结果</el-button>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="section">
      <h3>模型资源</h3>
      <el-table
        :data="modelResources"
        border
      >
        <el-table-column
          prop="resourceName"
          label="资源名称"
        />
        <el-table-column
          prop="organName"
          label="所属机构"
        />
        <el-table-column
          prop="fileNum"
          label="原始记录数"
        />
        <el-table-column
          prop="alignmentNum"
          label="对齐后记录数量"
        />
        <el-table-column
          prop="primitiveParamNum"
          label="原始变量数量"
        />
        <el-table-column
          prop="modelParamNum"
          label="入模变量数量"
        />
        <!-- <el-table-column
          prop=""
          label="操作"
        >
          <template>
            <el-button type="text">下载字段</el-button>
          </template>
        </el-table-column> -->
      </el-table>
    </div>
    <div class="section">
      <h3>模型耗时</h3>
      <el-descriptions :column="1" label-class-name="time-consuming-label" :colon="false">
        <el-descriptions-item label-class-name="total-time-label">
          <span class="el-icon-time" />
          <span class="total-time-label">总耗时:</span>
          <span v-if="model.totalTime" class="total-time">{{ model.totalTime | timeFilter }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-for="item in modelComponent" :key="item.componentId" :label="item.componentName">
          <el-progress :percentage="item.timeRatio * 100" status="success" class="progress-line" />
          <span class="el-icon-time" />
          <span>耗时 {{ item.timeConsuming | timeFilter }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </div>
    <div class="section">
      <h3>模型评估指标</h3>
      <el-descriptions class="quotas" :column="1" border :label-style="{'width':'300px'}">
        <el-descriptions-item label="① ROOT MEAN SQUARED ERROR">{{ anotherQuotas.rootMeanSquaredError }}</el-descriptions-item>
        <el-descriptions-item label="② MEAN SQUARED ERROR">{{ anotherQuotas.meanSquaredError }}</el-descriptions-item>
        <el-descriptions-item label="③ EXPLAINED VARIANCE">{{ anotherQuotas.explainedVariance }}</el-descriptions-item>
        <el-descriptions-item label="④ MEAN SQUARED LOG ERROR">{{ anotherQuotas.meanSquaredLogError }}</el-descriptions-item>
        <el-descriptions-item label="⑤ R2 SCORE">{{ anotherQuotas.r2Score }}</el-descriptions-item>
        <el-descriptions-item label="⑥ MEAN ABSOLUTE ERROR">{{ anotherQuotas.meanAbsoluteError }}</el-descriptions-item>
        <el-descriptions-item label="⑦ MEDIAN ABSOLUTE ERROR">{{ anotherQuotas.medianAbsoluteError }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <!-- <div class="section">
      <h3>模型ks值</h3>
      <line-chart :chart-data="lineChartData" />
    </div> -->
  </div>
</template>

<script>
import { getModelDetail, getModelPrediction } from '@/api/model'
import { getToken } from '@/utils/auth'
// import LineChart from '@/components/Charts/LineChart.vue'

export default {
  components: {
    // LineChart
  },
  filters: {
    quotaTypeFilter(type) {
      const typeMap = {
        1: '训练样本集',
        2: '测试样本集'
      }
      return typeMap[type]
    },
    timeFilter(time) {
      const hour = parseInt(time / 3600) < 10 ? '0' + parseInt(time / 3600) : parseInt(time / 3600)
      const min = parseInt(time % 3600 / 60) < 10 ? '0' + parseInt(time % 3600 / 60) : parseInt(time % 3600 / 60)
      const sec = parseInt(time % 3600 % 60) < 10 ? '0' + parseInt(time % 3600 % 60) : parseInt(time % 3600 % 60)
      return hour + ':' + min + ':' + sec
    }
  },
  data() {
    return {
      listLoading: false,
      model: {},
      modelQuotas: [],
      modelResources: [],
      modelId: 24,
      modelComponent: [],
      lineChartData: [],
      anotherQuotas: [],
      taskState: null
    }
  },
  computed: {
    hasModelDownloadPermission() {
      return this.$store.getters.buttonPermissionList.includes('ModelResultDownload')
    }
  },
  created() {
    this.fetchData()
    // this.getChartsData()
  },
  methods: {
    toProjectCreatePage() {
      this.$router.push({
        name: 'ProjectCreate'
      })
    },
    searchProject() {
      console.log('searchProject', this.searchName)
    },
    fetchData() {
      this.listLoading = true
      this.taskId = this.$route.params.id
      this.modelId = this.$route.query.modelId || 0
      getModelDetail({ taskId: this.taskId }).then((response) => {
        this.listLoading = false
        console.log('response.data', response.result)
        const { model, modelQuotas, modelResources, modelComponent, anotherQuotas, taskState } = response.result
        this.model = model
        this.anotherQuotas = anotherQuotas
        this.modelQuotas = modelQuotas
        this.modelResources = modelResources
        this.modelComponent = modelComponent
        this.taskState = taskState
      })
    },
    getChartsData() {
      getModelPrediction({ modelId: this.modelId }).then(res => {
        this.lineChartData = res.result.prediction
        console.log(this.lineChartData)
      })
    },
    async download() {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/task/downloadTaskFile?modelId=${this.modelId}&taskId=${this.taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    }
  }
}
</script>
<style lang="scss" scoped>
::v-deep .time-consuming-label {
  width: 100px;
}
.container {
  .total-time-label{
    font-size: 18px;
    margin-right: 10px;
  }
  .total-time{
    font-size: 20px;
  }
  .progress-line {
    width: 400px;
    margin-left: 10px;
  }
  .el-icon-time {
    color: #409EFF;
    margin-right: 3px;
  }
  .section {
    padding: 10px 30px 30px;
    border-radius: 20px;
    background-color: #fff;
    margin-bottom: 30px;
  }
  .img-container{
    display: flex;
    img{
      width: 500px;
      max-width: 100%;
    }
  }
}
::v-deep .el-table th{
  background: #fafafa;
}
::v-deep .el-descriptions-item__container{
  align-items: center;
}
.download-button{
  margin-left: 5px;
}
.quotas{
  max-width: 800px;
}
</style>
