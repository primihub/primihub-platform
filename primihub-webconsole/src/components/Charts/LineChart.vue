<template>
  <div :class="className" :xAxis="xAxis" :style="{height:height,width:width}" />
</template>

<script>
require('echarts/theme/macarons.js')
import resize from './mixins/resize'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent
} from 'echarts/components'
// 标签自动布局、全局过渡动画等特性
import { LabelLayout, UniversalTransition } from 'echarts/features'
// 引入 Canvas 渲染器，注意引入 CanvasRenderer 或者 SVGRenderer 是必须的一步
import { CanvasRenderer } from 'echarts/renderers'

// 注册必须的组件
echarts.use([
  TitleComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,
  LineChart,
  LabelLayout,
  UniversalTransition,
  CanvasRenderer
])

export default {
  mixins: [resize],
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '350px'
    },
    autoResize: {
      type: Boolean,
      default: true
    },
    chartData: {
      type: Array,
      required: true,
      default: () => []
    },
    xAxis: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      chart: null
    }
  },
  watch: {
    chartData(val) {
      if (val) {
        this.setOptions(this.xAxis, val)
      }
    },
    xAxis(val) {
      if (val) {
        this.setOptions(val, this.chartData)
      }
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart()
    })
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$el)
      this.setOptions(this.xAxis, this.chartData)
    },
    setOptions(xAxis, chartData) {
      this.chart && this.chart.clear()
      this.chart && this.chart.setOption({
        title: {
          text: '日曝光量'
        },
        xAxis: {
          name: '人数',
          type: 'category',
          data: xAxis,
          boundaryGap: false,
          axisLabel: {
            formatter: '{value}万'
          }
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '{value}万'
          },
          splitArea: {
            show: true,
            areaStyle: {
              color: ['rgba(250,250,250,0.1)', 'rgba(200,200,200,0.1)']
            }
          }
        },
        series: [
          {
            data: chartData,
            type: 'line',
            smooth: true,
            areaStyle: {},
            itemStyle: {
              color: '#5ab1ef'
            }
          }
        ]
      })
    }
  }
}
</script>
