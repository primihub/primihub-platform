{
	"roles": {
	  "server": "Alice",
	  "client": [
		"Bob",
		"Charlie"
	  ]
	},
	"common_params": {
	  "model": "HFL_neural_network",
	  "method": "Plaintext",
	  "process": "train",
	  "task_name": "HFL_NN",
	  "task": "${taskNNType}",
	  "delta": 1e-3,
	  "l2_norm_clip": 1.0,
	  "noise_multiplier": 2.0,
	  "learning_rate": 1e-2,
	  "alpha": 1e-4,
	  "optimizer": "adam",
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
