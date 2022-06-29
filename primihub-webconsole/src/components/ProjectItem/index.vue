<template>
  <div class="item" @click="toProjectDetail">
    <div class="header">
      <p><el-tag :type="statusStyle(project.status)" size="medium">{{ project.status | projectAuditStatusFilter }}</el-tag></p>
      <p class="header-title">{{ project.projectName }}</p>
    </div>
    <div class="main">
      <div class="text"><span>发起方：</span><span>{{ project.createdOrganName }}</span></div>
      <div class="text"><span>协作方：</span><span>{{ project.providerOrganNames }}</span></div>
      <div class="text"><span>资源数量：</span><span>{{ project.resourceNum }}个</span></div>
      <div class="text"><span>模型数量：</span><span>{{ project.modelNum }}个</span></div>
      <div class="text"><span>创建时间：</span><span>{{ project.createDate }}</span></div>
    </div>
    <div class="footer">
      <div class="model-status text">
        <span>{{ project.modelAssembleNum }}</span>
        <span>配置中 <i class="el-icon-setting icon-1" /></span>
      </div>
      <div class="model-status text">
        <span>{{ project.modelRunNum }}</span>
        <span>运行中 <i class="el-icon-refresh icon-2" /></span>
      </div>
      <div class="model-status text">
        <span>{{ project.modelSuccessNum }}</span>
        <span>成功 <i class="el-icon-circle-check icon-3" /></span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProjectItem',
  props: {
    isActive: {
      type: Boolean,
      default: false
    },
    project: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  methods: {
    toProjectDetail() {
      this.$router.push({
        name: 'ProjectDetail',
        params: { id: this.project.id }
      })
    },
    toggleClick() {
      this.$emit('toggleClick')
    },
    statusStyle(status) {
      return status === 0 ? 'primary' : status === 1 ? 'success' : status === 2 ? 'danger' : 'primary'
    }
  }
}
</script>

<style lang="scss" scoped>
@import "../../styles/variables.scss";
p {
  margin-block-start:0;
  margin-block-end:0;
}
.item {
  width: 285px;
  box-sizing: border-box;
  padding: 10px 0 0 0;
  border-radius: 10px;
  font-size: 14px;
  background: #fff;
  margin: 10px;
  color: rgba(0,0,0,0.85);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  &:hover {
    box-shadow: 2px 4px 8px rgba(0,0,0,.05);
  }
}
.header {
  font-size: 16px;
  color: rgba(0,0,0,.85);
  padding: 5px 20px;
  &-title{
    padding: 0 5px 5px;
    margin-top: 10px;
    line-height: 1.5;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    width: 100%;
    color: rgba(3, 10, 46, 0.85);
    border-bottom: $borderColor solid 1px;
  }
}
.main {
  flex-grow: 1;
  line-height: 20px;
  font-size: 12px;
  margin: 5px 20px 10px 20px;
  padding: 0 5px;
  color: rgba(0,0,0,.65);
}
.footer {
  display: flex;
  justify-content: space-evenly;
  align-items: center;
  flex-shrink: 0;
  height: 60px;
  padding: 0 15px;
  font-size: 12px;
  background-color: rgba(242, 245, 255, 0.75);
}
.model-status {
  margin: 0;
  display: flex;
  justify-content: center;
  flex-direction: column;
  text-align: center;
  border-right: 1px solid $borderColor;
  flex: 1;
  &:last-child{
    border: none;
  }
}
.icon-1 {
  color: rgb(144, 147, 153);
}
.icon-2 {
  color: rgb(64, 158, 255);
}
.icon-3 {
  color: rgb(103, 194, 58);
}
.text {
  font-size: 13px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}
</style>
