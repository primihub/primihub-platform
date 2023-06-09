<template>
  <div class="iframe">
    <navbar />
    <iframe id="iframeContain" src="https://chat.primihub.com/" frameborder="0" />
    <fade-back />
  </div>
</template>

<script>
import Navbar from '@/components/CustomNavbar'
import FadeBack from '@/layout/components/FadeBack'

export default {
  components: {
    Navbar,
    FadeBack
  }
}
</script>

<style scoped>
::v-deep .app.svelte-ac4rv4.svelte-ac4rv4{
  width: 100%;
  max-width: 100%!important;
  margin: 0!important;
}
::-webkit-scrollbar {
  display: none;
}
.iframe{
  width: 100%;
  height: calc(100vh - 120px);
  overflow: auto;
}
#iframeContain{
  width: 100%;
  height: 100%;
  overflow: hidden;
  min-width: 100%;
}
</style>
