<template>
  <div v-loading="loading" class="container">
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
          <PHUpload :image-url="logoUrl" class="upload" type="logo" @success="handleAvatarSuccess" @remove="handleRemove" />
        </el-descriptions-item>
        <el-descriptions-item :content-style="{'flex-direction': 'column'}" label="是否添加logo文字">
          <el-radio-group v-model="showLogoTitle" @input="handleShowLogoTitleChange">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="2">否</el-radio>
          </el-radio-group>
          <div v-if="showLogoTitle === 1" class="logo-title">
            <el-input v-model="logoText" size="small" placeholder="请输入logo文字" clearable @clear="handleLogoTitleClear" @input="handleLogoTitleChange" />
            <el-button class="logo-button" type="primary" size="small" @click="changeSettings(params)">保存</el-button>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="登录页描述">
          <ConfirmInput name="loginDescription" type="textarea" :default-value="loginDescription" @submit="handleInputSubmit" />
        </el-descriptions-item>

        <el-descriptions-item label="隐藏建议与反馈">
          <el-switch v-model="params.isHideFadeBack" @change="handleFadeBackChange" />
        </el-descriptions-item>
        <el-descriptions-item label="隐藏底部版本号">
          <el-switch v-model="params.isHideFooterVersion" @change="handleVersionChange" />
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
      'logoTitle'
    ]),
    showLogoTitle: {
      get() {
        return this.$store.state.settings.showLogoTitle
      },
      set(val) {
        return val
      }
    }
  },
  async created() {
    await this.getHomepage()
    this.logoText = this.logoTitle
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
      this.params.showLogoTitle = val
      if (val === 2) {
        this.params.logoTitle = null
      }
      this.changeSettings()
    },
    handleFadeBackChange(val) {
      this.params.isHideFadeBack = val
      this.changeSettings()
    },
    handleLogoTitleClear() {
      this.params.logoTitle = ''
      this.changeSettings()
    },
    handleVersionChange(val) {
      this.params.isHideFooterVersion = val
      this.changeSettings()
    },
    handleLogoTitleChange(val) {
      this.params.logoTitle = val
    },
    handleRemove({ type }) {
      if (type === 'favicon') {
        this.params.favicon = ''
      } else if (type === 'logo') {
        this.params.logoUrl = ''
      } else if (type === 'loginLogoUrl') {
        this.params.loginLogoUrl = ''
      }
      this.changeSettings()
    },
    handleAvatarSuccess({ fileUrl, type, imgUrl }) {
      if (type === 'favicon') {
        this.params.favicon = fileUrl
      } else if (type === 'logo') {
        this.params.logoUrl = fileUrl
      } else if (type === 'loginLogoUrl') {
        this.params.loginLogoUrl = fileUrl
      }
      this.changeSettings()
    },
    handleInputSubmit({ inputValue, name }) {
      switch (name) {
        case 'title':
          this.params.title = inputValue
          break
        case 'loginDescription':
          this.params.loginDescription = inputValue
          break
        default:
          break
      }
      this.changeSettings()
    },
    changeSettings() {
      this.loading = true
      this.changeHomepage(this.params).then(() => {
        this.$message.success('更新成功')
        setTimeout(() => {
          this.loading = false
        }, 300)
      })
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

