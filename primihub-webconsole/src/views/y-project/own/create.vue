<script>
import ViewForm from "../../../components/ViewBox/form.vue"
import { saveProject } from '@/api/project'
import { resourceFilePreview } from '@/api/resource'
import { getLocalOrganInfo } from '@/api/center'
import ProjectResourceDialog from '@/components/ProjectResourceDialog'
import ResourceTable from '@/components/ResourceTable'
import ProviderOrganDialog from '@/components/ProviderOrganDialog'
import ResourcePreviewDialog from '@/components/ResourcePreviewDialog'

export default {
  name: "YProjectCreate",
  components: {
    ViewForm,
    ProjectResourceDialog,
    ResourceTable,
    ProviderOrganDialog,
    ResourcePreviewDialog
  },
  data() {
    return {
      loading: false,
      form: {
        projectName: '',
        projectEnName: '',
        projectDesc: '',
        projectType: '',
        agreementDesc: '',
        initiateOrganId: '',
        initiateOrganName: '',
        providerOrganIds: []
      },
      rules: {
        projectName: [
          { required: true, message: '请输入项目中文名称', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        projectEnName: [
          { required: true, message: '请输入项目英文名称', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        projectType: [
          { required: true, message: '请选择项目类型', trigger: 'change' }
        ],
        projectDesc: [
          { required: true, message: '请输入项目描述', trigger: 'blur' },
          { min: 0, max: 200, message: '长度200字符以内', trigger: 'blur' }
        ],
        agreementDesc: [
          { required: true, message: '请输入合约描述', trigger: 'blur' },
          { min: 0, max: 200, message: '长度200字符以内', trigger: 'blur' }
        ],
        initiateOrganId: [
          { required: true, message: '请选择发起方', trigger: 'blur' }
        ],
        providerOrganIds: [
          { required: true, message: '请选择协作方', trigger: 'blur' }
        ]
      },
      resourceList: {},
      previewList: [],
      resourceDialogVisible: false,
      previewDialogVisible: false,
      providerOrganDialogVisible: false,
      selectedOrganId: '',
      selectedData: [],
    }
  },
  computed: {
    thisInstitution() {
      return this.$store.getters.userOrganId === this.selectedOrganId
    },
    filterData () {
      return (organId) => this.resourceList[organId] || []
    }
  },
  async created() {
    await this.getLocalOrganInfo()
  },
  methods: {
    /** get local organ info */
    async getLocalOrganInfo() {
      const { result = {}} = await getLocalOrganInfo()
      this.form.initiateOrganId = result.sysLocalOrganInfo?.organId
      this.form.initiateOrganName = result.sysLocalOrganInfo?.organName
    },

    /** open resource dialog */
    openResourceDialog(id) {
      this.selectedOrganId = id
      this.resourceDialogVisible = true
    },

    /** close resource dialog */
    closeResourceDialog() {
      this.resourceDialogVisible = false
    },

    /** submit resource dialog */
    handleDialogSubmit(data) {
      const newObj = { [this.selectedOrganId]: data.filter(item => item.organId === this.selectedOrganId) }
      this.resourceList = { ...this.resourceList, ...newObj }
      this.closeResourceDialog()
    },

    /** preview resource dialog */
    async handleResourcePreview({ resourceId }) {
      const { result } = await resourceFilePreview({ resourceId })
      this.previewList = result.dataList
      this.previewDialogVisible = true
    },

    /** Remove resource */
    handleResourceRemove({ organId, resourceId }) {
      const index = this.resourceList[organId].findIndex(item => item.resourceId === resourceId)
      this.resourceList[organId].splice(index, 1)
    },

    /** submit provider Organ dialog */
    handleProviderOrganSubmit(data) {
      this.form.providerOrganIds = data
      this.providerOrganDialogVisible = false
    },

    /** Remove provider Organ */
    handleProviderRemove(index) {
      this.form.providerOrganIds.splice(index, 1)
    },

    /** save resource or next step */
    handelSave() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {

        } else {
          return false
        }
      })
    },

    /** go to resource list */
    toListPage() {
      this.$router.push({
        name: "YProjectOwnList",
      })
    }
  }
}
</script>

<template>
  <div class="y-project-create-container">
    <view-form width="800px" @save="handelSave" @cancel="toListPage">
      <template v-slot:container>
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
        >
          <el-form-item label="项目中文名称" prop="projectName">
            <div class="item-wrap-normal">
              <el-input
                v-model="form.projectName"
                maxlength="20"
                minlength="3"
                show-word-limit
                placeholder="请输入项目中文名称"
              />
            </div>
          </el-form-item>
          <el-form-item label="项目英文名称" prop="projectEnName">
            <div class="item-wrap-normal">
              <el-input
                v-model="form.projectEnName"
                maxlength="20"
                minlength="3"
                show-word-limit
                placeholder="请输入项目英文名称"
              />
            </div>
          </el-form-item>
          <el-form-item label="项目类型" prop="projectType">
            <el-select v-model="form.projectType" placeholder="请选择项目类型">
              <el-option label="区域一" value="shanghai"></el-option>
              <el-option label="区域二" value="beijing"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="项目描述" prop="projectDesc">
            <div class="item-wrap-normal">
              <el-input
                v-model="form.projectDesc"
                type="textarea"
                maxlength="200"
                minlength="3"
                show-word-limit
                placeholder="请输入项目描述"
              />
            </div>
          </el-form-item>
          <el-form-item label="合约描述" prop="agreementDesc">
            <div class="item-wrap-normal">
              <el-input
                v-model="form.agreementDesc"
                type="textarea"
                maxlength="200"
                minlength="3"
                show-word-limit
                placeholder="请输入合约描述"
              />
            </div>
          </el-form-item>
          <el-form-item label="发起方" prop="initiateOrganId">
            <p class="organ"> <i class="el-icon-office-building" /> {{ form.initiateOrganName }} </p>
            <el-button type="primary" plain @click="openResourceDialog(form.initiateOrganId)" >添加资源到此项目</el-button>
            <ResourceTable
              v-if="filterData(form.initiateOrganId).length"
              :creator="true"
              :show-status="false"
              row-key="resourceId"
              :data="filterData(form.initiateOrganId)"
              @remove="handleResourceRemove"
              @preview="handleResourcePreview"
            />
          </el-form-item>
          <el-form-item label="协作方" prop="providerOrganIds">
            <el-button plain @click="providerOrganDialogVisible = true"
              >添加协作者</el-button
            >
            <div
              v-for="(organ, index) in form.providerOrganIds"
              :key="organ.globalId"
              class="organ-item"
            >
              <p>
                <span class="organ"><i class="el-icon-office-building" /> {{ organ.globalName }}</span>
                <i class="el-icon-delete remove-provider"  @click="handleProviderRemove(index)" />
              </p>
              <el-button  v-if="selectedData" type="primary" plain @click="openResourceDialog(organ.globalId)">添加资源到此项目</el-button>

              <ResourceTable
                v-if="filterData(organ.globalId).length > 0"
                :show-status="false"
                :creator="true"
                :this-institution="thisInstitution"
                row-key="resourceId"
                :data="filterData(organ.globalId)"
                @remove="handleResourceRemove"
                @preview="handleResourcePreview"
              />
            </div>
          </el-form-item>
        </el-form>
      </template>
    </view-form>
     <!-- add resource dialog -->
     <ProjectResourceDialog
      ref="dialogRef"
      top="10px"
      width="800px"
      :selected-data="resourceList[selectedOrganId]"
      title="添加资源"
      :show-preview-button="thisInstitution"
      :organ-id="selectedOrganId"
      :visible="resourceDialogVisible"
      @close="closeResourceDialog"
      @submit="handleDialogSubmit"
      @preview="handleResourcePreview"
    />
     <!-- add provider organ dialog -->
     <ProviderOrganDialog :selected-data="form.providerOrganIds" :visible.sync="providerOrganDialogVisible" title="添加协作方" @submit="handleProviderOrganSubmit" @close="handleProviderOrganSubmit" />
    <!-- preview dialog -->
    <ResourcePreviewDialog
      :data="previewList"
      :visible.sync="previewDialogVisible"
      append-to-body
      width="1000px"
      @close="previewDialogVisible = false"
    />
  </div>
</template>

<style lang="scss" scoped>
.y-project-create-container{
  ::v-deep .el-input, ::v-deep .el-textarea{
    width: 500px;
  }
}
.remove-provider{
  margin-left: 5px;
  color: #f56c6c;
  cursor: pointer;
}
</style>
