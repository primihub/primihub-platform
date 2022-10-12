<template>
  <el-dialog
    :before-close="closeDialog"
    v-bind="$attrs"
  >
    <div class="form-container">
      <el-form ref="form" :model="formData" label-width="100px" :rules="rules" label-position="right">
        <el-form-item label="草稿名称：" prop="draftName">
          <el-input v-model="formData.draftName" placeholder="请输入草稿名称" />
        </el-form-item>
      </el-form>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button size="small" @click="closeDialog">取 消</el-button>
      <el-button size="small" type="primary" @click="handleSubmit">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'SaveModelDraftDialog',
  props: {
    draftName: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      formData: {
        draftName: this.draftName
      },
      rules: {
        draftName: [
          { required: true, message: '请输入草稿名称' }
        ]
      }
    }
  },
  methods: {
    closeDialog() {
      this.$refs.form.resetFields()
      this.$emit('close')
    },
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.$emit('submit', this.formData)
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.form-container{
  padding: 30px 30px;
}
</style>
