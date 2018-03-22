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
mgmt.makeEdgeLabel('ACTS_IN').multiplicity(MULTI).make()
mgmt.makeEdgeLabel('DIRECTS').multiplicity(SIMPLE).make()
mgmt.commit()

// Define vertex labels
mgmt = graph.openManagement()
mgmt.makeVertexLabel('movie').make()
mgmt.makeVertexLabel('person').make()

mgmt.commit()

println "\n=============";[]
println "Creating keys";[]
println "=============\n";[]
// Define vertex property keys
mgmt = graph.openManagement()
mgmt.makePropertyKey('name').dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey('type').dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey('rating').dataType(Float.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey('runtime').dataType(Integer.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey('year').dataType(Integer.class).cardinality(Cardinality.SINGLE).make()
mgmt.commit()

// Define edge property keys
mgmt = graph.openManagement()
mgmt.makePropertyKey('character').dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey('labelE').dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.commit()


// Look at the properties
mgmt = graph.openManagement()
types = mgmt.getRelationTypes(PropertyKey.class)
types.each{println "$it\t: " +
                    mgmt.getPropertyKey("$it").dataType() +
                    " " + mgmt.getPropertyKey("$it").cardinality()}

mgmt.commit()

println "\n==============";[]
println "Building index";[]
println "==============\n";[]

// Construct a composite index for a few commonly used property keys
graph.tx().rollback()
mgmt=graph.openManagement()

idx1 = mgmt.buildIndex('nameIndex',Vertex.class)
idx2 = mgmt.buildIndex('typeIndex',Vertex.class)
idx3 = mgmt.buildIndex('yearIndex',Vertex.class)
idx4 = mgmt.buildIndex('characterIndex',Edge.class)

iname = mgmt.getPropertyKey('name')
itype = mgmt.getPropertyKey('type')
iyear = mgmt.getPropertyKey('year')
ichar = mgmt.getPropertyKey('character')

idx1.addKey(iname).buildCompositeIndex()
idx2.addKey(itype).buildCompositeIndex()
idx3.addKey(iyear).buildCompositeIndex()
idx4.addKey(ichar).buildCompositeIndex()

mgmt.commit()


println "\n=================================";[]
println "Waiting for the index to be ready";[]
println "=================================\n";[]

mgmt.awaitGraphIndexStatus(graph, 'nameIndex').
     status(SchemaStatus.REGISTERED).call()

mgmt.awaitGraphIndexStatus(graph, 'typeIndex').
     status(SchemaStatus.REGISTERED).call()
	 
mgmt.awaitGraphIndexStatus(graph, 'yearIndex').
     status(SchemaStatus.REGISTERED).call()

mgmt.awaitGraphIndexStatus(graph, 'characterIndex').
     status(SchemaStatus.REGISTERED).call()

// Load the Marvel Movies 2 graph and display a few statistics.
// Not all of these steps use the index so Janus Graph will give us some warnings.
println "\n============================";[]
println "Loading marvel movies graph 2";[]
println "============================\n";[]
graph.io(graphml()).readGraph('/work/janusgraph/scripts/marvel_movie_graph2.graphml')
graph.tx().commit();[]

// Setup our traversal source object
g = graph.traversal()

// Display a few statistics
mov = g.V().has('type','movie').count().next();[]
per = g.V().has('type','person').count().next();[]
cha = g.E().has('character').count().next();[]
edg = g.E().count().next();[]

println "Movies      : $mov";[]
println "Persons     : $per";[]
println "Characters  : $cha";[]
println "Edges       : $edg";[]

def globals = [:]
globals << [g : graph.traversal()]