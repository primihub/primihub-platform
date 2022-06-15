<template>
  <div class="app-container">
    <el-steps :active="step" align-center>
      <el-step title="选择模板" />
      <el-step title="编辑模型" />
    </el-steps>
    <div class="step-wrap">
      <div v-if="step===1" class="tmp-list">
        <div v-for="item in modelTmp" :key="item.id" class="tmp-item" :class="['tmp-item', item.id === formData.modelType ? 'current' : '']" @click="selectTmp(item.id)">
          <img src="/images/tmp.png">
          <div class="context">
            <span>{{ item.name }}</span>
          </div>
        </div>
      </div>
      <div v-if="step===2" class="model-editor">
        <div id="dag-box" ref="dagBox" class="content">
          <DagComponent v-if="hasMounted" :width="dagWidth" :height="dagHeight" />
        </div>
        <div class="aside">
          <div class="base-info-list">
            <div class="title">基本信息配置</div>
            <br>
            <el-form ref="form" :model="formData" :rules="rules" label-width="80px">
              <el-form-item label="模型名称" prop="modelName">
                <el-input v-model="formData.modelName" size="mini" />
              </el-form-item>
              <el-form-item label="模型描述" prop="modelDesc">
                <el-input v-model="formData.modelDesc" size="mini" type="textarea" />
              </el-form-item>
              <el-form-item label="选择资源" prop="resourceId">
                <el-select v-model="formData.resourceId" placeholder="请选择">
                  <el-option
                    v-for="item in resourceList"
                    :key="item.resourceId"
                    :label="item.resourceName"
                    :value="item.resourceId"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="Y值字段" prop="yValueColumn">
                <el-select v-model="formData.yValueColumn" placeholder="请选择">
                  <el-option
                    v-for="key in resourceKeyList"
                    :key="key"
                    :label="key"
                    :value="key"
                  />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
    <div v-if="step===1" class="handle-bar">
      <el-button v-if="$store.getters.buttonPermissionList.includes('ModelEdit')" type="primary" @click="nextStep">下一步</el-button>
      <el-button @click="goBack()">返回</el-button>
    </div>
    <div v-if="step===2" class="run-button-wrap">
      <el-button icon="el-icon-video-play" type="primary" round @click="createModel">运行</el-button>
      <el-button round @click="step=1">上一步</el-button>
    </div>
  </div>
</template>

<script>
import { getProjectDetail } from '@/api/project'
import { createModel } from '@/api/model'
import DagComponent from '@/views/dag'

export default {
  components: { DagComponent },
  data() {
    return {
      hasMounted: false,
      dagWidth: null,
      dagHeight: null,
      formData: {
        modelType: null,
        projectId: null,
        modelName: '',
        modelDesc: '',
        yValueColumn: '',
        resourceId: ''
      },
      rules: {
        modelName: [
          { required: true, message: '请输入模型名称', trigger: 'blur' },
          { max: 20, message: '长度在20个字符以内', trigger: 'blur' }
        ],
        modelDesc: [
          { required: true, message: '请输入模型描述', trigger: 'blur' },
          { max: 100, message: '长度在100个字符以内', trigger: 'blur' }
        ],
        resourceId: [
          { required: true, message: '请选择资源', trigger: 'change' }
        ],
        yValueColumn: [
          { required: true, message: '请选择Y值字段', trigger: 'change' }
        ]
      },
      resourceList: [],
      resourceKeyList: ['id', 'name', 'age', 'value'],
      modelTmp: [{
        id: 1,
        name: '联邦学习ID对齐'
      }, {
        id: 2,
        name: 'V-XGBoost'
      }, {
        id: 3,
        name: 'V-逻辑回归'
      }, {
        id: 4,
        name: '线性回归'
      }],
      step: 1
    }
  },
  created() {
    const query = this.$route.query
    const { projectId } = query
    if (projectId) {
      this.formData.projectId = projectId
      this.getProjectDetail(projectId)
    }
  },
  mounted() {
  },
  methods: {
    nextStep() {
      if (!this.formData.modelType) {
        this.$message({
          message: '请先选择模型模版',
          type: 'warning'
        })
        return
      }
      this.step = 2
      this.$nextTick(() => {
        const dagBox = this.$refs.dagBox || null
        if (dagBox) {
          this.dagWidth = dagBox.offsetWidth
          this.dagHeight = dagBox.offsetHeight
          this.hasMounted = true
        }
      })
    },
    getProjectDetail(projectId) {
      getProjectDetail({ projectId }).then(res => {
        const resourceList = res.result.resources
        this.resourceList = resourceList
      })
    },
    selectTmp(type) {
      console.log('selectTmp type', type)
      this.formData.modelType = type
    },
    createModel() {
      this.$refs.form.validate(valid => {
        if (valid) {
          createModel(this.formData)
            .then(({ result }) => {
              const { modelId: id } = result
              this.$router.push({
                name: 'ModelDetail',
                params: { id }
              })
              this.$message({ type: 'success', message: '创建模型成功' })
            })
        }
      })
    },
    goBack() {
      const id = this.$route.query.projectId
      this.$router.push({
        name: 'ProjectDetail',
        params: { id }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.app-container {
  position: relative;
  height: calc(100vh - 50px);
}
.tmp-list {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 100px;
  .tmp-item {
    cursor: pointer;
    position: relative;
    width: 150px;
    height: 100px;
    border-radius: 6px;
    overflow: hidden;
    border: 1px solid #ccc;
    margin-left: 10px;
    margin-right: 10px;
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .context {
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      text-align: center;
      color: #ffffff;
      position: absolute;
      line-height: 100px;
      background-color: rgba(0,0,0,.4);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    &.current {
      border: 4px solid #0079ff;
    }
  }
}
.handle-bar {
  display: flex;
  justify-content: center;
  margin-top: 80px;
}
.run-button-wrap {
  position: absolute;
  bottom: 50px;
  left: 50%;
  transform: translate(-150px, 0);
  display: flex;
}
.model-editor {
  margin-top: 10px;
  display: flex;
  min-height: 500px;
  height: calc(100vh - 160px);
  border: 1px solid #cccccc;
 .content {
   flex-grow: 1;
   display: flex;
   justify-content: center;
   align-items: center;
 }
 .aside {
   padding-top: 10px;
   padding-right: 10px;
   padding-left: 10px;
   flex-shrink: 0;
   width: 350px;
   background-color: #f5f5f5;
   .title {
     font-size: 16px;
     font-weight: 500;
   }
 }
}
</style>
