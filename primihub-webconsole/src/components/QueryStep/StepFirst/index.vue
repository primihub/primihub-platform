<template>
  <div class="search-area">
    <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="demo-form">
      <div class="select-resource">
        <div v-if="isSelected" class="select-row">
          <el-form-item label="已选资源" prop="selectResources" />
          <div class="resource-box">
            <ResourceItemSimple v-for="item in selectResources" :key="item.resourceId" :data="item" :show-close="true" class="select-item" @delete="handleDelete" />
          </div>
        </div>
        <div v-else class="dialog-con">
          <el-form-item label="选择查询资源" prop="resourceName">
            <el-input ref="resourceInputRef" v-model="form.resourceName" placeholder="选择查询资源" @click="openDialog" @keyup.enter.native="openDialog">
              <el-button slot="append" icon="el-icon-search" type="primary" @click="openDialog" />
            </el-input>
            <ResourceDialog ref="dialogRef" :dialog-visible="dialogVisible" :search-name="form.resourceName" @close="handleDialogCancel" @submit="handleDialogSubmit" />
          </el-form-item>
        </div>
        <el-form-item label="检索ID" prop="pirParam">
          <el-input v-model="form.pirParam" placeholder="请输入检索id" />
        </el-form-item>
        <el-button v-if="hasPermission" style="margin-top: 12px;" type="primary" class="query-button" @click="next">查询<i class="el-icon-search el-icon--right" /></el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import { pirSubmitTask } from '@/api/PIR'
import ResourceItemSimple from '@/components/ResourceItemSimple'
import ResourceDialog from '@/components/ResourceDialog'

export default {
  components: {
    ResourceItemSimple,
    ResourceDialog
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
      selectResources: [] // 选中资源
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
        this.$refs.resourceInputRef.focus()
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
      // if (!this.form.resourceName) {
      //   this.$message({
      //     message: '请输入查询资源名称',
      //     type: 'warning'
      //   })
      //   return
      // }
      this.dialogVisible = true
    },
    handleDialogCancel() {
      this.form.resourceName = ''
      this.dialogVisible = false
    },
    handleDialogSubmit(data) {
      this.selectResources = data.list
      this.dialogVisible = false
      this.form.resourceName = ''
      console.log(this.selectResources)
    },
    handleDelete(data) {
      const index = this.selectResources.findIndex(item => item.resourceId === data.id)
      this.selectResources.splice(index, 1)
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
.dialog-footer{
  width: 100%;
  display: inline-block;
  text-align: center;
}
</style>
