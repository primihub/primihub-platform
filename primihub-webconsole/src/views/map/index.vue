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
        zoom: 2.2,
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
                  title: item.globalName
                }
              })
            })
          } catch (err) {
            console.log('error==', err)
          }

          const map = this.map

          // Add a new source from our GeoJSON data and
          // set the 'cluster' option to true. GL-JS will
          // add the point_count property to your source data.
          map.addSource('earthquakes', {
            type: 'geojson',
            // Point to GeoJSON data. This example visualizes all M1.0+ earthquakes
            // from 12/22/15 to 1/21/16 as logged by USGS' Earthquake hazards program.
            data: geoJson,
            cluster: true,
            clusterMaxZoom: 14, // Max zoom to cluster points on
            clusterRadius: 50 // Radius of each cluster when clustering points (defaults to 50)
          })

          map.addLayer({
            id: 'clusters',
            type: 'circle',
            // type: "symbol",
            source: 'earthquakes',
            filter: ['has', 'point_count'],
            paint: {
              // Use step expressions (https://docs.mapbox.com/mapbox-gl-js/style-spec/#expressions-step)
              // with three steps to implement three types of circles:
              //   * Blue, 20px circles when point count is less than 100
              //   * Yellow, 30px circles when point count is between 100 and 750
              //   * Pink, 40px circles when point count is greater than or equal to 750
              'circle-color': ['step', ['get', 'point_count'], '#51bbd6', 100, '#f1f075', 750, '#f28cb1'],
              'circle-radius': ['step', ['get', 'point_count'], 20, 100, 30, 750, 40]

            }

          })

          map.addLayer({
            id: 'cluster-count',
            type: 'symbol',
            source: 'earthquakes',
            filter: ['has', 'point_count'],
            layout: {
              'text-field': ['get', 'point_count_abbreviated'],
              'text-font': ['DIN Offc Pro Medium', 'Arial Unicode MS Bold'],
              'text-size': 12
            }
          })

          const imgUrl = require('@/assets/icon-fill.png')
          map.loadImage(imgUrl, (err, image) => {
            if (err) throw err
            map.addImage('store-icon', image)
            map.addLayer({
              id: 'unclustered-point',
              type: 'symbol',
              source: 'earthquakes',
              filter: ['!', ['has', 'point_count']],
              'layout': {
                'icon-image': 'store-icon',
                'icon-size': 0.12,
                'text-field': ['get', 'title'],
                'text-offset': [0, 1.25],
                'text-anchor': 'top'
              }
            })
          })

          // inspect a cluster on click
          map.on('click', 'clusters', (e) => {
            const features = map.queryRenderedFeatures(e.point, {
              layers: ['clusters']
            })
            const clusterId = features[0].properties.cluster_id
            map
              .getSource('earthquakes')
              .getClusterExpansionZoom(clusterId, (err, zoom) => {
                if (err) return

                map.easeTo({
                  center: features[0].geometry.coordinates,
                  zoom: zoom
                })
              })
          })

          // When a click event occurs on a feature in
          // the unclustered-point layer, open a popup at
          // the location of the feature, with
          // description HTML from its properties.
          map.on('click', 'unclustered-point', (e) => {
            const coordinates = e.features[0].geometry.coordinates.slice()

            // Ensure that if the map is zoomed out such that
            // multiple copies of the feature are visible, the
            // popup appears over the copy being pointed to.
            while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
              coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360
            }
          })

          map.on('mouseenter', 'clusters', () => {
            map.getCanvas().style.cursor = 'pointer'
          })
          map.on('mouseleave', 'clusters', () => {
            map.getCanvas().style.cursor = ''
          })
        })
      })
    }
  }
}
</script>

<style scoped>
    .custom-data{
        color:#51bbd6;
        color:#f1f075;
        color:#f28cb1;
    }
</style>
