#!/bin/bash

experiment_name=$1

run_non_exec_scripts_on_remote_machine() {
    local remote_hosts=("alfa" "beta" "gama")   
    local file_name_memory_sum=$1
    local file_name_memory_process=$2
    remote_user="nebojsa"
    remote_folder="/home/nebojsa/energies/project/${experiment_name}/performace_scripts/"
    for remote_host in "${remote_hosts[@]}"
    do
        echo "SSH connection to remote host: " $remote_host
        ssh "$remote_user@$remote_host" << EOF
            cd "\$HOME/energies/project/${experiment_name}/performace_scripts/"
            source bin/activate
            python3 performance_non_exec.py ${file_name_memory_sum} ${file_name_memory_process}
EOF
    done
}

run_create_PP_scripts_on_remote_machine() {
    local remote_host="omega"   
    local file_name_memory_sum=$1
    local file_name_memory_process=$2
    local number_of_operations=$3
    remote_user="nebojsa"
    remote_folder="/home/nebojsa/energies/project/${experiment_name}/performace_scripts/"

    echo "SSH connection to remote host: " $remote_host
    ssh "$remote_user@$remote_host" << EOF
        cd "\$HOME/energies/project/${experiment_name}/performace_scripts/"
        source bin/activate
        python3 create_power_promise_performance.py ${file_name_memory_sum} ${file_name_memory_process} ${number_of_operations}
EOF
}

run_create_auction_scripts_on_remote_machine() {
    local remote_host="omega"   
    local file_name_memory_sum=$1
    local file_name_memory_process=$2
    local number_of_operations=$3
    local PP_id=$4
    remote_user="nebojsa"
    remote_folder="/home/nebojsa/energies/project/${experiment_name}/performace_scripts/"

    echo "SSH connection to remote host: " $remote_host
    ssh "$remote_user@$remote_host" << EOF
        cd "\$HOME/energies/project/${experiment_name}/performace_scripts/"
        source bin/activate
        python3 create_auction_performance.py ${file_name_memory_sum} ${file_name_memory_process} ${number_of_operations} ${PP_id}
EOF
}

run_create_bid_scripts_on_remote_machine() {
    local remote_host="omega"   
    local file_name_memory_sum=$1
    local file_name_memory_process=$2
    local number_of_operations=$3
    local auction_id=$4
    remote_user="nebojsa"
    remote_folder="/home/nebojsa/energies/project/${experiment_name}/performace_scripts/"

    echo "SSH connection to remote host: " $remote_host
    ssh "$remote_user@$remote_host" << EOF
        cd "\$HOME/energies/project/${experiment_name}/performace_scripts/"
        source bin/activate
        python3 create_bid_performance.py ${file_name_memory_sum} ${file_name_memory_process} ${number_of_operations} ${auction_id}
EOF
}

