{
	"roles": {
		"server": "Alice",
		"client": [
			"Bob",
			"Charlie"
		]
	},
	"common_params": {
		"model": "HFL_logistic_regression",
		"method": "Plaintext",
		"process": "train",
		"task_name": "HFL_logistic_regression",
		"n_length": ${nLength!2048},
		"delta": 1e-3,
		"noise_multiplier": ${noiseMultiplier!2.0},
		"l2_norm_clip": ${l2NormClip!1.0},
		"secure_mode": ${secureMode!true},
		"learning_rate": ${learningRate!0.1},
		"alpha": ${alpha!0.0001},
		"batch_size": ${batchSize!100},
		"global_epoch": ${globalEpoch!100},
		"local_epoch": ${localEpoch!1},
		"selected_column": null,
		"id": "${id!id}",
		"label": "${label!y}",
		"print_metrics": ${printMetrics!true},
		"metric_path": "${indicatorFileName}"
	},
	"role_params": {
		"Bob": {
			"data_set": "${label_dataset}",
			"model_path": "${hostModelFileName}"
		},
		"Charlie": {
			"data_set": "${guest_dataset}",
			"model_path": "${guestModelFileName}"
		},
		"Alice": {
			"data_set": "${arbiter_dataset}"
		}
	}
}