import Vue from 'vue'
import Vuex from 'vuex'
import getters from './getters'
import app from './modules/app'
import settings from './modules/settings'
import permission from './modules/permission'
import user from './modules/user'
import project from './modules/project'
import application from './modules/application'
import watchRouter from './modules/watchRoute'

Vue.use(Vuex)

const store = new Vuex.Store({
  modules: {
    app,
    settings,
    user,
    permission,
    project,
    application,
    watchRouter
  },
  getters
})

export default store
