{
	"roles": {
	  "server": "Alice",
	  "client": [
		"Bob",
		"Charlie"
	  ]
	},
	"common_params": {
	  "model": "HFL_neural_network",
	  "method": "Plaintext",
	  "process": "train",
	  "task_name": "HFL_NN",
	  "task": "${taskNNType}",
	  "delta": ${delta!0.001},
	  "l2_norm_clip": ${l2NormClip!1.0},
	  "noise_multiplier": ${noiseMultiplier!2.0},
	  "learning_rate": ${learningRate!0.01},
	  "alpha": ${alpha!0.0001},
	  "optimizer": "${optimizer!"adam"}",
	  "batch_size": ${batchSize!100},
	  "global_epoch": ${globalEpoch!100},
	  "local_epoch": ${localEpoch!1},
	  "selected_column": null,
	  "id": "${id!"id"}",
	  "label": "${label!"y"}",
	  "print_metrics": ${printMetrics!false?c}
	},
	"role_params": {
	  "Bob": {
		"data_set": "${label_dataset}",
		"model_path": "${hostModelFileName}",
		"metric_path": "${indicatorFileName}"
	  },
	  "Charlie": {
		"data_set": "${guest_dataset}",
		"model_path": "${guestModelFileName}",
		"metric_path": "${indicatorFileName}"
	  },
	  "Alice": {
		"data_set": "${arbiter_dataset}",
		"metric_path": "/data${indicatorFileName}"
	  }
	}
}
