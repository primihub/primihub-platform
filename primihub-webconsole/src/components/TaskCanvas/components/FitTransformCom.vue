<template>
  <div class="container">
    <el-tabs v-model="activeName">
      <el-tab-pane :label="simpleImputerString.typeName" :name="simpleImputerString.typeCode">
        <div class="flex margin-bottom-5">
          <span>以</span>
          <el-select v-model="simpleImputerString.inputValue" size="small" placeholder="请选择" style="margin: 0 10px;" @change="handleChange">
            <el-option
              v-for="item in simpleImputerString.inputValues"
              :key="item.key"
              :label="item.val"
              :value="item.key"
            />
          </el-select>
          <span>填充</span>
        </div>
      </el-tab-pane>
      <el-tab-pane :label="simpleImputerNumeric.typeName" :name="simpleImputerNumeric.typeCode">
        <div class="flex margin-bottom-5">
          <span>以</span>
          <el-select v-model="simpleImputerNumeric.inputValue" size="small" placeholder="请选择" style="margin: 0 10px;" @change="handleChange">
            <el-option
              v-for="item in simpleImputerNumeric.inputValues"
              :key="item.key"
              :label="item.val"
              :value="item.key"
            />
          </el-select>
          <span>填充</span>
        </div>
      </el-tab-pane>
    </el-tabs>
    <div class="table-container" :class="{'is-open': isOpen}">
      <el-table
        :data="activeName === SIMPLE_IMPUTER_STRING ? stringColumnData : numericColumnData"
        size="mini"
        style="max-height: 100%;"
        border
      >
        <el-table-column
          prop="fieldName"
          label="特征"
        />
        <el-table-column
          prop="fillValue"
          label="填充"
        />
      </el-table>
      <div v-if="activeName === SIMPLE_IMPUTER_STRING ? stringColumnData.length>15 : numericColumnData.length>15" class="mask">
        <el-button type="text" class="popper-btn" @click="isOpen =! isOpen">{{ text }}<i :class="isOpen?'el-icon-arrow-up':'el-icon-arrow-down'" /></el-button>
      </div>
    </div>

  </div>
</template>

<script>
import { String } from '@/const/filedType'
import { SIMPLE_IMPUTER_STRING, SIMPLE_IMPUTER_NUMBER } from '@/const/componentCode'

export default {
  props: {
    nodeData: {
      type: Array,
      default: () => []
    },
    columnData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      isOpen: false,
      activeName: '',
      SIMPLE_IMPUTER_STRING,
      SIMPLE_IMPUTER_NUMBER
    }
  },
  computed: {
    simpleImputerString() {
      return this.nodeData.find(item => item.typeCode === SIMPLE_IMPUTER_STRING)
    },
    simpleImputerNumeric() {
      return this.nodeData.find(item => item.typeCode === SIMPLE_IMPUTER_NUMBER)
    },
    stringColumnData() {
      return this.columnData.filter(item => item.fieldType === String)
    },
    numericColumnData() {
      return this.columnData.filter(item => item.fieldType !== String)
    },
    tableHeight() {
      return `calc(100vh - 355px)`
    },
    text() {
      let text = ''
      if (this.isOpen === false) {
        text = '展开'
      } else if (this.isOpen === true) {
        text = '收起'
      }
      return text
    }
  },
  created() {
    this.init()
  },
  methods: {
    setStringColumnData() {
      this.stringColumnData.map(item => {
        this.$set(item, 'fillValue', this.simpleImputerString.inputValues.find(item => item.key === this.simpleImputerString.inputValue).val)
      })
      console.log('this.stringColumnData', this.stringColumnData)
    },
    setNumericColumnData() {
      this.numericColumnData.map(item => {
        this.$set(item, 'fillValue', this.simpleImputerNumeric.inputValues.find(item => item.key === this.simpleImputerNumeric.inputValue).val)
      })
      console.log('this.numericColumnData', this.numericColumnData)
    },
    init() {
      this.activeName = this.simpleImputerString.typeCode
      this.total = this.activeName === SIMPLE_IMPUTER_STRING ? this.stringColumnData.length : this.numericColumnData.length
      this.setStringColumnData()
      this.setNumericColumnData()
    },
    handleChange() {
      if (this.activeName === SIMPLE_IMPUTER_STRING) {
        this.setStringColumnData()
      } else {
        this.setNumericColumnData()
      }
      this.$emit('change', {
        simpleImputerString: {
          strategy: this.simpleImputerString.inputValue
        },
        simpleImputerNumeric: {
          strategy: this.simpleImputerNumeric.inputValue
        }
      })
    }
  }

}
</script>

<style lang="scss" scoped>
::v-deep .el-table th.el-table__cell{
  padding: 5px 0;
  line-height: 1;
}
::v-deep .el-table .cell{
  font-size: 12px;
}
// ::v-deep .el-tabs__item{
//   border: 1px solid #E4E7ED;
//   border-top-left-radius: 4px;
//   border-top-right-radius: 4px;
//   padding: 0 5px;
// }
.container{
  height: 100%;
}
.table-container{
  height: calc(100vh - 375px);
  overflow: hidden;
  padding-bottom: 30px;
  position: relative;
  &.is-open{
    height: 100%;
    .mask{
      bottom: -24px;
    }
  }
}
.mask{
  width: 100%;
  position: absolute;
  height: 50px;
  left: 0;
  right: 0;
  bottom: 0px;
  background: linear-gradient(#fff, transparent);
  z-index: 10;
}
.popper-btn{
  width: 100%;
}
</style>
