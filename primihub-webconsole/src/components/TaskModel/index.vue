<template>
  <div v-loading="listLoading" class="container" :class="{'disabled': model.isDel === 1, 'model': type === 'model'}">
    <div class="section">
      <h2 class="infos-title">模型信息</h2>
      <el-row type="flex">
        <el-col class="desc-col" :span="6">
          <div class="desc-label">模型ID:</div>
          <div class="desc-content">
            <el-link v-if="task.isCooperation === 0 && oneself" type="primary" @click="toModelDetail">{{ model.modelId }}</el-link>
          </div>
        </el-col>
        <el-col class="desc-col" :span="6">
          <div class="desc-label">模型名称:</div>
          <div class="desc-content">{{ model.modelName }}</div>
        </el-col>
      </el-row>
      <el-row type="flex">
        <el-col class="desc-col" :span="6">
          <div class="desc-label">基础模型:</div>
          <div class="desc-content">{{ model.modelType | modelTypeFilter }}</div>
        </el-col>
        <el-col v-if="type==='model'" class="desc-col" :span="6">
          <div class="desc-label">任务ID:</div>
          <div class="desc-content">
            <el-link type="primary" @click="toModelTaskDetail">{{ task.taskIdName }}</el-link>
          </div>
        </el-col>
      </el-row>
      <el-row v-if="type==='model'" type="flex">
        <el-col class="desc-col" :span="6">
          <div class="desc-label">任务名称:</div>
          <div class="desc-content">{{ task.taskName }}</div>
        </el-col>
        <el-col class="desc-col" :span="6">
          <div class="desc-label">建模完成时间:</div>
          <div class="desc-content">{{ model.createDate }}</div>
        </el-col>
      </el-row>
      <el-row v-if="type==='model'" type="flex">
        <el-col class="desc-col" :span="6">
          <div class="desc-label">角色:</div>
          <div class="desc-content">{{ oneself ? '发起方': '协作方' }}</div>
        </el-col>
      </el-row>
      <el-row>
        <el-col class="desc-col" :span="24">
          <div class="desc-label">模型描述:</div>
          <div class="desc-content">
            <editInput style="width:70%;" type="textarea" show-word-limit maxlength="200" :value="model.modelDesc" @change="handleDescChange" />
          </div>
        </el-col>
      </el-row>
      <div v-if="type === 'model' && model.isDel !== 1 && task.isCooperation === 0" class="buttons">
        <el-button type="danger" icon="el-icon-delete" @click="deleteModelTask">删除模型</el-button>
      </div>
    </div>

    <div class="section">
      <h3>模型耗时</h3>
      <el-descriptions :column="1" label-class-name="time-consuming-label" :colon="false">
        <el-descriptions-item label-class-name="total-time-label">
          <span class="el-icon-time" />
          <span class="total-time-label">总耗时:</span>
          <span v-if="model.totalTime" class="total-time">{{ model.totalTime | timeFilter }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-for="item in modelComponent" :key="item.componentId" :label="item.componentName">
          <el-progress :percentage="item.timeRatio * 100" status="success" class="progress-line" />
          <span class="el-icon-time" />
          <span>耗时 {{ item.timeConsuming | timeFilter }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </div>
    <div class="section">
      <h3>模型评估分数</h3>
      <el-table
        :data="modelQuotas"
        style="width: 100%"
      >
        <el-table-column
          prop="modelType"
          label="模型"
        />
        <el-table-column
          prop="quotaType"
          label="数据集"
        />
        <!-- <el-table-column
          prop="percent"
          label="占比"
        /> -->
        <el-table-column
          prop="train_auc"
          label="AUC"
        />
        <el-table-column
          prop="train_acc"
          label="ACC"
        />
        <el-table-column
          prop="train_ks"
          label="KS"
        />
      </el-table>

    </div>

  </div>
</template>

<script>
import { getModelDetail, deleteModel, updateModelDesc } from '@/api/model'
import editInput from '@/components/editInput'

export default {
  components: {
    editInput
  },
  filters: {
    quotaTypeFilter(type) {
      const typeMap = {
        1: '训练样本集',
        2: '测试样本集'
      }
      return typeMap[type]
    },
    modelTypeFilter(type) {
      const statusMap = {
        2: '纵向-XGBoost',
        3: '横向-LR',
        4: 'MPC_LR'
      }
      return statusMap[type]
    }
  },
  props: {
    state: {
      type: Number,
      default: 0
    },
    projectStatus: {
      type: Number,
      default: 0
    },
    type: {
      type: String,
      default: 'task'
    }
  },
  data() {
    return {
      listLoading: false,
      model: {},
      modelQuotas: [],
      modelResources: [],
      modelId: 0,
      modelComponent: [],
      lineChartData: [],
      anotherQuotas: [],
      taskState: null,
      projectId: 0,
      task: {},
      oneself: false
    }
  },
  created() {
    this.taskId = this.$route.query.taskId || this.$route.params.taskId
    this.fetchData()
  },
  methods: {
    handleDescChange({ change, value }) {
      if (change) {
        this.model.modelDesc = value
        updateModelDesc({
          modelId: this.modelId,
          modelDesc: this.model.modelDesc
        }).then(({ code }) => {
          if (code === 0) {
            this.$message.success('修改成功')
          } else {
            this.$message.error('修改失败')
          }
        })
      }
    },
    toModelDetail() {
      this.$router.push({
        path: `/model/detail/${this.modelId}`,
        query: { taskId: this.taskId }
      })
    },
    toModelTaskDetail() {
      this.$router.push({
        path: `/project/detail/${this.projectId}/task/${this.taskId}`
      })
    },
    deleteModelTask() {
      this.$confirm('此操作将永久删除该模型, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.listLoading = true
        deleteModel({ modelId: this.modelId }).then(res => {
          if (res.code === 0) {
            this.$message({
              message: '删除成功',
              type: 'success',
              duration: 1000
            })
            this.fetchData()
          }
          this.listLoading = false
        }).catch(() => {
          this.listLoading = false
        })
      })
    },
    fetchData() {
      this.listLoading = true
      getModelDetail({ taskId: this.taskId }).then((response) => {
        this.listLoading = false
        console.log('response.data', response.result)
        const { model, modelResources, modelComponent, anotherQuotas, task, project, oneself } = response.result
        this.task = task
        this.model = model
        this.modelId = model.modelId
        this.anotherQuotas = anotherQuotas
        this.oneself = oneself
        // format model score list
        if (JSON.stringify(anotherQuotas) !== '{}') {
          for (let i = 0; i < 2; i++) {
            const modelType = model.modelType === 3 ? '横向-LR' : model.modelType === 4 ? 'MPC_LR' : '纵向-XGBoost'
            this.modelQuotas.push({
              modelType,
              quotaType: i === 0 ? '测试集' : '评估集',
              train_acc: anotherQuotas.train_acc,
              train_auc: anotherQuotas.train_auc,
              train_ks: anotherQuotas.train_ks
            })
          }
        }
        console.log(this.modelQuotas)
        this.modelResources = modelResources.filter(item => item.resourceType !== 3)
        this.modelComponent = modelComponent
        this.projectId = project.id
      })
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/description.scss";
::v-deep .time-consuming-label {
  width: 100px;
}
h3{
  font-size: 16px;
}
.description{
  display: flex;
}
.desc-item{
  flex: 1;
  color: #606268;
  font-size: 14px;
  display: flex;
  line-height: 1.5;
  .label{
    margin-right: 10px;
  }
}
.container {
  &.disabled{
    filter:progid:DXImageTransform.Microsoft.BasicImage(graysale=1);
    -webkit-filter: grayscale(100%);
  }
  .total-time-label{
    font-size: 18px;
    margin-right: 10px;
  }
  .total-time{
    font-size: 20px;
  }
  .progress-line {
    width: 400px;
    margin-left: 10px;
  }
  .el-icon-time {
    color: #409EFF;
    margin-right: 3px;
  }
  .section {
    background-color: #fff;
    margin-bottom: 30px;
    padding-bottom: 20px;
  }
  .container.model .section{
    padding: 20px;
  }
}
::v-deep .el-table th{
  background: #fafafa;
}
::v-deep .el-descriptions-item__container{
  align-items: center;
}
.download-button{
  margin-left: 5px;
}
.quotas{
  max-width: 800px;
}
.buttons{
  display: flex;
  justify-content: flex-end;
}
</style>
