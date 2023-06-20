#!/bin/bash
# napraviti funkciju od ovoga i dodati u prepare-build-for-server
experiment_name=$1

remote_user="nebojsa"
remote_host="alfa"
remote_folder="/home/nebojsa/energies/project/${experiment_name}"
folder="./${experiment_name}/${remote_host}/*"

ssh $remote_host "mkdir -p $remote_folder"

scp -r $folder "$remote_user@$remote_host:$remote_folder/"