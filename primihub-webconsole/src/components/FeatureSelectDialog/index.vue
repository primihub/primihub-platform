<template>
  <el-dialog
    title="选择特征"
    width="50%"
    v-bind="$attrs"
    :before-close="handleClose"
  >
    <checkbox v-if="data" :options="data" :checked="checkList" @change="handleChange" />
    <NoData v-else />
    <span slot="footer" class="dialog-footer">
      <el-button size="small" @click="handleClose">取 消</el-button>
      <el-button size="small" type="primary" @click="handleDialogSubmit">确 定</el-button>
    </span>
  </el-dialog>

</template>

<script>
import NoData from '@/components/NoData'
import Checkbox from '@/components/BaseCheckbox'

export default {
  name: '',
  components: {
    NoData,
    Checkbox
  },
  props: {
    data: {
      type: Array,
      default: () => []
    },
    selectedData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      dialogVisible: false,
      checkList: this.selectedData
    }
  },
  watch: {
    selectedData(newVal) {
      if (newVal) {
        console.log('watch selectedData', newVal)
        this.checkList = newVal
      }
    }
  },
  created() {
    this.currentData = {
      calculationField: this.data,
      isIndeterminate: true,
      checkAll: false,
      checked: this.data
    }
  },
  methods: {
    handleClose() {
      this.$emit('close')
    },
    handleDialogSubmit() {
      this.$emit('submit', this.checkList)
    },
    handleChange({ checked }) {
      console.log('handleChange', checked)
      this.checkList = checked
    }

  }
}
</script>
