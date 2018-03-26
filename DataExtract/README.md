# Data Extraction

This is a couple Java classes that read data from a PostgreSQL and generate the GraphML file.
Also will generate .graph files with the Gremlin syntax to create the graph.

------------------------

The [Extract class](https://github.com/rgomesf/janus/blob/master/DataExtract/src/enei/data/extract/ExtractData.java) can be run with three options:
* readdb - it will read data froom db and generate the GraphML and .graph files
* writeremote - will write the .graph files in a Gremlin server
* both - will execute the previous steps

To run, just import DataExtract folder as a Java project. (.project files were also added to be easier to import)


After writing graph in Gremlin Server + JanusGraph it will be something very similiar to this:
<br>
![alt text](https://github.com/rgomesf/janus/blob/master/DataExtract/assets/graphfish.png "Graph fish")


##### Pre-requisites

To import a Graph is required that:
* the [janusgraph docker image](https://github.com/rgomesf/janus/tree/master/janusgraph) is up and running
* the [postgresql docker image](https://github.com/rgomesf/janus/tree/master/postgresqldata) is up and running


##### Project structure

assets folder has the Graph fish image
conf folder has the configuration files required to connect an Gremlin server and execute stuff.
libs folder has the required libs to execute export from PostgreSQL and import the Graph
src folder java class files



