<template>
  <div class="container">
    <div class="search-area">
      <el-form :model="query" label-width="100px" :inline="true" @keyup.enter.native="search">
        <el-form-item>
          <el-select v-model="query.organId" placeholder="请选择机构" clearable @clear="handleClear">
            <el-option
              v-for="item in organList"
              :key="item.globalId"
              :label="item.globalName"
              :value="item.globalId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="query.resourceName" placeholder="请输入资源名称" clearable @clear="handleClear" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="query.tagName" placeholder="请输入标签" clearable @clear="handleClear" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" class="search-button" @click="search">查询</el-button>
          <el-button icon="el-icon-refresh-right" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="resource">
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane v-if="!$store.getters.isOrganAdmin" label="机构内资源" name="0">
          <el-table
            v-loading="loading"
            :data="resourceList"
            border
          >
            <el-table-column
              prop="resourceName"
              label="资源名称"
            />
            <el-table-column
              prop="organName"
              label="资源所属机构"
              min-width="120"
            />
            <el-table-column
              label="资源描述"
              prop="resourceDesc"
              min-width="100"
            />
            <el-table-column
              label="可见性"
              prop="resourceAuthType"
              min-width="100"
            />
            <el-table-column
              prop="tags"
              label="标签"
            >
              <template slot-scope="{row}">
                <el-tag v-for="tag in row.tags" :key="tag.tagId" type="success" size="mini" class="tag">{{ tag.tagName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column
              label="数据信息"
              min-width="160"
            >
              <template slot-scope="{row}">
                特征量：{{ row.fileColumns }}<br>
                样本量：{{ row.fileRows }} <br>
                正例样本数量：{{ row.fileYRows || 0 }}<br>
                正例样本比例：{{ row.fileYRatio || 0 }}%<br>
                <el-tag v-if="row.fileContainsY" class="containsy-tag" type="primary" size="mini">包含Y值</el-tag>
                <el-tag v-else class="containsy-tag" type="danger" size="mini">不包含Y值</el-tag>
              </template>
            </el-table-column>
            <el-table-column
              label="操作"
              fixed="right"
              width="160"
              align="center"
            >
              <template slot-scope="{row}">
                <el-button type="text" @click="applyResource(row)">申请授权</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="其他机构资源" name="1">
          <el-table
            v-loading="loading"
            :data="resourceList"
            border
          >
            <el-table-column
              prop="resourceName"
              label="资源名称"
            />
            <el-table-column
              prop="organName"
              label="资源所属机构"
              min-width="120"
            />
            <el-table-column
              label="资源描述"
              prop="resourceDesc"
              min-width="100"
            />
            <el-table-column
              prop="resourceTag"
              label="标签"
            >
              <template slot-scope="{row}">
                <el-tag v-for="(tag,index) in row.resourceTag" :key="index" type="success" size="mini" class="tag">{{ tag }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column
              label="数据信息"
              min-width="160"
            >
              <template slot-scope="{row}">
                特征量：{{ row.fileColumns }}<br>
                样本量：{{ row.fileRows }} <br>
                正例样本数量：{{ row.fileYRows || 0 }}<br>
                正例样本比例：{{ row.fileYRatio || 0 }}%<br>
                <el-tag v-if="row.fileContainsY" class="containsy-tag" type="primary" size="mini">包含Y值</el-tag>
                <el-tag v-else class="containsy-tag" type="danger" size="mini">不包含Y值</el-tag>
              </template>
            </el-table-column>

            <el-table-column
              label="操作"
              fixed="right"
              width="160"
              align="center"
            >
              <template slot-scope="{row}">
                <!-- <el-button type="text" @click="openDialog(row)">获取资源</el-button> -->
                <el-button type="text" @click="applyResource(row)">申请授权</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="OpenMPC" name="2">
          <el-table
            v-loading="loading"
            :data="resourceList"
            border
          >
            <el-table-column
              prop="resourceName"
              label="资源名称"
            />
            <el-table-column
              prop="organName"
              label="资源所属机构"
              min-width="120"
            />
            <el-table-column
              label="资源描述"
              prop="resourceDesc"
              min-width="100"
            />
            <el-table-column
              prop="tags"
              label="标签"
            >
              <template slot-scope="{row}">
                <el-tag v-for="tag in row.tags" :key="tag.tagId" type="success" size="mini" class="tag">{{ tag.tagName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column
              label="数据信息"
              min-width="160"
            >
              <template slot-scope="{row}">
                特征量：{{ row.fileColumns }}<br>
                样本量：{{ row.fileRows }} <br>
                正例样本数量：{{ row.fileYRows || 0 }}<br>
                正例样本比例：{{ row.fileYRatio || 0 }}%<br>
                <el-tag v-if="row.fileContainsY" class="containsy-tag" type="primary" size="mini">包含Y值</el-tag>
                <el-tag v-else class="containsy-tag" type="danger" size="mini">不包含Y值</el-tag>
              </template>
            </el-table-column>

            <el-table-column
              label="操作"
              fixed="right"
              width="160"
              align="center"
            >
              <template slot-scope="{row}">
                <el-button type="text" @click="openDialog(row)">获取资源</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <pagination v-show="pageCount>1" :limit.sync="pageSize" :page-count="pageCount" :page.sync="pageNo" :total="total" @pagination="handlePagination" />
    </div>

  </div>
</template>

<script>
import { getDataResourceToApply, getDataSetList, saveDataResourceAssignLocal, saveDataResourceAssign } from '@/api/resource'
import { getFusionDataResourceToApply } from '@/api/fusionResource'
import { joiningPartners } from '@/api/center'
import { getAvailableOrganList } from '@/api/center'
import { getSetting } from '@/api/system'
import Pagination from '@/components/Pagination'

export default {
  components: { Pagination },
  data() {
    return {
      activeName: '',
      loading: false,
      tags: [],
      organList: [],
      query: {
        resourceId: '',
        resourceName: '',
        tagName: '',
        userName: '',
        resourceSource: '',
        organId: '',
        resourceAuthType: '',
        fileContainsY: ''
      },
      resourceSourceList: [{
        label: '文件上传',
        value: 1
      }, {
        label: '数据库导入',
        value: 2
      }],
      YValueOptions: [{
        label: '包含',
        value: 1
      }, {
        label: '不包含',
        value: 0
      }],
      resourceList: [],
      total: 0,
      pageCount: 0,
      pageNo: 1,
      pageSize: 10,
      organId: 0,
      isReset: false,
      telephoneNumber: ''
    }
  },
  async created() {
    this.activeName = this.$store.getters.isOrganAdmin ? '1' : '0'
    await this.getAvailableOrganList()
    await this.fetchData()
  },
  methods: {
    handleClick() {
      console.log(this.activeName)
      this.fetchData()
    },
    async getSetting() {
      const res = await getSetting({ configKey: 'data.OpenMPC.contact' })
      console.log(res.data)
      this.telephoneNumber = res.result
    },
    joiningPartners(publicKey, gateway) {
      this.loading = true
      const params = {
        publicKey,
        gateway
      }
      joiningPartners(params).then(res => {
        if (res.code === 0) {
          this.$message.success('连接成功')
          setTimeout(() => {
            this.$router.push({
              name: 'UnionList'
            })
          }, 300)
        } else {
          this.$message.error(res.msg)
        }
        this.loading = false
      }).catch(err => {
        console.log(err)
        this.loading = false
      })
    },
    applyResource(row) {
      console.log(row)
      if (this.activeName === '0') {
        // 申请机构内资源
        saveDataResourceAssignLocal({ resourceId: row.resourceId, organId: row.organId }).then(res => {
          if (res.code === 0) {
            this.$message.success('授权成功')
            this.fetchData()
          } else {
            this.$message.error('授权失败')
          }
        })
      } else if (this.activeName === '1') {
        saveDataResourceAssign({ resourceFusionId: row.resourceId, organId: row.organId }).then(res => {
          if (res.code === 0) {
            this.$message.success('授权成功')
            this.fetchData()
          } else {
            this.$message.success('授权失败')
          }
        })
      }
    },
    async openDialog({ releaseType = 3, nodeServer }) {
      console.log('nodeServer', JSON.parse(nodeServer))
      // releaseType 发布类型  1、官网填报，2、后台添加，3、节点上传
      if (releaseType !== 3) {
        await this.getSetting()
      }
      const messageHtmlStr = releaseType === 3 ? '<p style="font-size: 16px; font-weight:bold;">确认发起节点连接获取对方公开数据集</p> <p>向对方节点发起连接申请，获取对方全部公开数据集</p>' : `<p style="font-size: 16px; font-weight:bold;">该数据集来自中间平台，请联系工作人员获取</p> <p>工作人员电话：${this.telephoneNumber}</p>`
      this.$confirm(messageHtmlStr, '获取数据集', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
        dangerouslyUseHTMLString: true
      }).then(() => {
        // if (releaseType === 3) {
        //   const { publicKey, gateway } = JSON.parse(nodeServer)
        //   this.joiningPartners(publicKey, gateway)
        // }
      }).catch(err => {
        console.log(err)
      })
    },
    handleClear() {
      this.fetchData()
    },
    reset() {
      this.isReset = true
      for (const key in this.query) {
        this.query[key] = ''
      }
      this.pageNo = 1
      this.fetchData()
    },
    async search() {
      this.pageNo = 1
      await this.fetchData()
    },
    handleChange(value, name) {
      if (name === 'user') {
        this.userValue = value
      } else if (name === 'organ') {
        this.organValue = value
      }
      console.log('value', value)
      console.log('a', name)
    },
    async fetchData() {
      console.log('1111', this.activeName)
      switch (this.activeName) {
        case '0':
          this.getDataResourceToApply()
          break
        case '1':
          this.getFusionDataResourceToApply()
          break
        case '2':
          this.getDataSetList()
          break
        default:
          this.fetchData()
          break
      }
    },
    async getDataResourceToApply() {
      this.loading = true
      this.resourceList = []
      // const otherOrganId = this.organList.map(item => item.globalId)
      const organName = this.organList.find(item => item.globalId === this.query.organId)?.globalName
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        // ownOrganId: this.$store.getters.userOrganId,
        // otherOrganId: otherOrganId.join(','),
        organName: organName || '',
        resourceName: this.query.resourceName,
        tagName: this.query.tagName
      }
      const { code, result } = await getDataResourceToApply(params)
      if (code === 0) {
        const { data, total, totalPage } = result
        this.total = total
        this.pageCount = totalPage
        if (data.length > 0) {
          this.resourceList = data
        }
      }
      this.loading = false
    },
    async getDataSetList() {
      this.loading = true
      this.resourceList = []
      const otherOrganId = this.organList.map(item => item.globalId)
      const organName = this.organList.find(item => item.globalId === this.query.organId)?.globalName
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        ownOrganId: this.$store.getters.userOrganId,
        otherOrganId: otherOrganId.join(','),
        organName: organName || '',
        resourceName: this.query.resourceName,
        tagName: this.query.tagName
      }
      const { code, result } = await getDataSetList(params)
      if (code === 0) {
        const { data, total, totalPage } = result
        this.total = total
        this.pageCount = totalPage
        if (data.length > 0) {
          this.resourceList = data
        }
      } else {
        this.resourceList = []
      }
      this.loading = false
    },
    async getFusionDataResourceToApply() {
      this.loading = true
      this.resourceList = []
      const organName = this.organList.find(item => item.globalId === this.query.organId)?.globalName
      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        organName: organName || '',
        organId: this.query.organId,
        resourceName: this.query.resourceName,
        tagName: this.query.tagName
      }
      const { code, result } = await getFusionDataResourceToApply(params)
      if (code === 0) {
        const { data, total, totalPage } = result
        this.total = total
        this.pageCount = totalPage
        if (data.length > 0) {
          this.resourceList = data
        }
      }
      this.loading = false
    },
    handlePagination(data) {
      this.pageNo = data.page
      this.fetchData()
    },
    async getAvailableOrganList() {
      const { result } = await getAvailableOrganList()
      this.organList = result
    }
  }
}
</script>
<style lang="scss" scoped>
::v-deep .el-message-box__status.el-icon-info{
  color: #E6A23C;
}
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
@import "~@/styles/resource.scss";
</style>
