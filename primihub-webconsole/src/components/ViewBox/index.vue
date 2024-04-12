
<script>

export default {
  name: 'ViewBox',
  data(){
    return {
      scopedSlots: undefined
    }
  },
  mounted() {
    this.scopedSlots = this.$scopedSlots
  }
}
</script>

<template>
  <div class="yy-list-container">
    <div class="yy-list--filter" v-if="scopedSlots && scopedSlots.filter">
      <slot name="filter" />
    </div>
    <div class="yy-list--operate" v-if="scopedSlots && scopedSlots.operate">
      <slot name="operate" />
    </div>
    <div class="yy-list--list" v-if="scopedSlots && scopedSlots.list">
      <slot name="list" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.yy-list--filter, .yy-list--list{
  padding: 30px 20px 20px 20px;
  border-radius: 6px;
  background-color: #fff;
}
.yy-list--filter{
  margin-bottom: 20px;
}
.yy-list--operate{
  margin-bottom: 20px;
}
</style>
