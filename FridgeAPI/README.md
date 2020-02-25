![Java CI](https://github.com/josem-athew/demos/workflows/Java%20CI/badge.svg?branch=master)
# Fridge API

A RESTful API microservice to facilitate 
* What's in the fridge?
* Story: I want to add, update, view, delete, or change what's in the fridge
* Constraint: implement a guardrail to prevent more than 12 cans of soda in the fridge at any time
* Constraint: there are multiple refrigerators

### Tech

* Java 11
* RatPack 1.7.6
* Spring Boot 2.2.4


### Build, Install, Run
* Maven

```sh
$ cd FridgeAPI
$ mvn clean install
$ java -jar FridgeAPI-1.0-SNAPSHOT.jar
```

### APIs

* GET All http://{host}:9000/fridges
* GET Fridge http://{host}:9000/fridges/:fridgename
* GET Item http://{host}:9000/fridges/:fridgename/:itemname
* PUT Fridge http://{host}:9000/fridges/:fridgename
* PUT Item http://{host}:9000/fridges/:fridgename/:itemname
* DELETE Fridge http://{host}:9000/fridges/:fridgename
* DELETE Item http://{host}:9000/fridges/:fridgename/:itemname


### Todos

 - Swagger/OpenAPI spec doc
 - Improve code coverage
 - Replace simple impl with secure authn/authz  
