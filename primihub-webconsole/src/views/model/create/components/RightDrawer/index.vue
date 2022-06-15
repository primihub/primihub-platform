<template>
  <div class="right-drawer">
    <template v-if="nodeData && showDataConfig">
      <el-form ref="form" v-loading="listLoading" :model="nodeData" label-width="80px" element-loading-spinner="el-icon-loading">
        <div v-for="item in nodeData.componentTypes" :key="item.typeCode">
          <el-form-item :label="item.typeName " :prop="item.typeCode">
            <template v-if="item.inputType === 'label'">
              <span class="label-text">{{ item.inputValue }}</span>
            </template>
            <template v-if="item.inputType === 'text'">
              <el-input v-model="item.inputValue" size="mini" />
            </template>
            <template v-if="item.inputType === 'select'">
              <el-select v-model="item.inputValue" placeholder="请选择" :value-key="item.typeCode" @change="handleChange(item)">
                <el-option
                  v-for="v in item.inputValues"
                  :key="v.key"
                  :label="v.val"
                  :value="v.key"
                />
              </el-select>
            </template>
          </el-form-item>
        </div>
      </el-form>
    </template>
    <template v-else>
      <el-form ref="form" :rules="rules" :model="modelData" label-width="80px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="modelData.taskName" size="mini" />
        </el-form-item>
        <el-form-item label="任务描述" prop="modelDesc">
          <el-input v-model="modelData.modelDesc" size="mini" type="textarea" />
        </el-form-item>
        <el-form-item label="训练类型" prop="trainType">
          <el-radio-group v-model="modelData.trainType" size="small">
            <el-radio :label="1">纵向</el-radio>
            <el-radio :label="2">横向</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </template>
  </div>
</template>

<script>
export default {
  props: {
    showDataConfig: {
      type: Boolean,
      default: false
    },
    listLoading: {
      type: Boolean,
      default: false
    },
    nodeData: {
      type: Object,
      default: () => {
        return {}
      }
    },
    modelData: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      rules: {
        taskName: [
          { required: true, message: '请输入任务名称', trigger: 'blur' },
          { max: 20, message: '长度在20个字符以内', trigger: 'blur' }
        ],
        modelName: [
          { required: true, message: '请输入模型名称', trigger: 'blur' },
          { max: 20, message: '长度在20个字符以内', trigger: 'blur' }
        ],
        modelDesc: [
          { required: true, message: '请输入任务描述', trigger: 'blur' },
          { max: 100, message: '长度在100个字符以内', trigger: 'blur' }
        ],
        trainType: [
          { required: true, message: '请选择训练类型', trigger: 'change' }
        ],
        yValueColumn: [
          { required: true, message: '请选择Y值字段', trigger: 'change' }
        ]
      }
    }
  },
  methods: {
    handleChange(item) {
      // console.log('modelData', this.modelData)
      this.$emit('change', item.typeCode, item)
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-form-item__content{
  display: block;
  margin-left: 0!important;
  width: 100%;
}
::v-deep .el-form-item__label{
  float: none;
}

.select{
  width: 100%;
}
.right-drawer {
  width:300px;
  height: 100%;
  background: #fff;
  padding: 10px 15px;
  border-top: 1px solid #cccfcc;
}
.label-text{
  color: #666;
}
</style>
