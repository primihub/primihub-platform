<template>
  <div v-loading="listLoading" class="right-drawer">
    <el-form v-if="nodeData" ref="form" :model="nodeData" :rules="rules" label-width="80px" element-loading-spinner="el-icon-loading">
      <template v-if="isDataSelect">
        <el-form-item>
          <p class="organ"><i class="el-icon-office-building" /> <span>发起方：</span> {{ initiateOrgan.organName }}</p>
          <el-button type="primary" size="small" plain @click="openDialog(initiateOrgan.organId, 1)">选择资源</el-button>
          <ResourceDec v-if="initiateOrgan.resourceId" :data="initiateOrgan" @change="handleResourceHeaderChange" />
        </el-form-item>
        <el-form-item>
          <template v-if="providerOrganOptions.length>0">
            <span class="organ"><i class="el-icon-office-building" /> <span>协作方：</span> {{ providerOrganName }}</span>
            <div class="organ-select">
              <el-select v-model="providerOrganId" placeholder="请选择" size="small" @change="handleProviderOrganChange">
                <el-option
                  v-for="(v,index) in providerOrganOptions"
                  :key="index"
                  :label="v.organName"
                  :value="v.organId"
                />
              </el-select>
              <el-button type="primary" size="small" plain @click="openDialog(providerOrganId,2)">选择资源</el-button>
            </div>
            <ResourceDec v-if="providerOrgans.length > 0" :data="providerOrgans[0]" @change="handleResourceHeaderChange" />
          </template>
          <template v-else>
            <i class="el-icon-office-building" /> <span>暂无审核通过的协作方 </span>
          </template>
        </el-form-item>
      </template>
      <template v-else>
        <div v-for="item in nodeData.componentTypes" :key="item.typeCode">
          <el-form-item :label="item.typeName " :prop="item.typeCode">
            <template v-if="item.inputType === 'label'">
              <span class="label-text">{{ item.inputValue }}</span>
            </template>
            <template v-if="item.inputType === 'text'">
              <el-input v-model="item.inputValue" size="mini" @change="handleChange" />
            </template>
            <template v-if="item.inputType === 'textarea'">
              <el-input v-model="item.inputValue" type="textarea" size="mini" @change="handleChange" />
            </template>
            <template v-if="item.inputType === 'radio'">
              <el-radio-group v-model="item.inputValue" @change="handleChange">
                <el-radio v-for="(r,index) in item.inputValues" :key="index" :label="r.key">{{ r.val }}</el-radio>
              </el-radio-group>
            </template>
            <template v-if="item.inputType === 'select'">
              <el-select v-model="item.inputValue" placeholder="请选择" :value-key="item.typeCode" @change="handleChange">
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
      </template>
    </el-form>
    <el-button type="primary" @click="save">保存</el-button>
    <!-- add resource dialog -->
    <ResourceDialog ref="dialogRef" top="10px" width="800px" :selected-data="selectedResourceId" title="选择资源" :table-data="resourceList[selectedOrganId]" :visible="dialogVisible" @close="handleDialogCancel" @submit="handleDialogSubmit" />
  </div>
</template>

<script>
import { getProjectResourceData, getProjectResourceOrgan } from '@/api/model'
import ResourceDialog from '@/components/ResourceDialog'
import ResourceDec from '@/components/ResourceDec'

export default {
  components: {
    ResourceDialog,
    ResourceDec
  },
  props: {
    nodeData: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    const modelNameValidate = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入模型名称'))
      } else {
        callback()
      }
    }
    return {
      form: {
        dynamicError: {
          name: ''
        }
      },
      listLoading: false,
      initiateOrgan: {},
      providerOrgans: [],
      providerOrganOptions: [],
      providerOrganId: '',
      providerOrganName: '',
      dialogVisible: false,
      selectedOrganId: '',
      resourceList: [],
      selectedResourceId: '',
      participationIdentity: 2,
      inputValues: [],
      inputValue: this.nodeData && this.nodeData.componentTypes[0].inputValue,
      rules: {
        modelName: [
          { required: true, trigger: 'blur', validator: modelNameValidate }
        ]
      }
    }
  },
  computed: {
    isDataSelect() {
      return this.nodeData && this.nodeData.componentCode === 'dataSet'
    }
  },
  watch: {
    async nodeData(newVal) {
      if (newVal) {
        if (this.nodeData.componentCode === 'dataSet') {
          await this.getProjectResourceOrgan()
          this.inputValue = this.nodeData.componentTypes[0].inputValue
          if (this.inputValue !== '') {
            this.inputValue = JSON.parse(this.inputValue)
            const providerOrgans = this.inputValue.filter(item => item.participationIdentity === 2)
            const initiateOrgan = this.inputValue.filter(item => item.participationIdentity === 1)
            this.providerOrgans = providerOrgans.length > 0 ? providerOrgans : this.providerOrgans
            this.initiateOrgan = initiateOrgan.length > 0 ? initiateOrgan[0] : this.initiateOrgan
            this.providerOrganId = this.providerOrgans.length > 0 ? this.providerOrgans[0].organId : ''
            this.providerOrganName = this.providerOrgans.filter(item => item.organId === this.providerOrganId)[0].organName
          }
        }
      }
    }
  },
  created() {
    this.projectId = Number(this.$route.query.projectId) || 0
  },
  methods: {
    handleResourceHeaderChange(data) {
      this.setInputValue(data)
      this.save()
    },
    async openDialog(organId, participationIdentity) {
      this.participationIdentity = participationIdentity
      this.selectedOrganId = organId
      if (this.selectedOrganId === '') {
        this.$message({
          message: '请先选择机构',
          type: 'warning'
        })
        return
      }
      if (this.inputValue) {
        const currentOrgan = this.inputValue.filter(item => item.organId === organId)[0]
        if (currentOrgan) {
          this.selectedResourceId = currentOrgan.resourceId
        } else {
          this.selectedResourceId = ''
        }
      }
      await this.getProjectResourceData()
      this.dialogVisible = true
    },
    handleChange() {
      console.log('handleChange', this.nodeData)
      this.$emit('change', this.nodeData)
    },
    handleProviderOrganChange(value) {
      this.providerOrganId = value
      this.providerOrganName = this.providerOrganOptions.filter(item => item.organId === value)[0].organName
    },
    handleDialogCancel() {
      this.dialogVisible = false
    },
    handleDialogSubmit(data) {
      // not selecting resource
      if (!data.resourceId) {
        this.dialogVisible = false
        return
      }
      if (this.participationIdentity === 1) {
        data.organName = this.initiateOrgan.organName
        this.initiateOrgan = data
      } else {
        data.organName = this.providerOrganOptions.filter(item => item.organId === this.providerOrganId)[0].organName
        this.providerOrgans = [data]
      }
      this.selectedResourceId = data.resourceId
      // set input value
      this.setInputValue(data)
      this.save()
      this.dialogVisible = false
    },
    setInputValue(data) {
      if (!data.calculationField) {
        data.calculationField = data.fileHandleField[0]
      }
      if (this.inputValue) {
        this.inputValues = this.inputValue
      }
      const posIndex = this.inputValues.findIndex(item => item.organId === data.organId)
      const currentData = {
        ...data
      }
      if (posIndex !== -1) {
        this.inputValues[posIndex] = currentData
      } else {
        this.inputValues.push(currentData)
      }
      console.log('inputValues', this.inputValues)
      this.nodeData.componentTypes[0].inputValue = JSON.stringify(this.inputValues)
    },
    async getProjectResourceData() {
      this.resourceList = []
      const params = {
        projectId: this.projectId,
        organId: this.selectedOrganId
      }
      const { code, result } = await getProjectResourceData(params)
      if (code === 0) {
        this.resourceList[this.selectedOrganId] = result
      }
    },
    async getProjectResourceOrgan() {
      this.listLoading = true
      const res = await getProjectResourceOrgan({ projectId: this.projectId })
      if (res.code === 0) {
        this.organs = res.result
        this.providerOrganOptions = this.organs.filter(item => item.participationIdentity === 2)
        this.initiateOrgan = this.organs.filter(item => item.participationIdentity === 1)[0]
        this.providerOrgans = []
        this.providerOrganId = ''
      }
      this.listLoading = false
    },
    save() {
      this.$emit('save')
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
  width: 300px;
  background: #fff;
  padding: 10px 15px;
}
.label-text{
  color: #666;
}
::v-deep .detail-title{
  width: 100px;
}
::v-deep .el-descriptions .is-bordered .el-descriptions-item__cell{
  padding:2px 5px!important;
}
::v-deep .el-checkbox__label{
  font-size: 12px!important;
}
.resource-data{
  font-size: 12px;
  margin-top: 10px;
}
.organ-select{
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.required{
  color: red;
  margin-right: 10px;
  font-size: 20px;
  line-height: 1;
}
</style>
