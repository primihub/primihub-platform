import Vue from 'vue'
import { getHomepage } from '@/api/system'
import WebTracing, { options } from '@web-tracing/vue2'

export function setUpTracing() {
  const developEnv = process.env.NODE_ENV === 'development'
  getHomepage().then(res => {
    const isOpenTracing = res.result.isOpenTracing
    if (!developEnv && isOpenTracing) {
      Vue.use(WebTracing, {
        dsn: '/ali/log/save',
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
    }
  })
}
