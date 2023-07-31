#!/bin/bash
# ./run-nodes-on-remote-servers.sh [experiment_name]

experiment_name=$1


run_nodes_on_remote_machine() {
    local remote_hosts=("$@")    

    remote_user="nebojsa"
    remote_folder="/home/nebojsa/energies/project/${experiment_name}"
    for remote_host in "${remote_hosts[@]}"
    do
        echo "SSH connection to remote host: " $remote_host
        ssh -tt "$remote_user@$remote_host" << EOT
        cd "\$HOME/energies/project/${experiment_name}/performance_scripts_remote/"
        if [ -d "lib" ]; then
            echo "Venv and python3 requirements exist"
        else
            echo "Creating venv in folder and installing python3 requirements."
            python3 -m venv .
            source ./bin/activate
            pip install -r requirements.txt 
        fi

        cd ..
        ./runnodes 
exit
EOT
    
    done
}

run_spring_server_on_remote_machine() {
    local remote_host=("$1")
    ssh $remote_host  "docker run  --network="host"  nhtacc/corda-energy-proxy:auto-exp-2" &
}


#copy localy created folders to remote hosts
remote_hosts=("omega" "alfa" "beta" "gama")
run_nodes_on_remote_machine "${remote_hosts[@]}"
echo "Sleep for 30 seconds and wait for nodes to startup on remote machines"
sleep 30s
run_spring_server_on_remote_machine "omega"
echo "Sleep for 40 seconds and wait for proxy to startup on remote machines"
sleep 40s
