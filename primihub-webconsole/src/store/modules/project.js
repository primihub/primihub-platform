import { getProjectDetail } from '@/api/project'

const state = {
  status: null,
  detailResult: null
}

const mutations = {
  SET_STATUS: (state, status) => {
    state.status = status
  },
  SET_DETAIL_RESULT: (state, list) => {
    state.detailResult = list
  }
}

const actions = {
  // user login
  getProjectDetail({ commit }, projectId) {
    getProjectDetail({ projectId }).then(({ result }) => {
      commit('SET_STATUS', result.status)
      commit('SET_DETAIL_RESULT', result)
    }).catch(error => {
      console.log(error)
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

