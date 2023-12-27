<template>
  <div v-loading="loading" class="app-container">
    <el-form
      ref="dataForm"
      :model="dataForm"
      :rules="dataRules"
      label-width="120px"
      class="demo-dataForm"
    >
      <div class="section-title">基本信息</div>
      <el-form-item label="资源名称" prop="resourceName">
        <div class="item-wrap-normal">
          <el-input
            v-model="dataForm.resourceName"
            maxlength="20"
            minlength="3"
            show-word-limit
          />
        </div>
      </el-form-item>
      <el-form-item label="资源描述" prop="resourceDesc">
        <div class="item-wrap-normal">
          <el-input
            v-model="dataForm.resourceDesc"
            type="textarea"
            maxlength="200"
            minlength="3"
            show-word-limit
          />
        </div>
      </el-form-item>
      <el-form-item label="标签" prop="tags">
        <div class="item-wrap-normal">
          <el-tag
            v-for="(tag,index) in dataForm.tags"
            :key="index"
            closable
            @close="handleClose(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="saveTagInput"
            v-model="tagInputValue"
            class="input-new-tag"
            size="small"
            maxlength="10"
            minlength="1"
            show-word-limit
            @keyup.enter.native="handleInputConfirm"
            @blur="handleInputConfirm"
          />
          <el-button class="button-new-tag" size="small" icon="el-icon-plus" type="primary" plain @click="showInput">添加标签</el-button>
          <p class="tips">标签个数不能超过5个</p>
        </div>
      </el-form-item>
      <el-form-item label="授权方式" prop="resourceAuthType">
        <div class="item-wrap-normal auth-container">
          <el-radio-group v-model="dataForm.resourceAuthType" @change="handleAuthTypeChange">
            <el-radio v-for="item in resourceAuthTypeOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
          <div v-if="dataForm.resourceAuthType !== 1" class="flex">
            <el-button type="primary" @click="openAuthDialog">指定授权</el-button>
            <div v-if="organValue.length > 0 && userValue.length > 0" style="margin-left: 10px;">已授权{{ organValue.length }}个机构，{{ userValue.length }}个用户</div>
          </div>
        </div>
      </el-form-item>
      <div class="section-title">数据引入</div>
      <template v-if="!isEditPage">
        <el-form-item label="资源来源" prop="resourceSource">
          <div class="item-wrap-normal">
            <el-radio-group v-model="dataForm.resourceSource" @change="handleRadioChange">
              <el-radio :label="1">文件上传</el-radio>
              <el-radio v-if="showDatabaseRadio" :label="2">数据库导入</el-radio>
            </el-radio-group>
          </div>
        </el-form-item>
        <el-form-item v-if="dataForm.resourceSource === 1">
          <upload :max-size="fileMaxSize" :show-tips="true" :single="true" @success="handleUploadSuccess" />
        </el-form-item>
        <template v-if="showDatabaseRadio && dataForm.resourceSource === 2">
          <DatabaseImport @success="handleImportSuccess" @change="handleImportChange" />
        </template>
      </template>
      <el-form-item v-else label="资源来源" prop="resourceSource">
        <div class="item-wrap-normal">
          <span>{{ dataForm.resourceSource | sourceFilter }}</span>
        </div>
      </el-form-item>
      <el-row
        :gutter="20"
        class="data-container"
        element-loading-text="加载中"
        element-loading-spinner="el-icon-loading"
      >
        <el-col v-if="fieldList.length > 0" :span="12">
          <EditResourceTable
            border
            height="500"
            :data="fieldList"
            :is-edit-page="isEditPage"
            @change="handleResourceChange"
          />
        </el-col>
        <el-col v-if="dataList.length >0" :span="12">
          <ResourcePreviewTable :data="dataList" height="500" />
        </el-col>
      </el-row>
      <el-form-item>
        <el-button
          type="primary"
          @click="submitForm"
        >
          保存
        </el-button>
        <el-button @click="goBack">取消</el-button>
      </el-form-item>
    </el-form>
    <el-dialog
      title="选择授权对象 / 选择机构"
      :visible.sync="dialogVisible"
      width="700px"
    >
      <template v-if="stepActive === 1">
        <div class="flex">
          <el-transfer
            ref="organTransfer"
            v-model="organValue"
            v-loading="organLoading"
            class="transfer-container"
            filterable
            :left-default-checked="organLeftDefaultChecked"
            :right-default-checked="organRightDefaultChecked"
            :titles="['已建立连接机构', '已选']"
            :format="{
              noChecked: '${total}',
              hasChecked: '${checked}/${total}'
            }"
            :data="organList"
            @change="handleChange($event, 'organ')"
          >
            <span slot-scope="{ option }">{{ option.label }}</span>
          </el-transfer>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleStep">下一步</el-button>
        </span>
      </template>
      <template v-else>
        <AuthorizationUserTransfer :value="userValue" @change="handleChange($event, 'user')" />
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="handleStep">上一步</el-button>
          <el-button @click="handleAuthConfirm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { saveResource, getResourceDetail, resourceFilePreview, displayDatabaseSourceType } from '@/api/resource'
import { getAvailableOrganList } from '@/api/center'
import { resourceAuthorizationType } from '@/const'
import Upload from '@/components/Upload'
import EditResourceTable from '@/components/EditResourceTable'
import ResourcePreviewTable from '@/components/ResourcePreviewTable'
import DatabaseImport from '@/components/DatabaseImport'
import AuthorizationUserTransfer from '@/components/authorizationUserTransfer'

export default {
  components: {
    Upload,
    EditResourceTable,
    ResourcePreviewTable,
    DatabaseImport,
    AuthorizationUserTransfer
  },
  data() {
    return {
      organLeftDefaultChecked: [],
      organRightDefaultChecked: [],
      organLoading: false,
      stepActive: 1,
      dialogVisible: false,
      organValue: [],
      userValue: [],
      fusionOrganList: [],
      isEditPage: false,
      fileUrl: '',
      dataForm: {
        resourceName: '',
        resourceDesc: '',
        tags: [],
        resourceSource: 1,
        resourceAuthType: 1,
        fileId: -1, // 文件id
        fieldList: [],
        fusionOrganList: []
      },
      dataRules: {
        resourceName: [
          { required: true, message: '请输入资源名称', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        resourceDesc: [
          { required: true, message: '请输入资源描述', trigger: 'blur' },
          { min: 0, max: 200, message: '长度200字符以内', trigger: 'blur' }
        ],
        tags: [
          { required: true, message: '请输入标签', trigger: 'blur' }
        ],
        resourceSource: [
          { required: true, message: '请选择来源', trigger: 'change' }
        ],
        resourceAuthType: [
          { required: true, message: '请选择授权方式', trigger: 'change' }
        ]
      },
      tagInputVisible: false,
      tagInputValue: '',
      fileMaxSize: 1024 * 1024 * 1, // file limit 1MB
      resource: null,
      loading: false,
      fieldTypeList: [{
        value: 0,
        label: 'String'
      }, {
        value: 1,
        label: 'Integer'
      }, {
        value: 2,
        label: 'Double'
      }, {
        value: 3,
        label: 'Long'
      }, {
        value: 4,
        label: 'Enum'
      }, {
        value: 5,
        label: 'Boolean'
      }],
      dataList: [], // resource preview
      fieldList: [], // resource field info
      props: {
        multiple: true,
        checkStrictly: false,
        emitPath: true,
        lazyLoad: this.lazyLoad,
        leaf: 'leaf',
        lazy: true
      },
      showDatabaseRadio: false,
      organList: [],
      userList: [],
      resourceAuthTypeOptions: resourceAuthorizationType
    }
  },
  async created() {
    this.isEditPage = this.$route.name === 'ResourceEdit'
    this.resourceId = this.$route.params.id
    if (this.isEditPage) {
      await this.getResourceDetail()
    }
    await this.displayDatabaseSourceType()
  },
  methods: {
    async openAuthDialog() {
      this.dialogVisible = true
      await this.getAvailableOrganList()
    },
    handleAuthConfirm() {
      this.stepActive = 1
      this.dialogVisible = false
      this.userValue = this.userRightDefaultChecked
      console.log('organValue', this.organValue)
      console.log('userValue', this.userValue)
    },
    handleCancel() {
      this.dialogVisible = false
      this.stepActive = 1
    },
    handleStep() {
      if (this.stepActive === 1 && this.organValue.length === 0) {
        this.$message.error('请先选择机构')
        return
      }
      this.stepActive = this.stepActive === 1 ? 2 : 1
      console.log('this.userValue', this.userValue)
      this.$nextTick(() => {
        this.$refs.organTransfer && this.$refs.organTransfer.clearQuery('left')
        this.$refs.organTransfer && this.$refs.organTransfer.clearQuery('right')
        this.$refs.userTransfer && this.$refs.userTransfer.clearQuery('left')
        this.$refs.userTransfer && this.$refs.userTransfer.clearQuery('right')
      })
    },
    handleChange(value, name) {
      if (name === 'organ') {
        this.organRightDefaultChecked = value
      } else {
        this.userRightDefaultChecked = value
      }
    },
    async getAvailableOrganList() {
      this.organLoading = true
      const { result } = await getAvailableOrganList()
      this.organList = result.map(item => {
        return {
          key: item.globalId,
          label: item.globalName
        }
      })
      this.organLoading = false
      console.log('organList', this.organList)
    },
    async displayDatabaseSourceType() {
      const res = await displayDatabaseSourceType()
      if (res.code === 0) {
        this.showDatabaseRadio = res.result
      }
    },
    handleRadioChange() {
      this.dataForm.fieldList = []
      this.fieldList = []
      this.dataList = []
    },
    handleImportSuccess(data) {
      this.dataForm.dataSource = data.dataSource
      this.fieldList = data.fieldList
      this.dataForm.fieldList = this.formatParams()
      this.dataList = data.dataList
    },
    handleImportChange() {
      this.fieldList = []
      this.dataForm.fieldList = this.fieldList
      this.dataList = []
    },
    handleResourceChange(data) {
      this.fieldList = data
      this.dataForm.fieldList = this.formatParams()
    },
    handleUploadSuccess({ fileId }) {
      this.dataForm.fileId = fileId
      this.resourceFilePreview()
    },
    resourceFilePreview() {
      this.fieldListLoading = true
      resourceFilePreview({ fileId: this.dataForm.fileId }).then(res => {
        this.dataList = res.result.dataList
        this.fieldList = res.result.fieldList
        this.dataForm.fieldList = this.formatParams()
        this.fieldListLoading = false
      })
    },
    formatParams() {
      const fieldList = []
      this.fieldList && this.fieldList.forEach(item => {
        const { fieldId, fieldName, fieldType, fieldDesc = '' } = item
        fieldList.push({
          fieldId,
          fieldName,
          fieldType,
          fieldDesc
        })
      })
      return fieldList
    },
    handleClose(tag) {
      this.dataForm.tags.splice(this.dataForm.tags.indexOf(tag), 1)
    },
    showInput() {
      if (this.dataForm.tags.length > 4) {
        this.$message({
          message: '标签个数不能超过5个',
          type: 'warning'
        })
        return
      }
      this.tagInputVisible = true
    },
    handleInputConfirm() {
      if (this.tagInputValue) {
        this.dataForm.tags.push(this.tagInputValue)
      }
      this.tagInputVisible = false
      this.tagInputValue = ''
    },
    handelTagsInputFocus() {
      if (this.dataForm.tags.length > 4) {
        this.$message({
          message: '标签个数不能超过5个',
          type: 'warning'
        })
      }
    },
    async submitForm() {
      this.$refs['dataForm'].validate(async(valid) => {
        if (valid) {
          if (this.dataForm.fieldList.length < 1) {
            this.$message({
              message: '请先上传文件或导入数据',
              type: 'warning'
            })
            this.loading = false
            return
          }
          if (this.isEditPage) {
            Object.assign(this.dataForm, {
              resourceId: this.$route.params.id
            })
          }
          if (this.dataForm.resourceAuthType !== 1) {
            this.organValue.forEach(item => {
              const current = this.organList.find(organ => organ.key === item)
              console.log(current, item)
              this.fusionOrganList.push({
                organGlobalId: current.key,
                organName: current.label
              })
            })
            if (this.fusionOrganList.length > 0) {
              this.dataForm.fusionOrganList = this.fusionOrganList
            }

            this.dataForm.userAssignList = this.userValue.map(item => {
              return {
                userId: item
              }
            })
          }

          this.loading = true
          console.log(this.dataForm)
          saveResource(this.dataForm).then(res => {
            this.loading = false
            if (res.code === 0) {
              this.$message.success('保存成功')
              this.toResourceDetail(res.result.resourceId)
            } else {
              this.loading = false
              this.$message({
                message: res.msg,
                type: 'error'
              })
            }
          }).catch(err => {
            this.loading = false
            console.log(err)
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    toResourceDetail(id) {
      this.$router.push({
        name: 'ResourceDetail',
        params: { id }
      })
    },
    async getResourceDetail() {
      this.loading = true
      const { result = {}} = await getResourceDetail(this.resourceId)
      const { resource, dataList, fieldList, fusionOrganList } = result
      const { resourceName, resourceDesc, resourceAuthType, resourceSource, tags, fileId, url } = resource
      this.resource = resource
      this.dataForm.resourceName = resourceName
      this.dataForm.resourceDesc = resourceDesc
      this.dataForm.resourceAuthType = resourceAuthType
      this.dataForm.resourceSource = resourceSource
      this.dataForm.fileId = fileId
      this.dataList = dataList || []
      this.fieldList = fieldList || []
      this.fusionOrganList = fusionOrganList.map(item => {
        return {
          organGlobalId: item.organGlobalId,
          organName: item.organName
        }
      })
      this.dataForm.fieldList = this.formatParams()
      tags.forEach(item => {
        this.dataForm.tags.push(item.tagName)
      })
      this.fileUrl = url
      this.loading = false
    },
    goBack() {
      this.$router.replace({
        name: 'ResourceList'
      })
    },
    async handleAuthTypeChange() {
      this.organValue = []
      this.userValue = []
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/variables.scss";
::v-deep .el-loading-mask{
  z-index: 1;
}
::v-deep .el-tag{
  margin-right: 6px;
}
::v-deep .el-upload-dragger{
  width: 250px;
  height: 100px;
  .el-icon-upload{
    margin: 20px 0;
  }
}
::v-deep .el-transfer-panel{
  width: 245px;
}
::v-deep .el-transfer__buttons{
  .el-button{
    display: block;
    padding: 5px 5px;
  }
  .el-button--primary.is-disabled{
    background-color: #fff;
    color: #333;
    border-color: #eee;
  }
  .el-button+.el-button{
    margin-left: 0;
  }
}
.app-container{
  padding: 25px 48px;
}
.section-title{
  font-size: 16px;
  color: #333;
  margin-bottom: 20px;
  position: relative;
  padding-bottom: 15px;
  &:before{
    content: '';
    display: inline-block;
    width: 5px;
    height: 26px;
    background-color: $mainColor;
    border-radius: 0px 5px 5px 0px;
    margin-right: 10px;
    transform: translateY(7px);
  }
  &::after{
    content: "";
    display: block;
    height: 1px;
    background: #eee;
    width: 100%;
    left: 0;
    position: absolute;
    bottom: 0;
  }
}
.transfer-container{
  text-align: left;
  // display: inline-block;
  margin: auto;
}
.item-wrap-normal {
  width: 660px;
}
.resource-box {
  display: flex;
  flex-wrap: wrap;
  .item {
    flex-shrink: 0;
    margin-bottom: 20px;
    margin-right: 20px;
  }
}
.el-upload__text{
  display: inline-block;
}
.input-new-tag {
  width: 90px;
  margin-right: 10px;
  vertical-align: middle;
}
.data-container{
  padding: 0 30px 30px;
}
.tips{
  font-size: 12px;
  color: #999;
  line-height: 1;
  margin-top: 5px;
}
.auth-container{
  position: relative;
}
::v-deep .el-cascader{
  width: 300px;
  position: absolute;
  top: 00px;
  left: 290px;
  z-index: 12;
}
::v-deep .el-table,::v-deep .el-divider__text, .el-link{
  font-size: 12px;
}
</style>
