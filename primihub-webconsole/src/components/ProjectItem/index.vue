<template>
  <div class="item" @click="toProjectDetail">
    <div class="header">
      <div class="header-title"><i class="el-icon-tickets" /> {{ project.projectName }}</div>
    </div>
    <div class="main">
      <!-- <div><span>机构数量：</span><span>{{ project.resourceOrganNum }}</span></div> -->
      <div><span>本地资源数量：</span><span>{{ project.resourceNum }}个</span></div>
      <div><span>外部资源(已授权/总数)：</span><span>{{ project.authResourceNum }}/{{ project.resourceNum }}</span></div>
      <div><span>模型数量：</span><span>{{ project.modelNum }}个</span></div>
    </div>
    <div class="footer">
      <span class="time">{{ project.createDate }}</span>
      <span class="username">{{ project.userName }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Hamburger',
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
        params: { id: this.project.projectId }
      })
    },
    toggleClick() {
      this.$emit('toggleClick')
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
.item {
  width: 260px;
  box-sizing: border-box;
  padding: 20px 20px 30px 20px;
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
  font-weight: bold;
  height: 40px;
  line-height: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  &-title{
    width: 230px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: $mainColor;
    &:hover{
      color: $mainColor;
    }
  }
}
.main {
  flex-grow: 1;
  line-height: 20px;
  font-size: 12px;
  margin-bottom: 10px;
  color: rgba(0,0,0,.85);
}
.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #e9e9e9;
  flex-shrink: 0;
  height: 40px;
  padding-top: 5px;
  padding-bottom: 5px;
  font-size: 12px;
  .username{
    width: 60px;
    line-height: 1.5;
    text-align: right;
  }
}
</style>
