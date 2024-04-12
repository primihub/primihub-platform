<script>
import OrganSelect from '@/components/OrganSelect'

export default {
  name: 'OwnResourceDetail',
  components: { OrganSelect },
  props: {
    data: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      resourceAuthType: 1,
      resourceAuthTypeList: [
        { value: 1, label: '公开' },
        { value: 2, label: '私有' },
        { value: 3, label: '指定机构可见' }
      ],
      authOrganList: []
    }
  },
  created() {

  },
  methods: {
    handleOrganChange() {},

    handelDeleteRow(id) {}
  }
}
</script>

<template>
  <div class="">
    <el-form>
      <el-form-item label="授权方式：">
        <el-radio-group v-model="resourceAuthType">
          <el-radio v-for="{ value, label } in resourceAuthTypeList" :key="value" :label="value"> {{ label }}</el-radio>
          <template v-if="resourceAuthType === 3">
            <OrganSelect :value="authOrganList" style="display:inline-block;" size="small" @change="handleOrganChange" />
          </template>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <el-table
      :data="data"
      class="table-list"
      v-bind="$attrs"
    >
      <el-table-column label="合作方名称" prop="organName" />
      <el-table-column label="开发可见时间" prop="ctime">
        <template v-slot="{row}">
          {{ row.ctime }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="100">
        <template v-slot="{row}">
          <el-button type="text" @click="handelDeleteRow(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
