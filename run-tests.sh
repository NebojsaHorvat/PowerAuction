#!/bin/bash
# ./run-tests.sh [experiment_name] [experiment tries number]

experiment_name=$1
repetition_number=$2

run_non_exec_scripts_on_remote_machine() {
    local remote_hosts=("alfa" "beta" "gama")   
    local file_name_memory_sum=$1
    local file_name_memory_process=$2
    local save_folder=$3
    remote_user="nebojsa"
    for remote_host in "${remote_hosts[@]}"
    do
        echo "SSH connection to remote host: " $remote_host
        ssh "$remote_user@$remote_host" << EOF
            cd "\$HOME/energies/project/${experiment_name}/performance_scripts_remote/"
            source bin/activate
            python3 performance_non_exec.py "${save_folder}${file_name_memory_sum}_${remote_host}" "${save_folder}${file_name_memory_process}_${remote_host}"
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
        python3 create_power_promise_performance.py "createPP/${file_name_memory_sum}_${remote_host}" "createPP/${file_name_memory_process}_${remote_host}" ${number_of_operations}
EOF
}

run_create_auction_scripts_on_remote_machine() {
    local remote_host="omega"   
    local file_name_memory_sum=$1
    local file_name_memory_process=$2
    local number_of_operations=$3
    remote_user="nebojsa"

    echo "SSH connection to remote host: " $remote_host
    ssh "$remote_user@$remote_host" << EOF
        cd "\$HOME/energies/project/${experiment_name}/performance_scripts_remote/"
        source bin/activate
        python3 create_auction_performance.py "createAuction/${file_name_memory_sum}_${remote_host}" "createAuction/${file_name_memory_process}_${remote_host}" ${number_of_operations} 
EOF
}

run_create_bid_scripts_on_remote_machine() {
    local remote_host="omega"   
    local file_name_memory_sum=$1
    local file_name_memory_process=$2
    local number_of_operations=$3
    remote_user="nebojsa"

    echo "SSH connection to remote host: " $remote_host
    ssh "$remote_user@$remote_host" << EOF
        cd "\$HOME/energies/project/${experiment_name}/performance_scripts_remote/"
        source bin/activate
        python3 create_bid_performance.py "createBid/${file_name_memory_sum}_${remote_host}" "createBid/${file_name_memory_process}_${remote_host}" ${number_of_operations}
EOF
}

run_issue_cache_to_producer_producer() {
    local remote_host="omega"   
    remote_user="nebojsa"

    echo "SSH connection to remote host: " $remote_host
    ssh "$remote_user@$remote_host" << EOF
    curl --noproxy '*' --request POST http://localhost:8085/api/auction/switch-party/producer
    curl --noproxy '*' --header "Content-Type: application/json" --request POST --data '{"party":"producer","amount":"10000"}' http://localhost:8085/api/auction/issueCash
EOF
}

# Create list which will conteaind all 
tries=()
for (( i = 1; i <= repetition_number; i++ )); do
    # Add the incremented number to the array
    tries+=("$i")
done

./run-nodes-on-remote-servers.sh "${experiment_name}"

#################################### Crete power promise tests

# # In order to crete prower promises we need to have sufficent amount of cash
# test_numbers=(1 10 100)

# for number_of_try in "${tries[@]}"
# do   
#     file_name_memory_base="memory_${experiment_name}_try${number_of_try}_transactions"
#     file_name_process_base="process_${experiment_name}_try${number_of_try}_transactions"
#     save_folder="createPP/"  

#     run_issue_cache_to_producer_producer
#     sleep 10s
#     # Run create PP tests
#     for number_of_tests in "${test_numbers[@]}"
#     do
#         file_name_memory="${file_name_memory_base}_cretePP_${number_of_tests}"
#         file_name_process="${file_name_process_base}_cretePP_${number_of_tests}"
#         run_create_PP_scripts_on_remote_machine "${file_name_memory}" "${file_name_process}" "${number_of_tests}"
#         run_non_exec_scripts_on_remote_machine "${file_name_memory}" "${file_name_process}" "${save_folder}"
#     done
    
#     # Restart network before next bach of tests
#     ./stop-nodes-on-remote-servers.sh "${experiment_name}"
#     ./run-nodes-on-remote-servers.sh "${experiment_name}"
# done

#################################### Crete auction tests


test_numbers=(1 10 100)
# test_numbers=(1)

for number_of_try in "${tries[@]}"
do   
    file_name_memory_base="memory_${experiment_name}_try${number_of_try}_transactions"
    file_name_process_base="process_${experiment_name}_try${number_of_try}_transactions"
    save_folder="createAuction/"
    run_issue_cache_to_producer_producer
    sleep 10s
    # Run create Auction tests
    for number_of_tests in "${test_numbers[@]}"
    do
        echo "RUNIGN TESTS"
        file_name_memory="${file_name_memory_base}_creteAuction_${number_of_tests}"
        file_name_process="${file_name_process_base}_creteAuction_${number_of_tests}"
        run_create_auction_scripts_on_remote_machine "${file_name_memory}" "${file_name_process}" "${number_of_tests}"
        run_non_exec_scripts_on_remote_machine "${file_name_memory}" "${file_name_process}" "${save_folder}"
    done
    
    # Restart network before next bach of tests
    ./stop-nodes-on-remote-servers.sh "${experiment_name}"
    ./run-nodes-on-remote-servers.sh "${experiment_name}"
done

#################################### Crete Bid tests

# test_numbers=(1 10 100)
# # test_numbers=(1)

# for number_of_try in "${tries[@]}"
# do   
#     file_name_memory_base="memory_${experiment_name}_try${number_of_try}_transactions"
#     file_name_process_base="process_${experiment_name}_try${number_of_try}_transactions"
#     save_folder="createBid/"
#     run_issue_cache_to_producer_producer
#     sleep 10s
#     # Run create Auction tests
#     for number_of_tests in "${test_numbers[@]}"
#     do
#         echo "RUNIGN TESTS"
#         file_name_memory="${file_name_memory_base}_creteBid_${number_of_tests}"
#         file_name_process="${file_name_process_base}_creteBid_${number_of_tests}"
#         run_create_bid_scripts_on_remote_machine "${file_name_memory}" "${file_name_process}" "${number_of_tests}"
#         run_non_exec_scripts_on_remote_machine "${file_name_memory}" "${file_name_process}" "${save_folder}"
#     done
    
#     # Restart network before next bach of tests
#     ./stop-nodes-on-remote-servers.sh "${experiment_name}"
#     ./run-nodes-on-remote-servers.sh "${experiment_name}"
# done

./stop-nodes-on-remote-servers.sh "${experiment_name}"