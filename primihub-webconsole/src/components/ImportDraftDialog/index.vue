<template>
  <el-dialog
    :before-close="closeDialog"
    v-bind="$attrs"
    width="1000px"
  >
    <div slot="title" class="dialog-header">
      <strong>导入草稿</strong>
      <span class="tip">若使用草稿将清空当前画布，替换为所选草稿</span>
    </div>
    <div v-loading="loading" class="dialog-container">
      <template v-if="noData">
        <no-data />
      </template>
      <template v-else>
        <div v-for="(item,index) in data" :key="index" :span="6" class="item">
          <div class="card" :class="{'selected': item.checked}" @click="handleSelect(item)">
            <div class="check">
              <el-checkbox v-model="item.checked" @change="handleSelect(item)" />
            </div>
            <div class="button" @click="deleteDraft(item)">
              <div class="delete-icon"><i class="el-icon-more" /></div>
              <el-button v-if="item.showDeleteButton" type="primary" size="mini" class="delete-button" plain @click.stop="handleDeleteDraft(item)">删除草稿</el-button>
            </div>
            <img :src="item.componentImage" class="image">
            <div class="info">
              <div class="info-name">{{ item.draftName }}</div>
              <div class="time">{{ item.updateDate }}保存</div>
            </div>
          </div>
        </div>
      </template>

    </div>

    <span slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">取 消</el-button>
      <el-button type="primary" @click="handleSubmit">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { getComponentDraftList, deleteComponentDraft } from '@/api/model'
import NoData from '@/components/NoData'

export default {
  name: 'ImportDraftDialog',
  components: {
    NoData
  },
  data() {
    return {
      loading: false,
      data: [],
      selectedDraft: null,
      noData: false
    }
  },
  computed: {
    visible() {
      return this.$attrs.visible
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.fetchData()
      }
    }
  },
  methods: {
    fetchData() {
      const params = {
        pageSize: this.pageSize
      }
      console.log('selectedDraft', this.selectedDraft)
      this.loading = true
      getComponentDraftList(params).then(res => {
        if (res.result.length > 0) {
          this.noData = false
          if (!this.selectedDraft) {
            this.data = res.result.map(item => {
              item.checked = false
              item.showDeleteButton = false
              return item
            })
          } else {
            this.data = res.result.map(item => {
              if (item.draftId === this.selectedDraft.draftId) {
                item.checked = true
              } else {
                item.checked = false
              }
              item.showDeleteButton = false
              return item
            })
          }
          this.loading = false
        } else {
          this.noData = true
          this.loading = false
        }

        console.log(this.data)
      }).catch(err => {
        console.log(err)
        this.loading = false
      })
    },
    closeDialog() {
      this.data = this.data.map(item => {
        item.checked = false
        item.showDeleteButton = false
        return item
      })
      this.$emit('close')
    },
    handleSubmit() {
      this.selectedDraft = this.data.find(item => item.checked === true)
      console.log('selectedDraft', this.selectedDraft)
      if (!this.selectedDraft) {
        this.$message({
          message: '请选择草稿',
          type: 'warning'
        })
        return
      }
      this.$confirm('若使用草稿将清空当前画布，替换为所选草稿, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$emit('submit', this.selectedDraft)
      }).catch(() => {
        this.listLoading = false
      })
    },
    handleSelect(draft) {
      draft.checked = !draft.checked
      const other = this.data.filter(item => item.draftId !== draft.draftId)
      other.map(item => {
        item.checked = false
      })
    },
    deleteDraft(draft) {
      draft.showDeleteButton = !draft.showDeleteButton
    },
    handleDeleteDraft(draft) {
      this.loading = true
      deleteComponentDraft({ draftId: draft.draftId }).then(res => {
        if (res.code === 0) {
          const posIndex = this.data.findIndex(item => item.draftId === draft.draftId)
          console.log(posIndex)
          this.data.splice(posIndex, 1)
          this.$message({
            message: '删除成功',
            type: 'success'
          })
          setTimeout(() => {
            this.loading = false
          }, 200)
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-dialog__body{
  padding: 10px 20px 20px 20px;
}
.card{
  position: relative;
  border-radius: 4px;
  border: 1px solid rgba(233, 233, 233, 1);
  overflow: hidden;
  &.selected{
    border: 1px solid #1677FF;
  }
  .check{
    width: 50px;
    height: 50px;
    position: absolute;
    top: 10px;
    left: 10px;
  }
}
.info{
  padding: 20px 15px;
}
.info-name{
  width: 100%;
  margin-bottom: 5px;
  display: inline-block;
  color: #333;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.dialog-container{
  padding: 0px 10px;
  overflow-y: scroll;
  width: 100%;
  max-height: 460px;
  display: flex;
  flex-wrap: wrap;
 align-items:flex-start;
  .item{
    width: 25%;
    padding: 8px;
  }
}
.time {
  font-size: 13px;
  color: #999;
}
.delete-icon{
  padding: 5px 1px;
  border: 1px solid rgba(0,0,0,.1);
  border-radius: 2px;
  width: 10px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}
.button {
  position: absolute;
  right: 10px;
  top: 10px;
  i{
    transform: rotate(90deg);
    font-size: 12px;
  }
  .delete-button{
    position: absolute;
    right: 0;
  }
}

.image {
  width: 100%;
  display: block;
}

.clearfix:before,
.clearfix:after {
    display: table;
    content: "";
}

.clearfix:after {
    clear: both
}
.el-col{
  margin-bottom: 15px;
}
.dialog-header{
  padding: 20px 15px 0px;
  .tip{
    font-size: 12px;
    color: rgba(0, 0, 0, .4);
    margin: 0 20px;
  }
}
.dialog-footer{
  display: flex;
  justify-content: center;
  padding-bottom: 20px;
}
</style>
