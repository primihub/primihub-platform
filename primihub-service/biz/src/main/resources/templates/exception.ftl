import primihub as ph
from primihub.dataset.abnormal import run_abnormal_process

@ph.context.function(role="host", protocol="None", datasets=${datasets}, port="-1")
def dispatch_abnormal_process():
    run_abnormal_process(ph.context.Context.params_map,
    ph.context.Context.dataset_map)