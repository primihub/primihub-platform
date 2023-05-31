<template>
  <div>
    <div ref="mapBox" style="width: 100%; height: calc(100vh - 50px); margin-top: 5px" />
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
        // projection: 'globe',
        center: [103.407, 31.9042],
        zoom: 4.5
      })

      this.map.addControl(new MapboxLanguage({ defaultLanguage: 'zh-Hans' }))

      const nav = new mapboxgl.NavigationControl()
      this.map.addControl(nav, 'top-right')
      this.map.addControl(new mapboxgl.FullscreenControl())
      this.map.on('load', () => {
        // this.map.setFog({})
        this.getData()
      })
    },
    getData() {
      getOrgInfo().then((res) => {
        const fusionMap = res.result.sysLocalOrganInfo.fusionMap
        if (!fusionMap) return
        const fusionMapKeys = Object.keys(fusionMap)
        getCoordinates({
          serverAddress: fusionMapKeys[0]
        }).then((res) => {
          const dataList = res.result.dataList
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
            // const color = marker.online ? '#F56C6C' : '#8a8a8a'
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
