<template>
  <div class="task-container">
    <div class="task-body">
      <el-form ref="form" :model="formData" :rules="rules" label-width="80px">
        <el-row :gutter="20" :class="{'not-clickable': isRun}">
          <el-col :span="12">
            <div class="task-setting">
              <h3>求交数据配置</h3>
              <div class="select-organ">
                <OrganCascader placeholder="请选择求交机构" :show-all-levels="false" @change="handleOrganSelect" />
              </div>
              <el-row :gutter="20" :class="{'mt20':showOtherOrgan }">
                <el-col :span="12">
                  <div class="header"><i class="el-icon-office-building" /> {{ formData.ownOrganName }}</div>
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
                </el-col>
                <el-col :span="12">
                  <template v-if="showOtherOrgan">
                    <div class="header"><i class="el-icon-office-building" /> {{ formData.otherOrganName }}</div>
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
                  </template>

                </el-col>
              </el-row>
            </div>
            <div class="task-setting">
              <h3>高级设置</h3>
              <div class="wrapper">
                <el-form-item label="输出资源路径" prop="outputFilePathType">
                  <el-select v-model="formData.outputFilePathType" placeholder="请选择">
                    <el-option label="自动生成" :value="0" />
                  </el-select>
                </el-form-item>
                <el-form-item label="关联键有重复值时" prop="outputNoRepeat">
                  <el-radio-group v-model="formData.outputNoRepeat">
                    <el-radio :label="0">输出内容不去重</el-radio>
                    <el-radio :label="1">输出内容去重</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="是否对&quot;可统计&quot;的附加列做全表统计" prop="columnCompleteStatistics">
                  <el-radio-group v-model="formData.columnCompleteStatistics">
                    <el-radio :label="0">是</el-radio>
                    <el-radio :label="1">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="task-setting">
              <h3>求交配置方式</h3>
              <el-form-item label="求交结果名称" prop="resultName">
                <el-input v-model="formData.resultName" placeholder="请输入内容" />
              </el-form-item>
              <el-row>
                <el-col :span="10">
                  <el-form-item label="输出内容" prop="outputContent">
                    <el-select v-model="formData.outputContent" placeholder="请选择">
                      <el-option label="交集" :value="0" />
                      <el-option label="差集" :value="1" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="10">
                  <el-form-item label="输出格式" prop="outputFormat">
                    <el-select v-model="formData.outputFormat" placeholder="请选择">
                      <el-option label="资源文件(csv)" :value="0" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="结果获取方" prop="resultOrgan">
                <el-checkbox-group v-model="formData.resultOrgan">
                  <el-checkbox v-for="(item,index) in resultOrgan" :key="index" :label="item.organId">{{ item.organName }}</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="备注（可选）" prop="remarks">
                <el-input v-model="formData.remarks" size="mini" type="textarea" resize="none" />
              </el-form-item>
            </div>
          </el-col>
        </el-row>
        <div class="button-wrapper">
          <el-button ref="btnRef" type="primary" :disabled="isRun" @click="submit">{{ buttonText }}</el-button>
          <!-- <el-button :disabled="!isRun" @click="cancel">取消</el-button> -->
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { getPsiResourceAllocationList, saveDataPsi, getPsiTaskDetails, cancelTask, retryPsiTask } from '@/api/PSI'
import OrganCascader from '@/components/OrganCascader'
const ORGAN_KEY = 'priOrgan'

export default {
  name: 'PSITaskCreate',
  components: {
    OrganCascader
  },
  props: {
    isReset: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      selectLoading: false,
      resourceId: 0,
      ownOrganResourceField: [],
      otherOrganResourceField: [],
      otherOrganId: '',
      // otherOrganName: '',
      // showOtherOrgan: false,
      tableDataB: [],
      tableDataA: [],
      formData: {
        ownOrganId: '',
        ownResourceId: '', // 本机构资源Id
        ownKeyword: '', // 本机构关联键
        otherOrganId: 0,
        otherResourceId: '', // 其他机构资源Id
        otherKeyword: '', // 其他机构关联键
        outputFormat: 0, // 输出方式
        outputFilePathType: 0, // 输出路径
        outputContent: '',
        resultOrgan: [],
        resultOrganIds: '',
        columnCompleteStatistics: 0, // 全表统计
        outputNoRepeat: 0, // 输出内容是否去重
        resultName: '',
        remarks: null,
        serverAddress: ''
      },
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
        columnCompleteStatistics: [
          { required: true, message: '请选择是否对"可统计"的附加列做全表统计' }
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
      },
      dataPsiTask: {}, // 运行任务结果
      isRun: false,
      buttonText: '确定',
      timmer: null,
      pageSize: 100,
      taskId: -1,
      retry: false
    }
  },
  computed: {
    showOtherOrgan: {
      get() {
        return this.formData.otherOrganId || ''
      },
      set() {

      }
    },
    otherOrganName: {
      get() {
        return this.formData.otherOrganName || ''
      },
      set() {

      }

    },
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
    }
  },
  watch: {
    isReset(val) {
      if (val) {
        this.buttonText = '确定'
        this.taskId = -1
        this.retry = false
      }
    }
  },
  async created() {
    const localOrganInfo = JSON.parse(localStorage.getItem(ORGAN_KEY))
    const { organId, organName } = this.$route.query
    this.formData.otherOrganId = organId || ''
    this.formData.otherOrganName = decodeURIComponent(organName) || localOrganInfo.organName || ''
    await this.getUserInfo()
    getPsiResourceAllocationList({
      // organId: this.formData.ownOrganId,
      pageSize: this.pageSize
    }).then(res => {
      this.tableDataA = res.result.data
    })
    if (organId) {
      getPsiResourceAllocationList({
        organId: this.formData.otherOrganId,
        pageSize: this.pageSize
      }).then(res => {
        this.tableDataB = res.result.data
      })
    }
  },
  methods: {
    async getUserInfo() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo'))
      const { organIdList, organIdListDesc } = userInfo
      this.formData.ownOrganId = organIdList
      this.formData.ownOrganName = organIdListDesc
      this.formData.resultOrgan.push(this.formData.ownOrganId)
    },
    getPsiTaskDetails() {
      getPsiTaskDetails({ taskId: this.taskId }).then(res => {
        const { taskState, taskId } = res.result
        this.taskData = res.result
        this.taskId = taskId
        // clearInterval(this.timmer)
        this.buttonText = '确定'
        this.dataPsiTask.taskState = taskState

        switch (taskState) {
          case 1:
            this.$notify({
              message: '创建成功',
              type: 'success',
              duration: 1000
            })
            this.retry = false
            this.buttonText = '确定'
            this.$emit('change', Object.assign({}, this.dataPsiTask, {
              resultName: this.formData.resultName
            }))
            clearInterval(this.timmer)
            this.isRun = false
            break
          case 3:
            // this.retry = true
            // this.buttonText = '重试'
            this.$notify({
              message: '创建失败',
              type: 'warning',
              duration: 1000
            })
            this.$emit('change', Object.assign({}, this.dataPsiTask, {
              resultName: this.formData.resultName
            }))
            clearInterval(this.timmer)
            this.isRun = false
            break
          case 4:
            // this.retry = true
            // this.buttonText = '重试'
            break
          default:
            break
        }
        // this.$emit('change', Object.assign({}, this.dataPsiTask, {
        //   resultName: this.formData.resultName
        // }))
      })
    },
    async getPsiResourceAllocationList(params) {
      const res = await getPsiResourceAllocationList(params)
      if (res.code === 0) {
        const { resourceField } = res.result
        return resourceField
      }
    },
    async handleOwnResourceChange(resourceId) {
      this.ownOrganResourceField = []
      this.formData.ownResourceId = resourceId
      this.formData.ownKeyword = ''
      const currentResource = this.tableDataA.find(item => item.resourceId === resourceId)
      currentResource.keywordList.forEach((item, index) => {
        this.ownOrganResourceField.push({
          value: index,
          name: item
        })
      })
    },
    async handleTargetResourceChange(resourceId) {
      this.otherOrganResourceField = []
      this.formData.otherResourceId = resourceId
      this.formData.otherKeyword = ''
      const currentResource = this.tableDataB.find(item => item.resourceId === resourceId)
      currentResource.keywordList.forEach((item, index) => {
        this.otherOrganResourceField.push({
          value: index,
          name: item
        })
      })
    },
    submit() {
      if (!this.formData.otherOrganId) {
        this.$message({
          message: '请选择求交机构',
          type: 'error'
        })
        return
      }
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.formData.resultOrganIds = this.formData.resultOrgan.join(',')
          this.$notify({
            message: '任务开始',
            type: 'info',
            duration: '1000'
          })
          if (this.retry) { // 点击重试
            await retryPsiTask({ taskId: this.taskId })
            this.$emit('retry', this.dataPsiTask)
          } else {
            this.isRun = true
            const res = await saveDataPsi(this.formData)
            const { dataPsiTask } = res.result
            this.dataPsiTask = dataPsiTask
            this.taskId = dataPsiTask.taskId
            this.$emit('submit', Object.assign({}, this.dataPsiTask, {
              resultName: this.formData.resultName
            }))
          }
          this.timmer = window.setInterval(() => {
            setTimeout(this.getPsiTaskDetails(), 0)
          }, 1500)
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    async cancel() {
      clearInterval(this.timmer)
      this.buttonText = '重试'
      this.isRun = false
      await cancelTask({ taskId: this.taskId })
      this.$notify({
        message: '取消成功',
        type: 'success',
        duration: 1000
      })
      this.$emit('cancel')
    },
    handleOrganSelect(data) {
      this.showOtherOrgan = true
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
    handleOwnKeywordChange(index) {
      this.formData.ownKeyword = this.ownOrganResourceField[index].name
    },
    handleOtherKeywordChange(index) {
      this.formData.otherKeyword = this.otherOrganResourceField[index].name
    }
  }
}
</script>
<style lang="scss" scoped>
::v-deep .el-form-item__content{
  display: block;
  margin-left: 0!important;
  width: 100%;
  line-height: 20px;
}
::v-deep .el-form-item__label{
  float: none;
}
// ::v-deep .el-input__inner{
//   height: 30px;
//   line-height: 1;
// }
.el-form-item{
  margin-bottom: 10px;
}
h2{
  font-size: 20px;
}
h3{
  font-size: 16px;
  margin-block-start: 0;
  margin-block-end: 0;
}
.task-setting{
  background: #fff;
  margin-bottom: 20px;
  padding: 25px;
  border-radius: 5px;
  position: relative;
  .header{
    max-height: 50px;
    margin: 20px 0 20px;
    line-height: 1.5;
  }
}
.task-body{
  position: relative;
}
.button-wrapper{
  position: absolute;
  bottom: 120px;
  right: 20px;
}
.not-clickable{
  cursor: default;
  pointer-events: none;
}
.select-organ{
  position: absolute;
  right: 40px;
  top: 20px;
  z-index: 10;
}
.mt20{
  margin-top: 20px;
}
</style>
