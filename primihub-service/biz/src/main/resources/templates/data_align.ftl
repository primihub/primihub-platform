{
  "roles": {
    "executor": [
      "Charlie", "Bob"
    ]
  },
  "common_params": {
    "model": "DataAlign"
  },
  "role_params": {
    "Bob": {
      "data_set": "${label_dataset}",
      "newDataSetId":"${detail['${label_dataset}'].newDataSetId}",
      "psiPath": "${detail['${label_dataset}'].psiPath}",
      "outputPath":"${detail['${label_dataset}'].outputPath}",
      "index":[
      <#assign bindex = 0>
      <#if detail['${label_dataset}'].index?size != 0>
        <#list detail['${label_dataset}'].index! as emp>
          <#if bindex != 0>,</#if>
          ${emp}
          <#assign bindex = bindex + 1>
        </#list>
      </#if>
      ]
    },
    "Charlie": {
      "data_set": "${guest_dataset}",
      "newDataSetId":"${detail['${guest_dataset}'].newDataSetId}",
      "psiPath": "${detail['${guest_dataset}'].psiPath}",
      "outputPath":"${detail['${guest_dataset}'].outputPath}",
      "index":[
        <#assign cindex = 0>
        <#if detail['${guest_dataset}'].index?size != 0>
          <#list detail['${guest_dataset}'].index! as emp>
            <#if cindex != 0>,</#if>${emp}
            <#assign cindex = cindex + 1>
          </#list>
        </#if>
      ]
    }
  }
}