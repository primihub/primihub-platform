<script>
import OrganSelect from '@/components/OrganSelect'

export default {
  name: 'CreateResourceStep1',
  components: { OrganSelect },
  data() {
    return {
      resourceAuthTypeList: [
        { value: 1, label: '公开' },
        { value: 2, label: '私有' },
        { value: 3, label: '指定机构可见' }
      ],
      tagList: [
        { value: 1, label: '电信' },
        { value: 2, label: '全球通' },
        { value: 3, label: '银行' },
        { value: 4, label: '移动' }
      ],
      updateCycleList: [
        { value: 1, label: '年' },
        { value: 2, label: '月' },
        { value: 3, label: '周' },
        { value: 4, label: '日' },
        { value: 5, label: '小时' }
      ],
      sceneList: [
        { value: 1, label: '安全求交' },
        { value: 2, label: '匿踪查询' },
        { value: 3, label: '多方计算' },
        { value: 4, label: '横向联邦' },
        { value: 5, label: '纵向联邦' }
      ],
      authOrganList: [],
      form: {
        resourceName: '',
        ENname: '',
        tags: [],
        resourceDesc: '',
        resourceAuthType: 1,
        updateCycle: 1,
        scene: 1,
        fusionOrganList: [], // 指定机构
        distributed: false
      },
      rules: {
        resourceName: [{ required: true, message: '请输入资源名称中文', trigger: 'blur' }],
        ENname: [{ required: true, message: '请输入英文名称', trigger: 'blur' }],
        resourceDesc: [{ required: true, message: '请输入资源描述', trigger: 'blur' }],
        resourceAuthType: [{ required: true, message: '请选择授权方式', trigger: 'change' }],
        tags: [{ required: true, message: '请选择数据集标签', trigger: 'change' }],
        scene: [{ required: true, message: '请选择应用场景', trigger: 'change' }]
      }
    }
  },
  mounted() {

  },
  methods: {
    validateForm() {
      let flag = false
      this.$refs.form.validate(valid => {
        flag = valid
      })
      return flag
    },

    getParams() {
      return this.form
    },

    handleOrganChange(data) {
      this.dataForm.fusionOrganList = data
    }
  }
}
</script>

<template>
  <div class="create-resource-step1">
    <el-form ref="form" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="资源名称中文" prop="resourceName">
        <el-input v-model="form.resourceName" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item label="英文名称" prop="ENname">
        <el-input v-model="form.ENname" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item label="资源描述" prop="resourceDesc">
        <el-input v-model="form.resourceDesc" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item label="数据集标签" prop="tags">
        <el-checkbox-group v-model="form.tags" size="mini" class="yy-border-checkbox">
          <el-checkbox v-for="{ value, label } in tagList" :key="value" :label="value" border>{{ label }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="更新周期" prop="updateCycle">
        <el-radio-group v-model="form.updateCycle" size="mini" class="yy-border-radio">
          <el-radio v-for="{ value, label } in updateCycleList" :key="value" :label="value" border>{{ label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="应用场景" prop="scene">
        <el-radio-group v-model="form.scene" size="mini" class="yy-border-radio">
          <el-radio v-for="{ value, label } in sceneList" :key="value" :label="value" border>{{ label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.scene === 2" label="分布式处理" prop="distributed">
        <el-switch v-model="form.distributed" />
        <p class="distributed-tip">数据量大时建议开启分布式预处理，可大大提升计算效率</p>
      </el-form-item>
      <el-form-item label="授权方式" prop="resourceAuthType">
        <el-radio-group v-model="form.resourceAuthType">
          <el-radio v-for="{ value, label } in resourceAuthTypeList" :key="value" :label="value"> {{ label }}</el-radio>
          <template v-if="form.resourceAuthType === 3">
            <OrganSelect :value="authOrganList" style="display:inline-block;" size="small" @change="handleOrganChange" />
          </template>
        </el-radio-group>
      </el-form-item>
    </el-form>
  </div>
</template>

<style lang="scss" scoped>
.distributed-tip{
  font-size: 12px;
  line-height: 24px;
  color: #999;
}
.create-resource-step1{
  width: 620px;
  margin: 0 auto;
}
</style>
