<script>
import YyText from './text.vue'
export default {
  name: 'ResourceDetailInfo',
  components: { YyText },
  props: {
    data: {
      type: Object,
      default: () => {}
    }
  }
}
</script>

<template>
  <div class="resource-detail-info">
    <el-row>
      <el-col :span="12">
        <yy-text label="资源中文名称：">{{ data.resourceName }}</yy-text>
        <yy-text label="资源描述：">{{ data.resourceDesc }}</yy-text>
        <yy-text label="更新周期：">{{ data.resourceName }}</yy-text>
        <yy-text label="数据目录：">{{ data.resourceName }}</yy-text>
      </el-col>
      <el-col :span="12">
        <yy-text label="英文名称：">{{ data.resourceName }}</yy-text>
        <yy-text label="数据集标签：">
          <template v-if="data.tags">
            <el-tag v-for="tag in data.tags" :key="tag.tagId" type="primary" size="mini">{{ tag.tagName }}</el-tag>
          </template>
          <template v-if="data.resourceTag">
            <el-tag v-for="(tag, index) in data.resourceTag" :key="index" type="primary" size="mini">{{ tag }}</el-tag>
          </template>
        </yy-text>
        <yy-text label="应用场景：">{{ data.resourceName }}</yy-text>
        <yy-text label="数据集主题：">{{ data.resourceName }}</yy-text>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
.resource-detail-info{
  padding: 20px 0;
}
</style>
