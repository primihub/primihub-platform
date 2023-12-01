<template>
  <div class="container">
    <div class="steps">
      <div class="search-area">
        <el-form ref="form" :model="form" :rules="rules" class="demo-form">
          <div class="select-resource">
            <el-form-item prop="taskName">
              <div class="flex" style="width: calc(100% - 135px);">
                <div class="label">任务名称</div>
                <el-input v-model="form.taskName" style="flex: 1;" maxlength="32" show-word-limit placeholder="请输入任务名称,限32字" />
              </div>
            </el-form-item>
            <div class="dialog-con" style="width: calc(100% - 135px);">
              <el-form-item prop="resourceName">
                <div class="flex">
                  <div class="label">选择查询资源</div>
                  <el-row type="flex" :gutter="10" style="flex: 1;">
                    <el-col :span="12">
                      <el-form-item prop="organId">
                        <el-select v-model="form.organId" style="width: 100%" placeholder="请选择机构" clearable @change="handleOrganChange">
                          <el-option
                            v-for="item in organList"
                            :key="item.globalId"
                            :label="item.globalName"
                            :value="item.globalId"
                          />
                        </el-select>
                      </el-form-item>

                    </el-col>
                    <el-col :span="12">
                      <el-form-item>
                        <div class="custom-input" :style="{'color': resourceName === '请选择机构下资源' ? '#C0C4CC' : '#606266'}" :class="{'disabled': form.organId === '' }" @click="openDialog"><span class="resource-name">{{ resourceName }}</span><i class="el-icon-arrow-down" /></div>
                        <el-popover
                          class="popover-container"
                          placement="top-start"
                          title="隐匿查询步骤:"
                          width="400"
                          trigger="hover"
                        >
                          <div>
                            <p>(1)添加资源：被查询机构先在其节点里【资源管理】菜单下添加数据资源</p>
                            <p>(2)资源授权：被查询机构向查询发起方授权资源权限，设置资源公开或指定机构可见</p>
                            <p>(3)发起查询：查询发起方在本方【隐匿查询】菜单下发起隐匿查询任务，并查看结果。</p>
                            <p>您可以通过搜索框查找机构开放给您的资源，如您未找到，请到对应机构先添加资源。</p>
                          </div>
                          <div slot="reference">
                            <svg-icon icon-class="problem" />
                          </div>
                        </el-popover>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </div>

              </el-form-item>
              <el-form-item v-if="form.selectResources" class="resource-container" label="已选资源" prop="selectResources">
                <ResourceItemSimple :data="selectResources" :show-close="true" class="select-item" @delete="handleDelete" />
              </el-form-item>
            </div>
            <div class="group">
              <!-- {{ searchGroup }} -->
              <div v-for="(item, index) in searchGroup" :key="index" class="flex">
                <div class="group-item">
                  <el-form-item prop="key">
                    <div class="flex">
                      <div class="label">查询主键</div>
                      <el-select v-model="item.key" style="width: 100%;flex: 1;" multiple no-data-text="暂无数据" placeholder="请选择查询主键" clearable @change="handleKeyChange($event,index)">
                        <el-option
                          v-for="(options,i) in primaryKeysOptions"
                          :key="i"
                          :label="options.fieldName"
                          :value="options.fieldName"
                        >
                          <template slot="default">
                            <div class="option-item">
                              <template v-if="options.fieldDesc">
                                <div class="option-label" :style="{'max-width': options.fieldDesc ? '200px' : '100%'}">
                                  <el-tooltip class="item" effect="dark" :content="options.fieldName" placement="top-start">
                                    <span>{{ options.fieldName }}</span>
                                  </el-tooltip>
                                </div>
                                <div class="option-desc">
                                  <el-tooltip class="item" effect="dark" :content="options.fieldDesc" placement="top-start">
                                    <span>{{ options.fieldDesc ? options.fieldDesc.length>8 ? options.fieldDesc.slice(0,8)+'...' : options.fieldDesc : '' }}</span>
                                  </el-tooltip>
                                </div>
                              </template>

                              <div v-else>
                                <span class="option-label">{{ options.fieldName }}</span>
                              </div>
                            </div>
                          </template>
                        </el-option>
                      </el-select>
                    </div>
                  </el-form-item>
                  <el-form-item prop="query">
                    <div class="flex" style="width: 100%;">
                      <div class="label">关键词</div>
                      <div class="flex query-input" @keyup.enter="handleKeyInputConfirm(index)">
                        <div v-for="(keyword,i) in item.keywords" :key="i" class="input-item" style="flex: 1;">
                          <el-input v-model="keyword.value" :placeholder="`请输入${keyword.fieldDesc}`" />
                        </div>
                      </div>
                    </div>
                    <div class="tag-container">
                      <div v-for="(tag, i) in item.tagValue" :key="i" class="tag-item">
                        {{ tag }}
                        <i class="el-icon-circle-close icon-pd-10" @click="removeTag(i,index)" />
                      </div>
                    </div>

                  </el-form-item>
                </div>
                <div class="flex align-center">
                  <el-button circle class="search-button" :disabled="searchGroup.length === 1" plain icon="el-icon-minus" @click="decreaseKeyType(index)" />
                  <el-button circle class="search-button" :disabled="searchGroup.length === 10" plain icon="el-icon-plus" @click="addKeyType" />
                </div>
              </div>
              <el-form-item>
                <p style="color: #999;margin-left:110px;line-height: 1;">基于关键词的精准查询，输入关键词后Enter即可。</p>
              </el-form-item>
            </div>
            <el-button v-if="hasSearchPermission" style="margin-top: 12px;" type="primary" class="query-button" @click="next">查询<i class="el-icon-search el-icon--right" /></el-button>
          </div>
        </el-form>
        <el-dialog
          title="选择资源"
          :visible.sync="dialogVisible"
          top="10px"
          class="dialog"
          width="800px"
          :before-close="handleDialogCancel"
        >
          <div class="dialog-body">
            <div class="search-input">
              <el-input
                v-model="searchKeyword"
                placeholder="请输入内容"
                class="search"
                @change="handleSearchNameChange"
                @keyup.enter.native="searchResource"
              >
                <el-button slot="append" icon="el-icon-search" @click="searchResource" />
              </el-input>
            </div>
            <ResourceTableSingleSelect max-height="560" :data="resourceList" :show-status="false" :selected-data="selectResources && selectResources.resourceId" @change="handleResourceChange" />
          </div>
          <div class="dialog-footer flex align-items-center" :class="{'justify-content-between': pageCount>1,'justify-content-center': pageCount<=1}">
            <pagination v-show="pageCount>1" :limit.sync="pageSize" :page.sync="pageNo" :page-count="pageCount" :total="total" layout="total, prev, pager, next" @pagination="handlePagination" />
            <div>
              <el-button @click="handleDialogCancel">取 消</el-button>
              <el-button type="primary" @click="handleDialogSubmit">确 定</el-button>
            </div>
          </div>

        </el-dialog>
      </div>
    </div>

  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { pirSubmitTask } from '@/api/PIR'
import { getAvailableOrganList } from '@/api/center'
import { getResourceList } from '@/api/fusionResource'
import ResourceItemSimple from '@/components/ResourceItemSimple'
import ResourceTableSingleSelect from '@/components/ResourceTableSingleSelect'
import Pagination from '@/components/Pagination'

export default {
  components: {
    ResourceItemSimple,
    ResourceTableSingleSelect,
    Pagination
  },
  data() {
    return {
      resourceList: [],
      searchKeyword: '',
      active: 0,
      pirParam: 0,
      taskId: 0,
      taskDate: 0,
      organList: [],
      dialogVisible: false,
      resourceName: '请选择机构下资源',
      form: {
        organId: '',
        resourceName: '',
        pirParam: '',
        selectResources: null,
        taskName: '',
        keyQuerys: []
      },
      searchGroup: [{
        key: [],
        keywords: [{ fieldDesc: '关键词', value: '' }],
        query: [],
        tagValue: []
      }],
      primaryKeysOptions: [],
      selectResources: null, // selected resource id list
      total: 0,
      currentPage: 1,
      pageNo: 1,
      pageSize: 5,
      pageCount: 0,
      rules: {
        taskName: [
          { required: true, message: '请输入任务名称', trigger: 'blur' }
        ],
        organId: [
          { required: true, message: '请选择机构', trigger: 'change' }
        ],
        resourceName: [
          { required: true, message: '请选择机构下资源', trigger: 'blur' }
        ],
        // key: [
        //   { required: true, message: '请选择查询主键', trigger: 'blur' }
        // ],
        // query: [
        //   { required: true, message: '请选择查询主键', trigger: 'blur' }
        // ],
        pirParam: [
          { required: true, message: '请输入关键词', trigger: 'blur' },
          { max: 50, message: '长度在50个字符以内' }
        ]
      }
    }
  },
  computed: {
    hasSearchPermission() {
      return this.buttonPermissionList.includes('PrivateSearchButton')
    },
    ...mapGetters([
      'buttonPermissionList'
    ])
  },
  async created() {
    await this.getAvailableOrganList()
  },
  methods: {
    removeTag(tagIndex, groupIndex) {
      console.log('tagIndex', tagIndex, 'groupIndex', groupIndex)
      console.log(this.searchGroup[groupIndex].query)
      this.searchGroup[groupIndex].tagValue.splice(tagIndex, 1)
      this.searchGroup[groupIndex].query.splice(tagIndex, 1)
    },
    handleKeyChange(value, index) {
      if (this.searchGroup[index].key.length <= 3) {
        const keywords = []
        for (let i = 0; i < value.length; i++) {
          const item = value[i]
          const current = this.primaryKeysOptions.find(val => val.fieldName === item)
          keywords.push({
            fieldDesc: current.fieldDesc || '关键词',
            value: ''
          })
          this.searchGroup[index].keywords = keywords
        }
      } else {
        this.$message.error('查询主键最多可选择3个')
        this.searchGroup[index].key.pop()
      }
    },
    addKeyType() {
      this.searchGroup.push({
        key: [],
        keywords: [{ fieldDesc: '关键词', value: '' }],
        query: [],
        tagValue: []
      })
    },
    decreaseKeyType(index) {
      this.searchGroup.splice(index, 1)
    },
    handleKeyInputConfirm(index) {
      const key = this.searchGroup[index].key
      const keywordValue = []
      this.searchGroup[index].keywords.map((keyword) => {
        if (keyword.value !== '') {
          keywordValue.push(keyword.value)
        }
      })
      if (keywordValue.length === 0) {
        this.$message({
          message: '请输入查询关键词',
          type: 'error'
        })
        return
      }
      if (keywordValue.length !== key.length) {
        this.$message.error('查询主键需跟关键词数量保持一致，请核验')
        return
      } else {
        this.searchGroup[index].query.push(keywordValue)
        this.searchGroup[index].tagValue.push(keywordValue.join(','))
        this.searchGroup[index].keywords.map(item => {
          item.value = ''
        })
      }
    },
    handleDelete() {
      this.form.resourceId = ''
      this.form.resourceName = ''
      this.resourceName = ''
      this.selectResources = null
      this.form.selectResources = null
    },
    handleDialogSubmit() {
      if (this.selectResources) {
        this.form.resourceName = this.selectResources.resourceName
        this.resourceName = this.selectResources.resourceName
        this.form.selectResources = this.selectResources
        this.primaryKeysOptions = this.selectResources.fieldList
        this.dialogVisible = false
      } else {
        this.$message({
          message: '请选择资源',
          type: 'warning'
        })
      }
    },
    async handlePagination(data) {
      this.pageNo = data.page
      await this.getResourceList()
    },
    async getResourceList() {
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        organId: this.form.organId,
        resourceName: this.searchKeyword
      }
      const { code, result } = await getResourceList(params)
      if (code === 0) {
        const { data, total, totalPage } = result
        this.total = total
        this.pageCount = totalPage
        this.resourceList = data
      }
    },
    handleSearchNameChange(searchName) {
      console.log(searchName)
      this.form.resourceName = searchName
    },
    handleDialogCancel() {
      this.dialogVisible = false
      this.searchKeyword = ''
      this.pageNo = 1
      if (!this.selectResources) {
        this.form.resourceName = ''
      }
    },
    searchResource() {
      this.pageNo = 1
      this.getResourceList()
    },
    handleResourceChange(data) {
      console.log('handleResourceChange', data)
      this.selectResources = data
    },
    async openDialog() {
      if (this.form.organId) {
        await this.getResourceList()
        this.dialogVisible = true
      } else {
        this.$refs.form.validateField('organId')
      }
    },
    async getAvailableOrganList() {
      this.organLoading = true
      const { result } = await getAvailableOrganList()
      this.organList = result
      this.organLoading = false
    },
    handleOrganChange(value) {
      this.organName = this.organList.find(item => item.globalId === value)?.globalName
      this.form.resourceName = ''
      this.form.selectResources = null
    },
    next() {
      console.log('searchGroup', this.searchGroup)
      if (!this.selectResources) {
        this.$message({
          message: '请选择资源',
          type: 'error'
        })
        return
      }
      if (this.searchGroup.find(item => item.query.length === 0)) {
        for (let i = 0; i < this.searchGroup.length; i++) {
          this.handleKeyInputConfirm(i)
        }
      }
      this.form.keyQuerys = this.searchGroup.map(item => {
        return {
          key: item.key,
          query: item.query
        }
      })
      console.log('form===>', this.form)
      this.$refs.form.validate(valid => {
        if (valid) {
          this.listLoading = true
          pirSubmitTask({
            param: {
              keyQuerys: this.form.keyQuerys,
              resourceId: this.selectResources.resourceId,
              taskName: this.form.taskName
            }
          }).then(res => {
            if (res.code === 0) {
              this.listLoading = false
              this.taskId = res.result.taskId
              this.$emit('next', this.taskId)
              this.toTaskDetailPage(this.taskId)
            } else {
              this.$message({
                message: res.msg,
                type: 'error'
              })
              this.listLoading = false
            }
          }).catch(err => {
            console.log(err)
            this.listLoading = false
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    toTaskDetailPage(id) {
      this.$router.push({
        name: 'PIRDetail',
        params: { id }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/variables.scss";
::v-deep .el-form-item__error{
  left: 110px;
}
::v-deep .el-form-item__label{
  width: 110px;
}
::v-deep .el-select .el-tag{
  position: relative;
  background-color: transparent;
  border: none;
  flex: 1;
  margin: 0;
  // justify-content: center;
  // padding: 0 30px;
  .el-select__tags-text{
    padding-left: 10px;
  }
  &::after{
    position: absolute;
    right: 0px;
    content: '';
    display: inline-block;
    width: 1px;
    height: 100%;
    background-color: #fcdffc;
  }
  &:last-child{
    &::after{
      width: 0;
    }
  }
}
.steps{
  background-color: #fff;
  padding:50px;
}
::v-deep .el-dialog__body{
  padding: 10px 20px;
}
.custom-input{
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 4px;
  border: 1px solid #DCDFE6;
  color: #C0C4CC;
  line-height: 40px;
  height: 40px;
  padding: 0 15px;
  cursor: pointer;
  &.disabled{
    background-color: #F5F7FA;
  }
  .resource-name{
    overflow: hidden;
    width: 100%;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
.dialog-body{
  min-height: 200px;
}
.search-input{
  width: 300px;
}
.search-area {
  margin: 20px auto;
  width: 755px;
  text-align: center;
}
.query-button {
  width: 200px;
  margin: 0 auto;
}
.select-row{
  display: flex;
  justify-content: flex-start;
  margin-bottom: 10px;
}
.dialog-con{
  text-align: left;
  .popover-container{
    position: absolute;
    top: 12px;
    right: -22px;
    font-size: 16px;
    line-height: 1;
  }
}
.no-data{
  color: #999;
  margin: 0 auto;
  text-align: center;
}
.dialog{
  text-align: left;
}
.dialog-footer{
  width: 100%;
  height: 50px;
  margin-bottom: 30px;
}
::v-deep .el-form-item__content{
  text-align: left;
}
.pagination-container {
  display: flex;
  justify-content: center;
}
.select-item{
  margin-left: 110px;
}

.label{
  width: 110px;
  text-align: right;
  vertical-align: middle;
  font-size: 14px;
  color: #666;
  line-height: 40px;
  padding: 0 12px 0 0;
  font-weight: bold;
  &::before{
    content: '*';
    color: #f56c6c;
    margin-right: 4px;
    font-size: 14px;
  }
}
.group{
  // background-color: #fcfcfc;
  // border-radius: 4px;
  padding: 20px 20px 0 0;
  border-top: 1px solid #dcdfe6;
  border-bottom: 1px solid #dcdfe6;
  &-item{
    flex: 1;

  }
}
.option-item{
  display: flex;
  justify-content: space-between;
}
.option-label{
  max-width: 100%;
  // flex-shrink: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.option-desc{
  width: 80px;
  color: #999;
  margin-left: 10px;
}
.search-button {
  background-color: #f6f6f6;
  border-radius: 6px;
  padding: 0px 15px;
  height: 40px;
  line-height: 40px;
  margin-left: 10px;
}
.query-input{
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding-right: 30px;
  height: 40px;
  width: calc(100% - 110px);
  flex: 1;
  .input-item{
    position: relative;
    box-sizing: border-box;
    &::after{
      position: absolute;
      right: 0px;
      top: 50%;
      transform: translate3d(0,-50%,0);
      content: '';
      display: inline-block;
      width: 1px;
      height: 20px;
      background-color: #fcdffc;
      z-index: 10;
    }
    &:last-child{
      &::after{
        width: 0;
      }
    }
  }
  ::v-deep .el-input__inner{
    border: none;
    // padding: 0 30px;
    height: 35px;
  }
}
.tag-container {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  margin-left: 110px;
  .tag-item {
    margin-top: 10px;
    height: 40px;
    line-height: 40px;
    padding: 3px 16px 3px 16px;
    background-color: #F6F6F6;
    margin-right: 10px;
    color: #666;
    border-radius: 5px;
    display: inline-flex;
    align-items: center;
    user-select: none;
    &:last-child {
      margin-right: 0px;
    }
    &:hover{
      color: #333;
    }
    .icon-pd-10 {
      cursor: pointer;
      padding-left: 8px;
      transition: all .3s;
    }
  }
}
</style>
