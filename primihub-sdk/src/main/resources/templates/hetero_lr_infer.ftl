{
	"roles": {
		"guest": [
			"Charlie"
		],
		"host":"Bob"
	},
	"common_params": {
		"model": "${model}",
		"process": "predict",
		"task_name": "VFL_logistic_regression_predict"
	},
	"role_params": {
		"Bob": {
			"data_set": "${label_dataset}",
			"model_path": "${hostModelFileName}",
			"predict_path": "${predictFileName}"
		},
		"Charlie": {
			"data_set": "${guest_dataset}",
			"model_path": "${guestModelFileName}"
		}
	}
}