<template>
  <div class="lazy-cascader" :style="{ width: width }">
    <!-- 禁用状态 -->
    <div
      v-if="disabled"
      class="el-input__inner lazy-cascader-input lazy-cascader-input-disabled"
    >
      <span v-show="placeholderVisible" class="lazy-cascader-placeholder">
        {{ placeholder }}
      </span>
      <div v-if="props.multiple" class="lazy-cascader-tags">
        <el-tag
          v-for="(item, index) in labelArray"
          :key="index"
          class="lazy-cascader-tag"
          type="info"
          disable-transitions
          closable
        >
          <span> {{ item.label.join(separator) }}</span>
        </el-tag>
      </div>
      <div v-else class="lazy-cascader-label">
        <el-tooltip
          placement="top-start"
          :content="labelObject.label.join(separator)"
        >
          <span>{{ labelObject.label.join(separator) }}</span>
        </el-tooltip>
      </div>
    </div>
    <!-- 禁用状态 -->
    <!-- 可选状态 -->
    <el-popover v-else ref="popover" trigger="click" placement="bottom-start">
      <!-- 搜索 -->
      <div class="lazy-cascader-search">
        <el-autocomplete
          v-if="filterable"
          v-model="keyword"
          :style="{ width: searchWidth || '100%' }"
          :popper-class="suggestionsPopperClass"
          class="inline-input"
          prefix-icon="el-icon-search"
          label="name"
          :fetch-suggestions="querySearch"
          :trigger-on-focus="false"
          placeholder="请输入"
          @select="handleSelect"
          @blur="isSearchEmpty = false"
        >
          <template slot-scope="{ item }">
            <div class="name" :class="isChecked(item[props.value])">
              {{ item[props.label].join(separator) }}
            </div>
          </template>
        </el-autocomplete>
        <div v-show="isSearchEmpty" class="empty">{{ searchEmptyText }}</div>
      </div>
      <!-- 搜索 -->
      <!-- 级联面板 -->
      <div class="lazy-cascader-panel">
        <el-cascader-panel
          ref="panel"
          v-model="current"
          :options="options"
          :props="currentProps"
          @change="change"
        />
      </div>
      <!-- 级联面板 -->
      <!--内容区域-->
      <div
        slot="reference"
        class="el-input__inner lazy-cascader-input"
        :class="disabled ? 'lazy-cascader-input-disabled' : ''"
      >
        <span v-show="placeholderVisible" class="lazy-cascader-placeholder">
          {{ placeholder }}
        </span>
        <div v-if="props.multiple" class="lazy-cascader-tags">
          <el-tag
            v-for="(item, index) in labelArray"
            :key="index"
            class="lazy-cascader-tag"
            type="info"
            size="small"
            disable-transitions
            closable
            @close="handleClose(item)"
          >
            <span> {{ item.label.join(separator) }}</span>
          </el-tag>
        </div>
        <div v-else class="lazy-cascader-label">
          <el-tooltip
            placement="top-start"
            :content="labelObject.label.join(separator)"
          >
            <span>{{ labelObject.label.join(separator) }}</span>
          </el-tooltip>
        </div>
        <span
          v-if="clearable && current.length > 0"
          class="lazy-cascader-clear"
          @click.stop="clearBtnClick"
        >
          <i class="el-icon-close" />
        </span>
      </div>
      <!--内容区域-->
    </el-popover>
    <!-- 可选状态 -->
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: Array,
      default: () => {
        return []
      }
    },
    separator: {
      type: String,
      default: ' > '
    },
    placeholder: {
      type: String,
      default: '请选择'
    },

    width: {
      type: String,
      default: '400px'
    },
    filterable: Boolean,
    clearable: Boolean,
    disabled: Boolean,
    props: {
      type: Object,
      default: () => {
        return {}
      }
    },
    suggestionsPopperClass: {
      type: String,
      default: 'suggestions-popper-class'
    },
    searchWidth: {
      type: String,
      default: ''
    },
    searchEmptyText: {
      type: String,
      default: '暂无数据'
    }
  },
  data() {
    return {
      isSearchEmpty: false,
      keyword: '',
      options: [],
      current: [],
      labelObject: { label: [], value: [] },
      labelArray: [],
      currentProps: {
        multiple: this.props.multiple,
        checkStrictly: this.props.checkStrictly,
        value: this.props.value,
        label: this.props.label,
        leaf: this.props.leaf,
        lazy: true,
        lazyLoad: this.lazyLoad
      }
    }
  },
  computed: {
    placeholderVisible() {
      if (this.current) {
        return this.current.length === 0
      } else {
        return true
      }
    }
  },
  watch: {
    current() {
      this.getLabelArray()
    },
    value(v) {
      this.current = v
    },
    keyword() {
      this.isSearchEmpty = false
    }
  },
  created() {
    this.initOptions()
  },
  methods: {
    // 搜索是否选中
    isChecked(value) {
      // 多选
      if (this.props.multiple) {
        const index = this.current.findIndex(item => {
          return item.join() === value.join()
        })
        if (index > -1) {
          return 'el-link el-link--primary'
        } else {
          return ''
        }
      } else {
        if (value.join() === this.current.join()) {
          return 'el-link el-link--primary'
        } else {
          return ''
        }
      }
    },
    // 搜索
    querySearch(query, callback) {
      this.props.lazySearch(query, list => {
        callback(list)
        if (!list || !list.length) this.isSearchEmpty = true
      })
    },
    // 选中搜索下拉搜索项
    handleSelect(item) {
      if (this.props.multiple) {
        const index = this.current.findIndex(obj => {
          return obj.join() === item[this.props.value].join()
        })
        if (index === -1) {
          this.$refs.panel.clearCheckedNodes()
          this.current.push(item[this.props.value])
          this.$emit('change', this.current)
        }
      } else {
        // 选中下拉选变更值
        if (
          this.current == null ||
          item[this.props.value].join() !== this.current.join()
        ) {
          this.$refs.panel.activePath = []
          this.current = item[this.props.value]
          this.$emit('change', this.current)
        }
      }
      this.keyword = ''
    },
    // 初始化数据
    async initOptions() {
      this.props.lazyLoad(0, list => {
        this.$set(this, 'options', list)
        if (this.props.multiple) {
          this.current = [...this.value]
        } else {
          this.current = this.value
        }
      })
    },
    async getLabelArray() {
      if (this.props.multiple) {
        const array = []
        for (let i = 0; i < this.current.length; i++) {
          const obj = await this.getObject(this.current[i])
          array.push(obj)
        }
        this.labelArray = array
        this.$emit('input', this.current)
        if (!this.disabled) {
          this.$nextTick(() => {
            this.$refs.popover.updatePopper()
          })
        }
      } else {
        this.labelObject = await this.getObject(this.current || [])
        this.$emit('input', this.current)
      }
    },
    /** 格式化id=>object */
    async getObject(id) {
      try {
        let options = this.options
        const nameArray = []
        for (let i = 0; i < id.length; i++) {
          const index = options.findIndex(item => {
            return item[this.props.value] === id[i]
          })
          nameArray.push(options[index][this.props.label])
          if (i < id.length - 1 && options[index].children === undefined) {
            const list = new Promise(resolve => {
              this.props.lazyLoad(id[i], list => {
                resolve(list)
              })
            })
            this.$set(options[index], 'children', await list)
            options = options[index].children
          } else {
            options = options[index].children
          }
        }
        return { value: id, label: nameArray }
      } catch (e) {
        this.current = []
        return { value: [], label: [] }
      }
    },
    // 懒加载数据
    async lazyLoad(node, resolve) {
      let current = this.current
      if (this.props.multiple) {
        current = [...this.current]
      }
      if (node.root) {
        resolve()
      } else if (node.data[this.props.leaf]) {
        resolve()
      } else if (node.data.children) {
        if (this.props.multiple) {
          this.current = current
        }
        resolve()
      } else {
        this.props.lazyLoad(node.value, list => {
          node.data.children = list
          if (this.props.multiple) {
            this.current = current
          }
          resolve(list)
        })
      }
    },
    // 删除多选值
    /** 删除**/
    handleClose(item) {
      const index = this.current.findIndex(obj => {
        return obj.join() === item.value.join()
      })
      if (index > -1) {
        this.$refs.panel.clearCheckedNodes()
        this.current.splice(index, 1)
        this.$emit('change', this.current)
      }
    },
    // 点击清空按钮
    clearBtnClick() {
      this.$refs.panel.clearCheckedNodes()
      this.current = []
      this.$emit('change', this.current)
    },
    change() {
      this.$emit('change', this.current)
    }
  }
}
</script>
<style lang="scss" scoped>
.lazy-cascader {
  display: inline-block;
  width: 300px;
  .lazy-cascader-input {
    position: relative;
    width: 100%;
    background: #fff;
    height: auto;
    min-height: 36px;
    padding: 5px;
    line-height: 1;
    cursor: pointer;
    > .lazy-cascader-placeholder {
      padding: 0 2px;
      line-height: 28px;
      color: #999;
      font-size: 14px;
    }
    > .lazy-cascader-label {
      padding: 0 2px;
      line-height: 28px;
      color: #606266;
      font-size: 14px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    > .lazy-cascader-clear {
      position: absolute;
      right: 0;
      top: 0;
      display: inline-block;
      width: 40px;
      height: 40px;
      text-align: center;
      line-height: 40px;
    }
  }
  .lazy-cascader-input-disabled {
    background-color: #f5f7fa;
    border-color: #e4e7ed;
    color: #c0c4cc;
    cursor: not-allowed;
    > .lazy-cascader-label {
      color: #c0c4cc;
    }
    > .lazy-cascader-placeholder {
      color: #c0c4cc;
    }
  }
}
.lazy-cascader-tag {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  margin: 2px;
  text-overflow: ellipsis;
  background: #f0f2f5;
  > span {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  > .el-icon-close {
    -webkit-box-flex: 0;
    -ms-flex: none;
    flex: none;
    background-color: #c0c4cc;
    color: #fff;
  }
}
.lazy-cascader-panel {
  margin-top: 10px;
  display: inline-block;
}
.suggestions-popper-class {
  width: auto !important;
  min-width: 200px;
}
.lazy-cascader-search {
  .empty {
    width: calc(100% - 24px);
    box-sizing: border-box;
    background-color: #fff;
    color: #999;
    text-align: center;
    position: absolute;
    z-index: 999;
    padding: 12px 0;
    margin-top: 12px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    &:before {
      content: "";
      position: absolute;
      top: -12px;
      left: 36px;
      width: 0;
      height: 0;
      border-left: 6px solid transparent;
      border-right: 6px solid transparent;
      border-top: 6px solid transparent;
      border-bottom: 6px solid #fff;
      filter: drop-shadow(0 -1px 2px rgba(0, 0, 0, 0.02));
    }
  }
}
</style>
