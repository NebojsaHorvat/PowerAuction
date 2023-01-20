## Usage
## Pre-Requisites
For development environment setup, please refer to: [Setup Guide](https://docs.corda.net/getting-set-up.html).


### Running the nodes:
Open a terminal and go to the project root directory and type: (to deploy the nodes using bootstrapper)
```
./gradlew clean deployNodes
```
Then type: (to run the nodes)
```
./build/nodes/runnodes
```

### Running the nodes in docker:
Open a terminal and go to the project root directory and type: (to deploy the nodes using docker)
```
./gradlew prepareDockerNodes
```
Position in build/nodes folder Then type: (to run the nodes in docker)
```
docker-compose up
```

### Running the client:

The client can be run by executing the below command from the project root:

`./gradlew runAuctionClient`

Please make sure that the nodes are already running before starting the client.
The client can be accessed at http://localhost:8085/
