<template>
  <div v-loading="loading" class="container">
    {{ logoTitle }}
    <div class="detail">
      <el-descriptions :column="1" label-class-name="detail-title">
        <el-descriptions-item label="页面标题">
          <ConfirmInput name="title" :default-value="title" @submit="handleInputSubmit" />
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
            <el-radio :label="1">是</el-radio>
            <el-radio :label="2">否</el-radio>
          </el-radio-group>
          <div v-if="showLogoTitle === 1" class="logo-title">
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
        <el-descriptions-item label="隐藏底部版本号" :content-style="{'flex-direction': 'column'}">
          <el-switch v-model="isHideFooterVersion" @change="handleVersionChange" />
          <div v-if="!isHideFooterVersion" class="logo-title">
            <el-input v-model="theFooterText" size="small" placeholder="请输入" clearable @clear="handleLogoTitleClear" @input="handleFooterTextChange" />
            <el-button class="logo-button" type="primary" size="small" @click="saveFooterText">保存</el-button>
          </div>
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
      theFooterText: '',
      isShowLogoTitle: 1
    }
  },
  computed: {
    ...mapState('settings', [
      'title',
      'favicon',
      'settingChanged',
      'loginLogoUrl',
      'logoUrl',
      'loginDescription',
      'isHideFadeBack',
      'isHideFooterVersion',
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
    }
  },
  async created() {
    await this.getHomepage()
    this.logoText = this.logoTitle
    console.log(this.logoText)
    this.theFooterText = this.footerText
    this.params = {
      title: this.title,
      isHideFadeBack: this.isHideFadeBack,
      isHideFooterVersion: this.isHideFooterVersion,
      logoUrl: this.logoUrl,
      loginDescription: this.loginDescription,
      favicon: this.favicon,
      logoTitle: this.logoTitle,
      loginLogoUrl: this.loginLogoUrl,
      showLogoTitle: this.showLogoTitle
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
    handleLogoTitleClear() {
      this.logoText = ''
      this.changeSettings({ state: 'logoTitle', mutation: 'SET_TITLE', value: '' })
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
      switch (name) {
        case 'title':
          this.params.title = inputValue
          this.changeSettings({ state: name, mutation: 'SET_TITLE', value: inputValue })
          break
        case 'loginDescription':
          this.params.loginDescription = inputValue
          this.changeSettings({ state: name, mutation: 'SET_DESCRIPTION', value: inputValue })
          break
        default:
          break
      }
    },
    // changeSettings() {
    //   this.loading = true
    //   this.changeHomepage(this.params).then(() => {
    //     this.$message.success('更新成功')
    //     setTimeout(() => {
    //       this.loading = false
    //     }, 300)
    //   })
    // },
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

