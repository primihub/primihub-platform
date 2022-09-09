<template>
  <div class="container">
    {{ modelId }}
    <el-form ref="form" :model="form" :rules="rules" class="form-container">
      <el-form-item label="推理使用模型" prop="modelName">
        <el-input v-model="form.modelName" class="model-name" placeholder="请选择模型" clearable @focus="openModelDialog" />
      </el-form-item>
      <el-form-item label="发起方:" prop="resourceId">
        <span>{{ createdOrgan }}</span>
        <el-button class="select-button" :disabled="createdOrgan === ''" size="small" type="primary" @click="handleResourceSelect(createdOrganId,1)">选择资源</el-button>
        <ResourceTable v-if="resourceList[createdOrganId] && resourceList[createdOrganId].length>0" :creator="true" :show-status="false" row-key="resourceId" :data="resourceList[createdOrganId]" @remove="handleRemove" />
      </el-form-item>
      <el-form-item v-for="(item,index) in providerOrgans" :key="index" label="参与方:" prop="resourceId">
        <span>{{ item.organName }}</span>
        <el-button class="select-button" :disabled="providerOrgans.length < 1" size="small" type="primary" @click="handleResourceSelect(item.organId, 2)">选择资源</el-button>
      </el-form-item>
      <el-form-item label="推理服务名称" prop="reasoningName">
        <el-input v-model="form.reasoningName" placeholder="请输入推理服务名称" />
      </el-form-item>
      <el-form-item label="推理服务描述" prop="reasoningDesc">
        <el-input
          v-model="form.reasoningDesc"
          type="textarea"
          placeholder="请输入推理服务描述，限100字"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">导入</el-button>
        <el-button>重置</el-button>
      </el-form-item>
    </el-form>

    <!-- model select dialog -->
    <ModelSelectDialog :visible="dialogVisible" @submit="handleModelSubmit" @close="handleClose" />
    <!-- add resource dialog -->
    <ResourceDialog ref="dialogRef" top="10px" width="800px" :selected-data="selectedResourceId" title="选择资源" :table-data="resourceList[selectedOrganId]" :visible="resourceDialogVisible" @close="handleResourceDialogCancel" @submit="handleResourceDialogSubmit" />
  </div>
</template>

<script>
import ModelSelectDialog from '@/components/ModelSelectDialog'
import ResourceTable from '@/components/ResourceTable'
import ResourceDialog from '@/components/ResourceDialog'

const res = {
  'code': 0,
  'msg': '请求成功',
  'result': {
    'total': 62,
    'pageSize': 5,
    'totalPage': 13,
    'index': 1,
    'data': [
      {
        'resourceId': '591acb22b324-c6592d22-ede7-4db9-adf5-4fc2d4c8bd34',
        'resourceName': 'psi-date',
        'resourceDesc': 'psi',
        'resourceType': 1,
        'resourceAuthType': 1,
        'resourceRowsCount': 10,
        'resourceColumnCount': 2,
        'resourceColumnNameList': 'company,date',
        'openColumnNameList': null,
        'resourceContainsY': null,
        'resourceYRowsCount': null,
        'resourceYRatio': null,
        'resourceTag': [
          'psi'
        ],
        'organId': '8bf56ee6-b004-4ada-b078-591acb22b324',
        'organName': 'test1',
        'createDate': '2022-09-09 03:28:31'
      },
      {
        'resourceId': '591acb22b324-82f7821e-900c-40c1-899f-c481fda8cd5e',
        'resourceName': 'host-50',
        'resourceDesc': 'host-50行数据',
        'resourceType': 1,
        'resourceAuthType': 1,
        'resourceRowsCount': 50,
        'resourceColumnCount': 7,
        'resourceColumnNameList': 'Class,y,x1,x2,x3,x4,x5',
        'openColumnNameList': null,
        'resourceContainsY': 1,
        'resourceYRowsCount': 50,
        'resourceYRatio': 100,
        'resourceTag': [
          'host'
        ],
        'organId': '8bf56ee6-b004-4ada-b078-591acb22b324',
        'organName': 'test1',
        'createDate': '2022-09-08 10:22:24'
      },
      {
        'resourceId': '591acb22b324-d53da14f-7b28-4e00-b776-18903072d4f7',
        'resourceName': '111',
        'resourceDesc': '111',
        'resourceType': 1,
        'resourceAuthType': 1,
        'resourceRowsCount': 10,
        'resourceColumnCount': 1,
        'resourceColumnNameList': 'test',
        'openColumnNameList': null,
        'resourceContainsY': null,
        'resourceYRowsCount': null,
        'resourceYRatio': null,
        'resourceTag': [
          '111'
        ],
        'organId': '8bf56ee6-b004-4ada-b078-591acb22b324',
        'organName': 'test1',
        'createDate': '2022-09-01 03:00:37'
      },
      {
        'resourceId': '591acb22b324-85f50da2-5fe9-414d-ab64-f23ad9dfd9d8',
        'resourceName': 'testpsi',
        'resourceDesc': 'testpsi',
        'resourceType': 1,
        'resourceAuthType': 1,
        'resourceRowsCount': 6000,
        'resourceColumnCount': 1,
        'resourceColumnNameList': 'guaranteetype',
        'openColumnNameList': null,
        'resourceContainsY': null,
        'resourceYRowsCount': null,
        'resourceYRatio': null,
        'resourceTag': [
          'psi'
        ],
        'organId': '8bf56ee6-b004-4ada-b078-591acb22b324',
        'organName': 'test1',
        'createDate': '2022-08-31 11:35:08'
      },
      {
        'resourceId': '591acb22b324-6496dd76-b4a3-4ea7-bcfe-191db385dd19',
        'resourceName': 'psi001',
        'resourceDesc': 'PSI001',
        'resourceType': 1,
        'resourceAuthType': 1,
        'resourceRowsCount': 1000,
        'resourceColumnCount': 1,
        'resourceColumnNameList': 'guaranteetype',
        'openColumnNameList': null,
        'resourceContainsY': null,
        'resourceYRowsCount': null,
        'resourceYRatio': null,
        'resourceTag': [
          'PSI'
        ],
        'organId': '8bf56ee6-b004-4ada-b078-591acb22b324',
        'organName': 'test1',
        'createDate': '2022-08-31 03:32:41'
      }
    ]
  },
  'extra': null
}

export default {
  name: 'ModelInferenceTask',
  components: {
    ResourceTable,
    ResourceDialog,
    ModelSelectDialog
  },
  data() {
    const validateResource = (rule, value, callback) => {
      console.log(value)
      if (!value) {
        return callback(new Error('年龄不能为空'))
      }
      setTimeout(() => {
        if (!Number.isInteger(value)) {
          callback(new Error('请输入数字值'))
        } else {
          if (value < 18) {
            callback(new Error('必须年满18岁'))
          } else {
            callback()
          }
        }
      }, 1000)
    }
    return {
      dialogVisible: false,
      resourceDialogVisible: false,
      selectedResourceId: '',
      resourceList: [],
      value: true,
      taskId: 0,
      modelId: 0,
      modelName: 0,
      selectedOrganId: '',
      createdOrgan: '',
      createdOrganId: '',
      providerOrgans: [{
        organId: '1222',
        organName: 'test2'
      }],
      initiateOrganResource: [],
      providerOrganResource: [],
      form: {
        modelName: '',
        taskId: '',
        resourceList: [],
        reasoningName: '',
        reasoningDesc: ''
      },
      participationIdentity: 1,
      rules: {
        modelName: [
          { required: true, message: '请选择模型' }
        ],
        reasoningName: [
          { required: true, message: '请输入推理服务名称' }
        ],
        reasoningDesc: [
          { required: true, message: '请输入推理服务描述，限100字' }
        ],
        resourceId: [
          { required: true, validator: validateResource }
        ]
      }
    }
  },
  created() {
    this.createdOrganId = this.$store.getters.userOrganId
    this.resourceList[this.createdOrganId] = [{
      'resourceId': '591acb22b324-c6592d22-ede7-4db9-adf5-4fc2d4c8bd34',
      'resourceName': 'psi-date',
      'resourceDesc': 'psi',
      'resourceType': 1,
      'resourceAuthType': 1,
      'resourceRowsCount': 10,
      'resourceColumnCount': 2,
      'resourceColumnNameList': 'company,date',
      'openColumnNameList': null,
      'resourceContainsY': null,
      'resourceYRowsCount': null,
      'resourceYRatio': null,
      'resourceTag': [
        'psi'
      ],
      'organId': '8bf56ee6-b004-4ada-b078-591acb22b324',
      'organName': 'test1',
      'createDate': '2022-09-09 03:28:31'
    }]
    console.log('userOrganName', this.$store.getters.userOrganName)
  },
  methods: {
    handleRemove() {},
    onSubmit() {},
    handleResourceSelect(organId, participationIdentity) {
      this.selectedOrganId = organId
      this.participationIdentity = participationIdentity
      this.resourceDialogVisible = true
    },
    handleClose() {
      // this.$refs.form.resetFields()
      this.dialogVisible = false
    },
    openModelDialog() {
      this.dialogVisible = true
    },
    async handleModelSubmit(data) {
      console.log(data)
      this.form.taskId = data.taskId
      this.createdOrgan = data.createdOrgan
      // this.providerOrgans = data.providerOrgans
      this.projectId = data.projectId
      this.form.modelName = data.modelName
      this.modelId = data.modelId
      this.dialogVisible = false
    },
    handleResourceDialogCancel() {
      this.resourceDialogVisible = false
    },
    handleResourceDialogSubmit(data) {
      console.log(data)
      this.resourceList[this.selectedOrganId] = data
      this.resourceList.push({
        resourceId: data.resourceId,
        participationIdentity: this.participationIdentity
      })
      this.resourceDialogVisible = false
      // this.resourceList[this.selectedOrganId] = data
      // this.selectedResourceId = data.resourceId
    }
  }
}
</script>

<style lang="scss" scoped>
.container{
  background: #fff;
  padding: 30px 0;
  height: 100%;
  .form-container{
    width: 700px;
    margin: 0 auto;
  }
}
.select-button{
  width: 100%;
}
.model-name{
  cursor:default
}
</style>
