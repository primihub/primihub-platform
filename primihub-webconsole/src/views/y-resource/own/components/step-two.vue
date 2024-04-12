<script>
import DatabaseImport from '@/components/DatabaseImport'
import BigFileUpdate from '@/components/BigFileUpdate'
import ResourcePreviewTable from '@/components/ResourcePreviewTable'
import { displayDatabaseSourceType } from '@/api/resource'

export default {
  name: 'CreateResourceStep2',
  components: {
    DatabaseImport,
    BigFileUpdate,
    ResourcePreviewTable
  },
  data() {
    return {
      isEditPage: true,
      showDatabaseRadio: false,
      dataList: [],
      form: {
        fileId: -1,
        resourceSource: 1,
        fieldList: [],
        dataSource: undefined
      }
    }
  },
  async created() {
    await this.displayDatabaseSourceType()
  },
  methods: {
    /** whether or not to display database imports */
    async displayDatabaseSourceType() {
      const res = await displayDatabaseSourceType()
      if (res.code === 0) {
        this.showDatabaseRadio = res.result
      }
    },

    /** change import type */
    handleImportSourceChange() {
      this.handleImportChange()
      this.form.dataSource = undefined
      this.form.fileId = -1
    },

    /** data-base‘s data import success */
    handleImportSuccess(data) {
      this.dataList = data.dataList
      this.form.dataSource = data.dataSource
      this.form.fieldList = this.formatParams(data.fieldList)
    },

    /** change data table */
    handleImportChange() {
      this.form.fieldList = []
      this.dataList = []
    },

    formatParams(fieldList) {
      const newFieldList = []
      fieldList && fieldList.forEach(item => {
        const { fieldId, fieldName, fieldType, fieldDesc = '' } = item
        newFieldList.push({
          fieldId,
          fieldName,
          fieldType,
          fieldDesc
        })
      })
      return newFieldList
    },

    validateForm() {
      let flag = true
      if (this.form.fieldList.length < 1) {
        this.$message({
          message: '请先上传文件或导入数据',
          type: 'warning'
        })
        flag = false
      }
      return flag
    },

    getParams() {
      return this.form
    }
  }
}
</script>

<template>
  <div class="create-resource-step2">
    <el-form ref="form" :model="form" label-width="110px" class="center-form">
      <el-form-item label="数据导入方式" prop="resourceSource">
        <el-radio-group v-model="form.resourceSource" @change="handleImportSourceChange">
          <el-radio :label="1">文件上传</el-radio>
          <el-radio v-if="showDatabaseRadio" :label="2">数据库导入</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.resourceSource === 1">
        <big-file-update />
      </el-form-item>
      <template v-if="showDatabaseRadio && form.resourceSource === 2">
        <DatabaseImport @success="handleImportSuccess" @change="handleImportChange" />
      </template>
    </el-form>
    <template v-if="dataList.length > 0">
      <ResourcePreviewTable :data="dataList" height="500" />
    </template>
  </div>
</template>
<style lang="scss" scoped>
.center-form{
  width: 620px;
  margin: 0 auto;
}
</style>
