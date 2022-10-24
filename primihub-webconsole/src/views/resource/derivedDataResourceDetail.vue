<template>
  <div class="container">
    <div class="detail">
      <el-descriptions title="衍生数据基本信息" :column="2">
        <el-descriptions-item label="数据名称">
          <el-input
            v-if="inputVisible"
            ref="saveTagInput"
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
        <el-descriptions-item label="数据描述">{{ resource.resourceDes }}</el-descriptions-item>
        <el-descriptions-item label="数据创建人">{{ resource.userName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ resource.createDate }}</el-descriptions-item>
        <el-descriptions-item label="授权方式">
          <span>{{ resource.resourceAuthType | authTypeFilter }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </div>
    <div class="detail">
      <el-descriptions title="衍生数据详情" :column="2">
        <el-descriptions-item label="创建衍生数据累计时间">{{ resource.resourceSource | sourceFilter }}</el-descriptions-item>
        <el-descriptions-item label="衍生数据样本量">{{ resource.fileRows }}</el-descriptions-item>
        <el-descriptions-item label="衍生数据特征维度">{{ resource.fileColumns }}</el-descriptions-item>
        <el-descriptions-item label="正例样本数量">{{ resource.fileYRows ? resource.fileYRows : '0' }}</el-descriptions-item>
        <el-descriptions-item label="正例样本比例">{{ resource.fileYRatio ? resource.fileYRatio : '0' }}%</el-descriptions-item>
      </el-descriptions>
    </div>
    <div class="detail">
      <el-descriptions title="原始数据" :column="2">
        <el-descriptions-item label="对齐特征">{{ resource.resourceSource | sourceFilter }}</el-descriptions-item>
        <el-descriptions-item label="加密方式">{{ resource.fileRows }}</el-descriptions-item>
        <el-descriptions-item label="我的数据">
          <el-link size="small" type="primary" @click="toResourceDetailPage(resource.resourceId)">{{ resource.resourceId }}</el-link>
        </el-descriptions-item>
        <el-descriptions-item label="参与方数据">{{ resource.fileYRows ? resource.fileYRows : '0' }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script>
import { getResourceDetail } from '@/api/resource'

export default {
  data() {
    return {
      resource: {
        'projectId': '141',
        'resourceId': '2b598a7e3298-4f62b21f-665e-4426-9068-4fc8f8186966',
        'resourceName': 'test2-的新资源',
        'taskId': '290',
        'taskIdName': '2cad8338-2e8c-4768-904d-2b598a7e3298',
        'resourceSource': 1,
        'fileRows': 50,
        'fileColumns': 7,
        'fileHandleStatus': 0,
        'fileContainsY': null,
        'fileYRows': null,
        'fileYRatio': null,
        'createDate': '2022-10-19 14:11:26',
        'userName': 'admin'
      },
      dialogVisible: false,
      resourceAuthType: 1,
      authType: '私有',
      originName: '',
      originList: [],
      resourceId: this.$route.params.id,
      dynamicTags: ['标签一', '标签二', '标签三'],
      inputVisible: false,
      inputValue: ''
    }
  },
  async created() {
    await this.fetchData()
  },
  methods: {
    async fetchData() {
      // const res = await getResourceDetail(this.resourceId)
      // if (res.code === 0) {
      //   this.result = res.result
      //   this.resource = this.result.resource
      //   this.resourceAuthType = this.resource.resourceAuthType
      //   this.dataList = this.result.dataList || []
      //   this.fieldList = this.result.fieldList || []
      // }
    },
    handleClose(tag) {
      this.dynamicTags.splice(this.dynamicTags.indexOf(tag), 1)
    },

    showInput() {
      this.inputVisible = true
      this.$nextTick(_ => {
        this.$refs.saveTagInput.$refs.input.focus()
      })
    },
    handleInputConfirm() {
      this.inputVisible = false
    },
    toResourceDetailPage(id) {
      this.$router.push({
        name: 'DerivedDataResourceDetail',
        params: { id }
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
.detail{
  padding: 30px;
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
</style>

