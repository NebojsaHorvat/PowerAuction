#!/bin/bash

experiment_name=$1


run_nodes_on_remote_machine() {
    local remote_hosts=("$@")    

    remote_user="nebojsa"
    remote_folder="/home/nebojsa/energies/project/${experiment_name}"
    for remote_host in "${remote_hosts[@]}"
    do
        echo "SSH connection to remote host: " $remote_host
        ssh "$remote_user@$remote_host" << EOF
        cd "\$HOME/energies/project/${experiment_name}"
        ./runnodes

EOF
    done
}

run_spring_server_on_remote_machine() {
    local remote_host=("$1")
    ssh $remote_host  "docker run  --network="host"  nhtacc/corda-energy-proxy"
}


#copy localy created folders to remote hosts
# remote_hosts=("omega" "alfa" "beta" "gama")
remote_hosts=( "alfa" )
run_nodes_on_remote_machine "${remote_hosts[@]}"

run_spring_server_on_remote_machine "omega"
