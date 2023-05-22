{
  "component_params": {
    "roles": {
      "guest": [
        "Charlie"
      ],
      "host": [
        "Bob"
      ]
    },
    "common_params": {
      "model": "HeteroLR",
      "task_name": "train",
      "learning_rate": 0.01,
      "alpha": 0.0001,
      "epochs": 50,
      "penalty": "l2",
      "optimal_method": "momentum",
      "momentum": 0.7,
      "random_state": 2023,
      "scale_type": "z-score",
      "batch_size": 512,
      "sample_method": "random",
      "sample_ratio": 0.3,
      "loss_type": "log",
      "prev_grad": 0,
      "metric_path": "${indicatorFileName}",
      "model_pred": "${predictFileName}"
    },
    "role_params": {
      "Bob": {
        "data_set": "${label_dataset}",
        "id": null,
        "selected_column": null,
        "add_noise": "regular",
        "tol": 0.001,
        "label": "y",
        "model_path": "${hostModelFileName}",
        "n_iter_no_change": 5
      },
      "Charlie": {
        "data_set": "${guest_dataset}",
        "id": null,
        "model_path": "${guestModelFileName}",
        "selected_column": null,
        "label": null
      }
    }
  }
}