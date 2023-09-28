import Vue from 'vue'
import ElementUI from 'element-ui'
import App from './App'
import store from './store'
import router from './router'
import { message } from '@/utils/resetMessage'
import 'normalize.css/normalize.css' // A modern alternative to CSS resets
import '@/theme/index.css'
import '@/styles/index.scss' // global css
import '@/icons' // icon
import '@/permission' // permission control
import { baiduAnalytics } from '@/utils/ba'
import filter from '@/filters'
import { setUpTracing } from '@/utils/webTracing'

import locale from 'element-ui/lib/locale/lang/zh-CN'

// common filter
Object.keys(filter).forEach(key => Vue.filter(key, filter[key]))

// add baidu analytics
baiduAnalytics()
// add web tracing
setUpTracing()
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
