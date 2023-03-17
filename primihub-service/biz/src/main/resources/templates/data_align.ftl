"""
Copyright 2022 Primihub

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     https: //www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

import primihub as ph
from primihub.client import primihub_cli as cli
from primihub import context, dataset
from primihub.dataset import register_dataset
import json
import csv
print(ph.context.Context.__dict__)

def register_new_dataset(sever_address, dataset_type, dataset_path, dataset_id):
    print("register_new_dataset: {} {} {} {}".format(sever_address, dataset_type, dataset_path, dataset_id))
    register_dataset(sever_address, dataset_type, dataset_path, dataset_id)

def generate_new_dataset(meta_info):
    psi_path = meta_info["psiPath"]
    new_dataset_id = meta_info["newDataSetId"]
    new_dataset_output_path = meta_info["outputPath"]
    psi_index = meta_info["index"]
    old_dataset_path = meta_info["localdata_path"]
    host_address = meta_info["host_address"]
    print(psi_path)
    print(new_dataset_id)
    print(new_dataset_output_path)
    print(psi_index)
    print(old_dataset_path)
    print("psi_path: {}, new_dataset_id {}, new_dataset_output_path {} psi_index {} old_dataset_path {}".format(
        psi_path, new_dataset_id, new_dataset_output_path, psi_index, old_dataset_path))
    intersection_map = {}
    intersection_set = set()
    intersection_list = list()
    with open(psi_path) as f:
        f_csv = csv.reader(f)
        header = next(f_csv)
        for items in f_csv:
            item = items[0].strip()
            if not item:
                continue
            intersection_set.add(item)
            intersection_list.append(item)

    with open(old_dataset_path) as old_f, open(new_dataset_output_path, 'w') as new_f:
        f_csv = csv.reader(old_f)
        header = next(f_csv)
        print(header)
        new_file_writer = csv.writer(new_f)
        new_file_writer.writerow(header)
        for row in f_csv:
            psi_key = row[psi_index]
            if psi_key not in intersection_set:
                continue
            else:
                if psi_key != intersection_list[0]:  #save to map
                  intersection_map[psi_key] = row
                else:
                  new_file_writer.writerow(row)
                  del intersection_list[0]
                while True:
                  if len(intersection_list) == 0:
                    break
                  psi_key = intersection_list[0]
                  if psi_key in intersection_map:
                    new_file_writer.writerow(intersection_map[psi_key])
                    del intersection_list[0]
                  else:
                    break
    register_new_dataset(host_address, "csv", new_dataset_output_path, new_dataset_id)

def parse_dataset_param():
    service_addr = ph.context.Context.params_map["DatasetServiceAddr"]
    print(service_addr)
    detail = ph.context.Context.params_map["detail"]
    print(detail)
    detail = detail.replace("=", ":")
    detail = detail.replace(";", ",")
    print(detail)
    detail_dict = json.loads(detail)
    print(detail_dict)
    local_dataset =  ph.context.Context.params_map["local_dataset"]
    local_dataset_path = ph.context.Context.params_map[local_dataset]
    local_meta_info = detail_dict[local_dataset]
    local_meta_info["localdata_path"] = local_dataset_path
    local_meta_info["host_address"] = service_addr
    return local_meta_info

# define a remote method
@ph.context.function(role='host', protocol='python', datasets=["${label_dataset}"], port="*:5555")
def func1(value=1):
    print("params: ", str(value))
    tmp_params_map = ph.context.Context.nodes_context["host"].datasets
    print(tmp_params_map)
    local_meta_info = parse_dataset_param()
    print(local_meta_info)
    generate_new_dataset(local_meta_info)


# define a remote method
@ph.context.function(role='guest', protocol='python', datasets=["${guest_dataset}"], port="localhost:5555")
def func2(value=2):
    print("params: ", str(value))
    tmp_params_map = ph.context.Context.nodes_context["guest"].datasets
    print(tmp_params_map)
    local_meta_info = parse_dataset_param()
    print(local_meta_info)
    generate_new_dataset(local_meta_info)
