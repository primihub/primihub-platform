<template>
  <div class="search-area">
    <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="demo-form">
      <div class="select-resource">
        <div v-if="isSelected" class="select-row">
          <el-form-item label="已选资源" prop="selectResources" />
          <div class="resource-box">
            <ResourceItemSimple v-for="item in selectResources" :key="item.resourceId" :data="item" :show-close="true" class="select-item" @delete="handleDelete" />
            <ResourceItemCreate @click="openDialog" />
          </div>
        </div>
        <div v-else class="dialog-con">
          <el-form-item label="选择查询资源" prop="resourceName">
            <OrganCascader placeholder="请选择" :show-all-levels="false" @change="handleOrganSelect" />
            <el-button icon="el-icon-search" type="primary" @click="openDialog">查询</el-button>
            <!-- <ResourceDialog ref="dialogRef" :server-address="serverAddress" :organ-id="organId" :dialog-visible="dialogVisible" @close="handleDialogCancel" @submit="handleDialogSubmit" /> -->
          </el-form-item>
        </div>
        <el-form-item label="检索ID" prop="pirParam">
          <el-input v-model="form.pirParam" placeholder="请输入检索id" />
        </el-form-item>
        <el-button v-if="hasPermission" style="margin-top: 12px;" type="primary" class="query-button" @click="next">查询<i class="el-icon-search el-icon--right" /></el-button>
      </div>
      <ProjectResourceDialog
        ref="dialogRef"
        class="dialog"
        :center="false"
        top="10px"
        width="800px"
        :selected-data="selectResources"
        title="添加资源"
        :server-address="serverAddress"
        :organ-id="organId"
        :visible="dialogVisible"
        @close="handleDialogCancel"
        @submit="handleDialogSubmit"
      />
    </el-form>
  </div>
</template>

<script>
import { pirSubmitTask } from '@/api/PIR'
import ResourceItemSimple from '@/components/ResourceItemSimple'
import ResourceItemCreate from '@/components/ResourceItemCreate'
// import ResourceDialog from '@/components/ResourceDialog'
import ProjectResourceDialog from '@/components/ProjectResourceDialog'
import OrganCascader from '@/components/OrganCascader'

export default {
  components: {
    ResourceItemSimple,
    ResourceItemCreate,
    // ResourceDialog,
    ProjectResourceDialog,
    OrganCascader
  },
  props: {
    hasPermission: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      noData: false,
      form: {
        resourceName: '',
        pirParam: '',
        selectResources: []
      },
      rules: {
        // resourceName: [
        //   { required: true, message: '请选择资源', trigger: 'blur' }
        // ],
        pirParam: [
          { required: true, message: '请输入检索ID', trigger: 'blur' },
          { max: 10, message: '长度在10个字符以内', trigger: 'blur' }
        ]
      },
      dialogVisible: false,
      listLoading: false,
      selectResources: [], // selected resource id list
      serverAddress: '',
      organId: ''
    }
  },
  computed: {
    isSelected() {
      return this.selectResources && this.selectResources.length > 0
    }
  },
  methods: {
    next() {
      if (this.selectResources.length === 0) {
        this.$message({
          message: '请选择资源',
          type: 'error'
        })
        return
      }
      this.dialogVisible = false

      this.$refs.form.validate(valid => {
        if (valid) {
          pirSubmitTask({
            resourceId: this.selectResources[0].resourceId,
            pirParam: this.form.pirParam
          }).then(res => {
            const { taskId, taskDate } = res.result
            this.$emit('next', {
              taskId,
              taskDate,
              pirParam: this.form.pirParam
            })
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    openDialog() {
      console.log(this.serverAddress)
      if (!this.serverAddress) {
        this.$message({
          message: '请先选择机构',
          type: 'warning'
        })
        return
      }
      this.dialogVisible = true
    },
    handleDialogCancel() {
      this.dialogVisible = false
    },
    handleDialogSubmit(data) {
      console.log('handleDialogSubmit', data)
      this.selectResources = data.filter(item => item.organId === this.organId)
      // console.log(this.selectResources)
      this.dialogVisible = false
      // this.serverAddress = ''
      // this.organId = ''
    },
    handleDelete(data) {
      const index = this.selectResources.findIndex(item => item.resourceId === data.id)
      this.selectResources.splice(index, 1)
    },
    handleOrganSelect(data) {
      this.serverAddress = data.serverAddress
      this.organId = data.organId
    }
  }
}
</script>

<style lang="scss" scoped>
.search-area {
  margin: 20px auto;
  width: 620px;
  text-align: center;
}
.query-button {
  width: 200px;
  margin: 0 auto;
}
.select-row{
  display: flex;
  justify-content: flex-start;
  margin-bottom: 10px;
}
.dialog-con{
  text-align: left;
}
.resource-box{
  display: flex;
  flex-flow: wrap;
  // margin-left: auto;

}
.select-item{
  margin-right: 10px;
  margin-bottom: 10px;
}
.no-data{
  color: #999;
  margin: 0 auto;
  text-align: center;
}
.dialog{
  text-align: left;
}
.dialog-footer{
  width: 100%;
  display: inline-block;
  text-align: center;
}
::v-deep .el-cascader{
  width: 410px;
  margin-right: 10px;
}
::v-deep .el-form-item__content{
  text-align: left;
}
</style>
