import primihub as ph
from primihub.FL.model.logistic_regression.homo_lr_dev import run_party


config = {
	'mode': 'Paillier',
	'n_length': 1024,
	'learning_rate': 'optimal',
	'alpha': 0.01,
	'batch_size': 100,
	'max_iter': 50,
	'n_iter_no_change': 5,
	'compare_threshold': 1e-6,
	'category': 2,
	'feature_names': None,
}


@ph.context.function(role='arbiter',
					 protocol='lr',
					 datasets=['${arbiter_dataset}'],
					 port='9010',
					 task_type="lr-train")
def run_arbiter_party():
	run_party('arbiter', config)


@ph.context.function(role='host',
					 protocol='lr',
					 datasets=['${label_dataset}'],
					 port='9020',
					 task_type="lr-train")
def run_host_party():
	run_party('host', config)


@ph.context.function(role='guest',
					 protocol='lr',
					 datasets=['${guest_dataset}'],
					 port='9030',
					 task_type="lr-train")
def run_guest_party():
	run_party('guest', config)