<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script>
import resize from './mixins/resize'
import * as echarts from 'echarts/core'
import { GaugeChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent
} from 'echarts/components'
import { LabelLayout, UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'

// 注册必须的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,
  GaugeChart,
  LabelLayout,
  UniversalTransition,
  CanvasRenderer
])
export default {
  name: 'GaugeChart',
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
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      chart: null
    }
  },
  watch: {
    chartData: {
      deep: true,
      handler(val) {
        if (val) {
          console.log(val)
          this.setOptions(val)
        }
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
      console.log('chartData', this.chartData)
      this.setOptions(this.chartData)
    },
    setOptions() {
      this.chart.setOption({
        series: [
          {
            type: 'gauge',
            radius: '100%',
            startAngle: 180,
            endAngle: 0,
            min: 0,
            max: 12000000,
            axisLine: {
              lineStyle: {
                width: 20,
                color: [
                  [0.25, '#FF6E76'],
                  [0.5, '#FDDD60'],
                  [0.75, '#58D9F9'],
                  [1, '#7CFFB2']
                ]
              }
            },
            pointer: {
              icon: 'path://M2090.36389,615.30999 L2090.36389,615.30999 C2091.48372,615.30999 2092.40383,616.194028 2092.44859,617.312956 L2096.90698,728.755929 C2097.05155,732.369577 2094.2393,735.416212 2090.62566,735.56078 C2090.53845,735.564269 2090.45117,735.566014 2090.36389,735.566014 L2090.36389,735.566014 C2086.74736,735.566014 2083.81557,732.63423 2083.81557,729.017692 C2083.81557,728.930412 2083.81732,728.84314 2083.82081,728.755929 L2088.2792,617.312956 C2088.32396,616.194028 2089.24407,615.30999 2090.36389,615.30999 Z',
              length: '70%',
              width: 10,
              offsetCenter: [0, '-20%'],
              itemStyle: {
                color: 'inherit'
              }
            },
            splitLine: {
              show: false
            },
            axisTick: {
              show: false
            },
            axisLabel: {
              show: false,
              color: 'inherit',
              distance: 40,
              fontSize: 0
            },
            detail: {
              fontSize: 20,
              offsetCenter: [0, '30%'],
              valueAnimation: false,
              formatter: function(value) {
                return value / 10000 + '万人'
              },
              color: '#666666'
            },
            data: [
              {
                value: this.chartData
              }
            ]
          }
        ]
      })
    }
  }
}
</script>
