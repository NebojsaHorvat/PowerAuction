from fileinput import filename
import requests
import time
import psutil
import json
import sys

# Get the command-line arguments
if len(sys.argv) != 5:
    print("Usage: create_auction_performance.py [file_name_memory_sum] [file_name_memory_process] [number_of_tests] [valid_asset_id]")
    sys.exit(1)

file_name_memory = sys.argv[1]
file_name_process = sys.argv[2]
number_of_tests= int(sys.argv[3])
asset_id = sys.argv[4]

# Find all java processes. While doing this test I made sure that all java processess which are not Corda are shutdown
process_filter = filter(lambda p: p.name() == "java", psutil.process_iter())
processes = list(process_filter)

# Make sure that prosumer is acitive party on server
response = requests.post("http://localhost:8085/api/auction/switch-party/producer")


################# Create powerPromise
headers={
    'Content-type':'application/json', 
    'Accept':'application/json'
}

proxies = {
    "http": "",
    "https": "",
}

for proc in processes:
    proc.pid,proc.cpu_percent()

# Put valid asset id
data=f'{{"assetId": "{asset_id}","basePrice": "1","deadline": "18-09-2023 01:59:07 PM"}}'


# Task
start = time.time()
for x in range(number_of_tests):
    response = requests.post("http://localhost:8085/api/auction/create",
    data=data,
    headers=headers,
    proxies=proxies
    )
end = time.time()

# Metrics
memory_sum_rss = 0
memory_sum_vms = 0
process_infos = list()
for proc in processes:
    proc_info = dict()
    with proc.oneshot():
        proc_info["pid"] = proc.pid
        proc_info["ppid"] = proc.ppid()
        proc_info["name"] = proc.name()
        proc_info["cpu_percent"] = proc.cpu_percent()

        mem_info = proc.memory_info()
        proc_info["mem_rss"] = mem_info.rss / (1024**2)
        memory_sum_rss += proc_info["mem_rss"]
        proc_info["mem_vms"] = mem_info.vms / (1024**2)
        memory_sum_vms += proc_info["mem_vms"]

        proc_info["num_threads"] = proc.num_threads()
        proc_info["nice_priority"] = proc.nice()
        proc_info["cmdline"] = proc.cmdline()
    process_infos.append(proc_info)

print (f'{{"EXP file name": "{file_name_process}","Elapsed time": {(end-start)}, "Memory rss": {memory_sum_rss}, "Memory vms": {memory_sum_vms} }}')
output_time_memory= f'{{"EXP file name": "{file_name_process}","Elapsed time": {(end-start)}, "Memory rss": {memory_sum_rss}, "Memory vms": {memory_sum_vms} }}'
with open(f"./data/{file_name_memory}", "w") as file:
        file.write(output_time_memory)

full_file_name = f"./data/{file_name_process}"
with open(full_file_name, 'w', encoding='utf-8') as f:
    json.dump(process_infos, f, ensure_ascii=False, indent=4)
