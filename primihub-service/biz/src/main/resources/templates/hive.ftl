{
  "env":{
    "job.mode":"STREAMING",
    "job.name":"SeaTunnel_Job"
  },
  "source":[
    {
      "driver" : "${driver}",
      "parallelism" : 1,
      "query" : "${sql}",
      "connection_check_timeout_sec" : 30,
      "batch_size":1,
      "fetch_size" : "1",
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
      "queue_name":"seatunnel_data",
      "rabbitmq.headers":{
        "traceId":"${traceId!id}"
      }
    }
  ]
}