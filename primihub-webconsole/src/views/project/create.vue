<template>
  <div class="app-container">
    <h2>新建项目</h2>
    <el-form
      ref="dataForm"
      :model="dataForm"
      :rules="dataRules"
      label-width="100px"
      class="demo-dataForm"
    >
      <el-form-item label="项目名称" prop="projectName">
        <div class="item-wrap-normal">
          <el-input v-model="dataForm.projectName" />
        </div>
      </el-form-item>
      <el-form-item label="项目描述" prop="projectDesc">
        <div class="item-wrap-normal">
          <el-input v-model="dataForm.projectDesc" type="textarea" />
        </div>
      </el-form-item>
      <!-- <el-form-item label="中心节点" prop="organId">
        <OrganCascader placeholder="请选择" :show-all-levels="false" @change="handleOrganSelect" />
      </el-form-item> -->
      <el-form-item label="添加资源" prop="resources">
        <div class="resource-box">
          <ResourceItemCreate class="item" @click="openDialog" />
          <ResourceItemSimple v-for="item in selectResources" :key="item.resourceId" class="item" :data="item" :show-close="true" @delete="handleDelete" />
        </div>
        <ProjectResourceDialog :dialog-visible="dialogVisible" :select-ids="selectIds" @close="handleDialogCancel" @submit="handleDialogSubmit" />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="submitForm('dataForm')"
        >
          立即创建
        </el-button>
        <el-button @click="goBack()">返回</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { saveProject } from '@/api/project'
import ResourceItemCreate from '@/components/ResourceItemCreate'
import ResourceItemSimple from '@/components/ResourceItemSimple'
import ProjectResourceDialog from '@/components/ProjectResourceDialog'

export default {
  components: { ResourceItemSimple, ResourceItemCreate, ProjectResourceDialog },
  data() {
    // const checkResource = (rule, value, cb) => {
    //   console.log('value', value.length)
    //   if (value.length === 0) {
    //     cb(new Error('请选择资源'))
    //   }
    // }
    return {
      dataForm: {
        projectName: '',
        projectDesc: '',
        organId: '',
        resources: []
      },
      dialogVisible: false,
      resourceList: [],
      dataRules: {
        projectName: [
          { required: true, message: '请输入项目名称', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        projectDesc: [
          { required: true, message: '请输入项目描述', trigger: 'blur' },
          { min: 0, max: 200, message: '长度200字符以内', trigger: 'blur' }
        ],
        organId: [
          { required: true, message: '请选择中心节点', trigger: 'blur' }
        ]
      },
      selectResources: [],
      selectIds: [],
      serverAddress: '',
      organId: ''
    }
  },
  methods: {
    addResource() {
      if (!this.serverAddress) {
        this.$message({
          message: '请先选择机构',
          type: 'warning'
        })
        return
      }
      this.openDialog()
    },
    openDialog() {
      console.log('open')
      this.dialogVisible = true
    },
    handleDialogCancel() {
      this.dialogVisible = false
    },
    handleDialogSubmit({ list, selectIds }) {
      console.log(list)
      this.selectResources = list
      this.selectIds = selectIds
      console.log('handleDialogSubmit selectIds', this.selectIds)
      this.dataForm.resources = selectIds
      this.dialogVisible = false
    },
    submitForm(formName) {
      if (this.selectResources.length === 0) {
        this.$message({
          message: '请先选择资源',
          type: 'warning'
        })
        return
      }
      this.$refs[formName].validate((valid) => {
        if (valid) {
          saveProject(this.dataForm).then(res => {
            if (res.code === 0) {
              const id = res.result.projectId
              this.$router.push({
                name: 'ProjectDetail',
                params: { id }
              })
            }
            console.log(res)
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    handleDelete(data) {
      const index = this.selectIds.indexOf(data.id)
      this.selectIds.splice(index, 1)
      this.selectResources.splice(index, 1)
    },
    goBack() {
      this.$router.replace({
        name: 'ProjectList'
      })
    },
    handleOrganSelect(data) {
      this.serverAddress = data.serverAddress
      this.dataForm.organId = data.organId
    }
  }
}
</script>
<style lang="scss">
  .item-wrap-normal {
    width: 400px;
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
</style>
