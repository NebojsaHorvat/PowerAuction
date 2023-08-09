#!/bin/bash
# ./stop-nodes-on-remote-servers.sh [experiment_name]

experiment_name=$1


stop_nodes_on_remote_machine() {
    local remote_hosts=("$@")    

    remote_user="nebojsa"
    remote_folder="/home/nebojsa/energies/project/${experiment_name}"
    for remote_host in "${remote_hosts[@]}"
    do
        echo "SSH connection to remote host: " $remote_host
        ssh -tt "$remote_user@$remote_host" << EOT
        killall -9 java
exit
EOT
    
    done
}

stop_spring_server_on_remote_machine() {
    local remote_host=("$1")
    local remote_user="nebojsa"
    ssh $remote_host  'docker exec $(docker ps --filter "ancestor=nhtacc/corda-energy-proxy:auto-exp-4" --format "{{.ID}}") sh -c "kill 1"'
}

# Do this in order to rewrite node persitance.mv.db files and remove all data from nodes
scp_node_persitance_files_to_remote_machine() {
    local remote_hosts=("$@")    
    # TODO remove exp folder if it exits
    remote_user="nebojsa"
    remote_folder_base="/home/nebojsa/energies/project/${experiment_name}"
    for remote_host in "${remote_hosts[@]}"
    do
        folders=($(find "./${experiment_name}/${remote_host}" -mindepth 1 -maxdepth 1 -type d -not -name "performance_scripts_remote" -exec basename {} \;))
        # echo "Folders in $remote_host folder in local: ${folders[@]} "
        for folder in "${folders[@]}"; do
            echo "Rewriting presistance.mv.db file on host $remote_host in folder $folder"
            echo "scp local file: ./${experiment_name}/${remote_host}/$folder/persitance.mv.db     to folder: $remote_folder_base/$folder"
            scp  "./${experiment_name}/${remote_host}/$folder/persistence.mv.db" "./${experiment_name}/${remote_host}/$folder/persistence.trace.db"  "$remote_user@$remote_host:$remote_folder_base/$folder"
        done
        # echo "Copying data to " $remote_host
        # folder="./${experiment_name}/${remote_host}/*"

        # ssh $remote_host "mkdir -p $remote_folder"

        # scp -r $folder "$remote_user@$remote_host:$remote_folder/"
    done
}


#copy localy created folders to remote hosts
remote_hosts=("omega" "alfa" "beta" "gama")
stop_nodes_on_remote_machine "${remote_hosts[@]}"
stop_spring_server_on_remote_machine "omega"
# Deleti databases of all nodes
scp_node_persitance_files_to_remote_machine "${remote_hosts[@]}"
