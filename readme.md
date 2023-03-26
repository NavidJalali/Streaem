# Streaem Home Assignment

## Problem
At Streaem we are building a platform that connects retailers and brands in ways that automate
several of their interactions. A core part of this capability is how we integrate with the product
feed from the retailer. We need to ingest this into our system and then use this data to enable
the brands to choose products against which they wish to build monetisation campaigns.
For this test we’d like you to build a stand-alone service that provides the following functionality:
1. On startup consumes an existing JSON feed from an external API and stores this feed in
   memory.
2. Exposes a REST endpoint to retrieve the information for a single product from the feed
3. Exposes a REST endpoint to retrieve all products of a given category, optionally filtered
   only by those in stock.
4. Exposes a REST endpoint to allow for updating of any of the fields of a single product
5. Exposes a REST endpoint to set the current stock level for a given product
   This is representative of some of the data management processes we build at Streaem. Before
   any online auctions can happen we need collect and continuously update the product data
   available for a given retailer. Ad hoc updates to stock levels will be increasingly important as we
   wish to reduce the chances of serving ads for products that are not currently in stock.

Product feed

To provide a product feed you can use the following image on DockerHub:
https://hub.docker.com/repository/docker/garryturk/mock-product-data
This can be ran by the following command line:

```shell
docker run --rm --name mpd -p 4001:4001 garryturk/mock-product-data
```
When ran this will expose a REST endpoint on port 4001, the following will retrieve an array of
100 product descriptions:

```shell
curl localhost:4001/productdata
```

Each product item is a JSON object with 4 fields - the product name, category, price and
description. Assume all products start with a stock level of 0.

## Solution
I tried to keep the solution as simple as possible. I used the following technologies:
- Java 19
- Tapir to define the endpoints, I used the endpoints to automatically generate the server routes as well as a swagger
file which you can find in the root of the repository called `api.yaml`
- Http4s to implement a non-blocking server
- ZIO as a functional effect to handle concurrency. I also used the STM module to handle the concurrent access to the
in-memory store using software transactional memory.
- ZIO Json for the JSON serialization and deserialization
- ZIO Test for the unit tests

There are test cases for all the endpoints. In the interest of time I have only tested the most basic cases. 
Also, due to time crunch I skipped setting up configuration and logging. The external service's endpoint is hardcoded.

## How to run
To run the application you can use the following command:
```shell
sbt run
```
For this you will need, a new(ish) JDK, basically 11 and above should work perfectly fine.
You will also need SBT installed. You can install it by using the following commands based on your OS:
```shell
brew install sbt
sudo apt-get install sbt
sudo yum install sbt
```

I have also included a jar built with Java 19 and a Dockerfile + docker-compose file to run the application in a container.
You can run it by using the following commands:

```shell
docker-compose up --build
```
And then you can clean up the containers by using the following command:
```shell
docker-compose down
```

## How to test
To run the tests you can use the following command:
```shell
sbt test
```
