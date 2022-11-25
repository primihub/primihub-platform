import defaultSettings from '@/settings'
import { getHomepage, changeHomepage } from '@/api/system'

const { fixedHeader, sidebarLogo, title: defaultTitle } = defaultSettings

const baseUrl = 'http://test1.primihub.com/'

const formatImgUrl = (data) => {
  const prefix = '/data/upload/2'
  return baseUrl + 'assets' + data.substring(prefix.length)
}

const state = {
  fixedHeader: fixedHeader,
  sidebarLogo: sidebarLogo,
  favicon: '',
  loginLogoUrl: '',
  logoUrl: '',
  logoTitle: '',
  loginDescription: '',
  title: '',
  name: '',
  logo: '',
  isHideFadeBack: true,
  isHideFooterVersion: true,
  showLogoTitle: 2, // 1: 添加 2:不添加
  settingChanged: false
}

const mutations = {
  CHANGE_SETTING: (state, { key, value }) => {
    state[key] = value
  },
  SET_CHANGE_STATUS: (state, status) => {
    state.settingChanged = status
  },
  SET_TITLE: (state, title) => {
    title = title === '' ? defaultTitle : title
    state.title = title
    changePageTitle(title)
  },
  SET_LOGO_URL: (state, logoUrl) => {
    if (logoUrl && logoUrl.indexOf(baseUrl) === -1) {
      logoUrl = formatImgUrl(logoUrl)
    }
    state.logoUrl = logoUrl
  },
  SET_LOGIN_LOGO_URL: (state, logoUrl) => {
    if (logoUrl && logoUrl.indexOf(baseUrl) === -1) {
      logoUrl = formatImgUrl(logoUrl)
    }
    state.loginLogoUrl = logoUrl
  },
  SET_LOGO_TITLE: (state, title) => {
    state.logoTitle = title
  },
  SET_FAVICON: (state, favicon) => {
    if (favicon && favicon.indexOf(baseUrl) === -1) {
      favicon = formatImgUrl(favicon)
    }
    state.favicon = favicon
    changePageFavicon(favicon)
  },
  SET_DESCRIPTION: (state, desc) => {
    state.loginDescription = desc
  },
  SET_FADE_BACK_STATUS: (state, status) => {
    state.isHideFadeBack = status
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
    if (res.code === 0 && res.result) {
      const { title = '', favicon, logoUrl, loginDescription, isHideFadeBack, isHideFooterVersion, logoTitle, showLogoTitle, loginLogoUrl } = res.result

      commit('SET_CHANGE_STATUS', false)
      commit('SET_TITLE', title)
      commit('SET_FAVICON', !favicon ? state.favicon : favicon)
      commit('SET_LOGO_URL', !logoUrl ? state.logoUrl : logoUrl)
      commit('SET_LOGIN_LOGO_URL', !loginLogoUrl ? state.loginLogoUrl : loginLogoUrl)
      commit('SET_DESCRIPTION', loginDescription || state.loginDescription)
      commit('SET_FADE_BACK_STATUS', isHideFadeBack || state.isHideFadeBack)
      commit('SET_VERSION_STATUS', isHideFooterVersion || state.isHideFooterVersion)
      commit('SET_LOGO_TITLE', logoTitle)
      commit('SET_SHOW_LOGO_STATUS', !showLogoTitle ? state.showLogoTitle : showLogoTitle)
    }
  },
  changeHomepage({ commit }, data, type) {
    return new Promise((resolve, reject) => {
      changeHomepage(data).then(res => {
        if (res.code === 0) {
          const { title, favicon, logoUrl, loginDescription, isHideFadeBack, isHideFooterVersion, logoTitle, showLogoTitle, loginLogoUrl } = data

          commit('SET_CHANGE_STATUS', true)
          commit('SET_TITLE', title)
          commit('SET_FAVICON', favicon)
          commit('SET_LOGO_URL', logoUrl)
          commit('SET_LOGIN_LOGO_URL', loginLogoUrl)
          commit('SET_DESCRIPTION', loginDescription)
          commit('SET_FADE_BACK_STATUS', isHideFadeBack)
          commit('SET_VERSION_STATUS', isHideFooterVersion)
          commit('SET_LOGO_TITLE', logoTitle)
          commit('SET_SHOW_LOGO_STATUS', showLogoTitle)

          resolve()
        }
      }).catch(error => {
        console.log(error)
        reject(error)
      })
    })
  }
}

function changePageFavicon(favicon) {
  const link = document.querySelector("link[rel*='icon']")
  link.href = favicon === '' ? 'data:;' : favicon
}

function changePageTitle(title) {
  const subTitle = document.title.split('-')[0]
  document.title = `${subTitle} - ${title}`
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

