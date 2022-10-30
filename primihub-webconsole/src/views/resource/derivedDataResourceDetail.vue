<template>
  <div v-loading="loading" class="container">
    <div class="detail">
      <el-descriptions title="衍生数据基本信息" :column="1" label-class-name="detail-title">
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
            v-model="resource.resourceDes"
            size="small"
            @keyup.enter.native="handleDescInputConfirm"
            @blur="handleDescInputConfirm"
          />
          <span v-else>
            <span>{{ resource.resourceDes }}</span>
            <i class="iconfont icon-edit edit-icon" @click="showDescInput" />
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="数据创建人">{{ resource.userName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ resource.createDate }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <div class="detail">
      <el-descriptions title="衍生数据详情" :column="1" label-class-name="detail-title">
        <el-descriptions-item label="创建衍生数据累计时间">{{ resource.totalTime }}</el-descriptions-item>
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
        <el-descriptions-item label="对齐特征">{{ resource.resourceSource | sourceFilter }}</el-descriptions-item>
        <el-descriptions-item label="加密方式">{{ resource.fileRows }}</el-descriptions-item>
        <el-descriptions-item label="我的数据">
          <el-button type="text" @click="toResourceDetailPage(resource.resourceId)">{{ resource.resourceId }}</el-button>
        </el-descriptions-item>
        <el-descriptions-item label="参与方数据">{{ resource.fileYRows ? resource.fileYRows : '0' }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import { getResourceDetail, saveResource } from '@/api/resource'

export default {
  data() {
    return {
      resource: {},
      loading: false,
      dialogVisible: false,
      resourceAuthType: 1,
      authType: '私有',
      originName: '',
      originList: [],
      resourceId: this.$route.params.id,
      inputVisible: false,
      descInputVisible: false,
      inputValue: ''
    }
  },
  async created() {
    await this.fetchData()
  },
  methods: {
    async fetchData() {
      const res = await getResourceDetail(this.resourceId)
      if (res.code === 0) {
        this.result = res.result
        this.resource = this.result.resource
      }
    },
    showInput() {
      this.inputVisible = true
    },
    showDescInput() {
      this.descInputVisible = true
    },
    handleInputConfirm() {
      this.inputVisible = false
      this.saveResource()
    },
    handleDescInputConfirm() {
      this.descInputVisible = false
      this.saveResource()
    },
    toResourceDetailPage(id) {
      this.$router.push({
        name: 'DerivedDataResourceDetail',
        params: { id }
      })
    },
    async download() {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/task/downloadTaskFile?taskId=${this.taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    },
    saveResource() {
      const params = {
        resourceId: this.resourceId,
        resourceName: this.resource.resourceName,
        resourceDesc: this.resource.resourceDesc
      }
      saveResource(params).then(res => {
        this.loading = true
        if (res.code === 0) {
          this.toResourceDetail(res.result.resourceId)
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
::v-deep .el-descriptions-item__container{
  align-items: center;
}
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
::v-deep .el-tag{
  margin:  0 3px;
}
::v-deep .detail-title{
  text-align: right;
  justify-content: flex-end;
  font-size: 16px;
}
::v-deep .el-descriptions__title{
  font-size: 20px;
  border-left: 3px solid $mainColor;
  padding-left: 10px;
}
::v-deep .el-descriptions__body{
  color: rgba(0,0,0,.85);
}
::v-deep .el-table,::v-deep .el-divider__text, .el-link{
  font-size: 12px;
}
.justify-content-center{
  justify-content: center;
}
.justify-content-between{
  justify-content: space-between;
}
.detail {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}
.auth-dialog{
  width: 100%;
  text-align: center;
}
.origin-select{
  margin-top: 20px;
}
.button{
  margin-left: 30px;
}
.buttons{
  display: flex;
  justify-content:flex-end;
}
</style>

