<script>
import YyText from '@/components/YyDescription'
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
        <yy-text label="项目中文名称：">{{ data.projectName }}</yy-text>
        <yy-text label="创建时间：">{{ data.createDate }}</yy-text>
        <yy-text label="项目角色：">{{ data.creator ? '项目发起方' : '项目协作方' }}</yy-text>
        <yy-text label="项目描述：">{{ data.projectDesc }}</yy-text>
      </el-col>
      <el-col :span="12">
        <yy-text label="项目英文名称：">{{ data.projectName }}</yy-text>
        <yy-text label="项目类型："> {{ data.projectName }} </yy-text>
        <yy-text label="项目ID：">{{ data.projectId }}</yy-text>
        <yy-text label="合约描述：">{{ data.projectName }}</yy-text>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
.resource-detail-info{
  padding: 20px 0;
}
</style>
