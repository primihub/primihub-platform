<template>
  <div v-loading="listLoading" class="container" :class="{'disabled': model.isDel === 1, 'model': type === 'model'}">
    <div v-if="hasModelSelectComponent" class="section">
      <h2 class="infos-title">模型信息</h2>
      <div class="description-container">
        <div class="desc-col">
          <div class="desc-label">模型ID:</div>
          <div class="desc-content">
            <el-link v-if="task.isCooperation === 0 && oneself" type="primary" @click="toModelDetail">{{ model.modelId }}</el-link>
            <template v-else>
              {{ model.modelId }}
            </template>
          </div>
        </div>
        <div class="desc-col">
          <div class="desc-label">模型名称:</div>
          <div class="desc-content">{{ model.modelName }}</div>
        </div>
        <div v-if="type==='model'" class="desc-col">
          <div class="desc-label">任务ID:</div>
          <div class="desc-content">
            <el-link type="primary" @click="toModelTaskDetail">{{ task.taskIdName }}</el-link>
          </div>
        </div>
        <div v-if="type=== 'model'" class="desc-col">
          <div class="desc-label">任务名称:</div>
          <div class="desc-content">{{ task.taskName }}</div>
        </div>
        <div v-if="type=== 'model'" class="desc-col">
          <div class="desc-label">角色:</div>
          <div class="desc-content">{{ oneself ? '发起方': '协作方' }}</div>
        </div>
        <div class="desc-col">
          <div class="desc-label">基础模型:</div>
          <div class="desc-content">{{ model.modelType | modelTypeFilter }}</div>
        </div>
        <div v-if="type=== 'model'" class="desc-col">
          <div class="desc-label">建模完成时间:</div>
          <div class="desc-content">{{ model.createDate }}</div>
        </div>
        <div class="desc-col" style="width: 100%;">
          <div class="desc-label">模型描述:</div>
          <div class="desc-content">
            <template v-if="task.isCooperation === 0">
              <EditInput style="width:70%;" type="textarea" show-word-limit maxlength="200" :value="model.modelDesc" @change="handleDescChange" />
            </template>
            <template v-else>
              {{ model.modelDesc }}
            </template>
          </div>
        </div>
      </div>
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
    <div v-if="hasModelSelectComponent" class="section">
      <h3>模型评估分数</h3>
      <el-table style="width: 100%" border :data="modelQuotas">
        <template v-for="(item,index) in tableHead">
          <el-table-column :key="index" :prop="item.column_name" :label="item.column_comment" />
        </template>
      </el-table>
    </div>
    <div v-if="hasModelSelectComponent && chartData.length > 0" class="section">
      <h3>模型评估视图</h3>
      <modelScoreChart :chart-data="chartData" width="500px" height="400px" />
    </div>

  </div>
</template>

<script>
import { getModelDetail, deleteModel, updateModelDesc } from '@/api/model'
import EditInput from '@/components/editInput'
import modelScoreChart from '@/components/Charts/ModelScoreChart'

export default {
  components: {
    EditInput,
    modelScoreChart
  },
  filters: {
    quotaTypeFilter(type) {
      const typeMap = {
        1: '训练样本集',
        2: '测试样本集'
      }
      return typeMap[type]
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
      tableHead: [],
      modelResources: [],
      modelId: 0,
      modelComponent: [],
      anotherQuotas: null,
      chartData: [],
      taskState: null,
      projectId: 0,
      task: {},
      oneself: false
    }
  },
  computed: {
    hasModelSelectComponent() {
      return this.modelComponent.find(component => component.componentCode === 'model')
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
        if (anotherQuotas.train_fpr && anotherQuotas.train_tpr) {
          this.chartData = [anotherQuotas.train_fpr, anotherQuotas.train_tpr]
        }
        delete this.anotherQuotas['train_fpr']
        delete this.anotherQuotas['train_tpr']
        delete this.anotherQuotas['gain_x']
        delete this.anotherQuotas['gain_y']
        delete this.anotherQuotas['lift_x']
        delete this.anotherQuotas['lift_y']
        delete this.anotherQuotas['train_thresholds']

        // format model score list
        if (JSON.stringify(anotherQuotas) !== '{}') {
          this.modelQuotas.push({
            quotaType: '测试集',
            ...anotherQuotas
          })
          this.tableHead.push({
            column_name: 'quotaType',
            column_comment: '数据集'
          })
          for (const key in anotherQuotas) {
            const column_comment = key.split('train_')[1].toUpperCase()
            console.log('column_comment', column_comment)
            this.tableHead.push({
              column_name: key,
              column_comment
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
@import "~@/styles/details.scss";
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
    color: #1677FF;
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
