<template>
  <div class="container">
    <div class="steps">
      <div class="search-area">
        <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="demo-form">
          <div class="select-resource">
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="form.taskName" maxlength="32" show-word-limit placeholder="请输入任务名称,限32字" />
            </el-form-item>
            <el-form-item label="预处理"  prop="examTaskId">
              <el-select
                style="width: 100%"
                v-model="form.examTaskId"
                filterable
                placeholder="请选择预处理任务"
                :loading="loading">
                <el-option
                  v-for="item in preprocessingList"
                  :key="item.taskId"
                  :label="item.taskName"
                  :value="item.taskId">
                </el-option>
              </el-select>
            </el-form-item>
            <el-button style="margin-top: 12px;" type="primary" class="query-button" @click="next">提交任务</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { submitPsiTaskRequest } from '@/api/PSI'
import { examTaskListRequest } from '@/api/preprocessing'

export default {
  data() {
    return {
      preprocessingList: [],
      form: {
        taskName: '',
        examTaskId: ''
      },
      rules: {
        taskName: [
          { required: true, message: '请输入任务名称', trigger: 'blur' }
        ],
        examTaskId: [
          { required: true, message: '请选择预处理任务', trigger: 'change' }
        ]
      }
    }
  },
  async created() {
    await this.getPreprocessingList()
  },
  methods: {
    // get examTask list
    async getPreprocessingList(query) {
      const { code, result } = await examTaskListRequest(query)
      if (code === 0){
        this.preprocessingList = result
      }
    },

    next() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.listLoading = true
          submitPsiTaskRequest({...this.form}).then(res => {
            if (res.code === 0) {
              const { taskId } = res.result.dataPsiTask
              this.listLoading = false
              this.$emit('next', taskId)
              this.toTaskDetailPage(taskId)
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

    toTaskDetailPage(id) {
      this.$router.push({
        name: 'PSIDetail',
        params: { id }
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
