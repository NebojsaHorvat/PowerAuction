from fileinput import filename
import requests
import time
import os
import psutil
import json

# Find all java processes. While doing this test I made sure that all java processess which are not Corda are shutdown
process_filter = filter(lambda p: p.name() == "java", psutil.process_iter())
processes = list(process_filter)

# Prints all java processes
# for i in process:
#   print (i.name,i.pid)

################# Create powerPromise

# Make sure that prosumer is acitive party on server
response = requests.post("http://localhost:8085/api/auction/switch-party/producer")

headers={
    'Content-type':'application/json', 
    'Accept':'application/json'
}

for proc in processes:
    proc.pid,proc.cpu_percent()

data='{"powerSuppliedInKW":"100.0","deliveryTime":"18-02-2022 04:52:10 PM", "powerSupplyDurationInMin":"60.0"}' 
# Change 1/10/100/1000
file_name = 'exp2_3.json'
# Task
start = time.time()
# Change 1/10/100/1000
for x in range(100):
    response = requests.post("http://localhost:8085/api/auction/asset/create",
    data=data,
    headers=headers
    )
    # print (response)
end = time.time()

# Metrics
memory_sum = 0
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
        memory_sum += proc_info["mem_rss"]
        proc_info["mem_vms"] = mem_info.vms / (1024**2)

        proc_info["num_threads"] = proc.num_threads()
        proc_info["nice_priority"] = proc.nice()
        proc_info["cmdline"] = proc.cmdline()
    process_infos.append(proc_info)
    # print(proc_info)

print (f"Elapsed time {(end-start)}")
print (f"Sum of memory usage {memory_sum}")


full_file_name = f"./data/{file_name}"
with open(full_file_name, 'w', encoding='utf-8') as f:
    json.dump(process_infos, f, ensure_ascii=False, indent=4)





# # print ( f"Percent of CPU used: {psutil.cpu_percent(interval=0.1)}%" )
# print ( f"Percent of CPU used pet core: {psutil.cpu_percent(interval=1, percpu=True)}%" )
# print(f"Memory usage {psutil.virtual_memory()}")
# print(f"Swap memory usage {psutil.swap_memory()}")

# print (f"Elapsed time {(end-start)}")
