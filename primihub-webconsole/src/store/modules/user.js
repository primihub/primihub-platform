import { login, logout, authLogin } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { resetRouter } from '@/router'
const USER_INFO = 'userInfo'
const PER_KEY = 'primihubPer'

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
    organChange: false
  }
}

const state = getDefaultState()

const mutations = {
  SET_USER_INFO: (state, userInfo) => {
    Object.assign(state, userInfo)
    localStorage.setItem(USER_INFO, JSON.stringify(userInfo))
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
    console.log('SET_ORGAN_CHANGE', organChange)
    state.organChange = organChange
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
          resolve()
        }
      }).catch(error => {
        console.log(error)
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

