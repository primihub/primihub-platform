{
  "roles": {
	"client": "Bob"
  },
  "common_params": {
	"model": "${model}",
	"process": "predict",
	"task_name": "HFL_logistic_regression_binclass_predict"
  },
  "role_params": {
	"Bob": {
	  "data_set": "${label_dataset}",
	  "model_path": "${hostModelFileName}",
	  "predict_path": "${predictFileName}"
	}
  }
}