<script>
import EditResourceTable from '../../components/edit-resource-table.vue'

export default {
  name: 'CreateResourceStep2',
  components: {
    EditResourceTable
  },
  props: {
    currentProp: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      dataContainerWidth: 900,
      isEditPage: true,
      form: {
        fieldList: []
      }
    }
  },
  mounted() {
    this.form.fieldList = this.currentProp.fieldList
  },
  methods: {
    handleResourceChange(data) {
      console.log(data)
      // this.fieldList = data
      // this.dataForm.fieldList = this.formatParams()
    },

    validateForm() {
      return true
    },

    getParams() {
      return this.form
    }
  }
}
</script>

<template>
  <div class="data-container">
    <EditResourceTable
      border
      height="500"
      :data="form.fieldList"
      :is-edit-page="isEditPage"
      @change="handleResourceChange"
    />
  </div>
</template>
