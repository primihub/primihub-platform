{
    "roles": {
        "server": "Alice",
        "client": [
            "Bob",
            "Charlie"
        ]
    },
    "common_params": {
        "model": "FL_Preprocess",
        "process": "fit_transform",
        "FL_type": "H",
        "task_name": "HFL_simpleimpute_fit_transform",
        "task": "classification",
        "selected_column": null,
        "id": "id",
        "label": "y",
        "preprocess_column": null,
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
    "role_params": {
        "Bob": {
            "data_set": "${label_dataset}",
            "preprocess_dataset_id": "${new_label_dataset}",
            "preprocess_dataset_path": "${new_label_dataset_path}",
            "preprocess_module_path": "${new_label_dataset_path}.pkl"
        },
        "Charlie": {
            "data_set": "${guest_dataset}",
            "preprocess_dataset_id": "${new_guest_dataset}",
            "preprocess_dataset_path": "${new_guest_dataset_path}",
            "preprocess_module_path": "${new_guest_dataset_path}.pkl"
        },
        "Alice": {
            "data_set": "${arbiter_dataset!""}"
        }
    }
}