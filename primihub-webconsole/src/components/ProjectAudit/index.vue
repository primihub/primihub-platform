<template>
  <el-form ref="auditForm" :model="auditForm">
    <el-form-item label="参与合作审核意见:">
      <el-input
        ref="auditInput"
        v-model="auditForm.auditOpinion"
        type="textarea"
        maxlength="200"
        minlength="3"
        show-word-limit
      />
    </el-form-item>
    <el-form-item>
      <div class="buttons">
        <el-button type="primary" size="small" @click="handleSubmit">同意</el-button>
        <el-button type="danger" size="small" @click="handleRefused">拒绝</el-button>
      </div>
    </el-form-item>
  </el-form>
</template>

<script>
import { approval } from '@/api/project'
import { encodeEmoji, matchEmoji } from '@/utils/emoji-regex'

export default {
  name: 'ProjectAudit',
  props: {
    projectId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      auditForm: {
        auditOpinion: '',
        auditStatus: 0
      }
    }
  },
  created() {
  },
  methods: {
    handleSubmit() {
      this.$confirm('同意加入发起方的此次项目合作', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.auditForm.auditStatus = 1
        this.approval()
      }).catch(() => {
        this.auditForm.auditStatus = 0
      })
    },
    handleRefused() {
      if (this.auditForm.auditOpinion === '') {
        this.$message({
          type: 'warning',
          message: '请填写审核意见'
        })
        this.$refs.auditInput.focus()
      } else {
        this.$confirm('拒绝与发起方的此次项目合作?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.auditForm.auditStatus = 2
          this.approval()
        }).catch(err => {
          console.log(err)
        })
      }
    },
    approval() {
      let auditOpinion = this.auditForm.auditOpinion
      console.log(matchEmoji(auditOpinion))
      auditOpinion = matchEmoji(auditOpinion) ? encodeEmoji(auditOpinion) : auditOpinion
      console.log('发送请求1', auditOpinion)
      const params = {
        type: 1,
        id: this.projectId,
        auditStatus: this.auditForm.auditStatus,
        auditOpinion
      }
      console.log(params)
      approval(params).then(res => {
        if (res.code === 0) {
          const message = this.auditForm.auditStatus === 1 ? '加入成功' : '已拒绝与发起方的此次项目合作'
          this.$message({
            type: 'success',
            message
          })
          location.reload()
        }
      }).catch(err => {
        console.warn(err)
      })
      // try {
      //   let auditOpinion = this.auditForm.auditOpinion
      //   console.log(matchEmoji(auditOpinion))
      //   auditOpinion = matchEmoji(auditOpinion) ? encodeEmoji(auditOpinion) : auditOpinion
      //   console.log('发送请求1', auditOpinion)
      //   const params = {
      //     type: 1,
      //     id: this.projectId,
      //     auditStatus: this.auditForm.auditStatus,
      //     auditOpinion
      //   }
      //   console.log(params)
      //   const res = await approval(params)
      //   if (res.code === 0) {
      //     const message = this.auditForm.auditStatus === 1 ? '加入成功' : '已拒绝与发起方的此次项目合作'
      //     this.$message({
      //       type: 'success',
      //       message
      //     })
      //     location.reload()
      //   }
      // } catch (error) {
      //   console.log(error)
      // }
    }
  }
}
</script>

