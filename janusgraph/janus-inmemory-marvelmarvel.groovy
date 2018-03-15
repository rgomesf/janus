// Create an in memory Janus Graph instance, define the schema and index and load marvel movies.
// This is intended to be loaded and run inside the Gremlin Console from the Janus
// Graph download. Usage :load janus-inmemory-marvel.groovy

println "\n=======================================";[]
println "Creating in-memory Janus Graph instance";[]
println "=======================================\n";[]
// Create a new graph instance
graph = JanusGraphFactory.open('inmemory')

println "\n===============";[]
println "Defining labels";[]
println "===============\n";[]
// Define edge labels and usage
mgmt = graph.openManagement()
mgmt.makeEdgeLabel('connection').multiplicity(MULTI).make()
mgmt.commit()

// Define vertex labels
mgmt = graph.openManagement()
mgmt.makeVertexLabel('movie').make()
mgmt.makeVertexLabel('actor').make()
mgmt.makeVertexLabel('character').make()
mgmt.makeVertexLabel('role').make()

mgmt.commit()

println "\n=============";[]
println "Creating keys";[]
println "=============\n";[]
// Define vertex property keys
mgmt = graph.openManagement()
mgmt.makePropertyKey('name').dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey('type').dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.commit()

// Define edge property keys
//mgmt = graph.openManagement()
//mgmt.makePropertyKey('labelE').dataType(Integer.class).cardinality(Cardinality.SINGLE).make()
//mgmt.commit()

println "\n==============";[]
println "Building index";[]
println "==============\n";[]

// Construct a composite index for a few commonly used property keys
graph.tx().rollback()
mgmt=graph.openManagement()

idx1 = mgmt.buildIndex('nameIndex',Vertex.class)
idx2 = mgmt.buildIndex('typeIndex',Vertex.class)

iname = mgmt.getPropertyKey('name')
itype = mgmt.getPropertyKey('type')

idx1.addKey(iname).buildCompositeIndex()
idx2.addKey(itype).buildCompositeIndex()

mgmt.commit()


println "\n=================================";[]
println "Waiting for the index to be ready";[]
println "=================================\n";[]

mgmt.awaitGraphIndexStatus(graph, 'nameIndex').
     status(SchemaStatus.REGISTERED).call()

mgmt.awaitGraphIndexStatus(graph, 'typeIndex').
     status(SchemaStatus.REGISTERED).call()

// Once the index is created force a re-index Note that a reindex is not strictly
// necessary here. It could be avoided by creating the keys and index as part of the
// same transaction. I did it this way just to show an example of re-indexing being
// done. A reindex is always necessary if the index is added after data has been
// loaded into the graph.

println "\n===========";[]
println "re-indexing";[]
println "===========\n";[]
mgmt = graph.openManagement()

mgmt.awaitGraphIndexStatus(graph, 'nameIndex').call()
mgmt.updateIndex(mgmt.getGraphIndex('nameIndex'), SchemaAction.REINDEX).get()

mgmt.awaitGraphIndexStatus(graph, 'typeIndex').call()
mgmt.updateIndex(mgmt.getGraphIndex('typeIndex'), SchemaAction.REINDEX).get()

mgmt.commit()

// Load the air-routes graph and display a few statistics.
// Not all of these steps use the index so Janus Graph will give us some warnings.
println "\n========================";[]
println "Loading air-routes graph";[]
println "========================\n";[]
graph.io(graphml()).readGraph('/work/janusgraph/scripts/marvel_movie_graph.graphml')
graph.tx().commit();[]

// Setup our traversal source object
g = graph.traversal()

// Display a few statistics
mov = g.V().has('type','movie').count().next();[]
act = g.V().has('type','actor').count().next();[]
cha = g.V().has('type','character').count().next();[]
con = g.E().hasLabel('connection').count().next();[]
edg = g.E().count().next();[]

println "Movies      : $mov";[]
println "Actors      : $act";[]
println "Characters  : $cha";[]
println "Connections : $con";[]
println "Edges       : $edg";[]

// Look at the properties, just as an exampl of how to do it!
println "\n========================";[]
println "Retrieving property keys";[]
println "========================\n";[]
mgmt = graph.openManagement()
types = mgmt.getRelationTypes(PropertyKey.class);[] 
types.each{println "$it\t: " + mgmt.getPropertyKey("$it").dataType() + " " + mgmt.getPropertyKey("$it").cardinality()};[]
mgmt.commit()   

def globals = [:]
globals << [m : graph.traversal()]