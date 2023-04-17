
import router, { constantRoutes, asyncRoutes, resetRouter } from '@/router'

/**
 * 创建菜单路由
 * @param {*} routers 所有动态路由
 * @param {*} rootList 后端返回数据list
 * @returns 权限路由集合
 */

function getRoutes(routers, rootList) {
  const realRoutes = []
  let curRoutes = {}
  // 根据当前router authCode做比对
  const filter = (code) => {
    return rootList.find(cur => {
      return cur.authCode === code
    })
  }
  routers.forEach((item, index) => {
    const current = filter(item.name)
    if (current) {
      curRoutes = Object.assign({}, curRoutes, {
        name: item.name,
        path: item.path,
        component: item.component,
        meta: item.meta,
        hidden: item.hidden || false,
        redirect: item.redirect || ''
      })

      curRoutes.children = []
      if (item.children && item.children.length > 0) {
        item.children.forEach(item => {
          const current = filter(item.name)
          if (current) {
            curRoutes.children.push({
              name: item.name,
              path: item.path,
              component: item.component,
              meta: item.meta,
              hidden: item.hidden || false,
              redirect: item.redirect || ''
            })
          }
        })
      }
      realRoutes.push(curRoutes)
    }
  })
  return realRoutes
}

/**
 * 遍历权限树，获取authType为3的按钮权限
 * @param {*} list 后端返回权限list
 * @returns 按钮权限集合
 */
function filterButtonPermission(list) {
  list = list.filter(item => item.authType === 3)
  const result = []
  list.forEach(item => {
    result.push(item.authCode)
  })
  return result
}

const state = {
  routes: [],
  buttonPermissionList: []
}

const mutations = {
  SET_ROUTES(state, routes) {
    state.routes = constantRoutes.concat(routes)
  },
  SET_BUTTON_PERMISSION(state, list) {
    state.buttonPermissionList = list
  }
}

const actions = {
  generateRoutes({ commit }, authList) {
    return new Promise(resolve => {
      const buttonPermission = filterButtonPermission(authList)
      const accessedRoutes = getRoutes(asyncRoutes, authList)
      resetRouter() // 先清除路由再添加，防止重复添加
      router.addRoutes(accessedRoutes)

      commit('SET_BUTTON_PERMISSION', buttonPermission)
      commit('SET_ROUTES', accessedRoutes)
      resolve(accessedRoutes)
    })
  }
}
export default {
  namespaced: true,
  state,
  mutations,
  actions
}
