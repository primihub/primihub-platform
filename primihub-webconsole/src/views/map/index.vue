<template>
  <div>
    <div ref="mapBox" style="width: 100%; height: calc(100vh - 75px); margin-top: 5px" />
  </div>
</template>
<script>
// import mapboxgl
import mapboxgl from 'mapbox-gl'
import 'mapbox-gl/dist/mapbox-gl.css'
// mapboxgl language
import MapboxLanguage from '@mapbox/mapbox-gl-language'

import { getOrgInfo, getCoordinates } from '@/api/map'

export default {
  data() {
    return {
      map: '',
      doms: {}
    }
  },
  mounted() {
    this.doms.Map = this.$refs.mapBox
    this.initMap()
  },
  methods: {
    initMap() {
      mapboxgl.accessToken =
        'pk.eyJ1IjoiemhhbmppbmdqaW5nIiwiYSI6ImNsZHNodWltZjF2cHkzdnFnYzhpc2dxa28ifQ.FnBt8nvjN6jtLpvJgU3Z6g'
      this.map = new mapboxgl.Map({
        container: this.doms.Map,
        style: 'mapbox://styles/mapbox/streets-v12', // style URL
        center: [116.407, 39.9042],
        zoom: 3,
        attributionControl: false,
        customAttribution: ''
      })

      this.map.addControl(new MapboxLanguage({ defaultLanguage: 'zh-Hans' }))

      const nav = new mapboxgl.NavigationControl()
      this.map.addControl(nav, 'top-right')
      this.map.addControl(new mapboxgl.FullscreenControl())
      this.map.on('load', () => {
        this.getData()
      })
    },
    getData() {
      getOrgInfo().then((res) => {
        const fusionMap = res.result.sysLocalOrganInfo.fusionMap
        const fusionMapKeys = Object.keys(fusionMap)
        getCoordinates({
          serverAddress: fusionMapKeys[0]
        }).then((res) => {
          // const dataList = res.result.dataList
          const dataList = [
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '青岛家电国创中心',
              'lat': 36.156178,
              'lon': 120.493318,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '杭州隐私计算实验室',
              'lat': 30.21421,
              'lon': 120.204729,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '郑州超算联合实验室',
              'lat': 34.819925,
              'lon': 113.548959,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '北京北航联合实验室',
              'lat': 39.9809427,
              'lon': 116.34097867654876,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '北京大禹智芯',
              'lat': 40.026803,
              'lon': 116.472013,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '北京江南天安',
              'lat': 39.977358,
              'lon': 116.385041,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '北京信联科技',
              'lat': 40.026832,
              'lon': 116.283893,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '南京信联科技',
              'lat': 31.958527,
              'lon': 118.846567,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '深圳微言科技',
              'lat': 22.575293,
              'lon': 114.070924,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '喀什深喀农牧',
              'lat': 39.476097,
              'lon': 75.996391,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '长沙超算中心',
              'lat': 28.178962,
              'lon': 112.950434,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '石家庄移动',
              'lat': 38.101321,
              'lon': 115.267165,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '常州北邮工业互联网研究院',
              'lat': 31.685021,
              'lon': 119.957698,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '广西桂林密码学联合实验室',
              'lat': 25.289893,
              'lon': 110.344571,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '石家庄中数智创',
              'lat': 38.037025,
              'lon': 114.569327,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': 'db540d4b-3ace-406d-9b10-ece67278c395',
              'globalName': '机构A',
              'lat': 38.0414,
              'lon': 114.4786,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': '2cad8338-2e8c-4768-904d-2b598a7e3298',
              'globalName': '机构B',
              'lat': 38.0414,
              'lon': 114.4786,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': '5c11d9dc-7d39-4d76-95fb-5f8da28388f4',
              'globalName': '机构C',
              'lat': 38.0414,
              'lon': 114.4786,
              'country': 'CN',
              'online': true
            },
            {
              'globalId': '5c11d9dc-7d39-4d76-95fb-5f8da28388f4',
              'globalName': '北京中信银行',
              'lat': 39.93848,
              'lon': 116.543397,
              'country': 'CN',
              'online': false
            },
            {
              'globalId': '5c11d9dc-7d39-4d76-95fb-5f8da28388f4',
              'globalName': '北京保险服务中心',
              'lat': 39.943247,
              'lon': 116.202213,
              'country': 'CN',
              'online': false
            },
            {
              'globalId': '5c11d9dc-7d39-4d76-95fb-5f8da28388f4',
              'globalName': '北京北理新源',
              'lat': 39.966138,
              'lon': 116.316577,
              'country': 'CN',
              'online': false
            },
            {
              'globalId': '5c11d9dc-7d39-4d76-95fb-5f8da28388f4',
              'globalName': '北京中国移动',
              'lat': 40.104674,
              'lon': 116.373778,
              'country': 'CN',
              'online': false,
              'offset': [0, -30],
              direction: 'bottom'
            },
            {
              'globalId': '5c11d9dc-7d39-4d76-95fb-5f8da28388f4',
              'globalName': '石家庄河北银行',
              'lat': 38.020874,
              'lon': 114.721829,
              'country': 'CN',
              'online': false
            },
            {
              'globalId': '5c11d9dc-7d39-4d76-95fb-5f8da28388f4',
              'globalName': '上海令牌云',
              'lat': 30.756125,
              'lon': 121.365173,
              'country': 'CN',
              'online': false
            }
          ]
          const geoJson = {
            type: 'FeatureCollection',
            crs: {
              type: 'name',
              properties: { name: 'urn:ogc:def:crs:OGC:1.3:CRS84' }
            },
            features: []
          }
          try {
            dataList.forEach((item) => {
              const lonlat = [item.lon, item.lat]
              geoJson.features.push({
                type: 'Feature',
                geometry: {
                  type: 'Point',
                  coordinates: lonlat
                },
                properties: {
                  title: item.globalName,
                  online: item.online
                }
              })
            })
          } catch (err) {
            console.log('error==', err)
          }

          const map = this.map

          dataList.forEach(function(marker) {
            console.log(marker.online)
            const color = marker.online ? '#36ab60' : '#8a8a8a'
            const marker_on = new mapboxgl.Marker({
              color,
              anchor: 'center',
              draggable: false
            }).setLngLat([marker.lon, marker.lat])
              .addTo(map)

            const el = marker_on.getElement()
            console.log(el)
            const div = document.createElement('div')
            div.className = marker.online ? 'marker-online' : 'marker-offline'
            div.innerHTML = `${marker.globalName}`
            const dot = document.createElement('span')
            const status = document.createElement('span')
            status.className = 'status-text'
            status.innerHTML = marker.online ? ' (在线)' : ' (离线)'
            dot.className = marker.online ? 'marker-dot' : 'marker-dot off'
            div.insertBefore(dot, div.firstChild)
            div.appendChild(status)
            new mapboxgl.Popup({ anchor: marker.direction ? marker.direction : 'bottom', offset: marker.offset || [0, -30], className: 'info', closeButton: false, closeOnClick: false })
              .setLngLat([marker.lon, marker.lat])
              .setDOMContent(div)
              .addTo(map)
          })
        })
      })
    }
  }
}
</script>

<style lang="scss">
.mapboxgl-popup-content{
  padding: 10px;
}
::v-deep .mapboxgl-marker{
  svg{
    height:35px
  }
}
.status-con{
  font-size: 12px;
  border-bottom: 1px solid #e9e9e9;
  margin-bottom: 5px;
  padding-left: 3px;
}
.marker-online{
  color: #333;
  font-size: 14px;
}
.marker-offline{
  color: #999;
  .status-text{
    color: #999;
  }
}
.marker-dot{
  border-radius: 50%;
  width: 6px;
  height: 6px;
  background-color: #36ab60;
  border: 1px solid #aad08f;
  display: inline-block;
  margin-right: 5px;
  animation: 2s infinite flash;
  box-shadow: 0 1px 2px rgb(0 0 0 / 10%);
  vertical-align: middle;
  &.off{
    background-color: #999;
    border: 1px solid #999;
    animation: none;
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
