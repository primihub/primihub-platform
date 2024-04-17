<script>
 export default {
  name: 'YyPart',
  props: {
    title: {
      type: String,
      default: ''
    },
    width: {
      type: String,
      default: ''
    }
  }
 }
</script>
<template>
   <div class="part">
    <div class="title-icon">
      <div class="part-title"> {{ title }} </div>
    </div>
    <div class="inner-container" >
      <div class="container-center" :style="{'max-width': width || '620px'}">
        <slot></slot>
      </div>
    </div>
   </div>
</template>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";

.title-icon{
  position: relative;
  padding-bottom: 13px;
  &:after{
    content: "";
    display: block;
    height: 1px;
    background: #eee;
    width: calc(100% - 40px);
    position: absolute;
    bottom: 0px;
    margin: 0 20px;
  }
  .part-title{
    font-size: 16px;
    margin-left: 20px;
    color: #333;
    &:before{
      content: "";
    display: inline-block;
    width: 5px;
    height: 26px;
    background-color: #1677FF;
    border-radius: 0px 5px 5px 0px;
    margin-right: 10px;
    -webkit-transform: translateY(7px);
    transform: translateY(7px);
    }
  }
}

.inner-container{
  padding: 20px 20px 0;
  margin: 30px auto;
  background: #fff;
}
.container-center{
  min-width: 320px;
  margin: 0 auto;
}
</style>

