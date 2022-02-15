import requests
import time
import os
import psutil
# import pprofile

# Make sure that prosumer is acitive party on server
response = requests.post("http://localhost:8085/api/auction/switch-party/prosumer")

p = psutil.Process(os.getpid())
#Create 1 powerPromise
headers={
    'Content-type':'application/json', 
    'Accept':'application/json'
}

data='{"powerSuppliedInKW":".0","deliveryTime":"16-02-2022 04:52:10 PM", "powerSupplyDurationInMin":"60.0"}' 
# profiler = pprofile.Profile()
# with profiler:
psutil.cpu_percent()
start = time.time()
for x in range(1):
    response = requests.post("http://localhost:8085/api/auction/asset/create",
    data=data,
    headers=headers
    )
end = time.time()
print ( f"Percent of CPU used: {psutil.cpu_percent(interval=0.0)}%" )

print(f"Memory usage {p.memory_info().rss/ 1024 ** 2} MB")
print(f"Memory usage {psutil.virtual_memory()}")
print(f"Swap memory usage {psutil.swap_memory()}")

print (f"Elapsed time {(end-start)}")

# profiler.dump_stats("./create_one_PP")
