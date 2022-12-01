import defaultSettings from '@/settings'
import { getHomepage, changeHomepage } from '@/api/system'

const { fixedHeader, sidebarLogo, title, isHideFadeBack, isHideFooterVersion, footerText, favicon, logoUrl: defaultLogoUrl, loginLogoUrl, logoTitle } = defaultSettings

const baseUrl = ''

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
  logoTitle,
  loginDescription: '',
  title,
  logo: '',
  isHideFadeBack,
  isHideFooterVersion,
  showLogoTitle: 1, // 1: 添加 2:不添加
  settingChanged: false,
  footerText
}

const mutations = {
  CHANGE_SETTING: (state, { key, value }) => {
    state[key] = value
  },
  SET_CHANGE_STATUS: (state, status) => {
    state.settingChanged = status
  },
  SET_FOOTER_TEXT: (state, text) => {
    state.footerText = text
  },
  SET_TITLE: (state, title) => {
    state.title = title
    changePageTitle(title)
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
    if (res.code === 0 && Object.keys(res.result).length > 0) {
      const { title = '', favicon, logoUrl, loginDescription = '', isHideFadeBack = state.true, isHideFooterVersion, logoTitle, showLogoTitle, loginLogoUrl, footerText } = res.result

      commit('SET_TITLE', title || state.title)
      commit('SET_FAVICON', favicon || defaultSettings.favicon)
      commit('SET_LOGO_URL', logoUrl || state.logoUrl)
      commit('SET_LOGIN_LOGO_URL', loginLogoUrl || state.loginLogoUrl)
      commit('SET_DESCRIPTION', loginDescription || state.loginDescription)
      commit('SET_FADE_BACK_STATUS', isHideFadeBack === undefined ? state.isHideFadeBack : isHideFadeBack)
      commit('SET_VERSION_STATUS', isHideFooterVersion === undefined ? state.isHideFooterVersion : isHideFooterVersion)
      commit('SET_LOGO_TITLE', logoTitle || state.logoTitle)
      commit('SET_SHOW_LOGO_STATUS', showLogoTitle === undefined ? state.showLogoTitle : showLogoTitle)
      commit('SET_FOOTER_TEXT', footerText || state.footerText)
    }
  },
  changeHomepage({ commit }, { state, mutation, value }) {
    return new Promise((resolve, reject) => {
      const params = {}
      if (!params[state]) {
        params[state] = ''
      }
      params[state] = value
      changeHomepage(params).then(res => {
        if (res.code === 0) {
          commit(mutation, value)
          commit('SET_CHANGE_STATUS', true)
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

