<template>
  <div
    id="map"
    ref="mapBox"
    v-loading="loading"
    element-loading-text="地图数据加载中"
    element-loading-spinner="el-icon-loading"
    style="width: 100%; height: calc(100vh - 50px); "
  />
</template>
<script>
import { getCoordinates } from '@/api/map'
import polygonData2 from './geoJson.json'

const accessToken = 'EE16920807918671432B6E77949CCEA4BD397862C88E80FCE789FUWGY4ZG3919'
const theme = 'light'
const colors = ['#BDD0C5', '#A1CEA1', '#BE86BD', '#59A6C8', '#7DC3DA', '#78A5DC', '#9E76D1', '#41b6c4', '#6EA8EB', '#D0956F', '#D5B776', '#E2D6B3', '#E6EAB5', '#EDB57F', '#A1CEA1', '#78A5DC', '#A275A7']

export default {
  data() {
    return {
      loading: false,
      map: null,
      data: [],
      mapData: {
        type: 'FeatureCollection',
        name: 'Generalizer_Output',
        features: []
      },
      labelData: {
        type: 'FeatureCollection',
        name: 'Generalizer_Output',
        features: []
      }
    }
  },
  async created() {
    if (this.$store.state.settings.isHideNodeMap) {
      this.$router.push('/project/list')
      return
    }
    await this.getData()
    this.loading = true
    await this.loadMapJs1(`https://webapi.luokuang.com/maps?ak=${accessToken}&plugins=Scale,ToolBar,ControlBar,OverView,Autocomplete`)
    await this.loadMapJs1(`https://webapi.luokuang.com/mapvgl?ak=${accessToken}&v=1.2.6`)
    this.initMap()
    this.loading = false
  },
  destroyed() {
    this.map && this.map.destroy()
    this.map = null
  },
  methods: {
    loadMapJs(src, cb) {
      const script = document.createElement('script')
      script.type = 'text/javascript'
      script.src = src
      document.getElementsByTagName('head')[0].appendChild(script)
      // 引入成功
      if (cb) {
        script.onload = cb
      }
    },
    loadMapJs1(src) {
      return new Promise((resolve) => {
        const script = document.createElement('script')
        script.type = 'text/javascript'
        script.src = src
        document.getElementsByTagName('head')[0].appendChild(script)
        script.onload = () => {
          resolve()
        }
      })
    },
    // 脱敏
    replaceText(name) {
      if (name.length < 6) {
        return name.slice(0, 2) + '****'
      } else {
        return name.slice(0, 3) + '****' + name.substring(name.length - 3)
      }
    },
    async initMap() {
      console.log('initMap')
      let map = null; let mapv = null; let text = null
      const clientHeight = document.body.clientHeight
      const center = clientHeight > 769 ? [106.4124519634895, 37.9043382673205] : [106.39800739525646, 37.9079696505369]
      map = new LKMap.Map('map', {
        center: center,
        style: 'lkmap://styles/6aa705fd38b1486b8539c9971e03be56',
        zoom: clientHeight > 769 ? 3.7 : 3.4,
        cursor: 'auto'
      })
      this.map = map
      map.on('load', async() => {
        addLayerFun()
        // 添加事件
        mapv.on('mousemove', 'map-layer', (e) => {
          text && text.remove()
          if (e.features.length > 0) {
            addText(this.data.filter(item => item.province === e.features[0].properties.name), e.lngLat)
            updateLayer(e.features[0].properties)
          }
        })
        mapv.on('mouseleave', 'map-layer', (e) => {
          if (text) {
            text.remove()
            text = null
          }
          // 恢复样式
          resatStyle()
        })
      })
      const addLayerFun = async() => {
        // 渲染地图数据
        mapv = new mapvgl.PolyonLayer({
          map: map,
          data: this.mapData
        })
        mapv.addLayer({
          id: 'map-layer',
          style: {
            cursor: 'pointer',
            color: '#fdd0a9',
            showBorder: true,
            borderWidth: 1,
            borderColor: '#644A29'
          }
        })
        // 渲染地图上文字
        const layerObj = new mapvgl.LabelsLayer({
          map: map, // 添加到的地图对象
          data: this.labelData
        })
        // 添加图层
        layerObj.addLayer({
          id: 'label-layer',
          style: {
            content: ['get', 'name'], // 从数据中获取文字数据
            color: '#644A29',
            size: 11,
            cursor: 'pointer',
            borderWidth: 0.5,
            borderColor: '#000'
          }
        })
        initTool()
      }
      // 更新图层样式
      const updateLayer = (data) => {
        mapv.updateLayer({
          id: 'map-layer',
          style: {
            // 根据参数adcode设置透明度
            opacity: ['case', ['==', ['get', 'name'], data.name], 1, 0.9]
          }
        })
      }

      const initTool = () => {
        // 工具条
        const toolBarTool = new LKMap.ToolBar({
          position: 'top-left',
          showCompass: true,
          labels: true
        })
        map.addControl(toolBarTool)
      }
      // 恢复样式
      const resatStyle = () => {
        mapv.updateLayer({
          id: 'map-layer',
          style: {
            opacity: 0.9
          }
        })
      }
      // 添加文本框
      const addText = (textStr, position) => {
        text && text.remove()
        let content = `<h3 class="province-title">${textStr[0].province} ${textStr.length}个节点</h3>`
        textStr.forEach((item) => {
          const organName = this.replaceText(item.globalName)
          const onlineText = item.online ? '（在线）' : '（离线）'
          const containerClass = item.online ? `label-container ${theme}` : `label-container ${theme} offline`
          content += `<div class="${containerClass}">
                        <p>
                          <span class="marker-dot"></span>
                          <span>${organName}</span>${onlineText}
                        </p>
                      </div>`
        })
        text = new LKMap.Text({
          position: position,
          text: content, // 内容
          anchor: 'top', // 设置文本对齐方式
          offset: new LKMap.Pixel(10, 20),
          style: { // 自定义样式
            cursor: 'pointer',
            opacity: 1,
            padding: '12px',
            borderRadius: '4px',
            backgroundColor: '#fff',
            borderWidth: 0,
            boxShadow: '0px 2px 6px 0px rgba(97,113,166,0.2)',
            fontSize: '14px',
            color: '#333'
          }
        })
        text.setMap(map)
      }
    },
    async getData() {
      const mapDataFeatures = []
      const labelDataFeatures = []
      const { code, result } = await getCoordinates()
      if (code === 0) {
        this.data = result.sort((a, b) => { return b.online - a.online })
        for (let i = 0; i < this.data.length; i++) {
          const item = this.data[i]
          for (const key in polygonData2.features) {
            const province = polygonData2.features[key]
            const include = mapDataFeatures.findIndex(data => data.properties.name === item.province) !== -1
            if (province.properties.name === item.province && !include) {
              // 添加不同区域色块
              province.properties.color = colors[i]
              // map 数据
              mapDataFeatures.push(province)
              // map上文字数据
              labelDataFeatures.push({
                'type': 'Feature',
                'geometry': {
                  'type': 'Point',
                  'coordinates': province.properties.centroid ? province.properties.centroid : province.properties.center
                },
                'properties': {
                  'name': `${province.properties.name} (${this.data.filter(val => val.province === item.province).length})`,
                  'province': province.properties.province
                }
              })
            }
          }
        }
        this.$set(this.mapData, 'features', mapDataFeatures)
        this.$set(this.labelData, 'features', labelDataFeatures)
      }
    }
  }
}
</script>

<style lang="scss">
::v-deep(.el-loading-mask){
  background-color: #fff!important;
}
.lkmap-marker .LKmap-label-content .label-content-container{
  padding: 0!important;
  background-color: #fff!important;
}
.province-title{
  margin-bottom: 5px;
}

#map{
  background-color: #ffffff;
}

.label-container{
  display: flex;
  align-items: center;
  padding: 0 5px;
  line-height: 1.5;
  &.dark{
    background-color: rgba(0,0,0,.2);
    color: #fff;
    box-shadow: 0 1px 2px rgb(0 0 0 / 10%);
  }

  .marker-dot{
    border-radius: 50%;
    width: 6px;
    height: 6px;
    background-color: #36ab60;
    border: 1px solid #aad08f;
    display: inline-block;
    margin-right: 5px;
    // animation: 2s infinite flash;
    box-shadow: 0 1px 2px rgb(0 0 0 / 10%);
    vertical-align: middle;
  }
  &.offline{
    color: #999;
    .marker-dot{
      background-color: #F56C6C;
      border: 1px solid #F56C6C;
      animation: none;
    }
  }
}
@-webkit-keyframes flash {
  from,
  50%,
  to {
    opacity: 1;
  }

  25%,
  75% {
    opacity: 0;
  }
}
@keyframes flash {
  from,
  50%,
  to {
    opacity: 1;
  }

  25%,
  75% {
    opacity: 0;
  }
}

</style>
