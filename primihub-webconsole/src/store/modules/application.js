import data from '@/views/applicationMarket/mock/data.json'

const state = {
  data,
  appTitle: '',
  appDescription: '',
  detailImg: ''
}

const mutations = {
  SET_TITLE: (state, title) => {
    state.appTitle = title
  },
  SET_DESCRIPTION: (state, des) => {
    state.appDescription = des
  },
  SET_IMG: (state, src) => {
    state.detailImg = src
  }
}

const actions = {
  getAppInfo({ commit }, name) {
    const currentApp = state.data.find(item => item.appName === name)
    const { appTitle, description, detailImg } = currentApp
    commit('SET_TITLE', appTitle)
    commit('SET_DESCRIPTION', description)
    commit('SET_IMG', require('../../assets/' + detailImg))
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

