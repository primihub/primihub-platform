<template>
  <div class="container">
    <div class="steps">
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
                  <ResourceSelect :value="formData.ownResourceId" :options="tableDataA" role="own" @focus="handleResourceFocus" @search="handleOwnResourceSearch" @change="handleOwnResourceChange" @clear="handleResourceClear" />
                </el-form-item>
                <el-form-item label="关联键" prop="ownKeyword">
                  <el-select v-model="formData.ownKeyword" v-loading="selectLoading" clearable placeholder="请选择" @change="handleOwnKeywordChange">
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
                  <ResourceSelect :value="formData.otherResourceId" :options="tableDataB" role="other" @focus="handleResourceFocus" @search="handleOtherResourceSearch" @change="handleTargetResourceChange" @clear="handleResourceClear" />
                </el-form-item>
                <el-form-item label="关联键" prop="otherKeyword">
                  <el-select v-model="formData.otherKeyword" placeholder="请选择" clearable @change="handleOtherKeywordChange">
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
            <el-row>
              <el-col :span="9">
                <el-form-item label="实现协议" prop="psiTag">
                  <el-select v-model="formData.psiTag" placeholder="请选择实现协议">
                    <el-option v-for="item in psiTagOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="关联键有重复值时" prop="outputNoRepeat">
                  <el-radio-group v-model="formData.outputNoRepeat">
                    <el-radio :label="0">输出内容不去重</el-radio>
                    <!-- <el-radio :label="1">输出内容去重</el-radio> -->
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

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
  </div>
</template>

<script>
import { getPsiResourceAllocationList, saveDataPsi } from '@/api/PSI'
import OrganCascader from '@/components/OrganCascader'
import ResourceSelect from '@/components/ResourceSelect'

const intersection = require('@/assets/intersection.svg')
const diffsection = require('@/assets/diffsection.svg')

export default {
  name: 'PSITask',
  components: {
    OrganCascader,
    ResourceSelect
  },
  data() {
    return {
      resourceName: '',
      selectLoading: false,
      ownOrganResourceField: [],
      otherOrganResourceField: [],
      pageSize: 100,
      total: 0,
      pageNo: 1,
      isRun: false, // task running state
      isReset: false,
      taskId: 0,
      tableDataA: [],
      tableDataB: [],
      formData: {
        ownOrganId: 0,
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
        serverAddress: '',
        psiTag: 0
      },
      ownResourceName: '',
      otherResourceName: '',
      rules: {
        ownResourceId: [
          { required: true, message: '请选择资源' }
        ],
        ownKeyword: [
          { required: true, message: '请选择关联键', trigger: 'change' }
        ],
        otherResourceId: [
          { required: true, message: '请选择资源', trigger: 'change' }
        ],
        otherKeyword: [
          { required: true, message: '请选择关联键', trigger: 'change' }
        ],
        outputFilePathType: [
          { required: true, message: '请选择输出资源路径', trigger: 'blur' }
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
        ],
        psiTag: [
          { required: true, message: '请选择实现协议' }
        ]
      },
      timer: null,
      psiTagOptions: [
        {
          value: 0,
          label: 'ECDH'
        },
        {
          value: 1,
          label: 'KKRT'
        }
      ]
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
  async created() {
    this.formData.ownOrganId = this.$store.getters.userOrganId
    this.formData.ownOrganName = this.$store.getters.userOrganName
    this.formData.resultOrgan.push(this.formData.ownOrganId)
  },
  methods: {
    async getPsiResourceAllocationList(params) {
      const { resourceName, organId } = params
      const res = await getPsiResourceAllocationList({
        resourceName,
        organId,
        pageSize: this.pageSize,
        serverAddress: this.formData.serverAddress
      })
      if (res.code === 0) {
        return res.result.data
      }
    },
    handleSubmit() {
      const enable = this.checkParams()
      if (!enable) return
      // max size is 200
      this.formData.resultName = this.formData.resultName.length > 200 ? this.formData.resultName.substring(0, 200) : this.formData.resultName
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.formData.resultOrganIds = this.formData.resultOrgan.join(',')
          this.isRun = true
          const res = await saveDataPsi(this.formData)
          if (res.code === 0) {
            this.$message({
              message: '创建完成',
              type: 'success'
            })
            this.toResultPage()
          } else {
            this.isRun = false
          }
        } else {
          console.log('error submit!!')
          this.isRun = false
          return false
        }
      })
    },
    toResultPage() {
      this.$router.push({
        name: 'PSIResult'
      })
    },
    checkParams() {
      const { otherOrganId, otherKeyword, ownResourceId, otherResourceId, ownKeyword } = this.formData
      let message = ''
      let enable = true
      if (!otherOrganId) {
        message = '请选择求交机构'
        enable = false
      } else if (ownResourceId === '') {
        message = '请选择使用方资源'
        enable = false
      } else if (otherResourceId === '') {
        message = '请选择加持方资源'
        enable = false
      } else if (otherKeyword === '') {
        message = '请选择加持方关联键'
        enable = false
      } else if (ownKeyword === '') {
        message = '请选择使用方关联键'
        enable = false
      }
      if (!enable) {
        this.$message({
          message,
          type: 'error'
        })
      }

      return enable
    },
    async handleResourceFocus(role) {
      this.$refs.form.resetFields()
      if (role === 'own') {
        this.tableDataA = await this.getPsiResourceAllocationList({
          organId: this.formData.ownOrganId
        })
      } else {
        if (this.formData.serverAddress === '') {
          this.$message({
            message: '请选择加持方',
            type: 'warning'
          })
          this.tableDataB = []
          return
        }
        this.tableDataB = []
        this.tableDataB = await this.getPsiResourceAllocationList({
          organId: this.formData.otherOrganId,
          resourceName: this.resourceName
        })
      }
    },
    handleOwnKeywordChange(index) {
      this.formData.ownKeyword = this.ownOrganResourceField[index]?.name
    },
    handleOtherKeywordChange(index) {
      this.formData.otherKeyword = this.otherOrganResourceField[index]?.name
    },
    async handleOwnResourceSearch(resourceName) {
      this.resourceName = resourceName
      this.tableDataA = await this.getPsiResourceAllocationList({
        organId: this.formData.organId,
        resourceName: this.resourceName
      })
    },
    async handleOtherResourceSearch(resourceName) {
      if (resourceName !== '') {
        this.resourceName = resourceName
        this.tableDataB = await this.getPsiResourceAllocationList({
          organId: this.formData.otherOrganId,
          resourceName: this.resourceName
        })
      }
    },
    handleResourceClear(role) {
      this.resourceName = ''
      if (role === 'own') {
        this.tableDataA = []
      } else if (role === 'other') {
        this.tableDataB = []
      }
    },
    handleOwnResourceChange(resourceId) {
      this.formData.ownResourceId = resourceId
      this.ownOrganResourceField = []
      this.formData.ownKeyword = ''
      const currentResource = this.tableDataA.find(item => item.resourceId === resourceId)
      this.ownResourceName = currentResource ? currentResource.resourceName : ''
      currentResource?.keywordList.forEach((item, index) => {
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
      this.otherResourceName = currentResource ? currentResource.resourceName : ''
      currentResource?.keywordList.forEach((item, index) => {
        this.otherOrganResourceField.push({
          value: index,
          name: item
        })
      })
    },
    async handleOrganSelect(data) {
      this.resourceName = ''
      this.formData.otherOrganId = data.organId
      this.formData.otherOrganName = data.organName
      this.otherOrganResourceField = []
      this.formData.otherResourceId = ''
      this.formData.otherKeyword = ''
      this.formData.serverAddress = data.serverAddress
      this.tableDataB = []
      this.tableDataB = await this.getPsiResourceAllocationList({
        organId: this.formData.otherOrganId
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
