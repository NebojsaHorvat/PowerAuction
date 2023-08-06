#!/bin/bash
# ./gather-results [experiment_name]

## TODO NOT TESTED !!!!!!!!!!!!!!!!!

experiment_name=$1
experiment_data_save_folder="/home/nebojsa/Documents/Nauka/Energies/2023/Experiments"
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
    # TODO remove exp folder if it exits
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




#copy experiment data from remote hosts to local machine
remote_hosts=("omega" "alfa" "beta" "gama")
scp_experiment_data_from_remote_machine "${remote_hosts[@]}" 


