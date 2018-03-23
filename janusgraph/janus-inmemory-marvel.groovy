// Criar um novo graph em memoria
graph = JanusGraphFactory.open('inmemory')

//Definir o schema. Não é obrigatorio, nas evita que o janusGraph ponha todas a propriedades como Object.clas

// Definir as labels dos edges, que têm que ser unicas em todo o graph.

// Definir as labels dos nodes (vertex)

// Definir as propriedados dos nodes

// Define também as propriedades para os edges, neste caso só temos uma 

// JanusGraph suporta Composite Index e Mixed Index (http://docs.janusgraph.org/latest/indexes.html)
// Composite Index só suportam igualdades. São normalmente mais rapidos que os Mixed indexes.
// Mixed indexes permitem mais condições para alem da igualdade, mas precisam de um index backend (Não estamos a usar)

// Apos a criação dos indices, temos que esperar que eles estejam prontos

// Chegou a altura de carregar o nosso graphml
// Nota: Não é garantido que o JanusGraph use os ids que estão no graphml. Normalmente ele gera novos ao inserir os nodes e edges. 

// E já podemos mostrar já algumas estatísticas do graph que acabamos de carregar
g = graph.traversal()

// Por último definimos o TraversalSource do graph para o g, para podermos fazer as nossas queries na Gremlin console.
def globals = [:]
globals << [g : graph.traversal()]