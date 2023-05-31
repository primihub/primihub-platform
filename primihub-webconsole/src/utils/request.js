import axios from 'axios'
import qs from 'qs'
import { MessageBox, Loading } from 'element-ui'
import store from '@/store'
import { getToken } from '@/utils/auth'
import { message } from '@/utils/resetMessage'

let loadingInstance = null
let needLoadingRequestCount = 0

function startLoading() {
  if (needLoadingRequestCount === 0) {
    loadingInstance = Loading.service({
      lock: true,
      text: '数据加载中...',
      spinner: 'el-icon-loading',
      background: 'rgba(255,255,255,0.5)'
    })
  }
  needLoadingRequestCount++
}
function endLoading() {
  needLoadingRequestCount--
  if (needLoadingRequestCount <= 0) {
    loadingInstance && loadingInstance.close()
  }
}

// create an axios instance
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  // withCredentials: true, // send cookies when cross-domain requests
  timeout: 20 * 1000 // request timeout
})

const timestamp = new Date().getTime()
const nonce = Math.floor(Math.random() * 1000 + 1)

// request interceptor
service.interceptors.request.use(
  config => {
    if (config.showLoading === undefined) {
      config.showLoading = true
    }
    if (config.showLoading) {
      startLoading()
    }
    if (store.getters.token) {
      config.headers['token'] = getToken()
    }
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        timestamp,
        nonce,
        token: getToken()
      }
    } else if (config.method === 'post') {
      if (config.type === 'json') {
        config.headers['Content-Type'] = 'application/json;charset=UTF-8'
        config.data = JSON.stringify({
          ...config.data,
          timestamp,
          nonce,
          token: getToken()
        })
      } else {
        config.headers['Content-Type'] = 'application/x-www-form-urlencoded'
        const data = qs.parse(config.data)
        config.data = qs.stringify({
          ...data,
          timestamp,
          nonce,
          token: getToken()
        }, { allowDots: true })
      }
    }

    return config
  },
  error => {
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// response interceptor
service.interceptors.response.use(
  response => {
    if (response.config.showLoading === undefined) {
      response.config.showLoading = true
    }
    endLoading()
    const { data } = response
    const { code, msg, result } = data
    if (code !== 0) {
      if (code === -1 || code === 1001 || code === 1007 || code === 1013) {
        return data
      } else if (code === 100) {
        message({
          message: '缺少参数',
          type: 'warning'
        })
        return data
      } else if (code === 101) {
        message({
          message: '无效参数',
          type: 'warning'
        })
      } else if (code === 102) {
        MessageBox.confirm('登录失效，请重新登录', '确认信息', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning',
          closeOnClickModal: false
        }).then(() => {
          store.dispatch('user/resetToken').then(() => {
            location.reload()
          })
        })
      } else if (code === 103) {
        message({
          message: '暂无权限',
          type: 'warning'
        })
        return response.data
      } else if (code === 107) { // token失效
        message({
          message: '登录失效，请重新登录',
          type: 'error',
          duration: 2000
        })
        setTimeout(() => {
          window.location.reload()
        }, 2000)
      } else if (code === 109 || code === 119 || code === 120 || code === 118) { // 登录限制
        message({
          message: msg,
          type: 'error',
          duration: 5000
        })
        return data
      } else if (code === 121) {
        return data
      } else if (result?.sysLocalOrganInfo?.fusionMap) {
        return data
      } else {
        message({
          message: msg || '请求异常',
          type: 'error'
        })
        return data
      }
      return data
    } else {
      return data
    }
  },
  err => {
    if (err && err.response) {
      switch (err.response.status) {
        case 400: err.message = '请求错误(400)'; break
        case 401: this.$router.push('/login'); break
        case 403: err.message = '拒绝访问(403)'; break
        case 404: err.message = '请求出错(404)'; break
        case 408: err.message = '请求超时(408)'; break
        case 500: err.message = '服务器错误(500)'; break
        case 501: err.message = '服务未实现(501)'; break
        case 502: err.message = '网络错误(502)'; break
        case 503: err.message = '服务不可用(503)'; break
        case 504: err.message = '网络超时(504)'; break
        case 505: err.message = 'HTTP版本不受支持(505)'; break
        default: err.message = `连接出错(${err.response.status})!`
      }
    }
    console.log('err' + err.message) // for debug
    endLoading()
    message({
      message: err.message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(err)
  }
)

export default service
