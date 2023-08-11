
import Vue from 'vue'
import { getTrackingID } from '@/api/common'
import ba from 'vue-ba'

export function baiduAnalytics() {
  getTrackingID().then(res => {
    try {
      if (res.code === 0) {
        const trackingID = res.result
        if (trackingID !== '') {
          Vue.use(ba, trackingID)
        }
      }
    } catch (error) {
      console.log(error)
    }
  })
}
