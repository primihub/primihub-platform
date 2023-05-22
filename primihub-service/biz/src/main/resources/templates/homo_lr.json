{
  "component_params": {
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
      "n_length": 2048,
      "delta": 1e-3,
      "noise_multiplier": 2.0,
      "l2_norm_clip": 1.0,
      "secure_mode": true,
      "learning_rate": "optimal",
      "alpha": 0.0001,
      "batch_size": 100,
      "global_epoch": 100,
      "local_epoch": 1,
      "selected_column": null,
      "id": "id",
      "label": "y",
      "print_metrics": true,
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
}