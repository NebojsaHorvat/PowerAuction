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
docker run --network="host"  nhtacc/corda-energy-proxy
```
Push docker image to repository:
```
docker login -u nhtacc
docker push nhtacc/corda-energy-proxy
```


// TODO 
Naterati skripte da mere peformance aplikacije na klasteru, one mere java procese, sad treba nekako da mere docker kontejnere i njihov memory i processor usage


Plan istrazivanja:
 - exp0 - on je reprodukcija onog proslog eksperimenta samo u distribuiranom okruzenju
 - exp1 - on je dokerizovano sve pa onda merim performanse
 - exp3 - on je prosirena aplikacija pokrenuta na host masini
 - exp4 - on je prosirena aplikacija koja je dokerizovana


Pitanja:
- da li su dovoljna ova 4 eksperimenta koja sam naveo gore?
- da li mozemo da prodjemo bez ubacivanja necega sto se vrti na clud-u? To moze da bude komplikacija zato sto je nas klaster sakriven iza jedne javne IP adrese.
- da li je ok da na drugi nacim merim performanse dokerizovane aplikacije i na koji to nacin da radim?
- da li je ok da probam da miksam raspored nodova po fizickim masinama na klasteru?
- da li je ok ovaj format rezultata koje dajem?


 