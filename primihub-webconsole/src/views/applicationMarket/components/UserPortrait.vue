<template>
  <el-row v-loading="loading" type="flex" justify="center" element-loading-text="加载中">
    <el-col :span="12">
      <div class="user-container">
        <div v-for="(item,key,index) in data" :key="item.key" class="item" :class="`item${index}`">
          <p class="item-title">{{ filterTitle(key) }}</p>
          <ul v-if="item" style="display:inline-block;">
            <li v-for="v in item" :key="v.key">
              {{ v.label }}: {{ v.value }}<span v-if="v.key !== 'age'">%</span>
            </li>
          </ul>
          <p v-else class="detail">{{ item.value }}</p>
        </div>
      </div>
    </el-col>
    <el-col :span="12">
      <el-form ref="form" :rules="rules" class="form-container" :model="form" label-width="80px" size="small">
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" style="width:100%;" placeholder="请选择">
            <el-option v-for="(item,index) in filterData('gender')" :key="index" :label="item.label" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model.number="form.age" placeholder="请输入年龄" />
        </el-form-item>
        <el-form-item label="岗位" prop="jobPosition">
          <el-select v-model="form.jobPosition" style="width:100%;" placeholder="请选择">
            <el-option v-for="(item,index) in filterData('jobPosition')" :key="index" :label="item.label" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="所在行业" prop="industry">
          <el-select v-model="form.industry" style="width:100%;" placeholder="请选择">
            <el-option v-for="(item,index) in filterData('industry')" :key="index" :label="item.label" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="所在城市" prop="city">
          <el-select v-model="form.city" style="width:100%;" placeholder="请选择">
            <el-option v-for="(item,index) in filterData('city')" :key="index" :label="item.label" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="隐私计算熟悉度" prop="familiarity">
          <el-select v-model="form.familiarity" style="width:100%;" placeholder="请选择">
            <el-option v-for="(item,index) in filterData('familiarity')" :key="index" :label="item.label" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="来访目的" prop="visitPurposes">
          <el-select v-model="form.visitPurposes" style="width:100%;" placeholder="请选择">
            <el-option v-for="(item,index) in filterData('visitPurposes')" :key="index" :label="item.label" :value="item.key" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">匿名提交</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-col>
  </el-row>
</template>

<script>
import { getVisitUsers, submitVisitUsers } from '@/api/market'

export default {
  data() {
    const checkAge = (rule, value, callback) => {
      console.log(value < 18 || value > 100)
      if (!value) {
        callback(new Error('请输入年龄'))
      } else if (!Number.isInteger(value)) {
        callback(new Error('年龄必须为数字'))
      } else if (value < 18 || value > 100) {
        callback(new Error('年龄需在18岁-100岁之间'))
      } else {
        callback()
      }
    }
    return {
      loading: false,
      data: [],
      form: {
        gender: '',
        age: '',
        industry: '',
        jobPosition: '',
        familiarity: '',
        visitPurposes: ''
      },
      rules: {
        age: [
          { required: true, validator: checkAge, trigger: 'blur' }
        ],
        gender: [{ required: true, message: '请选择性别' }],
        industry: [{ required: true, message: '请选择所在行业' }],
        familiarity: [{ required: true, message: '请选择隐私计算熟悉度' }],
        city: [{ required: true, message: '请选择所在城市' }],
        jobPosition: [{ required: true, message: '请选择行业' }],
        visitPurposes: [{ required: true, message: '请选择来访目的' }]
      },
      submitParams: []
    }
  },
  created() {
    this.getVisitUsers()
  },
  methods: {
    getVisitUsers() {
      getVisitUsers().then(res => {
        if (res.code === 0) {
          this.data = res.result
        }
      })
    },
    onSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          for (const key in this.form) {
            this.submitParams.push({
              key,
              value: this.form[key]
            })
          }
          this.loading = true
          submitVisitUsers({ param: this.submitParams }).then(({ code }) => {
            if (code === 0) {
              setTimeout(() => {
                this.loading = false
                this.$message.success('你提交的信息已收录进"来访用户实时画像"')
                this.getVisitUsers()
                this.onReset()
              }, 1000)
            } else {
              this.loading = false
            }
          }).catch(() => {
            this.loading = false
          })
        }
      })
    },
    setValue() {
      this.loading = true
      const listData = JSON.parse(JSON.stringify(this.data))
      for (const key in this.form) {
        const val = this.form[key]
        const posIndex = listData.findIndex(v => v.key === key)
        const options = listData[posIndex].options
        if (posIndex) {
          if (options) {
            const index = options.findIndex(item => item.key === val)
            if (index !== -1) {
              const value = options[index].value
              options[index].value = (Math.floor((value + 0.01) * 100) / 100)
              if (index !== 0) {
                const firstVal = options[0].value
                options[0].value = (Math.floor((firstVal - 0.01) * 100) / 100)
              } else {
                const lastVal = options[options.length - 1].value
                options[options.length - 1].value = (Math.floor((lastVal - 0.01) * 100) / 100)
              }
            }
          } else {
            listData[posIndex].value = val
          }
        } else {
          listData[posIndex].value = val
        }
      }
      setTimeout(() => {
        this.loading = false
        this.$message.success('提交成功')
        this.data = listData
        console.log(this.data)
      }, 1000)
    },
    onReset() {
      for (const key in this.form) {
        this.form[key] = ''
      }
      this.submitParams = []
      this.$refs.form.resetFields()
    },
    filterTitle(key) {
      const label = key === 'gender' ? '性别' : key === 'age' ? '年龄' : key === 'jobPosition' ? '岗位分布' : key === 'industry' ? '行业分布' : key === 'familiarity' ? '隐私计算熟悉度' : key === 'city' ? '城市分布' : key === 'visitPurposes' ? '来访目的' : ''
      return label
    },
    filterData(name) {
      for (const key in this.data) {
        if (name === key) {
          return this.data[key]
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-form-item__label{
  line-height: 1.5;
}
ul{
  margin-block-start: 10px;
  margin-block-end: 10px;
  margin-inline-start: 0px;
  margin-inline-end: 0px;
  padding-inline-start: 0px;
  margin-left: 10px;
}
.form-container{
  padding: 60px;
}
.user-container{
  margin-top: 30px;
  width: 100%;
  height: 400px;
  position: relative;
  background: url('../../../assets/userImage.png') center center no-repeat;
  background-size: contain;
  display: flex;
  justify-content: center;
  li{
    list-style: none;
    font-size: 12px;
    line-height: 1.5;
  }
  .detail{
    font-size: 12px;
    text-align: center;
    margin-top: 10px;
  }
  .item-title{
    background-color: #15adf5;
    color: #fff;
    border-radius: 16px;
    padding: 3px 10px;
    text-align: center;
    font-size: 15px;
    margin: 0 auto;
  }
  .item{
    position: absolute;
    // width: 120px;
    &0{
      left: 0px;
      top: 50px;
    }
    &1{
      left: 178px;
      top: -20px;
    }
    &2{
      left: 326px;
      top: 0px;
    }
    &3{
      left: 20px;
      top: 210px;
    }
    &4{
      left: 345px;
      top: 150px;
    }
    &5{
      left: 195px;
      top: 327px;
    }
    &6{
      left: 305px;
      top: 288px;
    }
  }
}
</style>
