<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script>
import vintage from 'echarts/theme/vintage'
// require('echarts/theme/vintage')
import resize from './mixins/resize'
import * as echarts from 'echarts/core'
import {
  TitleComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,
  TooltipComponent
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
  LabelLayout,
  UniversalTransition,
  CanvasRenderer,
  TooltipComponent
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
      default: '350px'
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
        this.setOptions()
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
      this.chart = echarts.init(this.$el, vintage)
      this.setOptions()
    },
    setOptions() {
      console.log(this.chartData)
      const xAxisData = this.chartData[0]
      const yAxisData = this.chartData[1]
      this.chart && this.chart.clear()
      this.chart && this.chart.setOption({
        title: {
          text: 'ROC Curve',
          left: 'center',
          textVerticalAlign: 'top',
          padding: 0,
          textStyle: {
            lineHeight: 66
          }
        },
        tooltip: {
          trigger: 'axis',
          show: true,
          backgroundColor: 'rgba(0,0,0,0.7)',
          borderWidth: 0,
          textStyle: {
            color: '#fff'
          },
          formatter: function(params) {
            const data = params[0].data
            return `<strong>fpr：</strong>${data[0]}<br/><strong>tpr：</strong>${data[1]}`
          }
        },
        xAxis: [
          {
            gridIndex: 0,
            min: 0,
            max: 1,
            name: 'False Positive Rate',
            nameGap: 15,
            nameLocation: 'middle',
            nameTextStyle: {
              verticalAlign: 'top',
              lineHeight: 50,
              color: '#333',
              fontSize: '14px'
            },
            splitLine: {
              show: true
            },
            boundaryGap: false
          }
        ],
        yAxis: [
          {
            gridIndex: 0,
            min: 0,
            max: 1,
            name: 'True Positive Rate',
            nameLocation: 'middle',
            nameTextStyle: {
              nameRotate: 180,
              lineHeight: 56,
              color: '#333',
              fontSize: '14px'
            },
            splitLine: {
              show: true
            }
            // splitArea: {
            //   show: true
            // }
          }
        ],
        series: [
          {
            name: 'I',
            type: 'line',
            itemStyle: {
              color: '#fac858'
            },
            data: xAxisData.map(function(value, index) {
              return [value, yAxisData[index]]
            }),
            markLine: {
              animation: false,
              silent: true,
              lineStyle: {
                type: 'dashed',
                color: '#999'
              },
              data: [
                [
                  {
                    coord: [0, 0],
                    symbol: 'none'
                  },
                  {
                    coord: [1, 1],
                    symbol: 'none'
                  }
                ]
              ]
            }
          }
        ]
      })
    }
  }
}
</script>
