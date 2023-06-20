#!/bin/bash

# build new
./gradlew clean deployNodes

experiment_name=$1
mkdir $experiment_name

mkdir ./$experiment_name/omega
mkdir ./$experiment_name/alfa
mkdir ./$experiment_name/beta
mkdir ./$experiment_name/gama

copy_node_data_localy() {
    local nodes=("${@:1:$#-1}")   # Create a local array variable and assign the function arguments to it
    local node_name="${@: -1}"    
    cp build/nodes/runnodes ./$experiment_name/$node_name
    cp build/nodes/runnodes.jar ./$experiment_name/$node_name
    # Iterate through each node that goes to alfa cluster node
    for node in "${nodes[@]}"
    do
        node_config=$node"_node.conf"
        cp -r build/nodes/$node/  ./$experiment_name/$node_name
        cp build/nodes/$node_config  ./$experiment_name/$node_name
    done
}


scp_node_data_to_remote_machine() {
    local remote_hosts=("$@")    

    remote_user="nebojsa"
    remote_folder="/home/nebojsa/energies/project/${experiment_name}"
    for remote_host in "${remote_hosts[@]}"
    do
        echo "Copying data to " $remote_host
        folder="./${experiment_name}/${remote_host}/*"

        ssh $remote_host "mkdir -p $remote_folder"

        scp -r $folder "$remote_user@$remote_host:$remote_folder/"
    done
    

}




omega_nodes=("Notary" "PowerCompany")
copy_node_data_localy "${omega_nodes[@]}" "omega"

alfa_nodes=("Prosumer")
copy_node_data_localy "${alfa_nodes[@]}" "alfa"

beta_nodes=("Customer" "Producer")
copy_node_data_localy "${beta_nodes[@]}" "beta"

gama_nodes=("GridAuthority")
copy_node_data_localy "${gama_nodes[@]}" "gama"

#copy localy created folders to remote hosts
# remote_hosts=("omega" "alfa" "beta" "gama")
remote_hosts=("alfa")
scp_node_data_to_remote_machine "${remote_hosts[@]}"


