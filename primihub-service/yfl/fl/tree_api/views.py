from django.http import HttpResponse
import json
import fedlearner.model.tree.trainer as trainer
from argparse import Namespace


def index(request):
    result = {"code": 0, "msg": "success", "result": {"a": 1, "b": "222"}}
    return HttpResponse(json.dumps(result), content_type="application/json")


def run(request):
    result = {"code": 0, "msg": "success"}
    role_para = request.GET.get('role')
    local_addr_para = request.GET.get('local_addr')
    peer_addr_para = request.GET.get('peer_addr')
    data_path_para = request.GET.get('data_path')
    output_path_para = request.GET.get('output_path')
    execute_tree_mode(role_para, local_addr_para, peer_addr_para, data_path_para, output_path_para)
    return HttpResponse(json.dumps(result), content_type="application/json")


def execute_tree_mode(role, local_addr, peer_addr, data_path, output_path,
                      application_id=None, worker_rank=None, num_workers=None, mode='train',
                      validation_data_path=None, no_data=False, file_ext='.csv', file_type='csv',
                      load_model_path=None, export_path=None, checkpoint_path=None,
                      verbosity=1, loss_type='logistic', learning_rate=0.3, max_iters=5, max_depth=3,
                      l2_regularization=1.0, max_bins=33, num_parallel=1, verify_example_ids=False,
                      ignore_fields='', cat_fields='', use_streaming=False, send_scores_to_follower=False,
                      send_metrics_to_follower=False, enable_packing=False, label_field='label'):
    params = Namespace(role=role, local_addr=local_addr, peer_addr=peer_addr, data_path=data_path,
                       output_path=output_path,
                       application_id=application_id, worker_rank=worker_rank, num_workers=num_workers, mode=mode,
                       validation_data_path=validation_data_path, no_data=no_data, file_ext=file_ext,
                       file_type=file_type,
                       load_model_path=load_model_path, export_path=export_path, checkpoint_path=checkpoint_path,
                       verbosity=verbosity, loss_type=loss_type, learning_rate=learning_rate,
                       max_iters=max_iters, max_depth=max_depth, l2_regularization=l2_regularization, max_bins=max_bins,
                       num_parallel=num_parallel, verify_example_ids=verify_example_ids, ignore_fields=ignore_fields,
                       cat_fields=cat_fields, use_streaming=use_streaming,
                       send_scores_to_follower=send_scores_to_follower,
                       send_metrics_to_follower=send_metrics_to_follower, enable_packing=enable_packing,
                       label_field=label_field)
    trainer.run(params)
