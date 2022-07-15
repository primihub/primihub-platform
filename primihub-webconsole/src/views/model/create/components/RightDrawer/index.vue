<template>
  <div class="right-drawer">
    <template v-if="nodeData && showDataConfig">
      <el-form ref="form" v-loading="listLoading" :model="nodeData" label-width="80px" element-loading-spinner="el-icon-loading">
        <template v-if="isDataSelect">
          <el-form-item>
            <p class="organ"><i class="el-icon-office-building" /> <span>发起方：</span> {{ nodeData.initiateOrgan.organName }}</p>
            <el-button type="primary" size="small" @click="openDialog(nodeData.initiateOrgan.organId, 1)">选择资源</el-button>
            <ResourceDec v-if="initiateOrganDes" :data="initiateOrganDes" />
          </el-form-item>
          <el-form-item>
            <template v-if="nodeData.providerOrgans.length>0">
              <span class="organ"><i class="el-icon-office-building" /> <span>协作方：</span> {{ providerOrganName }}</span>
              <el-select v-model="providerOrganId" placeholder="请选择" size="small" @change="handleProviderOrganChange">
                <el-option
                  v-for="(v) in nodeData.providerOrgans"
                  :key="v.organId"
                  :label="v.organName"
                  :value="v.organId"
                />
              </el-select>
              <el-button type="primary" size="small" @click="openDialog(providerOrganId,2)">选择资源</el-button>
              <ResourceDec v-if="providerOrganDes" :data="providerOrganDes" />
            </template>
            <template v-else>
              <i class="el-icon-office-building" /> <span>暂无审核通过的协作方 </span>
            </template>
          </el-form-item>
        </template>
        <div v-for="item in nodeData.componentTypes" v-else :key="item.typeCode">
          <el-form-item :label="item.typeName " :prop="item.typeCode">
            <template v-if="item.inputType === 'label'">
              <span class="label-text">{{ item.inputValue }}</span>
            </template>
            <template v-if="item.inputType === 'text'">
              <el-input v-model="item.inputValue" size="mini" />
            </template>
            <template v-if="item.inputType === 'select'">
              <el-select v-model="item.inputValue" placeholder="请选择" :value-key="item.typeCode" @change="handleChange(item)">
                <el-option
                  v-for="(v,index) in item.inputValues"
                  :key="index"
                  :label="v.val"
                  :value="v.key"
                />
              </el-select>
            </template>
          </el-form-item>
        </div>
      </el-form>
    </template>
    <template v-else>
      <el-form ref="form" :rules="rules" :model="modelData" label-width="80px">
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="modelData.modelName" size="mini" />
        </el-form-item>
        <el-form-item label="模型描述" prop="modelDesc">
          <el-input v-model="modelData.modelDesc" size="mini" type="textarea" />
        </el-form-item>
        <el-form-item label="训练类型" prop="trainType">
          <el-radio-group v-model="modelData.trainType" size="small">
            <el-radio :label="1">纵向</el-radio>
            <el-radio :label="2">横向</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </template>

    <!-- add resource dialog -->
    <ResourceDialog ref="dialogRef" top="10px" width="800px" :selected-data="selectedData" title="添加资源" :table-data="resourceList[selectedOrganId]" :visible="dialogVisible" @close="handleDialogCancel" @submit="handleDialogSubmit" />
  </div>
</template>

<script>
import { getProjectResourceData } from '@/api/model'
import ResourceDialog from '@/components/ResourceDialog'
import ResourceDec from '@/components/ResourceDec'

export default {
  components: {
    ResourceDialog,
    ResourceDec
  },
  props: {
    showDataConfig: {
      type: Boolean,
      default: false
    },
    nodeData: {
      type: Object,
      default: () => {
        return {}
      }
    },
    modelData: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      listLoading: false,
      providerOrganId: '',
      providerOrganName: '',
      dialogVisible: false,
      selectedOrganId: '',
      resourceList: [],
      selectedData: [],
      initiateOrganDes: null,
      providerOrganDes: null,
      participationIdentity: 0,
      inputValues: [],
      rules: {
        taskName: [
          { required: true, message: '请输入任务名称', trigger: 'blur' },
          { max: 20, message: '长度在20个字符以内', trigger: 'blur' }
        ],
        modelName: [
          { required: true, message: '请输入模型名称', trigger: 'blur' },
          { max: 20, message: '长度在20个字符以内', trigger: 'blur' }
        ],
        trainType: [
          { required: true, message: '请选择训练类型', trigger: 'change' }
        ],
        yValueColumn: [
          { required: true, message: '请选择Y值字段', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    isDataSelect() {
      return this.nodeData && this.nodeData.componentCode === 'dataAlignment' && this.nodeData.initiateOrgan
    }
  },
  created() {
    this.projectId = Number(this.$route.query.projectId) || 0
  },
  methods: {
    openDialog(organId, participationIdentity) {
      this.participationIdentity = participationIdentity
      this.selectedOrganId = organId
      this.dialogVisible = true
      this.getResourceList()
    },
    handleChange(item) {
      this.$emit('change', item.typeCode, item)
    },
    handleProviderOrganChange(value) {
      this.providerOrganName = this.nodeData.providerOrgans.filter(item => item.organId === value)[0].organName
    },
    handleDialogCancel() {
      this.dialogVisible = false
    },
    handleDialogSubmit(data) {
      const posIndex = this.inputValues.findIndex(item => item.organId === data.organId)
      const currentData = {
        organId: data.organId,
        organName: data.organName,
        resourceId: data.resourceId,
        participationIdentity: this.participationIdentity
      }
      if (posIndex !== -1) {
        this.inputValues[posIndex] = currentData
      } else {
        this.inputValues.push(currentData)
      }
      this.nodeData.componentTypes[0].inputValue = JSON.stringify(this.inputValues)

      if (this.participationIdentity === 1) {
        this.initiateOrganDes = data
        this.selectedData = [this.initiateOrganDes]
      } else {
        this.providerOrganDes = data
        this.selectedData = [this.providerOrganDes]
      }
      this.dialogVisible = false
    },
    async getResourceList() {
      this.listLoading = true
      this.resourceList = []
      const params = {
        projectId: this.projectId,
        organId: this.selectedOrganId
      }
      const { code, result } = await getProjectResourceData(params)
      if (code === 0) {
        this.resourceList[this.selectedOrganId] = result
      }
      this.listLoading = false
    }
  }
}
</script>

<style lang="scss" scoped>
p {
  margin-block-start: .2em;
  margin-block-end: .2em;
}
::v-deep .el-form-item__content{
  display: block;
  margin-left: 0!important;
  width: 100%;
}
::v-deep .el-form-item__label{
  float: none;
}

.select{
  width: 100%;
}
.right-drawer {
  width:300px;
  height: 100%;
  background: #fff;
  padding: 10px 15px;
  border-top: 1px solid #cccfcc;
}
.label-text{
  color: #666;
}
::v-deep .detail-title{
  width: 100px;
}
::v-deep .el-descriptions .is-bordered .el-descriptions-item__cell{
  padding: 5px!important;
}
.resource-data{
  font-size: 12px;
  margin-top: 10px;
}
</style>
