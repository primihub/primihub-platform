import data from '@/views/applicationMarket/mock/data.json'
import { readNumber, getMarketInfo } from '@/api/market'

const state = {
  data,
  appTitle: '',
  appDescription: '',
  detailImg: '',
  type: '',
  origin: '',
  marketInfo: {}
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
  },
  SET_LAYOUT: (state, layout) => {
    state.layout = layout
  },
  SET_DATA: (state, data) => {
    state.data = data
  },
  SET_ORIGIN: (state, origin) => {
    state.origin = origin
  },
  SET_MARKET: (state, info) => {
    state.marketInfo = info
  }
}

const actions = {
  getAppInfo({ commit }, name) {
    const currentApp = state.data.find(item => item.appName === name)
    const { appTitle, description, detailImg, layout } = currentApp
    commit('SET_TITLE', appTitle)
    commit('SET_DESCRIPTION', description)
    commit('SET_LAYOUT', layout)
    commit('SET_IMG', require('../../assets/' + detailImg))
  },
  getReadNumber({ commit }, params) {
    // type key PIR,PSI,reasoning,ADPrediction,UserPortrait
    return new Promise((resolve, reject) => {
      readNumber(params).then(res => {
        if (res.code === 0 && Object.keys(res.result).length > 0) {
          data.map(item => {
            if (res.result[item.appName]) {
              item.readNumber = res.result[item.appName]
            }
          })
          commit('SET_DATA', data)
          resolve(res.result)
        } else {
          resolve(null)
        }
      }).catch(error => {
        console.log(error)
        reject(error)
      })
    })
  },
  getLocationOrigin({ commit }) {
    /**
     *  https://node1.primihub.com/
     *  https://node2.primihub.com/
     *  https://node3.primihub.com/
     *  http://test1.primihub.com/
     *  host = 'node1' or 'node2' or 'node3' or 'test1'
     **/
    const host = window.location.host
    const origin = host.indexOf('.com') !== -1 ? host.split('.')[0] : 'other'
    commit('SET_ORIGIN', origin)
  },

  async getMarketInfo({ commit }) {
    const {result, code} = await getMarketInfo()
    if (code === 0) {
      commit('SET_MARKET', result)
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

