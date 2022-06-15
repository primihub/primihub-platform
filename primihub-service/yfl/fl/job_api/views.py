import time
from django.http import HttpResponse
import json
from collections import deque
from concurrent.futures import ThreadPoolExecutor
from tree_api.views import execute_tree_mode
from config.mysql_util import MySqlConnection
import random
import os
from queue import Queue
import matplotlib

matplotlib.use('Agg')
import matplotlib.pyplot as plt

pool = ThreadPoolExecutor(max_workers=500)


def run(request):
    json_str = request.body
    param = json.loads(json_str)
    pool.submit(dfs_traverse, param)
    result = {"code": 0, "msg": "success"}
    return HttpResponse(json.dumps(result), content_type="application/json")


def dfs_traverse(param):
    model_components = param['modelComponents']
    act_queue = Queue()
    act_dict = {}
    done_dict = {}
    task_list = []
    for component_single in model_components:
        if 'input' not in component_single or (
                component_single['input'] is not None and len(component_single['input'])) == 0:
            act_queue.put(component_single)
        act_dict[component_single['componentId']] = component_single

    while act_queue.qsize() != 0:
        current_component = act_queue.get()
        if ('input' in current_component
                and current_component['input'] is not None and len(current_component['input']) != 0):
            is_satisfy = True
            for current_input in current_component['input']:
                if done_dict.get(current_input['componentId']) is None:
                    is_satisfy = False
                    break
            if not is_satisfy:
                act_queue.put(current_component)
                break
        task_list.append(current_component)
        done_dict[current_component['componentId']] = current_component

        if ('output' in current_component
                and current_component['output'] is not None and len(current_component['output']) != 0):
            for current_output in current_component['output']:
                if done_dict.get(current_output['componentId']) is None:
                    act_queue.put(act_dict[current_output['componentId']])

    model_id = param['modelId']
    resource_pack = param['resources']

    execute_piece_task(task_list, model_id, resource_pack)


train_img_url = '/data/fileimages'
train_exp_url = '/data/result'


def execute_piece_task(task_list, model_id, resource_pack):
    try:
        current_mysql_connection = MySqlConnection('privacy')
    except Exception as e:
        print(e)

    task_follower_addr = 'localhost:50051'
    task_leader_addr = 'localhost:50052'

    task_follower_data = resource_pack[0]['filePath']
    task_leader_data = resource_pack[1]['filePath']

    for task_single in task_list:
        start_time = int(time.time() * 1000)

        update_component(current_mysql_connection, start_time, None, 2, task_single['componentId'])
        print("componentCode", task_single['componentCode'], 'is running')
        if task_single['componentCode'] == 'model':
            if (task_single['componentValues'] is not None
                    and len(task_single['componentValues']) != 0
                    and task_single['componentValues'][0]['key'] == 'modelType'
                    and task_single['componentValues'][0]['val'] == '2'):
                task_follower_output = '{train_exp_url}/exp/follower_train_{model_id}_{component_id}.output'.format(
                    train_exp_url=train_exp_url, model_id=model_id, component_id=task_single['componentId'])
                task_leader_output = '{train_exp_url}/exp/leader_train_{model_id}_{component_id}.output'.format(
                    train_exp_url=train_exp_url, model_id=model_id, component_id=task_single['componentId'])
                task_leader_img = '{train_img_url}/exp/leader_train_img_{model_id}_{component_id}.png'.format(
                    train_img_url=train_img_url, model_id=model_id, component_id=task_single['componentId'])
                task_follower_img = '{train_img_url}/exp/follower_train_img_{model_id}_{component_id}.png'.format(
                    train_img_url=train_img_url, model_id=model_id, component_id=task_single['componentId'])

                if not os.path.exists('{train_img_url}/exp'.format(train_img_url=train_img_url)):
                    os.makedirs('{train_img_url}/exp'.format(train_img_url=train_img_url))

                tree_mod_train(current_mysql_connection, task_single, model_id, task_follower_addr, task_leader_addr,
                               task_follower_data, task_leader_data, task_follower_output, task_leader_output,
                               task_leader_img, task_follower_img)
        elif task_single['componentCode'] == 'dataAlignment':
            time.sleep(random.randint(3, 15))
        elif task_single['componentCode'] == 'features':
            time.sleep(random.randint(3, 15))
        elif task_single['componentCode'] == 'sample':
            time.sleep(random.randint(3, 15))
        elif task_single['componentCode'] == 'exception':
            time.sleep(random.randint(3, 15))
        elif task_single['componentCode'] == 'featureCoding':
            time.sleep(random.randint(3, 15))
        elif task_single['componentCode'] == 'assessment':
            time.sleep(random.randint(3, 15))
        end_time = int(time.time() * 1000)
        update_component(current_mysql_connection, None, end_time, 1, task_single['componentId'])

    current_mysql_connection.close()


def tree_mod_train(current_mysql_connection, task_single, model_id, task_follower_addr, task_leader_addr,
                   task_follower_data, task_leader_data, task_follower_output, task_leader_output,
                   task_leader_img, task_follower_img):
    task_follower = pool.submit(execute_tree_mode, 'follower', task_follower_addr, task_leader_addr,
                                task_follower_data,
                                task_follower_output)
    task_leader = pool.submit(execute_tree_mode, 'leader', task_leader_addr, task_follower_addr,
                              task_leader_data,
                              task_leader_output)

    while not task_follower.done() or not task_leader.done():
        if task_follower.exception() or task_leader.exception():
            update_component(current_mysql_connection, None, None, 3, task_single['componentId'])
            print("task failed")
            return
        print("task is running please wait")
        time.sleep(3)
    print("task done")
    if task_follower.exception() or task_leader.exception():
        update_component(current_mysql_connection, None, None, 3, task_single['componentId'])
        print("task failed")
        return

    leader_output = deque(open(task_leader_output), 2)
    draw_line_pic(leader_output[1], task_leader_img)
    leader_quota = json.loads(leader_output[0].replace("\'", "\""))
    update_quota(current_mysql_connection, 1, task_leader_img, model_id, task_single['componentId'],
                 leader_quota['auc'], leader_quota['ks'], leader_quota['acc'],
                 leader_quota['precision'], leader_quota['recall'], leader_quota['f1'])

    follower_output = deque(open(task_follower_output), 2)
    draw_line_pic(follower_output[1], task_follower_img)
    follower_quota = json.loads(follower_output[0].replace("\'", "\""))
    update_quota(current_mysql_connection, 2, task_follower_img, model_id, task_single['componentId'],
                 follower_quota['auc'] if 'auc' in follower_quota else 0,
                 follower_quota['ks'] if 'ks' in follower_quota else 0,
                 follower_quota['acc'] if 'acc' in follower_quota else 0,
                 follower_quota['precision'] if 'precision' in follower_quota else 0,
                 follower_quota['recall'] if 'recall' in follower_quota else 0,
                 follower_quota['f1'] if 'f1' in follower_quota else 0)


def draw_line_pic(line_str, line_img):
    squares = line_str.split(',')
    square_length = len(squares)
    for i in range(square_length):
        squares[i] = '{:.4}'.format(float(squares[i]))
    plt.plot(squares, linewidth=5)
    plt.title("prediction", fontsize=24)
    plt.xlabel("x", fontsize=10)
    plt.ylabel("y", fontsize=10)
    plt.tick_params(axis="both", labelsize=12)
    plt.savefig(line_img)
    plt.close()


def update_component(current_mysql_connection, start_time, end_time, component_state, component_id):
    update_component_sql = '''update data_component 
                                    set component_state={component_state}
                                    {start_time_parse}
                                    {end_time_parse}
                                    where component_id={component_id} '''
    start_time_parse = ''' ,start_time={start_time} '''.format(start_time=start_time) if start_time is not None else ''
    end_time_parse = ''' ,end_time={end_time} '''.format(end_time=end_time) if end_time is not None else ''
    current_mysql_connection.exec_non_query(
        update_component_sql.format(start_time_parse=start_time_parse,
                                    end_time_parse=end_time_parse,
                                    component_state=component_state,
                                    component_id=component_id))


def update_quota(current_mysql_connection, quota_type, quota_images, model_id, component_id, auc,
                 ks, gini, precision, recall, f1_score):
    insert_quota_sql = ''' insert into data_model_quota(
                                    quota_type,quota_images,model_id,component_id,auc
                                    ,ks,gini,`precision`,recall,f1_score,is_del)
                            values({quota_type},'{quota_images}',{model_id},{component_id},{auc}
                                    ,{ks},{gini},{precision},{recall},{f1_score},{is_del})'''
    current_mysql_connection.exec_non_query(
        insert_quota_sql.format(quota_type=quota_type,
                                quota_images=quota_images,
                                model_id=model_id,
                                component_id=component_id,
                                auc=auc,
                                ks=ks,
                                gini=gini,
                                precision=precision,
                                recall=recall,
                                f1_score=f1_score,
                                is_del=0))
