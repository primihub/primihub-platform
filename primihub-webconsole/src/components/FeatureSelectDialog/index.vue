<template>
  <el-dialog
    title="选择特征"
    width="50%"
    v-bind="$attrs"
    :before-close="handleClose"
  >
    <el-radio-group v-if="data.length>0" v-model="radio">
      <el-radio v-for="item in data" :key="item.key" :disabled="hasSelectedFeatures.includes(item.val)" :label="item.val" />
    </el-radio-group>
    <NoData v-else />
    <span slot="footer" class="dialog-footer">
      <el-button size="small" @click="handleClose">取 消</el-button>
      <el-button size="small" type="primary" @click="handleDialogSubmit">确 定</el-button>
    </span>
  </el-dialog>

</template>

<script>
import NoData from '@/components/NoData'

export default {
  name: '',
  components: {
    NoData
  },
  props: {
    hasSelectedFeatures: {
      type: Array,
      require: false,
      default: () => []
    },
    data: {
      type: Array,
      default: () => []
    },
    selectedData: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      dialogVisible: false,
      checkList: this.selectedData,
      radio: this.selectedData
    }
  },
  watch: {
    selectedData(newVal) {
      if (newVal) {
        console.log('watch selectedData', newVal)
        this.radio = newVal
      } else {
        this.radio = ''
      }
    }
  },
  created() {
    console.log('hasSelectedFeatures', this.hasSelectedFeatures)
  },
  methods: {
    handleClose() {
      this.$emit('close')
    },
    handleDialogSubmit() {
      this.$emit('submit', this.radio)
    }

  }
}
</script>
