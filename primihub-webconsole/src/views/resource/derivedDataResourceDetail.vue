<template>
  <div v-if="resource" v-loading="loading" class="container">
    <div class="detail">
      <el-descriptions title="衍生数据基本信息" :column="2" label-class-name="detail-title">
        <el-descriptions-item label="数据名称">
          <el-input
            v-if="inputVisible"
            ref="nameInput"
            v-model="resource.resourceName"
            size="small"
            @keyup.enter.native="handleInputConfirm"
            @blur="handleInputConfirm"
          />
          <span v-else>
            <span>{{ resource.resourceName }}</span>
            <i class="iconfont icon-edit edit-icon" @click="showInput" />
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="数据ID">{{ resource.resourceId }}</el-descriptions-item>
        <el-descriptions-item label="数据描述">
          <el-input
            v-if="descInputVisible"
            ref="descInput"
            v-model="resource.resourceDesc"
            size="small"
            @keyup.enter.native="handleDescInputConfirm"
            @blur="handleDescInputConfirm"
          />
          <span v-else>
            <span>{{ resource.resourceDesc }}</span>
            <i class="iconfont icon-edit edit-icon" @click="showDescInput" />
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="数据创建人">{{ resource.userName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ resource.createDate }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <div class="detail">
      <el-descriptions title="衍生数据详情" :column="2" label-class-name="detail-title">
        <el-descriptions-item label="创建衍生数据累计时间">{{ (resource.totalTime) / 1000 | timeFilter }}</el-descriptions-item>
        <el-descriptions-item label="衍生数据样本量">{{ resource.fileRows }}</el-descriptions-item>
        <el-descriptions-item label="衍生数据特征维度">{{ resource.fileColumns }}</el-descriptions-item>
        <el-descriptions-item label="正例样本数量">{{ resource.fileYRows ? resource.fileYRows : '0' }}</el-descriptions-item>
        <el-descriptions-item label="正例样本比例">{{ resource.fileYRatio ? resource.fileYRatio : '0' }}%</el-descriptions-item>
      </el-descriptions>
      <div class="buttons">
        <el-button type="primary" @click="download">下载结果</el-button>
      </div>
    </div>
    <div class="detail">
      <el-descriptions title="原始数据" :column="2" label-class-name="detail-title">
        <el-descriptions-item :label="`${resource.tag}特征`">{{ alignFeature }}</el-descriptions-item>
        <el-descriptions-item label="我的数据">
          <el-link :underline="false" type="primary" @click="toResourceDetailPage(resource.initiateOrganResource)">{{ resource.initiateOrganResource }}</el-link>
        </el-descriptions-item>
        <el-descriptions-item label="协作方数据">
          <el-link :underline="false" type="primary" @click="toResourceDetailPage(resource.providerOrganResource)">
            {{ resource.providerOrganResource && resource.providerOrganResource }}
          </el-link>
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import { getDerivationResourceData, saveResource } from '@/api/resource'

export default {
  data() {
    return {
      resource: null,
      loading: false,
      dialogVisible: false,
      resourceAuthType: 1,
      authType: '私有',
      originName: '',
      originList: [],
      resourceId: this.$route.params.id,
      inputVisible: false,
      descInputVisible: false,
      inputValue: '',
      alignFeature: ''
    }
  },
  async created() {
    await this.fetchData().catch(err => {
      console.log(err)
      this.loading = false
    })
  },
  methods: {
    async fetchData() {
      this.loading = true
      const res = await getDerivationResourceData({ resourceId: this.resourceId })
      if (res.code === 0) {
        this.resource = res.result
        const alignFeature = this.resource.alignFeature && JSON.parse(this.resource.alignFeature)
        if (alignFeature && alignFeature.length > 0) {
          this.alignFeature = alignFeature.map(item => item.selectedExceptionFeatures)?.join()
        } else {
          this.alignFeature = alignFeature === '' ? 'ID' : alignFeature
        }
        this.loading = false
      }
    },
    showInput() {
      this.inputVisible = true
      this.$nextTick(() => {
        this.$refs.nameInput.focus()
      })
    },
    showDescInput() {
      this.descInputVisible = true
      this.$nextTick(() => {
        this.$refs.descInput.focus()
      })
    },
    handleInputConfirm() {
      this.inputVisible = false
      this.saveResource()
    },
    handleDescInputConfirm() {
      this.descInputVisible = false
      if (!this.resource.resourceDesc) return
      this.saveResource()
    },
    toResourceDetailPage(id) {
      this.$router.push({
        name: 'UnionResourceDetail',
        params: { id }
      })
    },
    async download() {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/resource/download?resourceId=${this.resource.resourceId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    },
    saveResource() {
      const { resourceId, resourceName, resourceDesc, resourceSource } = this.resource
      const params = {
        resourceId,
        resourceName,
        resourceDesc,
        resourceAuthType: 2,
        resourceSource
      }
      saveResource(params).then(res => {
        this.loading = true
        if (res.code === 0) {
          this.loading = false
          this.$message({
            message: '修改成功',
            type: 'success'
          })
        } else {
          this.loading = false
          this.$message({
            message: res.msg,
            type: 'error'
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
.container{
  background: #fff;
  padding: 30px;
}
.detail{
  background-color: #fff;
  margin-bottom: 30px;
}
.edit-icon{
  color: $mainColor;
  vertical-align: middle;
  margin-left: 10px;
}
.descriptions{
  line-height: 24px;
}
@import "~@/styles/variables.scss";
::v-deep .detail-title{
  text-align: right;
  justify-content: flex-end;
}
::v-deep .el-descriptions__title{
  font-size: 18px;
  border-left: 3px solid $mainColor;
  padding-left: 10px;
}
::v-deep .el-descriptions__body{
  color: rgba(0,0,0,.85);
}
.detail {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}
.button{
  margin-left: 30px;
}
.buttons{
  display: flex;
  justify-content:flex-end;
}
</style>

