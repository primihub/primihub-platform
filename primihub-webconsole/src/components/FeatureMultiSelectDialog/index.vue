<template>
  <el-dialog
    title="选择特征"
    width="50%"
    v-bind="$attrs"
    :before-close="handleClose"
  >
    <el-checkbox-group v-if="data.length>0" v-model="checkList">
      <el-checkbox v-for="item in data" :key="item.key" :label="item.val" />
    </el-checkbox-group>
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
        this.checkList = newVal
      }
    }
  },
  methods: {
    handleClose() {
      this.$emit('close')
    },
    handleDialogSubmit() {
      this.$emit('submit', this.checkList)
    }

  }
}
</script>
