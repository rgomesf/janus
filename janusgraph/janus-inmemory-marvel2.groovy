// Criar um novo graph em memoria
graph = JanusGraphFactory.open('inmemory')

//Definir o schema. Não é obrigatorio, nas evita que o janusGraph ponha todas a propriedades como Object.clas

// Definir as labels dos edges, que têm que ser unicas em todo o graph.
mgmt = graph.openManagement()
mgmt.makeEdgeLabel('ACTS_IN').multiplicity(MULTI).make() //uma pessoa pode entrar em varios filmes
mgmt.makeEdgeLabel('DIRECTS').multiplicity(MULTI).make()
mgmt.commit()

// Definir as labels dos nodes (vertex)
mgmt = graph.openManagement()
mgmt.makeVertexLabel('movie').make()
mgmt.makeVertexLabel('person').make()
mgmt.commit()

// Definir as propriedados dos nodes
mgmt = graph.openManagement()
mgmt.makePropertyKey('name').dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey('type').dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey('rating').dataType(Float.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey('runtime').dataType(Integer.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey('year').dataType(Integer.class).cardinality(Cardinality.SINGLE).make()
mgmt.commit()

// Define também as propriedades para os edges, neste caso só temos uma 
mgmt = graph.openManagement()
mgmt.makePropertyKey('character').dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.commit()

// JanusGraph suporta graph indexes e vertex indexes (http://docs.janusgraph.org/latest/indexes.html)
// Vamos só criar alguns indices compostos como exemplo de como tornar mais rapidas as pesquisas por valores exatos

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

// Apos a criação dos indices, temos que esperar que eles estejam prontos
mgmt.awaitGraphIndexStatus(graph, 'nameIndex').
     status(SchemaStatus.REGISTERED).call()

mgmt.awaitGraphIndexStatus(graph, 'typeIndex').
     status(SchemaStatus.REGISTERED).call()
	 
mgmt.awaitGraphIndexStatus(graph, 'yearIndex').
     status(SchemaStatus.REGISTERED).call()

mgmt.awaitGraphIndexStatus(graph, 'characterIndex').
     status(SchemaStatus.REGISTERED).call()


// Chegou a altura de carregar o nosso graphml
// Nota: Não é garantido que o JanusGraph use os ids que estão no graphml. Normalmente ele gera novos ao inserir os nodes e edges. 
graph.io(graphml()).readGraph('/work/janusgraph/scripts/marvel_movie_graph2.graphml')
graph.tx().commit();[]

// E já podemos mostrar já algumas estatísticas do graph que acabamos de carregar
g = graph.traversal()
mov = g.V().has('type','movie').count().next();[]
per = g.V().has('type','person').count().next();[]
cha = g.E().has('character').count().next();[]
edg = g.E().count().next();[]

println "Movies      : $mov";[]
println "Persons     : $per";[]
println "Characters  : $cha";[]
println "Edges       : $edg";[]


// Por último definimps TraversalSource do graph para o g, para podermos fazer as nossas queries.
def globals = [:]
globals << [g : graph.traversal()]