{
	"roles": {
	  "client": "Bob"
	},
	"common_params": {
	  "model": "HFL_neural_network",
	  "process": "predict",
	  "task_name": "HFL_NN_binclass_predict"
	},
	"role_params": {
	  "Bob": {
		"data_set": "${label_dataset}",
		"model_path": "${hostModelFileName}",
		"predict_path": "${predictFileName}"
	  }
	}
}
