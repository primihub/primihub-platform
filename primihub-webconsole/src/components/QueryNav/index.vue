<template>
  <div class="resource-nav">
    <div class="resource-nav-row">
      <span class="resource-nav-title">分类：</span>
      <div class="resource-nav-item">
        <span v-for="classItem in sortTypeOptions" :key="classItem.key" :class="{'active' : sortTypeIndex === classItem.key}" @click="handleNavClick('sortType',classItem.key)">
          {{ classItem.label }}
        </span>
      </div>
    </div>
    <div class="resource-nav-row">
      <span class="resource-nav-title">授权类型：</span>
      <div class="resource-nav-item">
        <span v-for="typeItem in authTypeOptions" :key="typeItem.key" :class="{'active' :authTypeIndex === typeItem.key}" @click="handleNavClick('authType',typeItem.key)">
          {{ typeItem.label }}
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'QueryNav',
  data() {
    return {
      sortTypeOptions: [ // 1.银行 2.电商 3.媒体 4.运营商 5.保险
        {
          label: '全部',
          key: 0
        },
        {
          label: '银行',
          key: 1
        },
        {
          label: '电商',
          key: 2
        },
        {
          label: '媒体',
          key: 3
        },
        {
          label: '运营商',
          key: 4
        },
        {
          label: '保险',
          key: 5
        }
      ],
      authTypeOptions: [ // 授权方式
        {
          label: '全部',
          key: 0
        },
        {
          label: '公开',
          key: 1
        },
        {
          label: '私有',
          key: 2
        }
      ],
      sortTypeIndex: 0, // 分类筛选选中index
      authTypeIndex: 0 // 授权类型选中index
    }
  },
  methods: {
    handleNavClick(name, key) {
      if (name === 'sortType') {
        this.sortTypeIndex = key
      } else if (name === 'authType') {
        this.authTypeIndex = key
      }
      this.$emit('click', {
        name,
        key
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.resource{
    &-nav{
      padding: 10px 30px 30px;
      font-size: 14px;
      background-color: #fff;
      &-row{
        height: 30px;
        line-height: 1;
      }
      &-title{
          display: inline-block;
          width: 70px;
          text-align: right;
          margin-right: 10px;
        }
      &-item{
        display: inline-block;
        span{
          cursor: pointer;
          margin: 10px;
          padding: 2px 3px;
          display: inline-block;
          &.active,&:hover{
            background-color:#1677FF;
            color: #fff;
            border-radius: 3px;
          }
        }
      }
    }
  }
</style>
