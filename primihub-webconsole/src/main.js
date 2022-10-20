import Vue from 'vue'
import ElementUI from 'element-ui'
import VueAnalytics from 'vue-analytics'
import App from './App'
import store from './store'
import router from './router'
import { message } from '@/utils/resetMessage'
import 'normalize.css/normalize.css' // A modern alternative to CSS resets
import 'element-ui/lib/theme-chalk/index.css'
import '@/styles/index.scss' // global css
import '@/icons' // icon
import '@/permission' // permission control
import { getTrackingID } from '@/api/common'
import filter from '@/filters'

// common filter
Object.keys(filter).forEach(key => Vue.filter(key, filter[key]))

import defaultSettings from '@/settings'

getTrackingID().then(res => {
  const isProd = process.env.NODE_ENV === 'production'
  try {
    if (res.code === 0) {
      const trackingID = res.result
      if (trackingID !== '' && defaultSettings.googleAnalytics) {
        Vue.use(VueAnalytics, {
          id: trackingID,
          router,
          debug: {
            enabled: !isProd,
            sendHitTask: isProd
          }
        })
      }
    }
  } catch (error) {
    console.log(error)
  }
})

// set ElementUI lang to zh
Vue.use(ElementUI, {
  locale: {
    el: {
      // 整体覆盖
      pagination: {
        pagesize: '条/页',
        total: `共 {total} 条记录`,
        goto: '跳至',
        pageClassifier: '页'
      }
    }
  }
})
// if need english element-ui, you can set it like this
// Vue.use(ElementUI, { locale })

Vue.prototype.$message = message

Vue.config.productionTip = false

new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})
