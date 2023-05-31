{
	"roles": {
		"guest": [
			"Charlie"
		],
		"host": [
			"Bob"
		]
	},
	"common_params": {
		"model": "HeteroXGBInfer",
		"task_name": "predict",
		"metric_path": "${indicatorFileName}",
		"model_pred": "${predictFileName}"
	},
	"role_params": {
		"Bob": {
			"data_set": "${label_dataset}",
			"id": "id",
			"selected_column": null,
			"label": "y",
			"lookup_table": "${hostLookupTable}",
			"model_path": "${hostModelFileName}"
		},
		"Charlie": {
			"data_set": "${guest_dataset}",
			"id": "id",
			"model_path": "${guestModelFileName}",
			"selected_column": null,
			"lookup_table": "${guestLookupTable}",
			"label": null
		}
	}
}