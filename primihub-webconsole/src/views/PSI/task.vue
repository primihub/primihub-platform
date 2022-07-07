<template>
  <div class="container">
    <div
      v-loading="taskLoading"
      class="steps"
      element-loading-text="任务创建中"
    >
      <div class="step">
        <div class="step-icon">
          <div class="step-circle">1</div>
          <div class="step-description">step1: 数据引入 <i class="el-icon-setting" /></div>
        </div>
        <div class="inner-con">
          <el-form ref="form" :model="formData" :rules="rules" label-width="70px">
            <div class="header">
              <div class="organ-container">
                <div class="organ"><span>使用方: </span><el-select v-model="formData.ownOrganName" disabled :placeholder="formData.ownOrganName" /></div>
                <div class="organ"><span>加持方: </span><OrganCascader placeholder="请选择求交机构" :show-all-levels="false" @change="handleOrganSelect" /></div>
              </div>
              <div class="line">
                <div class="line-icon">交</div>
              </div>
            </div>
            <div class="item-row">
              <div class="item">
                <el-form-item label="资源表" prop="ownResourceId">
                  <el-select v-model="formData.ownResourceId" placeholder="请选择" @change="handleOwnResourceChange">
                    <el-option
                      v-for="item in tableDataA"
                      :key="item.resourceId"
                      :label="item.resourceName"
                      :value="item.resourceId"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="关联键" prop="ownKeyword">
                  <el-select v-model="formData.ownKeyword" v-loading="selectLoading" placeholder="请选择" @change="handleOwnKeywordChange">
                    <el-option
                      v-for="(item,index) in ownOrganResourceField"
                      :key="index"
                      :label="item.name"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </div>
              <div class="center">
                <span>关系预览</span>
                <p><img :src="centerImg" alt="" width="48"></p>
              </div>
              <div class="item">
                <el-form-item label="资源表" prop="otherResourceId">
                  <el-select v-model="formData.otherResourceId" placeholder="请选择" @change="handleTargetResourceChange">
                    <el-option
                      v-for="item in tableDataB"
                      :key="item.resourceId"
                      :label="item.resourceName"
                      :value="item.resourceId"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="关联键" prop="otherKeyword">
                  <el-select v-model="formData.otherKeyword" placeholder="请选择" @change="handleOtherKeywordChange">
                    <el-option
                      v-for="(item,index) in otherOrganResourceField"
                      :key="index"
                      :label="item.name"
                      :value="index"
                    />
                  </el-select>
                </el-form-item>
              </div>
            </div>
          </el-form>
        </div>
      </div>
      <div class="step">
        <div class="step-icon">
          <div class="step-circle">2</div>
          <div class="step-description">step2: 高级配置 <i class="el-icon-setting" /></div>
        </div>
        <div class="inner-con">
          <el-form ref="form" :model="formData" :rules="rules" label-width="140px">
            <el-form-item label="求交结果名称" prop="resultName">
              <el-input v-model="resultName" placeholder="请输入内容" />
            </el-form-item>
            <el-row>
              <el-col :span="8">
                <el-form-item label="输出内容" prop="outputContent">
                  <el-select v-model="formData.outputContent" placeholder="请选择">
                    <el-option label="交集" :value="0" />
                    <el-option label="差集" :value="1" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="输出格式" prop="outputFormat">
                  <el-select v-model="formData.outputFormat" placeholder="请选择">
                    <el-option label="资源文件(csv)" :value="0" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="输出资源路径" prop="outputFilePathType">
                  <el-select v-model="formData.outputFilePathType" placeholder="请选择">
                    <el-option label="自动生成" :value="0" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="关联键有重复值时" prop="outputNoRepeat">
              <el-radio-group v-model="formData.outputNoRepeat">
                <el-radio :label="0">输出内容不去重</el-radio>
                <!-- <el-radio :label="1">输出内容去重</el-radio> -->
              </el-radio-group>
            </el-form-item>
            <!-- <el-form-item label="结果获取方" prop="resultOrgan">
              <el-checkbox-group v-model="formData.resultOrgan">
                <el-checkbox v-for="(item,index) in resultOrgan" :key="index" :label="item.organId">{{ item.organName }}</el-checkbox>
              </el-checkbox-group>
            </el-form-item> -->
            <el-form-item label="备注（可选）" prop="remarks">
              <el-input
                v-model="formData.remarks"
                size="mini"
                type="textarea"
                resize="none"
                maxlength="200"
                minlength="3"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
      <div class="button-wrapper">
        <el-button ref="btnRef" icon="el-icon-check" type="primary" :disabled="isRun" @click="handleSubmit">提交任务</el-button>
      </div>
    </div>
    <el-dialog
      :visible.sync="dialogVisible"
      :append-to-body="true"
      top="5vh"
      width="50%"
    >
      <PSI-task-detail :data="taskData" />
    </el-dialog>
  </div>
</template>

<script>
import { getPsiResourceAllocationList, getPsiTaskList, saveDataPsi, getPsiTaskDetails } from '@/api/PSI'
import OrganCascader from '@/components/OrganCascader'
import PSITaskDetail from '@/components/PSITaskDetail'

const intersection = require('@/assets/intersection.svg')
const diffsection = require('@/assets/diffsection.svg')

export default {
  name: 'PSITask',
  components: {
    OrganCascader,
    PSITaskDetail
  },
  data() {
    return {
      dialogVisible: false,
      selectLoading: false,
      ownOrganResourceField: [],
      otherOrganResourceField: [],
      taskLoading: false,
      pageSize: 10,
      totalPage: 0,
      total: 0,
      pageNo: 1,
      isRun: false, // task running state
      dataPsiTask: [], // task running result
      allDataPsiTask: [], // all task running result
      isReset: false,
      resultLoading: false,
      taskId: 0,
      tableDataA: [],
      tableDataB: [],
      taskData: {}, // task detail
      formData: {
        ownOrganId: '',
        ownResourceId: '', // 本机构资源Id
        ownKeyword: '', // 本机构关联键
        otherOrganId: 0,
        otherResourceId: '', // 其他机构资源Id
        otherKeyword: '', // 其他机构关联键
        outputFormat: 0, // 输出方式
        outputFilePathType: 0, // 输出路径
        outputContent: 0,
        resultOrgan: [],
        resultOrganIds: '',
        outputNoRepeat: 0, // 输出内容是否去重
        resultName: '',
        remarks: null,
        serverAddress: ''
      },
      ownResourceName: '',
      otherResourceName: '',
      rules: {
        ownResourceId: [
          { required: true, message: '请选择资源' }
        ],
        ownKeyword: [
          { required: true, message: '请选择关联键' }
        ],
        otherResourceId: [
          { required: true, message: '请选择资源' }
        ],
        otherKeyword: [
          { required: true, message: '请选择关联键' }
        ],
        outputFilePathType: [
          { required: true, message: '请选择输出资源路径' }
        ],
        outputNoRepeat: [
          { required: true, message: '请选择是否去重' }
        ],
        resultName: [
          { required: true, message: '请输入结果名称' },
          { max: 50, message: '长度在50个字符以内', trigger: 'blur' }
        ],
        outputContent: [
          { required: true, message: '请选择输出内容' }
        ],
        outputFormat: [
          { required: true }
        ],
        resultOrgan: [
          { required: true, message: '请选择结果获取方' }
        ]
      }
    }
  },
  computed: {
    // 结果获取方
    resultOrgan() {
      const { ownOrganId, ownOrganName, otherOrganId, otherOrganName } = this.formData
      const options = [
        {
          organId: ownOrganId,
          organName: ownOrganName
        }
      ]
      if (otherOrganId) {
        options.push({
          organId: otherOrganId,
          organName: otherOrganName
        })
      }
      return options
    },
    centerImg() {
      return this.formData.outputContent === 0 ? intersection : this.formData.outputContent === 1 ? diffsection : intersection
    },
    resultName() {
      return `${this.ownResourceName}-${this.otherResourceName}`
    }
  },
  watch: {
    resultName(newVal) {
      if (newVal) {
        this.formData.resultName = this.resultName
      } else {
        this.formData.resultName = ''
      }
    }
  },
  created() {
    this.formData.ownOrganId = this.$store.getters.userOrganId
    this.formData.ownOrganName = this.$store.getters.userOrganName
    this.formData.resultOrgan.push(this.formData.ownOrganId)
    getPsiResourceAllocationList({
      pageSize: this.pageSize
    }).then(res => {
      this.tableDataA = res.result.data
    })
  },
  methods: {
    handlePagination(data) {
      this.pageNo = data.page
      this.getPsiTaskList()
    },
    getPsiTaskList() {
      this.taskLoading = true
      getPsiTaskList({
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }).then(res => {
        const { data, totalPage, total } = res.result
        this.allDataPsiTask = data
        this.totalPage = totalPage
        this.total = total
        this.taskLoading = false
      })
    },
    handleSubmit() {
      if (!this.formData.otherOrganId) {
        this.$message({
          message: '请选择求交机构',
          type: 'error'
        })
        return
      }
      // max size is 200
      this.formData.resultName = this.formData.resultName.length > 200 ? this.formData.resultName.substring(0, 200) : this.formData.resultName
      console.log('this.formData.resultName', this.formData.resultName)
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.formData.resultOrganIds = this.formData.resultOrgan.join(',')
          this.$notify({
            message: '任务开始',
            type: 'info',
            duration: '1000'
          })
          this.isRun = true
          const res = await saveDataPsi(this.formData)
          const { dataPsiTask } = res.result
          this.dataPsiTask = [dataPsiTask]
          console.log(this.dataPsiTask)
          this.taskId = this.dataPsiTask[0].taskId
          this.taskLoading = true
          this.timmer = window.setInterval(() => {
            setTimeout(this.getPsiTaskDetails(), 0)
          }, 1500)
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    getPsiTaskDetails() {
      getPsiTaskDetails({ taskId: this.taskId }).then(res => {
        const { taskState } = res.result
        this.taskData = res.result
        switch (taskState) {
          case 1:
            this.$notify({
              message: '创建成功',
              type: 'success',
              duration: 1000
            })
            clearInterval(this.timmer)
            this.dialogVisible = true
            this.taskLoading = false
            this.isRun = false
            break
          case 3:
            this.$notify({
              message: '创建失败',
              type: 'warning',
              duration: 1000
            })
            clearInterval(this.timmer)
            this.taskLoading = false
            this.isRun = false
            break
          default:
            break
        }
        console.log(this.dataPsiTask)
        this.dataPsiTask[0].taskState = taskState
      })
    },
    handleSingleResultDelete() {
      this.dataPsiTask = []
      this.dialogVisible = false
    },
    handleOwnKeywordChange(index) {
      this.formData.ownKeyword = this.ownOrganResourceField[index].name
    },
    handleOtherKeywordChange(index) {
      this.formData.otherKeyword = this.otherOrganResourceField[index].name
    },
    handleOwnResourceChange(resourceId) {
      console.log(resourceId)
      this.ownOrganResourceField = []
      this.formData.ownResourceId = resourceId
      this.formData.ownKeyword = ''
      const currentResource = this.tableDataA.find(item => item.resourceId === resourceId)
      this.ownResourceName = currentResource.resourceName
      // this.formData.resultName = this.formData.resultName !== '' ? `${this.formData.resultName}-${resourceName}` : resourceName
      console.log('resultName', this.formData.resultName)
      currentResource.keywordList.forEach((item, index) => {
        this.ownOrganResourceField.push({
          value: index,
          name: item
        })
      })
    },
    handleTargetResourceChange(resourceId) {
      this.otherOrganResourceField = []
      this.formData.otherResourceId = resourceId
      this.formData.otherKeyword = ''
      const currentResource = this.tableDataB.find(item => item.resourceId === resourceId)
      this.otherResourceName = currentResource.resourceName
      // const resourceName = currentResource.resourceName
      // if (this.formData.resourceId !== resourceId) {
      //   resourceName = ''
      // }
      // this.formData.resultName = this.formData.resultName !== '' ? `${this.formData.resultName}-${resourceName}` : resourceName
      currentResource.keywordList.forEach((item, index) => {
        this.otherOrganResourceField.push({
          value: index,
          name: item
        })
      })
    },
    async getPsiResourceAllocationList(params) {
      const res = await getPsiResourceAllocationList(params)
      if (res.code === 0) {
        const { resourceField } = res.result
        return resourceField
      }
    },
    handleOrganSelect(data) {
      this.formData.otherOrganId = data.organId
      this.formData.otherOrganName = data.organName
      this.otherOrganResourceField = []
      this.formData.otherResourceId = ''
      this.formData.otherKeyword = ''
      this.formData.serverAddress = data.serverAddress
      getPsiResourceAllocationList({
        serverAddress: this.formData.serverAddress,
        organId: this.formData.otherOrganId,
        pageSize: this.pageSize
      }).then(res => {
        this.tableDataB = res.result.data
      })
    },
    handleClose() {
      this.dialogVisible = false
    }
  }
}
</script>
<style lang="scss" scoped>
@import "../../styles/variables.scss";
::v-deep .el-form-item{
  margin-bottom: 15px;
}
.container{
//  min-width: 1200px;
//  max-width: 1500px;
width: 1200px;
 overflow: hidden;
 &::before{
  border-left: 1px solid #cccccc;
  content: '';
  display: block;
  height: 100%;
 }
}
.inner-con{
  background: #fff;
  border-radius: $sectionBorderRadius;
  padding: 30px 50px;
  margin: 20px 50px;
}
.step-icon{
  display: flex;
  align-items: center;
  .step-circle{
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background-color: $mainColor;
    text-align: center;
    line-height: 30px;
    color: #fff;
  }
  .step-description{
    font-size: 16px;
    margin-left: 20px;
    color: #333;
  }
}
::v-deep .el-input.is-disabled .el-input__inner{
  background-color: transparent;
}
.header{
  overflow: hidden;
  position: relative;
  height: 40px;
  margin-bottom: 20px;
  .organ-container{
    display: flex;
    justify-content: space-around;
  }
  .line{
    border-top: 1px dotted #cccccc;
    width: 60%;
    margin:-20px auto 0;
    // position: absolute;
    // top: 50%;
    &-icon{
      width: 20px;
      height: 20px;
      border: 1px solid #666;
      position: absolute;
      left: 50%;
      transform: translate3d(-50%,0,0);
      top: 11px;
      background-color: #fff;
      font-size: 12px;
      line-height: 20px;
      text-align: center;
      color: #333;
    }
  }
  .organ{
    background-color: #fff;
    font-size: 14px;
    font-weight: bold;
    span{
      width: 60px;
      display: inline-block;
      font-size: 16px;
    }
  }
}
.item-row{
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 781px;
  margin: 0 auto;
  .item{
    // display: flex;
    // justify-content: flex-end;
    // float: right;
    // width: 255px;
    // margin-right: 50px;
    &:last-child{
      margin-right: 12px;
    }
  }
}
.center{
  width: 125px;
  // margin: 0 25px 0 60px;
  margin-left: 20px;
  text-align: center;
  font-size: 12px;
  color: #666;
}
.button-wrapper{
  padding-right: 20px;
  margin: 30px;
  text-align: right;
}
</style>
