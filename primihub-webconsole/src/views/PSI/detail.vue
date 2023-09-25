<template>
  <div class="container">
    <div class="detail">
      <h3>基础信息</h3>
      <div class="description-container">
        <div class="desc-col">
          <div class="desc-label">任务名称:</div>
          <div class="desc-content">{{ taskData.taskName }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">任务ID:</div>
          <div class="desc-content">{{ taskData.taskIdName }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">结果输出方式:</div>
          <div class="desc-content">{{ taskData.outputFormat === '0'? '资源文件(csv)': '' }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">结果名称:</div>
          <div class="desc-content">{{ taskData.resultName }}</div>
        </div>
      </div>
    </div>
    <div class="detail">
      <h3>数据配置</h3>
      <div class="description-container">
        <div class="desc-col">
          <div class="desc-label">发起方:</div>
          <div class="desc-content">{{ taskData.ownOrganName }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">协作方:</div>
          <div class="desc-content">{{ taskData.otherOrganName }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">资源表:</div>
          <div class="desc-content">
            <el-link type="primary" @click="toResourceDetailPage(taskData.ownResourceId)">{{ taskData.ownResourceName }}</el-link>
          </div>
        </div>
        <div class="desc-col">
          <div class="desc-label">资源表:</div>
          <div class="desc-content">
            <el-link type="primary" @click="toUnionResourceDetailPage(taskData.otherResourceId)">{{ taskData.otherResourceName }}</el-link>
          </div>
        </div>
        <div class="desc-col">
          <div class="desc-label">关联键:</div>
          <div class="desc-content">{{ taskData.ownKeyword }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">关联键:</div>
          <div class="desc-content">{{ taskData.otherKeyword }}</div>
        </div>
      </div>
    </div>
    <div class="detail">
      <h3>实现方法</h3>
      <div class="description-container">
        <div class="desc-col">
          <div class="desc-label">输出内容:</div>
          <div class="desc-content">{{ taskData.outputContent=== 0 ? '交集': '差集' }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">实现方法:</div>
          <div class="desc-content">{{ taskData.tag === 0? 'ECDH': 'KKRT' }}</div>
        </div>
      </div>
    </div>
    <div class="detail">
      <h3>具体实现</h3>
      <div class="description-container">
        <div class="desc-col">
          <div class="desc-label">任务发起时间:</div>
          <div class="desc-content">{{ taskData.createTime }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">任务耗时:</div>
          <div class="desc-content">{{ taskData.consuming | timeFilter }}</div>
        </div>
        <div class="desc-col" style="width: 100%;">
          <div class="desc-label">计算结果:</div>
          <div class="desc-content flex">
            <el-link :underline="false" type="primary" @click="downloadPsiTask">{{ taskData.resultName }}.csv <svg-icon icon-class="download" /></el-link>
            <el-button size="small" type="primary" plain @click="downloadPsiTask">下载文档</el-button>
            <el-button size="small" type="primary" plain @click="handlePreview">在线预览</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- preview dialog -->
    <ResourcePreviewDialog
      :data="previewList"
      :visible.sync="previewDialogVisible"
      append-to-body
      width="1000px"
      @close="closeDialog"
    />
  </div>
</template>

<script>
import { getPsiTaskDetails } from '@/api/PSI'
import { getDataResource } from '@/api/fusionResource'
import { resourceFilePreview } from '@/api/resource'
import ResourcePreviewDialog from '@/components/ResourcePreviewDialog'
import { getToken } from '@/utils/auth'

export default {
  components: {
    ResourcePreviewDialog
  },
  data() {
    return {
      previewList: [],
      previewDialogVisible: false,
      taskData: [],
      resource: {},
      dialogVisible: false,
      resourceAuthType: 1,
      authType: '私有',
      originName: '',
      originList: [],
      resourceId: this.$route.params.id,
      taskId: this.$route.params.id,
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
    async downloadPsiTask() {
      const timestamp = new Date().getTime()
      const nonce = Math.floor(Math.random() * 1000 + 1)
      const token = getToken()
      window.open(`${process.env.VUE_APP_BASE_API}/data/psi/downloadPsiTask?taskId=${this.taskData.taskId}&timestamp=${timestamp}&nonce=${nonce}&token=${token}`, '_self')
    },
    async fetchData() {
      const res = await getPsiTaskDetails({ taskId: this.taskId })
      if (res.code === 0) {
        this.taskData = res.result
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
    },
    closeDialog() {
      this.previewDialogVisible = false
    },
    async handlePreview() {
      await this.resourceFilePreview()
      this.previewDialogVisible = true
    },
    async resourceFilePreview() {
      const res = await resourceFilePreview({ resourceId: this.taskData.id })
      this.previewList = res.result?.dataList
    }

  }
}
</script>

<style lang="scss" scoped>
.detail{
  margin-bottom: 40px;
  h3{
    border-left: 3px solid #1677FF;
    padding-left: 10px;
  }
}
.description-container{
  display: flex;
  flex-flow: wrap;
  background-color: #fafafa;
  padding: 20px 20px 10px 20px;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 20px;
  border-radius: 12px;
}
.infos-title{
  font-size: 16px;
}

.desc-col{
  width: 50%;
  flex-shrink: 0;
  display: flex;
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
}
.desc-label{
  margin-right: 10px;
}
.desc-content{
  flex: 1;
  padding-right: 10px;
}

</style>
