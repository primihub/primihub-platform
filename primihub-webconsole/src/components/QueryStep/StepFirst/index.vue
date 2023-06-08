<template>
  <div class="search-area">
    <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="demo-form">
      <div class="select-resource">
        <div v-if="isSelected" class="select-row">
          <el-form-item label="已选资源" prop="selectResources" />
          <div class="resource-box">
            <ResourceItemSimple v-for="item in selectResources" :key="item.resourceId" :data="item" :show-close="true" class="select-item" @delete="handleDelete" />
            <ResourceItemCreate @click="openDialog(true)" />
          </div>
        </div>
        <div v-else class="dialog-con">
          <el-form-item label="选择查询资源" prop="resourceName">
            <el-select v-model="form.organId" style="width: 100%" placeholder="请选择" clearable @change="handleOrganChange" @clear="handleClear('organId')">
              <el-option
                v-for="item in organList"
                :key="item.globalId"
                :label="item.globalName"
                :value="item.globalId"
              />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="关键词" prop="pirParam">
          <el-input v-model="form.pirParam" placeholder="请输入关键词" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item>
          <p :style="{color: '#999', lineHeight: 1}">基于关键词的精准查询，多条件查询请使用英文,分隔。例: a,b,c</p>
        </el-form-item>
        <el-button v-if="hasPermission" style="margin-top: 12px;" type="primary" class="query-button" @click="next">查询<i class="el-icon-search el-icon--right" /></el-button>
      </div>
      <ProjectResourceDialog
        ref="dialogRef"
        class="dialog"
        title="选择资源"
        top="10px"
        width="800px"
        :show-preview-button="false"
        :show-delete-button="false"
        :center="false"
        :selected-data="selectResources"
        :organ-id="form.organId"
        :visible="dialogVisible"
        @close="handleDialogCancel"
        @submit="handleDialogSubmit"
      />
    </el-form>
  </div>
</template>

<script>
import { getAvailableOrganList } from '@/api/center'
import { pirSubmitTask } from '@/api/PIR'
import ResourceItemSimple from '@/components/ResourceItemSimple'
import ResourceItemCreate from '@/components/ResourceItemCreate'
import ProjectResourceDialog from '@/components/ProjectResourceDialog'

export default {
  components: {
    ResourceItemSimple,
    ResourceItemCreate,
    ProjectResourceDialog
  },
  props: {
    hasPermission: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      organList: [],
      noData: false,
      form: {
        organId: '',
        resourceName: '',
        pirParam: '',
        selectResources: []
      },
      rules: {
        resourceName: [
          { required: true, message: '请选择资源', trigger: 'blur' }
        ],
        pirParam: [
          { required: true, message: '请输入关键词', trigger: 'blur' },
          { max: 50, message: '长度在50个字符以内', trigger: 'blur' }
        ]
      },
      dialogVisible: false,
      listLoading: false,
      selectResources: [], // selected resource id list
      organId: '',
      isReset: false,
      cascaderValue: [],
      type: 'add',
      timer: null,
      taskId: -1
    }
  },
  computed: {
    isSelected() {
      return this.selectResources && this.selectResources.length > 0
    }
  },
  async created() {
    await this.getAvailableOrganList()
  },
  destroyed() {
    clearInterval(this.timer)
  },
  methods: {
    handleClear(name) {
      this.form[name] = ''
    },
    async getAvailableOrganList() {
      this.organLoading = true
      const { result } = await getAvailableOrganList()
      this.organList = result
      this.organLoading = false
    },
    handleOrganChange(value) {
      this.organName = this.organList.find(item => item.globalId === value)?.globalName
      this.openDialog()
    },
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
          this.listLoading = true
          if (this.form.pirParam.indexOf('，') !== -1 || this.form.pirParam.indexOf('；') !== -1 || this.form.pirParam.indexOf(';') !== -1) {
            this.$message.error('多条件查询请使用英文,分隔')
            return
          }
          pirSubmitTask({
            resourceId: this.selectResources[0].resourceId,
            pirParam: this.form.pirParam.replace(/(\s|;)+$/g, '')
          }).then(res => {
            if (res.code === 0) {
              this.listLoading = false
              this.taskId = res.result.taskId
              this.$emit('next', this.taskId)
              this.toTaskListPage()
            } else {
              this.$message({
                message: res.msg,
                type: 'error'
              })
              this.listLoading = false
            }
          }).catch(err => {
            console.log(err)
            this.listLoading = false
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    toTaskListPage() {
      this.$router.push({
        name: 'PrivateSearchList'
      })
    },
    openDialog(isAdd) {
      this.type = isAdd ? 'add' : ''
      this.dialogVisible = true
    },
    handleDialogCancel() {
      this.dialogVisible = false
      this.cascaderValue = this.type === 'add' ? this.cascaderValue : []
    },
    handleDialogSubmit(data) {
      console.log(data)
      if (data.length > 0) {
        this.selectResources = data.filter(item => item.organId === this.form.organId)
        this.dialogVisible = false
      } else {
        this.$message({
          message: '请选择资源',
          type: 'warning'
        })
      }
    },
    handleDelete(data) {
      const index = this.selectResources.findIndex(item => item.resourceId === data.id)
      this.selectResources.splice(index, 1)
      this.cascaderValue = []
    }
  }
}
</script>

<style lang="scss" scoped>
.search-area {
  margin: 20px auto;
  width: 595px;
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
  width: 485px;
  margin-right: 10px;
}
::v-deep .el-form-item__content{
  text-align: left;
}
</style>
