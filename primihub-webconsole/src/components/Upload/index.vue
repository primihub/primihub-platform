<template>
  <div>
    <uploader
      ref="uploader"
      :options="options"
      :file-status-text="fileStatusText"
      class="uploader"
      :auto-start="false"
      @file-added="onFileAdded"
      @file-success="onFileSuccess"
      @file-error="onFileError"
      @file-progress="onFileProgress"
    >
      <uploader-unsupport />
      <uploader-drop>
        <p class="upload-icon"><i class="el-icon-upload" /></p>
        <p class="upload-text"> 将文件拖到此处，或选择文件上传</p>
        <uploader-btn ref="uploaderBtn" class="uploaderBtn" :single="single">选择文件</uploader-btn>
      </uploader-drop>
      <uploader-list />
    </uploader>
    <p v-if="showTips" class="upload-tip">
      1.只能上传.csv文件，且不超过1MB <br>
      2.请确保上传的资源文件编码为UTF8
    </p>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import SparkMD5 from 'spark-md5'
import Vue from 'vue'
import store from '@/store'
import uploader from 'vue-simple-uploader'
Vue.use(uploader)

// 切片大小
const CHUNK_SIZE = 1 * 1024 * 1024
// 可传文件类型集合
// const FILE_TYPES = ['text/csv', 'application/vnd.ms-excel']
const FILE_SUFFIXS = ['csv', ' sqlite', 'sqlite3', 'db', 'db3', 's3db', 'sl3']

export default {
  props: {
    maxSize: {
      type: Number,
      default: 1024 * 1024 * 1
    },
    single: {
      type: Boolean,
      default: false
    },
    showTips: {
      type: Boolean,
      default: false
    },
    fileSuffix: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      isError: false,
      options: { // 文件上传时options
        // 上传地址
        target: `${process.env.VUE_APP_BASE_API}/sys/file/upload`,
        // 只能上传单文件
        singleFile: true,
        // 是否开启服务器分片校验。默认为 true
        testChunks: false,
        // 分片大小
        chunkSize: CHUNK_SIZE,
        // 并发上传数，默认为 3
        simultaneousUploads: 3,
        testMethod: 'POST', // 测试的时候使用的 HTTP 方法
        // 分片校验函数，判断分片是否上传，秒传和断点续传基于此方法
        // checkChunkUploadedByResponse: (chunk, message) => {
        //   const messageObj = JSON.parse(message)
        //   console.log('checkChunkUploadedByResponse', message)
        //   const dataObj = messageObj.result
        //   // if (dataObj.uploaded !== undefined) {
        //   //   return dataObj.uploaded
        //   // }
        //   // 判断文件或分片是否已上传，已上传返回 true，这里的 uploadedChunks 是后台返回
        //   return (dataObj.uploadedChunks && dataObj.uploadedChunks || []).indexOf(chunk.offset + 1) >= 0
        // },
        query: (file, chunk) => {
          return {
            ...file.params,
            fileSource: '1',
            timestamp: new Date().getTime(),
            nonce: Math.floor(Math.random() * 1000 + 1),
            token: getToken()
          }
        }
      },
      // attrs: {
      //   accept: FILE_TYPES.join(',')
      // },

      // 修改上传状态
      fileStatusTextObj: {
        success: '上传成功',
        error: '上传错误',
        uploading: '正在上传',
        paused: '停止上传',
        waiting: '等待中'
      },
      // 上传并发数
      simultaneousUploads: 3,
      uploadIdInfo: null,
      uploadFileList: [],
      fileChunkList: []
    }
  },
  methods: {
    // 用于文件校验，忽略该文件则返回 false，文件不会添加到上传列表中
    onFileAdded(file, event) {
      console.log('onFileAdded', file)
      const suffix = file.getExtension()
      if (!FILE_SUFFIXS.includes(suffix)) {
        this.$message({
          message: '上传文件格式不正确，请重新上传',
          type: 'warning'
        })
        file.ignored = true
      } else if (file.size > this.maxSize) {
        this.$message({
          message: '上传文件不能大于1MB，请重新上传',
          type: 'warning'
        })
        file.ignored = true
      } else {
        this.getFileMD5(file, md5 => {
          if (md5 !== '') {
          // 修改文件唯一标识
            file.uniqueIdentifier = md5
            file.params = this.params
            // 恢复上传
            file.resume()
          }
        })
      }
    },
    // 上传成功事件
    onFileSuccess(rootFile, file, response, chunk) {
      const res = JSON.parse(response)
      if (res.code === 0) {
        this.$emit('success', res.result.sysFile)
        this.$message({
          message: '上传成功',
          type: 'success'
        })
      } else if (res.code === 102) {
        this.$message({
          message: '登录失效，请重新登录',
          type: 'error',
          duration: 2000,
          closeOnClickModal: false
        })
        setTimeout(() => {
          store.dispatch('user/resetToken').then(() => {
            location.reload()
          })
        }, 2000)
        file.cancel()
        return
      } else {
        this.$message({
          message: res.msg,
          type: 'error'
        })
        file.cancel()
        return
      }
    },
    // 上传过程出错处理
    onFileError(rootFile, file, message, chunk) {
      console.log('上传失败，请重新上传')
      this.$message({
        message: '上传失败，请重新上传',
        type: 'warning'
      })
      file.cancel()
    },
    // 文件上传进度
    onFileProgress(rootFile, file, chunk) {
      console.log(`当前进度：${Math.ceil(file._prevProgress * 100)}%`)
    },
    fileStatusText(status, response) {
      if (status === 'md5') {
        return '校验MD5'
      } else {
        return this.fileStatusTextObj[status]
      }
    },
    saveFileUploadId(data) {
    // localStorage.setItem(FILE_UPLOAD_ID_KEY, data)
    },
    getFileMD5(file, callback) {
      const spark = new SparkMD5.ArrayBuffer()
      const fileReader = new FileReader()
      const blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
      let currentChunk = 0
      const chunks = Math.ceil(file.size / CHUNK_SIZE)
      const startTime = new Date().getTime()
      file.pause()
      loadNext()
      fileReader.onload = function(e) {
        spark.append(e.target.result)
        if (currentChunk < chunks) {
          currentChunk++
          loadNext()
        } else {
          const md5 = spark.end()
          console.log(`MD5计算完毕：${md5}，耗时：${new Date().getTime() - startTime} ms.`)
          callback(md5)
        }
      }
      fileReader.onerror = function() {
        this.$message.error('文件读取错误')
        file.cancel()
      }
      function loadNext() {
        const start = currentChunk * CHUNK_SIZE
        const end = ((start + CHUNK_SIZE) >= file.size) ? file.size : start + CHUNK_SIZE
        fileReader.readAsArrayBuffer(blobSlice.call(file.file, start, end))
      }
    },
    handleError(file) {
      console.log(file.fileType)
      if (file.size > this.maxSize) {
        this.$message({
          message: '上传文件不能大于1MB，请重新上传',
          type: 'warning'
        })
      } else if (file.fileType !== 'text/csv') {
        this.$message({
          message: '上传文件不正确，请重新上传',
          type: 'warning'
        })
      }
      return false
    }
  }
}
</script>

<style lang="scss" scoped>
p{
  margin-block-start: 0;
  margin-block-end: 0;
}
.upload-icon{
  font-size: 40px;
  color: #1677FF;
}
.upload-text{
  color: #606266;
  font-size: 14px;
}
.uploader {
  width:660px;
  min-width: 300px;
  margin: 20px 0;
  font-size: 12px;
  text-align: center;
}
.uploader-drop{
  background-color: transparent;
  border-radius: 6px;
  &:hover{
    cursor: pointer;
    border-color: #1677FF;
  }
}
.upload-container{
  background-color: transparent;
  border-radius: 6px;
  border: 1px dashed #ccc;
  padding: 20px 0;
  &:hover{
    cursor: pointer;
    border-color: #1677FF;
  }
}
.upload-tip{
  font-size: 14px;
  color: #606266;
  line-height: 1.75;
}
</style>
