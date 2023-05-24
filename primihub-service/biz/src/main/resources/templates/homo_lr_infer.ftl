{
  "roles": {
    "client": "Bob"
  },
  "common_params": {
    "model": "HFL_logistic_regression",
    "process": "predict",
    "task_name": "HFL_logistic_regression_binclass_predict"
  },
  "role_params": {
    "Bob": {
      "data_set": "${label_dataset}",
      "model_path": "${hostModelFileName}",
      "predict_path": "Bob_predict.csv"
    }
  }
}