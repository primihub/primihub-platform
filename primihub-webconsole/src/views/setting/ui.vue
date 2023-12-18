<template>
  <div v-loading="loading" class="container">
    <div v-if="loaded" class="detail">
      <el-descriptions :column="1" label-class-name="detail-title">
        <el-descriptions-item label="是否显示logo">
          <el-radio-group v-model="isShowLogo" @input="handleShowLogoChange">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
        </el-descriptions-item>
        <el-descriptions-item label="favicon">
          <span class="favicon">
            <PHUpload :image-url="favicon" type="favicon" @success="handleAvatarSuccess" @remove="handleRemove" />
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="登录页logo">
          <PHUpload :image-url="loginLogoUrl" class="upload" type="loginLogoUrl" @success="handleAvatarSuccess" @remove="handleRemove" />
        </el-descriptions-item>
        <el-descriptions-item label="导航logo">
          <PHUpload :image-url="logoUrl" class="upload" type="logoUrl" @success="handleAvatarSuccess" @remove="handleRemove" />
        </el-descriptions-item>
        <el-descriptions-item :content-style="{'flex-direction': 'column'}" label="是否添加logo文字">
          <el-radio-group v-model="showLogoTitle" @input="handleShowLogoTitleChange">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
          <div v-if="showLogoTitle" class="logo-title">
            <el-input v-model="logoText" size="small" placeholder="请输入logo文字" clearable @clear="handleLogoTitleClear" @input="handleLogoTitleChange" />
            <el-button class="logo-button" type="primary" size="small" @click="saveLogoTitle">保存</el-button>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="登录页描述">
          <ConfirmInput name="loginDescription" type="textarea" :default-value="loginDescription" @submit="handleInputSubmit" />
        </el-descriptions-item>

        <el-descriptions-item label="隐藏建议与反馈">
          <el-switch v-model="isHideFadeBack" @change="handleFadeBackChange" />
        </el-descriptions-item>
        <el-descriptions-item label="隐藏应用市场">
          <el-switch v-model="isHideAppMarket" @change="handleAppMarketChange" />
        </el-descriptions-item>
        <el-descriptions-item label="隐藏底部版本号" :content-style="{'flex-direction': 'column'}">
          <el-switch v-model="isHideFooterVersion" @change="handleVersionChange" />
          <div v-if="!isHideFooterVersion" class="logo-title">
            <el-input v-model="theFooterText" size="small" placeholder="请输入" clearable @clear="handleLogoTitleClear" @input="handleFooterTextChange" />
            <el-button class="logo-button" type="primary" size="small" @click="saveFooterText">保存</el-button>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="隐藏大模型">
          <el-switch v-model="isHideBigModel" @change="handleBigModelChange" />
        </el-descriptions-item>
        <el-descriptions-item label="是否开启数据采集">
          <el-radio-group v-model="isOpenTracing" @input="handleTracingChange">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
        </el-descriptions-item>
        <el-descriptions-item label="是否隐藏分布式隐私计算服务网络">
          <el-radio-group v-model="isHideNodeMap" @input="handleNodeMapChange">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex'
import PHUpload from '@/components/PHUpload'
import ConfirmInput from '@/components/ConfirmInput'

export default {
  name: 'UI',
  components: {
    PHUpload,
    ConfirmInput
  },
  data() {
    return {
      loading: false,
      params: {},
      logoText: '',
      theFooterText: ''
    }
  },
  computed: {
    ...mapState('settings', [
      'loaded',
      'favicon',
      'settingChanged',
      'loginLogoUrl',
      'logoUrl',
      'loginDescription',
      'logoTitle',
      'footerText'
    ]),
    showLogoTitle: {
      get() {
        return this.$store.state.settings.showLogoTitle
      },
      set(val) {
        return val
      }
    },
    isShowLogo: {
      get() {
        console.log('store value', this.$store.state.settings.isShowLogo)
        return this.$store.state.settings.isShowLogo
      },
      set(val) {
        return val
      }
    },
    isOpenTracing: {
      get() {
        console.log('store value', this.$store.state.settings.isOpenTracing)
        return this.$store.state.settings.isOpenTracing
      },
      set(val) {
        return val
      }
    },
    isHideNodeMap: {
      get() {
        console.log('store value', this.$store.state.settings.isHideNodeMap)
        return this.$store.state.settings.isHideNodeMap
      },
      set(val) {
        return val
      }
    },
    isHideFooterVersion: {
      get() {
        return this.$store.state.settings.isHideFooterVersion
      },
      set(val) {
        return val
      }
    },
    isHideFadeBack: {
      get() {
        return this.$store.state.settings.isHideFadeBack
      },
      set(val) {
        return val
      }
    },
    isHideAppMarket: {
      get() {
        return this.$store.state.settings.isHideAppMarket
      },
      set(val) {
        return val
      }
    },
    isHideBigModel: {
      get() {
        return this.$store.state.settings.isHideBigModel
      },
      set(val) {
        return val
      }
    }
  },
  async created() {
    // await this.getHomepage()
    this.logoText = this.logoTitle
    this.theFooterText = this.footerText
    this.params = {
      isHideFadeBack: this.isHideFadeBack,
      isHideFooterVersion: this.isHideFooterVersion,
      logoUrl: this.logoUrl,
      loginDescription: this.loginDescription,
      favicon: this.favicon,
      logoTitle: this.logoTitle,
      loginLogoUrl: this.loginLogoUrl,
      showLogoTitle: this.showLogoTitle,
      isShowLogo: this.isShowLogo
    }
  },
  methods: {
    handleShowLogoTitleChange(val) {
      if (val === 2 && this.logoText !== '') {
        this.logoText = ''
        this.changeSettings({ state: 'logoTitle', mutation: 'SET_LOGO_TITLE', value: this.logoText }).then(() => {
          this.changeSettings({ state: 'showLogoTitle', mutation: 'SET_SHOW_LOGO_STATUS', value: val })
        })
      } else {
        this.changeSettings({ state: 'showLogoTitle', mutation: 'SET_SHOW_LOGO_STATUS', value: val })
      }
    },
    handleFadeBackChange(val) {
      this.params.isHideFadeBack = val
      this.changeSettings({ state: 'isHideFadeBack', mutation: 'SET_FADE_BACK_STATUS', value: val })
    },
    handleShowLogoChange(val) {
      this.params.isShowLogo = val
      this.changeSettings({ state: 'isShowLogo', mutation: 'SET_LOGO_STATUS', value: val })
    },
    handleTracingChange(val) {
      this.params.isOpenTracing = val
      this.changeSettings({ state: 'isOpenTracing', mutation: 'SET_TRACING_STATUS', value: val })
    },
    handleNodeMapChange(val) {
      this.params.isHideNodeMap = val
      this.changeSettings({ state: 'isHideNodeMap', mutation: 'SET_NODE_MAP_STATUS', value: val })
    },
    handleAppMarketChange(val) {
      this.params.isHideAppMarket = val
      this.changeSettings({ state: 'isHideAppMarket', mutation: 'SET_APP_MARKET_STATUS', value: val })
    },
    handleBigModelChange(val) {
      this.params.isHideBigModel = val
      this.changeSettings({ state: 'isHideBigModel', mutation: 'SET_BIG_MODEL_STATUS', value: val })
    },
    handleLogoTitleClear() {
      this.logoText = ''
      this.changeSettings({ state: 'logoTitle', mutation: 'SET_LOGO_TITLE', value: '' })
    },
    handleVersionChange(val) {
      this.params.isHideFooterVersion = val
      this.changeSettings({ state: 'isHideFooterVersion', mutation: 'SET_VERSION_STATUS', value: val })
    },
    handleLogoTitleChange(val) {
      this.logoText = val
    },
    handleFooterTextChange(val) {
      this.theFooterText = val
    },
    saveFooterText() {
      if (this.theFooterText === '') {
        this.$message.error('请输入footer文字')
        return
      }
      this.changeSettings({ state: 'footerText', mutation: 'SET_FOOTER_TEXT', value: this.theFooterText })
    },
    saveLogoTitle() {
      if (this.logoText === '') {
        this.$message.error('请输入logo标题')
        return
      }
      this.changeSettings({ state: 'logoTitle', mutation: 'SET_LOGO_TITLE', value: this.logoText })
    },
    handleRemove({ type }) {
      if (type === 'favicon') {
        this.params.favicon = ''
        this.changeSettings({ state: type, mutation: 'SET_FAVICON', value: '' })
      } else if (type === 'logoUrl') {
        this.params.logoUrl = ''
        this.changeSettings({ state: type, mutation: 'SET_LOGO_URL', value: '' })
      } else if (type === 'loginLogoUrl') {
        this.params.loginLogoUrl = ''
        this.changeSettings({ state: type, mutation: 'SET_LOGIN_LOGO_URL', value: '' })
      }
    },
    handleAvatarSuccess({ fileUrl, type, imgUrl }) {
      if (type === 'favicon') {
        this.params.favicon = fileUrl
        this.changeSettings({ state: type, mutation: 'SET_FAVICON', value: fileUrl })
      } else if (type === 'logoUrl') {
        this.params.logoUrl = fileUrl
        this.changeSettings({ state: type, mutation: 'SET_LOGO_URL', value: fileUrl })
      } else if (type === 'loginLogoUrl') {
        this.params.loginLogoUrl = fileUrl
        this.changeSettings({ state: type, mutation: 'SET_LOGIN_LOGO_URL', value: fileUrl })
      }
    },
    handleInputSubmit({ inputValue, name }) {
      this.params.loginDescription = inputValue
      this.changeSettings({ state: name, mutation: 'SET_DESCRIPTION', value: inputValue })
    },
    async changeSettings(data) {
      this.loading = true
      await this.changeHomepage(data)
      this.$message.success('更新成功')
      setTimeout(() => {
        this.loading = false
      }, 300)
    },
    ...mapActions('settings', [
      'getHomepage',
      'changeHomepage'
    ])
  }
}
</script>

<style lang="scss" scoped>
.column{
  flex-direction: column;
}
.upload{
  // width: 250px;
  margin-right: 20px;
}
::v-deep .detail-title{
  width: 130px;
  display: inline-block;
}
.detail{
  padding: 20px;
  background-color: #fff;
}
.favicon{
  width: 50px;
  height: 50px;
  display: inline-block;
}
.logo-button{
  margin-left: 10px;
}
.logo-title{
  margin-top: 10px;
  display: flex;
}
.tip{
  font-size: 12px;
  color: #999;
}
</style>

