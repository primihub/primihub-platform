<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="15">
        <line-chart :chart-data="lineChartData" :x-axis="xAxis" />
      </el-col>
      <el-col :span="9">
        <div>
          <h2>人数覆盖</h2>
          <gauge-chart :chart-data="gaugeChartData" height="200px" width="100%" />
        </div>
        <div class="slider">
          <el-slider
            ref="sliderRef"
            v-model="sliderValue"
            :max="1200"
            :format-tooltip="formatTooltip"
            @change="handleSliderChange"
            @mouseleave="handleMouseLeave"
          />
          <p>说明：请拖动滑块修改人群覆盖数</p>
        </div>
      </el-col>
    </el-row>

  </div>
</template>

<script>
import LineChart from '@/components/Charts/LineChart.vue'
import GaugeChart from '@/components/Charts/GaugeChart.vue'

export default {
  name: 'ADPrediction',
  components: {
    GaugeChart,
    LineChart
  },
  data() {
    return {
      sliderValue: 200,
      lineChartData: [10, 20, 30, 50, 70, 62, 65],
      xAxis: [0, 200, 400, 600, 800, 1000, 1200]
    }
  },
  computed: {
    gaugeChartData() {
      return this.sliderValue * 10000
    }
  },
  watch: {
    sliderValue(val) {
      if (val) {
        this.getChartData()
      }
    }
  },
  mounted() {
    this.getChartData()
    console.log(this.xAxis)
    this.$nextTick(() => {
      // this.$refs.sliderRef.setPosition(20)
    })
  },
  methods: {
    getChartData() {
      const bigArr = [400, 410, 420, 430, 480, 550, 560]
      const length = this.lineChartData.length
      const start = Math.ceil(this.sliderValue / length)
      this.xAxis[0] = start
      if (this.sliderValue > 900) {
        this.lineChartData[0] = bigArr[0] + this.sliderValue * 0.2
      } else {
        this.lineChartData[0] = this.sliderValue * 0.2
      }

      for (let i = 1; i < length; i++) {
        this.xAxis[i] = this.xAxis[i - 1] + start
        if (this.sliderValue > 800) {
          this.lineChartData[i] = bigArr[i] + this.sliderValue * 0.2
        } else {
          this.lineChartData[i] = this.lineChartData[i - 1] + this.lineChartData[i - 1] * 0.2
          // this.lineChartData[0] = this.sliderValue * 0.8
        }
      }
      console.log('lineChartData:', this.lineChartData)
      console.log(this.xAxis)
    },
    formatTooltip(val) {
      return `${val}万`
    },
    handleSliderChange(value) {
      console.log('value', value)
      // this.xAxis = this.xAxis.map(item => {
      //   return item + 100
      // })
      this.lineChartData = this.lineChartData.map(item => {
        if (this.sliderValue > 800) {
          return item + (item * 0.2)
        } else {
          return item + (item * 0.5)
        }
      })
      console.log(this.xAxis)
    },
    handleMouseLeave() {}
  }
}
</script>

<style lang="scss" scoped>
.slider{
  position: relative;
  top: -20px;
  p{
    font-size: 14px;
    color: #999;
  }
}
</style>
