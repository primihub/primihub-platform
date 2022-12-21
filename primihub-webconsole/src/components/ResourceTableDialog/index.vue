<template>
  <el-dialog
    title="添加资源"
    :before-close="closeDialog"
    v-bind="$attrs"
    width="600px"
  >
    <el-table
      ref="multipleTable"
      border
      row-key="resourceId"
      :data="data"
      @selection-change="handleSelectionChange"
    >
      <el-table-column
        v-if="multiple"
        :reserve-selection="true"
        :selectable="checkSelectable"
        type="selection"
        width="55"
      />
      <el-table-column
        label="资源名称 / Id"
      >
        <template slot-scope="{row}">
          <el-link type="primary" @click="toResourceDetailPage(row.resourceId)">{{ row.resourceName }}</el-link><br>
          {{ row.resourceId }}
        </template>
      </el-table-column>
      <el-table-column
        prop="tags"
        label="关键词"
      >
        <template slot-scope="{row}">
          <el-tag v-for="(tag,index) in row.resourceTag" :key="index" type="success" size="mini" class="tag">{{ tag }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="resourceAuthType"
        label="可见性"
        align="center"
      >
        <template slot-scope="{row}">
          {{ row.resourceAuthType | authTypeFilter }}
        </template>
      </el-table-column>
      <el-table-column
        prop="resourceSource"
        label="资源类型"
        align="center"
      >
        <template slot-scope="{row}">
          {{ row.resourceType | sourceFilter }}
        </template>
      </el-table-column>
      <el-table-column
        label="数据信息"
        min-width="200"
      >
        <template slot-scope="{row}">
          特征量：{{ row.resourceRowsCount }}<br>
          样本量：{{ row.resourceColumnCount }} <br>
          正例样本数量：{{ row.resourceYRowsCount || 0 }}<br>
          正例样本比例：{{ row.resourceYRatio || 0 }}%<br>
          <el-tag v-if="row.resourceContainsY" class="containsy-tag" type="primary" size="mini">包含Y值</el-tag>
          <el-tag v-else class="containsy-tag" type="danger" size="mini">不包含Y值</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="上传时间"
        min-width="160"
      >
        <template slot-scope="{row}">
          {{ row.createDate }}
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        fixed="right"
        width="220"
        align="center"
      >
        <template slot-scope="{row}">
          <el-button icon="el-icon-view" size="mini" type="primary" @click="handlePreview(row.resourceId)">预览</el-button>
          <el-button size="mini" icon="el-icon-delete" type="danger" @click="handleDelete(row.resourceId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <span slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">取 消</el-button>
      <el-button type="primary" @click="handleSubmit">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  props: {
    data: {
      type: Array,
      default: () => []
    },
    multiple: {
      type: Boolean,
      default: false
    },
    buttons: { // preview delete ...
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      multipleSelection: []
    }
  },
  methods: {
    handleSelectionChange(value) {
      if (!this.multiple) return
      this.multipleSelection = value
    },
    closeDialog() {
      this.$emit('close')
    },
    handleSubmit() {
      this.$emit('submit', this.multipleSelection)
    }
  }
}
</script>
<style lang="scss" scoped>

</style>
