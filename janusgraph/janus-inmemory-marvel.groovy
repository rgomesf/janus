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
mgmt.makePropertyKey('character').dataType(String.class).cardinality(Cardinality.SINGLE).make() //Vamos assumir que só tem uma personagem por filme
mgmt.commit()

// JanusGraph suporta Composite Index e Mixed Index (http://docs.janusgraph.org/latest/indexes.html)
// Composite Index só suportam igualdades. São normalmente mais rapidos que os Mixed indexes.
// Mixed indexes permitem mais condições para alem da igualdade, mas precisam de um index backend (Não estamos a usar)

graph.tx().rollback() // Garantir que não está nenhuma transação em execução
mgmt=graph.openManagement()

iname = mgmt.getPropertyKey('name')
itype = mgmt.getPropertyKey('type')
iyear = mgmt.getPropertyKey('year')
ichar = mgmt.getPropertyKey('character')

mgmt.buildIndex('byName',Vertex.class).addKey(iname).buildCompositeIndex()
mgmt.buildIndex('byType',Vertex.class).addKey(itype).buildCompositeIndex()
mgmt.buildIndex('byYear',Vertex.class).addKey(iyear).buildCompositeIndex()
mgmt.buildIndex('byNameAndType', Vertex.class).addKey(iname).addKey(itype).buildCompositeIndex()
mgmt.buildIndex('byCharacter',Edge.class).addKey(ichar).buildCompositeIndex()

mgmt.commit()

// Apos a criação dos indices, temos que esperar que eles estejam prontos
mgmt.awaitGraphIndexStatus(graph, 'byName').status(SchemaStatus.REGISTERED).call()
mgmt.awaitGraphIndexStatus(graph, 'byType').status(SchemaStatus.REGISTERED).call()
mgmt.awaitGraphIndexStatus(graph, 'byYear').status(SchemaStatus.REGISTERED).call()
mgmt.awaitGraphIndexStatus(graph, 'byNameAndType').status(SchemaStatus.REGISTERED).call()
mgmt.awaitGraphIndexStatus(graph, 'byCharacter').status(SchemaStatus.REGISTERED).call()

// Chegou a altura de carregar o nosso graphml
// Nota: Não é garantido que o JanusGraph use os ids que estão no graphml. Normalmente ele gera novos ao inserir os nodes e edges. 
graph.io(graphml()).readGraph('/work/janusgraph/graphml/marvel.graphml')
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

// Por último definimos o TraversalSource do graph para o g, para podermos fazer as nossas queries.
def globals = [:]
globals << [g : graph.traversal()]