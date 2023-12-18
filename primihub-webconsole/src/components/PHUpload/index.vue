<template>
  <div>
    <el-upload
      class="avatar-uploader"
      accept=".png,.jpg,.jpeg,.ico"
      :data="uploadDataReq"
      :action="action"
      :show-file-list="false"
      :on-success="handleAvatarSuccess"
      :before-upload="beforeAvatarUpload"
      :style="{width,height}"
    >
      <div v-if="imgUrl" class="img-container" @mouseover="showMask = true" @mouseleave="showMask = false">
        <img :src="imgUrl" class="avatar">
        <div v-if="showMask" class="mask">
          <i class="el-icon-edit" />
          <i class="el-icon-delete" @click.prevent.stop="handleRemove" />
        </div>
      </div>

      <i v-else class="el-icon-plus avatar-uploader-icon" />
    </el-upload>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'

export default {
  name: 'PHUpload',
  props: {
    size: {
      type: Number,
      default: 2 // MB
    },
    type: {
      type: String,
      default: 'logo'
    },
    imageUrl: {
      type: String,
      default: ''
    },
    width: {
      type: String,
      default: '150px'
    },
    height: {
      type: String,
      default: 'auto'
    }
  },
  data() {
    return {
      action: '',
      showMask: false,
      uploadDataReq: {
        fileSource: '2',
        token: getToken(),
        timestamp: new Date().getTime(),
        nonce: Math.floor(Math.random() * 1000 + 1)
      },
      params: {}
    }
  },
  computed: {
    imgUrl: {
      get() {
        return this.imageUrl
      },
      set(val) {
        return val
      }
    }
  },
  watch: {
    imageUrl(newVal) {
      if (newVal) {
        this.imgUrl = newVal
      }
    }
  },
  created() {
    this.action = `${process.env.VUE_APP_BASE_API}/sys/file/upload`
  },
  methods: {
    handleRemove(e) {
      this.$confirm('删除后将使用默认图片, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$emit('remove', {
          type: this.type
        })
      }).catch(() => {
        this.listLoading = false
      })
    },
    handleAvatarSuccess(res, file) {
      const fileUrl = res.result.sysFile.fileUrl
      console.log(res.result.sysFile.fileUrl)
      this.imgUrl = URL.createObjectURL(file.raw)
      this.$emit('success', {
        imgUrl: this.imgUrl,
        type: this.type,
        fileUrl
      })
    },
    beforeAvatarUpload(file) {
      let isOk = false
      if (this.type === 'favicon' && file.name.split('.')[1] !== 'ico') {
        this.$message.error(`上传格式需为ico格式!`)
        isOk = false
      } else {
        isOk = true
      }

      const isLt2M = file.size / this.size * 1024 * 1024
      if (!isLt2M) {
        this.$message.error(`图片大小不能超过 ${this.size} MB!`)
      }
      return isOk && isLt2M
    }
  }
}
</script>

<style lang="scss">
.img-container{
  background: #f0f2f5;
  position: relative;
  padding: 10px;
  .mask{
    position: absolute;
    height: 100%;
    top: 0;
    left: 0;
    right: 0;
    background: rgba(0,0,0,.6);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 14px;
    justify-content: space-evenly;
  }
}
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
// .avatar-uploader .el-upload:hover {
//   border-color: #1677FF;
// }
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}
.avatar {
  width: 100%;
  height: 100%;
  display: block;
}
</style>
