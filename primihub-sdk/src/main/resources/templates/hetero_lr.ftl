{
	"roles": {
		"guest": [
			"Charlie"
		],
		"host": "Bob"
	},
	"common_params": {
		"model": "VFL_logistic_regression",
		"method": "Plaintext",
		"process": "train",
		"task_name": "VFL_logistic_regression_plaintext_train",
		"learning_rate": 1e-0,
		"alpha": 1e-4,
		"epoch": ${epoch!10},
		"shuffle_seed":${shuffleSeed!0},
		"batch_size": ${batchSize!100},
		"print_metrics": true
	},
	"role_params": {
		"Bob": {
			"data_set": "${label_dataset}",
			"selected_column": null,
			"id": "id",
			"label": "y",
			"model_path": "${hostModelFileName}",
			"metric_path": "${indicatorFileName}"
		},
		"Charlie": {
			"data_set": "${guest_dataset}",
			"selected_column": null,
			"id": "id",
			"model_path": "${guestModelFileName}"
		}
	}
}