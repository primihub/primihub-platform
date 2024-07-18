<template>
  <div class="container">
    <div class="steps">
      <div class="search-area">
        <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="demo-form">
          <div class="select-resource">
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="form.taskName" maxlength="32" show-word-limit placeholder="请输入任务名称,限32字" />
            </el-form-item>
            <el-form-item label="隐私求交"  prop="psiRecordId">
              <el-select
                style="width: 100%"
                v-model="form.psiRecordId"
                filterable
                placeholder="请选择隐私求交"
                :loading="loading">
                <el-option
                  v-for="item in psiList"
                  :key="item.recordId"
                  :label="item.psiName"
                  :value="item.recordId">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="模型分"  prop="scoreModelType">
              <el-select
                style="width: 100%"
                v-model="form.scoreModelType"
                filterable
                placeholder="请选择模型分"
                >
                <el-option
                  v-for="item in scoreModelList"
                  :key="item.scoreModelType"
                  :label="item.scoreModelName"
                  :value="item.scoreModelType"
                  />
              </el-select>
            </el-form-item>
            <el-button v-if="hasSearchPermission" style="margin-top: 12px;" type="primary" class="query-button" @click="next">查询<i class="el-icon-search el-icon--right" /></el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { submitPirPhase1Request } from '@/api/PIR'
import { psiRecordListRequest } from '@/api/PSI'
import { getScoreModelListRequest } from '@/api/scoreModel'

export default {
  data() {
    return {
      loading: false,
      form: {
        taskName: '',
        psiRecordId: '',
        scoreModelType: ''
      },
      psiList: [],
      scoreModelList: [],
      rules: {
        taskName: [
          { required: true, message: '请输入任务名称', trigger: 'blur' }
        ],
        psiRecordId: [
          { required: true, message: '请选择预处理', trigger: 'change' }
        ],
        scoreModelType: [
          { required: true, message: '请选择模型分', trigger: 'change' }
        ],
      }
    }
  },
  computed: {
    hasSearchPermission() {
      return this.buttonPermissionList.includes('PrivateSearchButton')
    },
    ...mapGetters([
      'buttonPermissionList'
    ])
  },
  async created() {
    await this.getScoreModelList()
    await this.getPsiRecordList('')
  },
  methods: {
    // get psi list
    async getPsiRecordList(query) {
      const { code, result } = await psiRecordListRequest(query)
      if (code === 0){
        this.psiList = result
      }
    },

    // get ScoreMode list
    async getScoreModelList() {
      this.organLoading = true
      const { result } = await getScoreModelListRequest()
      this.scoreModelList = result
      this.organLoading = false
    },

    // submit
    next() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.listLoading = true
          submitPirPhase1Request({...this.form}).then(res => {
            if (res.code === 0) {
              this.listLoading = false
              this.toTaskListPage()
            } else {
              this.$message.error(res.msg)
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
        name: 'PrivateSearch'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
.steps{
  background-color: #fff;
  padding:50px;
}

.search-input{
  width: 300px;
}

.search-area {
  margin: 20px auto;
  width: 595px;
  text-align: center;
}

.query-button {
  width: 200px;
  margin: 0 auto;
}
</style>
