{
  "env":{
    "job.mode":"STREAMING",
    "job.name":"SeaTunnel_Job",
    "checkpoint.timeout" : 3600000
  },
  "source":[
    {
      <#if dbusername!="">
        "user" : "${dbusername}",
      </#if>
      <#if dbpassword!="">
        "password" : "${dbpassword}",
      </#if>
      "driver" : "${driver}",
      "parallelism" : 1,
      "query" : "${sql}",
      "connection_check_timeout_sec" : 30,
      "fetch_size" : "10000",
      "plugin_name" : "Jdbc",
      "url" : "${url}"
    }
  ],
  "sink":[
    {
      "plugin_name":"RabbitMQ",
      "host":"${host!rabbitmq}",
      "port":${port},
      "virtual_host":"${virtual_host}",
      "username":"${username!guest}",
      "password":"${password!guest}",
      "queue_name":"seatunnel.data",
      "rabbitmq.headers":{
        "traceId":"${traceId!id}"
      }
    }
  ]
}