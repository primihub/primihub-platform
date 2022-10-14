<template>
  <el-dialog
    :visible.sync="visible"
    :before-close="closeDialog"
    v-bind="$attrs"
    width="30%"
    center
  >
    <el-form ref="form" :model="form" :rules="rules">
      <el-form-item label="请输入拒绝原因" prop="auditOpinion">
        <el-input
          v-model="form.auditOpinion"
          type="textarea"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">取 消</el-button>
      <el-button type="primary" @click="handleSubmit">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { approval } from '@/api/project'
import { encodeEmoji, matchEmoji } from '@/utils/emoji-regex'

export default {
  name: 'ResourceApprovalDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    resourceId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      form: {
        type: 2,
        auditOpinion: '',
        auditStatus: 0
      },
      rules: {
        auditOpinion: [
          { required: true, message: '请输入拒绝原因' }
        ]
      }
    }
  },
  methods: {
    closeDialog() {
      this.form.auditOpinion = ''
      this.$refs.form.resetFields()
      this.$emit('close')
    },
    async handleSubmit() {
      this.form.auditStatus = 2
      await this.approval()
    },
    async approval() {
      try {
        let auditOpinion = this.form.auditOpinion
        auditOpinion = matchEmoji(auditOpinion) ? encodeEmoji(auditOpinion) : auditOpinion
        const params = {
          type: 2,
          id: this.resourceId,
          auditStatus: this.form.auditStatus,
          auditOpinion
        }
        const res = await approval(params)
        if (res.code === 0) {
          this.$message({
            type: 'success',
            message: '已拒绝'
          })
          this.$emit('submit')
        }
      } catch (error) {
        console.log(error)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.dialog-footer{
  justify-content: center;
}
::v-deep .el-dialog__body{
  padding: 10px 20px 0 20px;
}
</style>
