{
  "roles": {
    "host": "Bob",
    "guest": [
      "Charlie"
    ]
  },
  "common_params": {
    "model": "FL_Preprocess",
    "process": "fit_transform",
    "FL_type": "V",
    "task_name": "VFL_simpleimpute_fit_transform",
    "task": "classification"
  },
  "role_params": {
    "Bob": {
      "data_set": "${label_dataset}",
      "selected_column": null,
      "id": "id",
      "label": "y",
      "preprocess_column": null,
      "preprocess_dataset_id": "${new_label_dataset}",
      "preprocess_dataset_path": "${new_label_dataset_path}",
      "preprocess_module_path": "${new_label_dataset_path}.pkl",
      "preprocess_module": {
        "SimpleImputer_string": {
          "column": null,
          "missing_values": "np.nan",
          "strategy": "${simpleImputerString}",
          "fill_value": null,
          "copy": true,
          "add_indicator": false,
          "keep_empty_features": false
        },
        "SimpleImputer_numeric": {
          "column": null,
          "missing_values": "np.nan",
          "strategy": "${simpleImputerNumeric}",
          "fill_value": null,
          "copy": true,
          "add_indicator": false,
          "keep_empty_features": false
        }
      }
    },
    "Charlie": {
      "data_set": "${guest_dataset}",
      "selected_column": null,
      "id": "id",
      "preprocess_column": null,
      "preprocess_dataset_id": "${new_guest_dataset}",
      "preprocess_dataset_path": "${new_guest_dataset_path}",
      "preprocess_module_path": "${new_guest_dataset_path}.pkl",
      "preprocess_module": {
        "SimpleImputer_string": {
          "column": null,
          "missing_values": "np.nan",
          "strategy": "${simpleImputerString}",
          "fill_value": null,
          "copy": true,
          "add_indicator": false,
          "keep_empty_features": false
        },
        "SimpleImputer_numeric": {
          "column": null,
          "missing_values": "np.nan",
          "strategy": "${simpleImputerNumeric}",
          "fill_value": null,
          "copy": true,
          "add_indicator": false,
          "keep_empty_features": false
        }
      }
    }
  }
}