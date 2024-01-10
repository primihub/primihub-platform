{
	"roles": {
		"guest": [
			"Charlie"
		],
		"host": "Bob"
	},
	"common_params": {
		"model": "HeteroXGBInfer",
		"task_name": "predict",
        "psi": null,
		"metric_path": "${indicatorFileName}",
		"model_pred": "${predictFileName}"
	},
	"role_params": {
		"Bob": {
			"data_set": "${label_dataset}",
			"id": "id",
			"selected_column": ${label_field0},
			"label": "y",
			"lookup_table": "${hostLookupTable}",
			"model_path": "${hostModelFileName}"
		},
		"Charlie": {
			"data_set": "${guest_dataset}",
			"id": "id",
			"model_path": "${guestModelFileName}",
			"selected_column": ${label_field1},
			"lookup_table": "${guestLookupTable}",
			"label": null
		}
	}
}