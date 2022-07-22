<template>
  <div class="poster-inner">
    <img src="/images/logo-text.png" alt="">
    <p class="slogan">致力于以隐私计算技术构建数据信任与安全</p>
  </div>
</template>

<script>
export default {
  name: 'Poster'
}
</script>

<style lang="scss" scoped>

.poster-inner{
  img {
    display: block;
    margin-left: auto;
    margin-right: auto;
    width: 220px;
  }
  .slogan {
    font-size: 22px;
    color: rgba(0, 0, 0, 0.45);
    margin: 10px 0;
  }
}
</style>
