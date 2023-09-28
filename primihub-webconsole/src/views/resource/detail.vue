<template>
  <div class="app-container">
    <h2>资源详情</h2>
    <div class="detail">
      <el-descriptions title="资源信息" :column="2" label-class-name="detail-title">
        <el-descriptions-item label="资源ID">{{ resource.resourceId }}</el-descriptions-item>
        <el-descriptions-item label="资源名称">{{ resource.resourceName }}</el-descriptions-item>
        <el-descriptions-item label="标签">
          <el-tag v-for="tag in resource.tags" :key="tag.tagId" type="primary" size="mini">{{ tag.tagName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="授权方式">
          <span>{{ resource.resourceAuthType | authTypeFilter }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="资源描述">{{ resource.resourceDesc }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <div class="detail">
      <el-descriptions title="数据信息" :column="2" label-class-name="detail-title">
        <el-descriptions-item label="样本量">{{ resource.fileRows }}</el-descriptions-item>
        <el-descriptions-item label="特征量">{{ resource.fileColumns }}</el-descriptions-item>
        <el-descriptions-item label="正例样本数量">{{ resource.fileYRows ? resource.fileYRows : '0' }}</el-descriptions-item>
        <el-descriptions-item label="正例样本比例">{{ resource.fileYRatio ? resource.fileYRatio : '0' }}%</el-descriptions-item>
        <el-descriptions-item label="数据来源">{{ resource.resourceSource | sourceFilter }}</el-descriptions-item>
        <el-descriptions-item v-if="resource.resourceSource === 1" label="数据大小">{{ resource.fileSize | fileSizeFilter }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <div class="detail">
      <el-row
        :gutter="20"
        class="data-container"
      >
        <el-col v-if="fieldList.length>0" :span="12">
          <h3>字段信息</h3>
          <EditResourceTable border :is-editable="false" :data="fieldList" height="500" />
        </el-col>
        <el-col v-if="dataList.length>0" :span="12">
          <h3>数据资源预览</h3>
          <ResourcePreviewTable :data="dataList" height="500" />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { getResourceDetail } from '@/api/resource'
import { getDataResource } from '@/api/fusionResource'
import EditResourceTable from '@/components/EditResourceTable'
import ResourcePreviewTable from '@/components/ResourcePreviewTable'

export default {
  components: {
    EditResourceTable,
    ResourcePreviewTable
  },
  data() {
    return {
      resource: {},
      dialogVisible: false,
      resourceAuthType: 1,
      authType: '私有',
      originName: '',
      originList: [],
      resourceId: this.$route.params.id,
      pageCount: 0,
      total: 0,
      pageNo: 0,
      pageSize: 10,
      resourceFieldList: [],
      dataList: [], // resource preview
      fieldList: [] // resource field info
    }
  },
  async created() {
    const source = this.$route.query.source || ''
    if (source === 'unionList') {
      await this.getDataResource()
    } else {
      await this.fetchData()
    }
  },
  methods: {
    async fetchData() {
      const res = await getResourceDetail(this.resourceId)
      if (res.code === 0) {
        this.result = res.result
        this.resource = this.result.resource
        this.resourceAuthType = this.resource.resourceAuthType
        this.dataList = this.result.dataList || []
        this.fieldList = this.result.fieldList || []
      }
    },
    async getDataResource() {
      const res = await getDataResource({
        resourceId: this.resourceId
      })
      if (res.code === 0) {
        this.result = res.result
        this.resource = this.result.resource
        this.resourceAuthType = this.resource.resourceAuthType
        this.dataList = this.result.dataList || []
        this.fieldList = this.result.fieldList || []
      }
    },
    cancel() {
      this.dialogVisible = false
    }

  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
::v-deep .el-tag{
  margin:  0 3px;
}
::v-deep .el-descriptions__title, h3{
  font-size: 18px;
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
  padding: 20px 0 20px 20px;
  border-top: 1px solid #f0f0f0;
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
</style>
