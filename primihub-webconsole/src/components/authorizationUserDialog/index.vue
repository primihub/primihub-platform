<template>
  <el-dialog
    title="选择授权对象 / 选择机构"
    width="50%"
    v-bind="$attrs"
    @close="handleClose"
  >

    <AuthorizationUserTransfer :value="userValue" @change="handleChange" />
    <span slot="footer" class="dialog-footer">
      <el-button type="primary" @click="handleCancel">取消</el-button>
      <el-button @click="handleAuthConfirm">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import AuthorizationUserTransfer from '@/components/authorizationUserTransfer'
export default {
  name: 'AuthorizationUserDialog',
  components: {
    AuthorizationUserTransfer
  },
  props: {
    resourceId: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      userValue: []
    }
  },
  mounted() {
  },
  methods: {
    handleChange(data) {
      this.userValue = data
    },
    handleClose() {
      this.$emit('close')
      this.userValue = []
    },
    handleCancel() {
      this.$emit('cancel')
      this.userValue = []
    },
    handleAuthConfirm() {
      if (this.userValue.length === 0) {
        this.$message.error('请选择授权对象')
        return
      } else {
        this.$emit('submit', this.userValue)
        // TODO add authorization api
        this.$message.success('授权成功')
        this.userValue = []
      }
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-transfer-panel{
  width: 245px;
}
::v-deep .el-transfer__buttons{
  .el-button{
    display: block;
    padding: 5px 5px;
  }
  .el-button--primary.is-disabled{
    background-color: #fff;
    color: #333;
    border-color: #eee;
  }
  .el-button+.el-button{
    margin-left: 0;
  }
}
.dialog-footer{
  padding-bottom: 30px;
}
</style>
