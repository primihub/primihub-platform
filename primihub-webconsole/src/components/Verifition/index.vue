<template>
  <div v-if="showBox" class="mask">
    <div :class="mode=='pop'?'verifybox':''" :style="{'width':parseInt(imgSize.width)+30+'px'}">
      <div v-if="mode=='pop'" class="verifybox-top">
        请完成安全验证
        <span v-if="showClose" class="verifybox-close" @click="closeBox">
          <i class="iconfont icon-close" />
        </span>
      </div>
      <div class="verifybox-bottom">
        <!-- 验证码容器 -->
        <VerifySlide
          v-if="showBox"
          ref="instance"
          :mode="mode"
          :img-size="imgSize"
          :block-size="blockSize"
          :bar-size="barSize"
          :default-img="defaultImg"
        />
      </div>
    </div>
  </div>
</template>
<script>
import VerifySlide from './Verify/VerifySlide'

export default {
  name: 'Vue2Verify',
  components: {
    VerifySlide
  },
  props: {
    type: {
      type: String,
      default: ''
    },
    mode: {
      type: String,
      default: 'pop'
    },
    explain: {
      type: String,
      default: ''
    },
    imgSize: {
      type: Object,
      default() {
        return {
          width: '310px',
          height: '155px'
        }
      }
    },
    blockSize: {
      type: Object,
      default: () => {
        return {
          width: '40px',
          height: '40px'
        }
      }
    },
    barSize: {
      type: Object,
      default: () => {}
    },
    showClose: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      // showBox:true,
      clickShow: false,
      // 默认图片
      defaultImg: require('@/assets/default.jpg')
    }
  },
  computed: {
    instance() {
      return this.$refs.instance || {}
    },
    showBox() {
      if (this.mode === 'pop') {
        return this.clickShow
      } else {
        return true
      }
    }
  },
  methods: {
    /**
     * refresh
     * @description 刷新
     * */
    refresh() {
      if (this.instance.refresh) {
        this.instance.refresh()
      }
    },
    closeBox() {
      this.clickShow = false
      this.$emit('close')
      // this.refresh()
    },
    show() {
      if (this.mode === 'pop') {
        this.clickShow = true
      }
    }
  }
}
</script>
<style>
.verifybox{
    position: relative;
    box-sizing: border-box;
    border-radius: 2px;
    border: 1px solid #e4e7eb;
    background-color: #fff;
    box-shadow: 0 0 10px rgba(0,0,0,.3);
    left: 50%;
    top:50%;
    transform: translate(-50%,-50%);
}
.verifybox-top{
    padding: 0 15px;
    height: 50px;
    line-height: 50px;
    text-align: left;
    font-size: 16px;
    color: #45494c;
    border-bottom: 1px solid #e4e7eb;
    box-sizing: border-box;
}
.verifybox-bottom{
    padding: 15px;
    box-sizing: border-box;
}
.verifybox-close{
    position: absolute;
    top: 0;
    right: 9px;
    width: 24px;
    height: 24px;
    text-align: center;
    cursor: pointer;
}
.mask{
    position: fixed;
    top: 0;
    left:0;
    z-index: 1001;
    width: 100%;
    height: 100vh;
    background: rgba(0,0,0,.3);
    /* display: none; */
    transition: all .5s;
}
.verify-tips{
    position: absolute;
    left: 0px;
    bottom:-2px;
    width: 100%;
    height: 30px;
    line-height:30px;
    color: #fff;
    font-size: 14px;
}
.suc-bg{
    background:rgb(103, 194, 58, .7)
}
.err-bg{
    background:rgb(245, 108, 108,.7)
}
.tips-enter,.tips-leave-to{
    bottom: -30px;
}
.tips-enter-active,.tips-leave-active{
    transition: bottom .5s;
}
/* ---------------------------- */
/*常规验证码*/
.verify-code {
    font-size: 20px;
    text-align: center;
    cursor: pointer;
    margin-bottom: 5px;
    border: 1px solid #ddd;
}

.cerify-code-panel {
    height: 100%;
    overflow: hidden;
}

.verify-code-area {
    float: left;
}

.verify-input-area {
    float: left;
    width: 60%;
    padding-right: 10px;

}

.verify-change-area {
    line-height: 30px;
    float: left;
}

.varify-input-code {
    display: inline-block;
    width: 100%;
    height: 25px;
}

.verify-change-code {
    color: #337AB7;
    cursor: pointer;
}

.verify-btn {
    width: 200px;
    height: 30px;
    background-color: #337AB7;
    color: #FFFFFF;
    border: none;
    margin-top: 10px;
}

/*滑动验证码*/
.verify-bar-area {
    position: relative;
    background: #f8faff;
    text-align: center;
    box-sizing: content-box;
    border: 1px solid #ddd;
    border-radius: 4px;
    width: 100%;
}

.verify-bar-area .verify-move-block {
    position: absolute;
    top: 0px;
    left: 0px;
    background: #f8faff;
    cursor: pointer;
    border-radius: 1px;
    box-shadow: 0 0 2px #888888;
}

.verify-bar-area .verify-move-block:hover {
    background-color: #1677FF;
    color: #FFFFFF;
}

.verify-bar-area .verify-left-bar {
    position: absolute;
    top: -1px;
    left: -1px;
    background: #f8faff;
    cursor: pointer;
    border: 1px solid #ddd;
    box-sizing: content-box;
}

.verify-img-panel {
    margin-bottom: 10px;
    border-top: 1px solid #ddd;
    border-bottom: 1px solid #ddd;
    border-radius: 3px;
    position: relative;
}

.verify-img-panel .verify-refresh {
    height: 25px;
    text-align: center;
    padding: 5px;
    cursor: pointer;
    position: absolute;
    top: 0;
    right: 5px;
    z-index: 2;
    font-size: 16px;
}

.verify-img-panel .icon-refresh {
    font-size: 20px;
    color: #fff;
}

.verify-img-panel .verify-gap {
    background-color: #fff;
    position: relative;
    z-index: 2;
    border: 1px solid #fff;
}

.verify-bar-area .verify-move-block .verify-sub-block {
    position: absolute;
    text-align: center;
    z-index: 3;
    /* border: 1px solid #fff; */
}

.verify-bar-area .verify-move-block .verify-icon {
    font-size: 18px;
}

.verify-bar-area .verify-msg {
    z-index: 3;
    width: 100%;
}

</style>
