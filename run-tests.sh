#!/bin/bash
# ./run-tests.sh [experiment_name] [experiment try number]

experiment_name=$1
repetition_number=$2

run_non_exec_scripts_on_remote_machine() {
    local remote_hosts=("alfa" "beta" "gama")   
    local file_name_memory_sum=$1
    local file_name_memory_process=$2
    remote_user="nebojsa"
    for remote_host in "${remote_hosts[@]}"
    do
        echo "SSH connection to remote host: " $remote_host
        ssh "$remote_user@$remote_host" << EOF
            cd "\$HOME/energies/project/${experiment_name}/performance_scripts_remote/"
            source bin/activate
            python3 performance_non_exec.py "${file_name_memory_sum}_${remote_host}" "${file_name_memory_process}_${remote_host}"
EOF
    done
}

run_create_PP_scripts_on_remote_machine() {
    local remote_host="omega"   
    local file_name_memory_sum=$1
    local file_name_memory_process=$2
    local number_of_operations=$3
    remote_user="nebojsa"

    echo "SSH connection to remote host: " $remote_host
    ssh "$remote_user@$remote_host" << EOF
        cd "\$HOME/energies/project/${experiment_name}/performance_scripts_remote/"
        source bin/activate
        python3 create_power_promise_performance.py "${file_name_memory_sum}_${remote_host}" "${file_name_memory_process}_${remote_host}" ${number_of_operations}
EOF
}

run_create_auction_scripts_on_remote_machine() {
    local remote_host="omega"   
    local file_name_memory_sum=$1
    local file_name_memory_process=$2
    local number_of_operations=$3
    local PP_id=$4
    remote_user="nebojsa"

    echo "SSH connection to remote host: " $remote_host
    ssh "$remote_user@$remote_host" << EOF
        cd "\$HOME/energies/project/${experimentexp_name}/performance_scripts_remote/"
        source bin/activate
        python3 create_auction_performance.py "${file_name_memory_sum}_${remote_host}" "${file_name_memory_process}_${remote_host}" ${number_of_operations} ${PP_id}
EOF
}

run_create_bid_scripts_on_remote_machine() {
    local remote_host="omega"   
    local file_name_memory_sum=$1
    local file_name_memory_process=$2
    local number_of_operations=$3
    local auction_id=$4
    remote_user="nebojsa"

    echo "SSH connection to remote host: " $remote_host
    ssh "$remote_user@$remote_host" << EOF
        cd "\$HOME/energies/project/${experiment_name}/performance_scripts_remote/"
        source bin/activate
        python3 create_bid_performance.py "${file_name_memory_sum}_${remote_host}" "${file_name_memory_process}_${remote_host}" ${number_of_operations} ${auction_id}
EOF
}

run_issue_cache_to_producer_producer() {
    local remote_host="omega"   
    remote_user="nebojsa"

    echo "SSH connection to remote host: " $remote_host
    ssh "$remote_user@$remote_host" << EOF
    curl --noproxy '*' --header "Content-Type: application/json" --request POST --data '{"party":"producer","amount":"10000"}' http://localhost:8085/api/auction/issueCash
EOF
}

# test_numbers=(1 10 100 200)
test_numbers=(1)

file_name_memory_base="memory_${experiment_name}_try${repetition_number}_transactions"
file_name_process_base="process_${experiment_name}_try${repetition_number}_transactions"

# In order to crete prower promises we need to have sufficent amount of cash
run_issue_cache_to_producer_producer

# Run create PP tests
for number_of_tests in "${test_numbers[@]}"
do
    file_name_memory="${file_name_memory_base}_cretePP_${number_of_tests}"
    file_name_process="${file_name_process_base}_cretePP_${number_of_tests}"
    run_create_PP_scripts_on_remote_machine "${file_name_memory}" "${file_name_process}" "${number_of_tests}"
    # run_non_exec_scripts_on_remote_machine "${file_name_memory}" "${file_name_process}"
done

