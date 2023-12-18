import { login, logout, authLogin } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { resetRouter } from '@/router'
const USER_INFO = 'userInfo'
const PER_KEY = 'primihubPer'
import { options } from '@web-tracing/vue2'

const getDefaultState = () => {
  return {
    permissionList: [],
    cTime: '', // 2022-03-25 09:55:53
    ctime: '', // 2022-03-25 09:55:53
    isEditable: 1,
    isForbid: 0,
    userAccount: '',
    userId: null,
    userName: '',
    userOrganId: '',
    userOrganName: '',
    token: getToken(),
    avatar: '',
    organChange: false,
    showValidation: false,
    registerType: 0
  }
}

const state = getDefaultState()

const setUserInfo = () => {
  localStorage.setItem(USER_INFO, JSON.stringify(state.userInfo))
}

const mutations = {
  SET_USER_INFO: (state, userInfo) => {
    Object.assign(state, userInfo)
    state.userInfo = userInfo
    setUserInfo()
  },
  SET_USER_ORGAN_ID: (state, organId) => {
    state.userOrganId = organId
  },
  SET_USER_NAME: (state, name) => {
    state.userName = name
  },
  SET_USER_ORGAN_NAME: (state, name) => {
    state.userOrganName = name
  },
  SET_PERMISSION(state, list) {
    localStorage.setItem(PER_KEY, JSON.stringify(list))
    state.permissionList = list
  },
  RESET_STATE: (state) => {
    Object.assign(state, getDefaultState())
  },
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
  },
  SET_ORGAN_CHANGE: (state, organChange) => {
    state.organChange = organChange
  },
  SET_USER_ACCOUNT: (state, userAccount) => {
    state.userAccount = userAccount
    state.userInfo.userAccount = state.userAccount
    setUserInfo()
  },
  SET_REGISTER_TYPE: (state, registerType) => {
    state.registerType = registerType
    state.userInfo.registerType = state.registerType
    setUserInfo()
  },
  SET_SHOW_VALIDATION: (state, showValidation) => {
    state.showValidation = showValidation
  }
}

const actions = {
  // user login
  authLogin({ commit }, userInfo) {
    return new Promise((resolve, reject) => {
      authLogin(userInfo).then(({ result, code }) => {
        if (code === 0) {
          const { sysUser = {}, token, grantAuthRootList } = result

          commit('SET_USER_INFO', sysUser)
          commit('SET_PERMISSION', grantAuthRootList)
          setToken(token)
          resolve()
        }
      }).catch(error => {
        console.log(error)
        reject(error)
      })
    })
  },
  // user login
  login({ commit }, userInfo) {
    return new Promise((resolve, reject) => {
      login(userInfo).then(({ result, code }) => {
        if (code === 0) {
          const { sysUser = {}, token, grantAuthRootList } = result

          commit('SET_USER_INFO', sysUser)
          commit('SET_PERMISSION', grantAuthRootList)
          setToken(token)
          if (options) {
            options.value.userUuid = sysUser.userId
          }
          resolve()
        } else if ((code === 109 && result > 3)) {
          commit('SET_SHOW_VALIDATION', true)
        } else if (code === 121) {
          commit('SET_SHOW_VALIDATION', true)
        } else {
          commit('SET_SHOW_VALIDATION', false)
        }
      }).catch(error => {
        console.log(error)
        commit('SET_SHOW_VALIDATION', false)
        reject(error)
      })
    })
  },
  // get user info
  getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      const userData = JSON.parse(localStorage.getItem(USER_INFO))
      commit('SET_USER_INFO', userData)
      commit('SET_USER_NAME', userData.userName)
      commit('SET_USER_ORGAN_ID', userData.organIdList)
      commit('SET_USER_ORGAN_NAME', userData.organIdListDesc)
      resolve(userData)
    })
  },
  getPermission({ commit, state }) {
    return new Promise((resolve, reject) => {
      const permissionList = JSON.parse(localStorage.getItem(PER_KEY))
      commit('SET_PERMISSION', permissionList)
      resolve(permissionList)
    })
  },

  // user logout
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      logout(state.token).then(() => {
        removeToken() // must remove  token  first
        resetRouter()
        location.reload() // 清除路由
        commit('RESET_STATE')
        resolve()
      }).catch(error => {
        reject(error)
      })
    })
  },

  // remove token
  resetToken({ commit }) {
    return new Promise(resolve => {
      removeToken() // must remove  token  first
      commit('RESET_STATE')
      resolve()
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

