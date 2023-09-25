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
      <!-- <p>资源表结构</p> -->
      <el-row
        :gutter="20"
        class="data-container"
      >
        <el-col v-if="fieldList.length>0" :span="12">
          <EditResourceTable border :is-editable="false" :data="fieldList" height="500" />
        </el-col>
        <el-col v-if="dataList.length>0" :span="12">
          <ResourcePreviewTable :data="dataList" height="500" />
        </el-col>
      </el-row>
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
          <div class="desc-content">{{ taskData.tag === 0? 'ECDH': 'KKRT' }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getPsiTaskDetails } from '@/api/PSI'
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
    }

  }
}
</script>

<style lang="scss" scoped>
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
