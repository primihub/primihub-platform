import defaultSettings from '@/settings'
import { getHomepage, changeHomepage } from '@/api/system'

const { fixedHeader, sidebarLogo, title, isHideFadeBack, isHideFooterVersion, footerText, favicon, logoUrl: defaultLogoUrl, loginLogoUrl, isHideAppMarket, isShowLogo, isOpenTracing } = defaultSettings

const baseUrl = '/'

const formatImgUrl = (data) => {
  const prefix = '/data/upload/2'
  if (data && data.indexOf(prefix) !== -1) {
    return baseUrl + 'assets' + data.substring(prefix.length)
  } else {
    return data
  }
}

const state = {
  fixedHeader,
  sidebarLogo,
  favicon,
  loginLogoUrl,
  logoUrl: defaultLogoUrl,
  logoTitle: '',
  loginDescription: '',
  title,
  logo: '',
  isHideFadeBack,
  isHideAppMarket,
  isHideFooterVersion,
  showLogoTitle: true, // 1: 添加 2:不添加
  settingChanged: false,
  footerText,
  loaded: false,
  isShowLogo,
  isOpenTracing,
  isHideBigModel: true,
  isHideNodeMap: true
}

const mutations = {
  CHANGE_SETTING: (state, { key, value }) => {
    state[key] = value
  },
  SET_LOGO_STATUS: (state, status) => {
    state.isShowLogo = status
  },
  SET_TRACING_STATUS: (state, status) => {
    state.isOpenTracing = status
  },
  SET_NODE_MAP_STATUS: (state, status) => {
    console.log('SET_NODE_MAP_STATUS', status)
    state.isHideNodeMap = status
  },
  SET_CHANGE_STATUS: (state, status) => {
    state.settingChanged = status
  },
  SET_REQUEST_STATUS: (state, status) => {
    state.loaded = status
  },
  SET_FOOTER_TEXT: (state, text) => {
    state.footerText = text
  },
  SET_LOGO_URL: (state, logoUrl) => {
    logoUrl = formatImgUrl(logoUrl)
    state.logoUrl = logoUrl
  },
  SET_LOGIN_LOGO_URL: (state, logoUrl) => {
    logoUrl = !logoUrl ? defaultLogoUrl : formatImgUrl(logoUrl)
    state.loginLogoUrl = logoUrl
  },
  SET_LOGO_TITLE: (state, title) => {
    state.logoTitle = title
  },
  SET_FAVICON: (state, favicon) => {
    favicon = formatImgUrl(favicon)
    state.favicon = favicon
    changePageFavicon(favicon)
  },
  SET_DESCRIPTION: (state, desc) => {
    state.loginDescription = desc
  },
  SET_FADE_BACK_STATUS: (state, status) => {
    state.isHideFadeBack = status
  },
  SET_APP_MARKET_STATUS: (state, status) => {
    state.isHideAppMarket = status
  },
  SET_BIG_MODEL_STATUS: (state, status) => {
    state.isHideBigModel = status
  },
  SET_VERSION_STATUS: (state, status) => {
    state.isHideFooterVersion = status
  },
  SET_SHOW_LOGO_STATUS: (state, status) => {
    state.showLogoTitle = status
  }
}

const actions = {
  changeSetting({ commit }, data) {
    commit('CHANGE_SETTING', data)
  },
  async getHomepage({ commit }) {
    const res = await getHomepage()
    if (res.code === 0 && res.result && Object.keys(res.result).length > 0) {
      const { favicon = state.favicon, logoUrl = state.logoUrl, loginDescription = state.loginDescription, isHideFadeBack = state.isHideFadeBack, isHideFooterVersion = state.isHideFooterVersion, logoTitle = state.logoTitle, showLogoTitle = state.showLogoTitle, loginLogoUrl = state.loginLogoUrl, footerText = state.footerText, isHideAppMarket = state.isHideAppMarket, isShowLogo = state.isShowLogo, isHideBigModel = state.isHideBigModel, isOpenTracing = state.isOpenTracing } = res.result
      console.log('isOpenTracing', isOpenTracing)
      commit('SET_LOGO_STATUS', isShowLogo)
      commit('SET_TRACING_STATUS', isOpenTracing)
      commit('SET_FAVICON', favicon)
      commit('SET_LOGO_URL', logoUrl)
      commit('SET_LOGIN_LOGO_URL', loginLogoUrl)
      commit('SET_DESCRIPTION', loginDescription)
      commit('SET_FADE_BACK_STATUS', isHideFadeBack)
      commit('SET_APP_MARKET_STATUS', isHideAppMarket)
      commit('SET_BIG_MODEL_STATUS', isHideBigModel)
      commit('SET_VERSION_STATUS', isHideFooterVersion)
      commit('SET_LOGO_TITLE', logoTitle)
      commit('SET_SHOW_LOGO_STATUS', showLogoTitle)
      commit('SET_FOOTER_TEXT', footerText)
    }
    commit('SET_REQUEST_STATUS', true)
  },
  async changeHomepage({ commit }, { state, mutation, value }) {
    const params = {}
    params[state] = value
    const res = await changeHomepage(params)
    if (res.code === 0) {
      commit(mutation, value)
      commit('SET_CHANGE_STATUS', true)
    }
  }
}

function changePageFavicon(favicon) {
  const link = document.querySelector("link[rel*='icon']")
  link.href = favicon === '' ? 'data:;' : favicon
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

