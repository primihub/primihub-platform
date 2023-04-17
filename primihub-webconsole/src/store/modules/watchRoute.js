
const state = {
  currentPath: ''
}

const mutations = {
  SET_PATH: (state, url) => {
    console.log('触发改变url==', url)
    state.currentPath = url
  }
}

const actions = {
  // getPATH
  getPath({ commit }, pathURL) {
    commit('SET_PATH', pathURL)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

