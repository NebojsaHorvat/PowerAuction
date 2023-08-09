#!/bin/bash
# ./gather-results [experiment_name]

experiment_name=$1
test_types=("createPP" "createAuction" "createBid")

experiment_data_save_folder="/home/nebojsa/Documents/Nauka/Energies/2023/Experiments"
combine_results_script_location="/home/nebojsa/Documents/Nauka/EnergiesProjekat/PowerAuctionBuildScripts-auto-exp-2/performance_scripts_remote/combine_results.py"
mkdir "${experiment_data_save_folder}/${experiment_name}"

mkdir "${experiment_data_save_folder}/${experiment_name}/createAuction"
mkdir "${experiment_data_save_folder}/${experiment_name}/createBid"
mkdir "${experiment_data_save_folder}/${experiment_name}/createPP"
mkdir "${experiment_data_save_folder}/${experiment_name}/raw_data_with_process_files"

mkdir "${experiment_data_save_folder}/${experiment_name}/raw_data_with_process_files/omega"
mkdir "${experiment_data_save_folder}/${experiment_name}/raw_data_with_process_files/alfa"
mkdir "${experiment_data_save_folder}/${experiment_name}/raw_data_with_process_files/beta"
mkdir "${experiment_data_save_folder}/${experiment_name}/raw_data_with_process_files/gama"


scp_experiment_data_from_remote_machine() {
    local remote_hosts=("$@")    
    remote_user="nebojsa"
    remote_folder="/home/nebojsa/energies/project/${experiment_name}"
    for remote_host in "${remote_hosts[@]}"
    do
        echo "Copying data from " $remote_host
        destination_folder="${experiment_data_save_folder}/${experiment_name}/raw_data_with_process_files/${remote_host}"
        source_folder="/home/nebojsa/energies/project/${experiment_name}/performance_scripts_remote/data/*"

        # scp -r nebojsa@omega:/home/nebojsa/energies/project/${experiment_name}/performance_scripts_remote/data/* .
        scp -r "${remote_user}@${remote_host}:${source_folder}" "${destination_folder}"
    done
}


run_gather_results_scripts_and_copy_data() {
    local remote_hosts=("$@")
    for test_type in "${test_types[@]}"
    do    
        for remote_host in "${remote_hosts[@]}"
        do
            echo "Copying script to data folder for $test_type from $remote_host"
            destination_file="${experiment_data_save_folder}/${experiment_name}/raw_data_with_process_files/${remote_host}/${test_type}/combine_results.py"
            destination_folder="${experiment_data_save_folder}/${experiment_name}/raw_data_with_process_files/${remote_host}/${test_type}/"
            cp "${combine_results_script_location}" "${destination_file}"
            echo "${combine_results_script_location}" 
            echo "${destination_file}"
            cd "${destination_folder}"
            python3 combine_results.py
            cp "${destination_folder}/combined_data.csv" "${experiment_data_save_folder}/${experiment_name}/${test_type}/${test_type}_${remote_host}.csv"
        done
    done
}

#copy experiment data from remote hosts to local machine
remote_hosts=("omega" "alfa" "beta" "gama")
scp_experiment_data_from_remote_machine "${remote_hosts[@]}" 
#combine data from different csv files and copy files in Experiment results repo folder
run_gather_results_scripts_and_copy_data  "${remote_hosts[@]}" 


