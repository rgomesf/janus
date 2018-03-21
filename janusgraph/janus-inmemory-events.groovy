// Create an in memory Janus Graph instance, define the schema and index and load air-routes.
// This is intended to be loaded and run inside the Gremlin Console from the Janus
// Graph download. Usage :load janus-inmemory.groovy

println "\n=======================================";[]
println "Creating in-memory Janus Graph instance";[]
println "=======================================\n";[]
// Create a new graph instance
graph = JanusGraphFactory.open('inmemory')

// Load the air-routes graph and display a few statistics.
// Not all of these steps use the index so Janus Graph will give us some warnings.
println "\n========================";[]
println "Loading Events graph";[]
println "========================\n";[]
graph.io(graphml()).readGraph('/work/janusgraph/scripts/events.graphml')
graph.tx().commit();[]



def globals = [:]
globals << [evt : graph.traversal()]