import Vue from 'vue'
import ElementUI from 'element-ui'
import App from './App'
import WebTracing, { options } from '@web-tracing/vue2'
import store from './store'
import router from './router'
import { message } from '@/utils/resetMessage'
import 'normalize.css/normalize.css' // A modern alternative to CSS resets
import 'element-ui/lib/theme-chalk/index.css'
import '@/styles/index.scss' // global css
import '@/icons' // icon
import '@/permission' // permission control
import { baiduAnalytics } from '@/utils/ba'
import filter from '@/filters'

import locale from 'element-ui/lib/locale/lang/zh-CN'

console.log('env', process.env.NODE_ENV)
const developEnv = process.env.NODE_ENV === 'development'
const dsn = developEnv ? 'http://192.168.99.10:32013/ali/log/save' : '/ali/log/save'

Vue.use(WebTracing, {
  dsn: dsn,
  appCode: 'primihub-platform',
  appName: '原语隐私计算平台',
  debug: !!developEnv,
  pv: true,
  performance: false,
  error: {
    core: true,
    server: false
  },
  event: true,
  recordScreen: false, // 是否开启录屏功能
  cacheMaxLength: 10,
  cacheWatingTime: 5000,
  scopeError: true
})
options.value.error = true
// common filter
Object.keys(filter).forEach(key => Vue.filter(key, filter[key]))

// add baidu analytics
baiduAnalytics()

// set pagination global options
locale.el.pagination = {
  pagesize: '条/页',
  total: `共 {total} 条记录`,
  goto: '跳至',
  pageClassifier: '页'
}
Vue.use(ElementUI, { locale })

Vue.prototype.$message = message

Vue.config.productionTip = false

new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})
