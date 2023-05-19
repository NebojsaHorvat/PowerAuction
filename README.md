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

```
./gradlew runAuctionClient
```

Please make sure that the nodes are already running before starting the client.
The client can be accessed at http://localhost:8085/


### Dockerising the client:
We have created Dockerfile which is used for puting client spring server into docker container. To build the image first build the client app:
```
./gradlew buildAuctionClient
```

Create docker image:
```
docker build --build-arg JAR_FILE=build/libs/*.jar -t nhtacc/corda-energy-proxy .
```

Run dockerised client:
```
docker run -p --network="host"  nhtacc/corda-energy-proxy
```
Push docker image to repository:
```
docker login -u nhtacc
docker push nhtacc/corda-energy-proxy
```

Run docker container with host network:
```
docker run --network="host"  ftn/corda-energy-proxy
```

