<template>
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
              <div class="organ"><span>发起方: </span><el-select v-model="formData.ownOrganName" disabled :placeholder="formData.ownOrganName" /></div>
              <div class="organ"><span>协作方: </span>
                <el-select v-model="formData.otherOrganName" disabled :placeholder="formData.otherOrganName" />
              </div>
            </div>
            <div class="line">
              <div class="line-icon">交</div>
            </div>
          </div>
          <div class="item-row">
            <div class="item">
              <el-form-item label="资源表" prop="ownResourceId">
                <ResourceSelect disabled :value="formData.ownResourceName" no-data-text="暂无数据" :options="tableDataA" role="own" />
              </el-form-item>
              <el-form-item label="关联键" prop="ownKeyword">
                <el-select v-model="formData.ownKeyword" v-loading="selectLoading" disabled no-data-text="暂无数据" />
              </el-form-item>
            </div>
            <div class="center">
              <span>关系预览</span>
              <p><img :src="centerImg" alt="" width="48"></p>
            </div>
            <div class="item">
              <el-form-item label="资源表" prop="otherResourceId">
                <ResourceSelect disabled :value="formData.otherResourceName" :options="tableDataB" role="other" no-data-text="暂无数据" />
              </el-form-item>
              <el-form-item label="关联键" prop="otherKeyword">
                <el-select v-model="formData.otherKeyword" disabled no-data-text="暂无数据" placeholder="请选择" />
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
            <el-input v-model="formData.resultName" disabled placeholder="请输入内容" />
          </el-form-item>
          <el-row>
            <el-col :span="8">
              <el-form-item label="输出内容" prop="outputContent">
                <el-select v-model="formData.outputContent" disabled placeholder="请选择">
                  <el-option label="交集" :value="0" />
                  <el-option label="差集" :value="1" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="输出格式" prop="outputFormat">
                <el-select v-model="formData.outputFormat" disabled placeholder="请选择">
                  <el-option label="资源文件(csv)" :value="0" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="输出资源路径" prop="outputFilePathType">
                <el-select v-model="formData.outputFilePathType" disabled placeholder="请选择">
                  <el-option label="自动生成" :value="0" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="9">
              <el-form-item label="实现协议" prop="psiTag">
                <el-select v-model="formData.psiTag" disabled placeholder="请选择实现协议">
                  <el-option v-for="item in psiTagOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="关联键有重复值时" prop="outputNoRepeat">
                <el-radio-group v-model="formData.outputNoRepeat">
                  <el-radio disabled :label="0">输出内容不去重</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="备注（可选）" prop="remarks">
            <el-input
              v-model="formData.remarks"
              disabled
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
    <el-dialog
      :visible.sync="dialogVisible"
      :append-to-body="true"
      top="5vh"
      width="700px"
    >
      <div v-loading="loading" element-loading-text="查询中">
        <PSI-task-detail :show-download="false" :data="taskData" />

        <h3>运算结果</h3>
        <div v-if="taskState === 1" style="background-color: #fafafa;padding: 10px 20px 10px 20px;">
          <table>
            <tbody>
              <tr>
                <td>intersection_row</td>
                <td>x40</td>
                <td>x20</td>
                <td>x21</td>
                <td>x26</td>
                <td>x5</td>
                <td>x43</td>
                <td>x46</td>
                <td>x17</td>
                <td>x27</td>
                <td>x11</td>
                <td>x23</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import { getPsiResourceAllocationList, saveDataPsi, getPsiTaskDetails } from '@/api/PSI'
import { getTaskData } from '@/api/task'
import ResourceSelect from '@/components/ResourceSelect'
import PSITaskDetail from '@/components/PSITaskDetail'

const intersection = require('@/assets/intersection.svg')
const diffsection = require('@/assets/diffsection.svg')

export default {
  name: 'PSITask',
  components: {
    ResourceSelect,
    PSITaskDetail
  },
  data() {
    return {
      loading: false,
      taskState: '',
      taskTimer: null,
      taskData: {},
      dialogVisible: false,
      options: [
        {
          value: 'http://fusion.primihub.svc.cluster.local:8080/',
          label: 'http://fusion.primihub.svc.cluster.local:8080/',
          children: [{
            value: '2cad8338-2e8c-4768-904d-2b598a7e3298',
            label: '机构B'
          }]
        }
      ],
      resourceName: '',
      selectLoading: false,
      ownOrganResourceField: ['id'],
      otherOrganResourceField: ['id'],
      pageSize: 100,
      total: 0,
      pageNo: 1,
      isRun: false, // task running state
      isReset: false,
      taskId: 0,
      tableDataA: [{
        resourceId: '1134',
        resourceName: 'psi测试数据'
      }],
      tableDataB: [
        {
          resourceId: '2b598a7e3298-ceb049a7-e2ce-4826-9fa9-5acd07ec67e1',
          resourceName: 'psi测试数据'
        }
      ],
      formData: {
        resultOrgan: [],
        ownOrganId: 0,
        ownResourceId: '', // 本机构资源Id
        ownResourceName: '',
        ownKeyword: '', // 本机构关联键
        otherOrganId: 0,
        otherResourceId: '', // 其他机构资源Id
        otherResourceName: '',
        otherKeyword: '', // 其他机构关联键
        outputFormat: 0, // 输出方式
        outputFilePathType: 0, // 输出路径
        outputContent: 0,
        outputNoRepeat: 0, // 输出内容是否去重
        resultName: 'psi测试数据结果',
        remarks: null,
        psiTag: 0
      },
      ownResourceName: 'psi测试数据',
      otherResourceName: 'psi测试数据',
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
          { max: 50, message: '长度在50个字符以内' }
        ],
        outputContent: [
          { required: true, message: '请选择输出内容' }
        ],
        outputFormat: [
          { required: true }
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
    centerImg() {
      return this.formData.outputContent === 0 ? intersection : this.formData.outputContent === 1 ? diffsection : intersection
    },
    ...mapState('application', ['origin'])
  },
  destroyed() {
    clearInterval(this.taskTimer)
  },
  async created() {
    this.getLocationOrigin()
    this.setDefault()
  },
  methods: {
    async setDefault() {
      const data = {
        'node1': {
          ownOrganId: this.$store.getters.userOrganId,
          ownOrganName: this.$store.getters.userOrganName,
          ownResourceId: '141',
          ownResourceName: 'PIR-PSI测试数据',
          ownKeyword: '姓名',
          otherOrganId: '3abfcb2a-8335-4bcc-b6f9-704a92e392fd',
          otherOrganName: '机构B',
          otherResourceId: '704a92e392fd-89fc0bd7-a4af-419d-b303-55604956628e',
          otherResourceName: 'PIR-PSI测试数据',
          otherKeyword: '姓名'
        },
        'node2': {
          ownOrganId: this.$store.getters.userOrganId,
          ownOrganName: this.$store.getters.userOrganName,
          ownResourceId: '132',
          ownResourceName: 'PIR-PSI测试数据',
          ownKeyword: '姓名',
          otherOrganId: '7aeeb3aa-75cc-4e40-8692-ea5fd5f5f9f0',
          otherOrganName: '机构A',
          otherResourceId: 'ea5fd5f5f9f0-96594465-4bff-4763-b3d4-226922c9a59a',
          otherResourceName: 'PIR-PSI测试数据',
          otherKeyword: '姓名'
        },
        'node3': {
          ownOrganId: this.$store.getters.userOrganId,
          ownOrganName: this.$store.getters.userOrganName,
          ownResourceId: '35',
          ownResourceName: 'PIR-PSI测试数据',
          ownKeyword: '姓名',
          otherOrganId: '7aeeb3aa-75cc-4e40-8692-ea5fd5f5f9f0',
          otherOrganName: '机构B',
          otherResourceId: '704a92e392fd-89fc0bd7-a4af-419d-b303-55604956628e',
          otherResourceName: 'PIR-PSI测试数据',
          otherKeyword: '姓名'
        },
        'other': {
          ownOrganId: this.$store.getters.userOrganId,
          ownOrganName: this.$store.getters.userOrganName,
          ownResourceId: '141',
          ownResourceName: 'PIR-PSI测试数据',
          ownKeyword: '姓名',
          otherOrganId: '3abfcb2a-8335-4bcc-b6f9-704a92e392fd',
          otherOrganName: '机构B',
          otherResourceId: '704a92e392fd-89fc0bd7-a4af-419d-b303-55604956628e',
          otherResourceName: 'PIR-PSI测试数据',
          otherKeyword: '姓名'
        },
        'test1': {
          ownOrganId: this.$store.getters.userOrganId,
          ownOrganName: this.$store.getters.userOrganName,
          ownResourceId: '527',
          ownResourceName: 'PIR-PSI测试数据',
          ownKeyword: '姓名',
          otherOrganId: '2cad8338-2e8c-4768-904d-2b598a7e3298',
          otherOrganName: '机构B',
          otherResourceId: '2b598a7e3298-d21b8fff-6c1d-4de1-9597-88c0c22d066a',
          otherResourceName: 'PIR-PSI测试数据',
          otherKeyword: '姓名'
        }
      }
      this.init(data[this.origin])
    },
    async init(data) {
      this.formData = Object.assign(this.formData, data)
      this.resultName = `${this.formData.ownResourceName}-${this.formData.otherResourceName}`
      this.formData.resultName = this.resultName
      this.formData.resultOrgan.push(this.formData.ownOrganId)
      if (this.origin !== 'other') {
        this.setResourceOptions(data)
      } else {
        this.tableDataA = [{
          resourceId: '67',
          resourceName: 'psi测试数据'
        }]
        this.tableDataB = [{
          resourceId: '4b38606341d8-bb78987c-bf07-422d-93cf-057d7f69a51e',
          resourceName: 'psi测试数据'
        }]
      }
    },
    async setResourceOptions(data) {
      this.tableDataA = await this.getPsiResourceAllocationList({
        resourceName: data.ownResourceName,
        organId: data.ownOrganId
      })

      this.tableDataB = await this.getPsiResourceAllocationList({
        resourceName: data.resourceName,
        organId: data.otherOrganId
      })
    },
    openDialog() {
      if (this.origin === 'other') {
        this.dialogVisible = true
        this.loading = true
        const { otherKeyword, otherOrganId, otherOrganName, otherResourceId, ownKeyword, ownOrganId, ownOrganName, ownResourceId, resultName, ownResourceName, otherResourceName } = this.formData
        this.taskData = {
          taskIdName: new Date().getTime(),
          otherKeyword,
          otherOrganId,
          otherOrganName,
          otherResourceId,
          otherResourceName,
          outputContent: 0,
          outputFilePathType: 0,
          outputFormat: '0',
          outputNoRepeat: 0,
          ownKeyword,
          ownOrganId,
          ownOrganName,
          ownResourceId,
          ownResourceName,
          remarks: '',
          resultName,
          resultOrganName: '机构A',
          tag: 0,
          taskState: 2
        }
        setTimeout(() => {
          this.$message.success('运行成功')
          this.taskState = 1
          this.isRun = false
          this.loading = false
        }, 1500)
      } else {
        getPsiTaskDetails({ taskId: this.taskId }).then(res => {
          if (res.code === 0) {
            this.dialogVisible = true
            this.taskData = res.result

            setTimeout(() => {
              this.$message.success('运行成功')
              this.taskState = 1
              this.isRun = false
              this.loading = false
            }, 1500)
          }
        })
      }
    },
    async getPsiResourceAllocationList(params) {
      const { resourceName, organId } = params
      const res = await getPsiResourceAllocationList({
        resourceName,
        organId,
        pageSize: this.pageSize
      })
      if (res.code === 0) {
        return res.result.data
      }
    },
    handleSubmit() {
      // max size is 200
      this.formData.resultName = this.resultName.length > 200 ? this.resultName.substring(0, 200) : this.resultName
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.formData.resultOrganIds = this.formData.resultOrgan.join(',')
          this.isRun = true
          if (this.origin === 'other') {
            this.loading = true
            this.openDialog()
          } else {
            const res = await saveDataPsi(this.formData)
            if (res.code === 0) {
              this.taskId = res.result.dataPsi.id
              this.loading = true
              this.openDialog()
            } else {
              this.isRun = false
            }
          }
        } else {
          console.log('error submit!!')
          this.isRun = false
          return false
        }
      })
    },
    getTaskData(taskId) {
      getTaskData({ taskId: this.taskId }).then(res => {
        if (res.code === 0) {
          this.taskState = res.result.taskState
          if (this.taskState === 3) {
            clearInterval(this.taskTimer)
            this.fail = true
            this.$message.error(res.result.taskErrorMsg)
          } else if (this.taskState !== 2) {
            this.taskData.taskState = this.taskState
            clearInterval(this.taskTimer)
          }
        }
        this.loading = false
        this.isRun = false
      }).catch(err => {
        console.log(err)
        this.isRun = false
        this.loading = false
      })
    },
    handleClose() {
      this.dialogVisible = false
    },
    ...mapActions('application', ['getLocationOrigin'])
  }
}
</script>
<style lang="scss" scoped>
@import "@/styles/variables.scss";
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
  padding: 30px 0px;
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

table,th,td {
  border: 1px solid #ebeef5;
}
table {
  width: 100%;
  margin: 0 auto;
  display: block;
  overflow-x: auto;
  border-spacing: 0;
}

tbody {
  white-space: nowrap;
}

th,
td {
  padding: 5px 10px;
  border-top-width: 0;
  border-left-width: 0;
}

th {
  position: sticky;
  top: 0;
  background: #fff;
  vertical-align: bottom;
}

th:last-child,
td:last-child {
  border-right-width: 0;
}

tr:last-child td {
  border-bottom-width: 0;
}
</style>
