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
    ssh $remote_host  'docker exec $(docker ps --filter "ancestor=nhtacc/corda-energy-proxy" --format "{{.ID}}") sh -c "kill 1"'
}


#copy localy created folders to remote hosts
remote_hosts=("omega" "alfa" "beta" "gama")
# remote_hosts=( "alfa" )
stop_nodes_on_remote_machine "${remote_hosts[@]}"
stop_spring_server_on_remote_machine "omega"