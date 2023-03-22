import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
import { getToken } from '@/utils/auth' // get token from cookie
import getPageTitle from '@/utils/get-page-title'

NProgress.configure({ showSpinner: false }) // NProgress Configuration

const whiteList = ['/register', '/login', '/forgotPwd', '/auth'] // no redirect whitelist
let flag = 0

router.beforeEach(async(to, from, next) => {
  // start progress bar
  NProgress.start()

  await store.dispatch('watchRouter/getPath', to.path)

  // set page title
  document.title = getPageTitle(to.meta.title)

  // determine whether the user has logged in
  const hasToken = getToken()
  if (to.matched.length === 0 && flag !== 0) { // 如果未匹配到路由
    console.log('未匹配到路由', to.path)
    Message({
      message: '暂无权限',
      type: 'warning'
    })
    next('/') // 不存在跳转首页
    NProgress.done()
  }
  if (hasToken) {
    if (to.path === '/login') {
      // if is logged in, redirect to the home page
      next({ path: '/' })
      NProgress.done()
    } else {
      if (flag === 0) { // 刷新后空白问题
        try {
          const permissionList = await store.dispatch('user/getPermission')
          await store.dispatch('permission/generateRoutes', permissionList)
          flag++
          next({ ...to, replace: true })
        } catch (error) {
          // remove token and go to login page to re-login
          await store.dispatch('user/resetToken')
          // Message.error(error || 'Has Error')
          next(`/login?redirect=${to.fullPath}`)
          NProgress.done()
        }
      } else {
        next()
      }
    }
  } else {
    /* has no token*/

    if (whiteList.indexOf(to.path) !== -1) {
      // debugger
      // in the free login whitelist, go directly
      next()
    } else {
      // other pages that do not have permission to access are redirected to the login page.
      next(`/login?redirect=${to.fullPath}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  // finish progress bar
  NProgress.done()
})
