from fileinput import filename
import requests
import time
import os
import psutil
import json

# Get the command-line arguments
if len(sys.argv) != 3:
    print("Usage: performance_non_exec.py [file_name_time] [file_name_process]")
    sys.exit(1)

# Find all java processes. While doing this test I made sure that all java processess which are not Corda are shutdown
process_filter = filter(lambda p: p.name() == "java", psutil.process_iter())
processes = list(process_filter)

headers={
    'Content-type':'application/json',
    'Accept':'application/json'
}

for proc in processes:
    proc.pid,proc.cpu_percent()

# file_name = 'exp1_3-alfa.json'
file_name_time = sys.argv[1]
file_name_process = sys.argv[2]

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
    # print(proc_info)

print (f"Elapsed time: {(end-start)}\nMemory rss: {memory_sum_rss}\nMemory vms: {memory_sum_vms}")
output_time_memory= f"Elapsed time: {(end-start)}\nMemory rss: {memory_sum_rss}\nMemory vms: {memory_sum_vms}"
with open(file_name_time, "w") as file:
        file.write(output_time_memory)


#print (f"Memory rss: {memory_sum_rss}")
#print (f"Memory vms: {memory_sum_vms}")


full_file_name = f"./data/{file_name_process}"
with open(full_file_name, 'w', encoding='utf-8') as f:
    json.dump(process_infos, f, ensure_ascii=False, indent=4)
