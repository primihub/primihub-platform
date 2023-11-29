<template>
  <div v-loading="listLoading" style="position: relative;">
    <div
      class="verify-img-out"
      :style="{height: (parseInt(setSize.imgHeight) + vSpace) + 'px'}"
    >
      <div
        class="verify-img-panel"
        :style="{width: setSize.imgWidth,
                 height: setSize.imgHeight,}"
      >
        <img :src="backImgBase?('data:image/png;base64,'+backImgBase):defaultImg" alt="" style="width:100%;height:100%;display:block">
        <div v-show="showRefresh" class="verify-refresh" @click="refresh"><i class="iconfont icon-refresh" />
        </div>
        <transition name="tips">
          <span v-if="tipWords" class="verify-tips" :class="passFlag ?'suc-bg':'err-bg'">{{ tipWords }}</span>
        </transition>
      </div>
    </div>
    <!-- 公共部分 -->
    <div
      class="verify-bar-area"
      :style="{width: setSize.imgWidth,
               height: barSize.height,
               'line-height':barSize.height}"
    >
      <span class="verify-msg" v-text="text" />
      <div
        class="verify-left-bar"
        :style="{width: (leftBarWidth!==undefined)?leftBarWidth: barSize.height, height: barSize.height, 'border-color': leftBarBorderColor, transaction: transitionWidth, background: leftBarBackGroundColor}"
      >
        <span class="verify-msg" v-text="finishText" />
        <div
          class="verify-move-block"
          :style="{width: barSize.height, height: barSize.height, 'background-color': moveBlockBackgroundColor, left: moveBlockLeft, transition: transitionLeft}"
          @touchstart="start"
          @mousedown="start"
        >
          <i :class="['verify-icon iconfont', iconClass]" />
          <div
            class="verify-sub-block"
            :style="{'width':Math.floor(parseInt(setSize.imgWidth)*47/310)+ 'px',
                     'height': setSize.imgHeight,
                     'top':'-' + (parseInt(setSize.imgHeight) + vSpace) + 'px',
                     'background-size': setSize.imgWidth + ' ' + setSize.imgHeight,
            }"
          >
            <img v-show="blockBackImgBase" :src="'data:image/png;base64,'+blockBackImgBase" alt="" style="width:100%;height:100%;display:block">
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
/**
 * VerifySlide
 * @description 滑块
 * */
import { aesEncrypt } from './../utils/ase'
import { resetSize } from './../utils/util'
import { getCaptcha, checkCaptcha } from '@/api/user'

//  "captchaType":"blockPuzzle",
export default {
  name: 'VerifySlide',
  props: {
    vSpace: {
      type: Number,
      default: 15
    },
    explain: {
      type: String,
      default: '向右滑动完成验证'
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
      default() {
        return {
          width: '40px',
          height: '40px'
        }
      }
    },
    barSize: {
      type: Object,
      default() {
        return {
          width: '310px',
          height: '40px'
        }
      }
    },
    defaultImg: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      listLoading: false,
      captchaType: 'blockPuzzle',
      secretKey: '', // 后端返回的加密秘钥 字段
      passFlag: '', // 是否通过的标识
      backImgBase: null, // 验证码背景图片
      blockBackImgBase: null, // 验证滑块的背景图片
      backToken: '', // 后端返回的唯一token值
      startMoveTime: '', // 移动开始的时间
      endMoveTime: '', // 移动结束的时间
      tipsBackColor: '', // 提示词的背景颜色
      tipWords: '',
      text: '',
      finishText: '',
      setSize: {
        imgHeight: 0,
        imgWidth: 0,
        barHeight: 0,
        barWidth: 0
      },
      top: 0,
      left: 0,
      moveBlockLeft: undefined,
      leftBarWidth: undefined,
      // 移动中样式
      moveBlockBackgroundColor: undefined,
      leftBarBorderColor: '#ddd',
      leftBarBackGroundColor: '#f8faff',
      iconClass: 'icon-right',
      status: false, // 鼠标状态
      isEnd: false,		// 是够验证完成
      showRefresh: true,
      transitionLeft: '',
      transitionWidth: '',
      isOk: false
    }
  },
  computed: {
    barArea() {
      return this.$el.querySelector('.verify-bar-area')
    },
    resetSize() {
      return resetSize
    }
  },
  mounted() {
    this.init()
    // 禁止拖拽
    this.$el.onselectstart = function() {
      return false
    }
  },
  methods: {
    // 生成 uuid
    uuid() {
      var s = []
      var hexDigits = '0123456789abcdef'
      for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1)
      }
      s[14] = '4' // bits 12-15 of the time_hi_and_version field to 0010
      s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1) // bits 6-7 of the clock_seq_hi_and_reserved to 01
      s[8] = s[13] = s[18] = s[23] = '-'

      var slider = 'slider' + '-' + s.join('')
      var point = 'point' + '-' + s.join('')
      // 判断下是否存在 slider
      console.log(localStorage.getItem('slider'))
      if (!localStorage.getItem('slider')) {
        localStorage.setItem('slider', slider)
      }
      if (!localStorage.getItem('point')) {
        localStorage.setItem('point', point)
      }
    },
    async init() {
      this.uuid()
      await this.getPicture()
      this.$nextTick(() => {
        const setSize = this.resetSize(this)	// 重新设置宽度高度
        for (const key in setSize) {
          this.$set(this.setSize, key, setSize[key])
        }
        this.text = this.explain
        this.$parent.$emit('ready', this)
      })

      const _this = this

      window.removeEventListener('touchmove', function(e) {
        _this.move(e)
      })
      window.removeEventListener('mousemove', function(e) {
        _this.move(e)
      })

      // 鼠标松开
      window.removeEventListener('touchend', function() {
        _this.end()
      })
      window.removeEventListener('mouseup', function() {
        _this.end()
      })

      window.addEventListener('touchmove', function(e) {
        _this.move(e)
      })
      window.addEventListener('mousemove', function(e) {
        _this.move(e)
      })

      // 鼠标松开
      window.addEventListener('touchend', function() {
        _this.end()
      })
      window.addEventListener('mouseup', function() {
        _this.end()
      })
    },

    // 鼠标按下
    start: function(e) {
      e = e || window.event
      let x = 0
      if (!e.touches) { // 兼容PC端
        x = e.clientX
      } else { // 兼容移动端
        x = e.touches[0].pageX
      }
      this.startLeft = Math.floor(x - this.barArea.getBoundingClientRect().left)
      this.startMoveTime = +new Date() // 开始滑动的时间
      if (this.isEnd === false) {
        this.text = ''
        this.moveBlockBackgroundColor = '#1677FF'
        this.leftBarBorderColor = '#1677FF'
        this.leftBarBackGroundColor = 'rgb(64, 158, 255, .3)'
        e.stopPropagation()
        this.status = true
      }
    },
    // 鼠标移动
    move: function(e) {
      e = e || window.event
      let x = 0
      if (this.status && this.isEnd === false) {
        if (!e.touches) { // 兼容PC端
          x = e.clientX
        } else { // 兼容移动端
          x = e.touches[0].pageX
        }
        const bar_area_left = this.barArea.getBoundingClientRect().left
        let move_block_left = x - bar_area_left // 小方块相对于父元素的left值
        if (move_block_left >= this.barArea.offsetWidth - parseInt(parseInt(this.blockSize.width) / 2) - 2) {
          move_block_left = this.barArea.offsetWidth - parseInt(parseInt(this.blockSize.width) / 2) - 2
        }
        if (move_block_left <= 0) {
          move_block_left = parseInt(parseInt(this.blockSize.width) / 2)
        }
        // 拖动后小方块的left值
        this.moveBlockLeft = (move_block_left - this.startLeft) + 'px'
        this.leftBarWidth = (move_block_left - this.startLeft) + 'px'
      }
    },

    // 鼠标松开
    end: function() {
      this.endMoveTime = +new Date()
      // 判断是否重合
      if (this.status && this.isEnd === false) {
        let moveLeftDistance = parseInt((this.moveBlockLeft || '').replace('px', ''))
        moveLeftDistance = moveLeftDistance * 310 / parseInt(this.setSize.imgWidth)
        const data = {
          captchaType: this.captchaType,
          'pointJson': this.secretKey ? aesEncrypt(JSON.stringify({ x: moveLeftDistance, y: 5.0 }), this.secretKey) : JSON.stringify({ x: moveLeftDistance, y: 5.0 }),
          'tokenKey': this.backToken
        }
        if (moveLeftDistance === 0 || isNaN(moveLeftDistance)) return
        checkCaptcha(data).then(res => {
          if (res.code === 0) {
            this.moveBlockBackgroundColor = '#5cb85c'
            this.leftBarBorderColor = '#5cb85c'
            this.leftBarBackGroundColor = 'rgb(103, 194, 58, .3)'
            this.iconClass = 'icon-check'
            this.showRefresh = false
            this.isEnd = true
            this.passFlag = true
            this.tipWords = `${((this.endMoveTime - this.startMoveTime) / 1000).toFixed(2)}s验证成功`
            const captchaVerification = this.secretKey ? aesEncrypt(this.backToken + '---' + JSON.stringify({ x: moveLeftDistance, y: 5.0 }), this.secretKey) : this.backToken + '---' + JSON.stringify({ x: moveLeftDistance, y: 5.0 })
            console.log('captchaVerification>>>>>>', captchaVerification)
            setTimeout(() => {
              this.tipWords = ''
              this.$parent.closeBox()
              this.$parent.$emit('success', { captchaVerification })
            }, 1000)
          } else {
            this.moveBlockBackgroundColor = '#F56C6C'
            this.leftBarBackGroundColor = 'rgba(245, 108, 108,.3)'
            this.leftBarBorderColor = '#F56C6C'
            this.iconClass = 'icon-close'
            this.passFlag = false
            this.$parent.$emit('error', this)
            this.tipWords = '验证失败'
            setTimeout(() => {
              this.tipWords = ''
              this.refresh()
            }, 1000)
          }
        })
        this.status = false
      }
    },

    refresh: async function() {
      this.showRefresh = true
      this.finishText = ''

      this.transitionLeft = 'left .3s'
      this.moveBlockLeft = 0

      this.leftBarWidth = undefined
      this.transitionWidth = 'width .3s'

      this.leftBarBorderColor = '#ddd'
      this.leftBarBackGroundColor = '#ddd'
      this.moveBlockBackgroundColor = '#f8faff'
      this.iconClass = 'icon-right'
      this.isEnd = false

      await this.getPicture()
      setTimeout(() => {
        this.transitionWidth = ''
        this.transitionLeft = ''
        this.text = this.explain
      }, 300)
    },

    // 请求背景图片和验证图片
    async getPicture() {
      this.listLoading = true
      const data = {
        captchaType: this.captchaType,
        clientUid: localStorage.getItem('slider'),
        ts: Date.now() // 现在的时间戳
      }
      const res = await getCaptcha(data)
      if (res.code === 0) {
        this.backImgBase = res.result.originalImageBase64
        this.blockBackImgBase = res.result.jigsawImageBase64
        this.backToken = res.result.token
        this.secretKey = res.result.secretKey
      } else {
        this.tipWords = res.msg
        this.backImgBase = null
        this.blockBackImgBase = null
      }
      this.listLoading = false
    }
  }
}
</script>

