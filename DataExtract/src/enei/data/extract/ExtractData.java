/**
 *
 */

package enei.data.extract;




import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLWriter;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.ConsistencyModifier;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;

import enei.data.extract.db.ConnectionManager;




/**
 * @author pmpires (Mar 2018)
 *
 */
public class ExtractData {



	private static final String	HASALARMS		= "hasalarms";
	private static final String	INRISKLIST		= "inrisklist";
	private static final String	TRAFFICTYPENAME	= "traffictype";
	private static final String	IMEI			= "IMEI";
	private static final String	IMSI			= "IMSI";
	private static final String	EVENT_DIRECTION	= "eventDirection";
	private static final String	CELL_ID			= "cellId";
	private static final String	EVT_START_DATE	= "evtStartDate";
	private static final String	ENTITYID		= "entityid";
	private static final String	BNUMBER			= "bnumber";
	private static final String	ANUMBER			= "anumber";





	public static void writeGraph() throws FileNotFoundException, IOException, InterruptedException {

		StringBuffer sbVert = new StringBuffer();
		StringBuffer sbEdges = new StringBuffer();
		String graphName = "graph";
		String connUrl = "jdbc:postgresql://localhost:5432/graphdata";
		try (JanusGraph graph = JanusGraphFactory.open("inmemory");
				Connection conn = ConnectionManager.getConnection(connUrl, "postgres", "postgres");
				PreparedStatement pStm = conn.prepareStatement(
						"select ev.*,(select shdesc from TRAFFIC_TYPE where traffic_type_id=ev.traffic_type_id) as traffic_type_name from events ev where company_id=505 and EVENT_TYPE_ID in (1,2) and EVENT_START_DATE BETWEEN TO_DATE('20/03/2018', 'DD/MM/YYYY') AND TO_DATE('21/03/2018', 'DD/MM/YYYY')",
						ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);) {

			// commented: unnecessary initialization - janus server already initializes the variable 'graph' with a janusgraph
			// sbVert.append(graphName + " = JanusGraphFactory.open(\"inmemory\")");
			// sbVert.append("\n");
			JanusGraphManagement management = graph.openManagement();
			System.out.println("Creating Indexes...");
			final PropertyKey entityid = management.makePropertyKey(ENTITYID).dataType(String.class).make();
			final PropertyKey anumberk = management.makePropertyKey(ANUMBER).dataType(String.class).make();
			final PropertyKey bnumberk = management.makePropertyKey(BNUMBER).dataType(String.class).make();
			final PropertyKey cellidk = management.makePropertyKey(CELL_ID).dataType(String.class).make();
			final PropertyKey imeiidk = management.makePropertyKey(IMEI).dataType(String.class).make();
			// first index EntityId
			JanusGraphManagement.IndexBuilder nameIndexBuilder = management.buildIndex("EntityIdx", Vertex.class).addKey(entityid);
			JanusGraphIndex nameIndex = nameIndexBuilder.buildCompositeIndex();
			management.setConsistency(nameIndex, ConsistencyModifier.LOCK);
			// second index ANumber
			JanusGraphManagement.IndexBuilder nameIndexBuilder2 = management.buildIndex("ANumberIdx", Vertex.class).addKey(anumberk);
			JanusGraphIndex nameIndex2 = nameIndexBuilder2.buildCompositeIndex();
			management.setConsistency(nameIndex2, ConsistencyModifier.LOCK);
			// third index BNumber
			JanusGraphManagement.IndexBuilder bNumberIndexBuilder = management.buildIndex("BNumberIdx", Vertex.class).addKey(bnumberk);
			JanusGraphIndex bNumberIndex = bNumberIndexBuilder.buildCompositeIndex();
			management.setConsistency(bNumberIndex, ConsistencyModifier.LOCK);
			// forth index by cellid
			JanusGraphManagement.IndexBuilder cellIndexBuilder = management.buildIndex("CellIdx", Vertex.class).addKey(cellidk);
			JanusGraphIndex cellIndex = cellIndexBuilder.buildCompositeIndex();
			management.setConsistency(cellIndex, ConsistencyModifier.LOCK);
			// fifth index by imei
			JanusGraphManagement.IndexBuilder imeiIndexBuilder = management.buildIndex("ImeiIdx", Vertex.class).addKey(imeiidk);
			JanusGraphIndex imeiIndex = imeiIndexBuilder.buildCompositeIndex();
			management.setConsistency(imeiIndex, ConsistencyModifier.LOCK);

			management.commit();
			System.out.println("Waiting for index being registered...");
			ResultSet rs = pStm.executeQuery();
			System.out.println("--------------------------");
			System.out.println("--- Adding vertices... ---");
			System.out.println("--------------------------");
			long s = System.currentTimeMillis();

			JanusGraphTransaction tx = graph.newTransaction();

			long count = 0;
			int fileNum = 1;
			while (rs.next()) {
				System.out.println("**********************************");
				String anumber = rs.getString("A_NUMBER");
				String bnumber = rs.getString("B_NUMBER");
				String entityID = rs.getString("ENTITY_ID");
				String eventDirection = rs.getString("EVENT_DIRECTION");
				Date evntStartDate = new Date(rs.getTimestamp("EVENT_START_DATE").getTime());
				String imei = rs.getString(IMEI);
				String imsi = rs.getString(IMSI);
				String cellID = rs.getString("CELL_ID");
				String trafficType = rs.getString("TRAFFIC_TYPE_NAME");
				if (trafficType == null) {
					trafficType = "";
				}
				// create vertices
				Vertex vrtEntity = tx.addVertex(T.label, "entity" + entityID, "type", "entity", ENTITYID, entityID, ANUMBER, anumber, BNUMBER, bnumber, EVENT_DIRECTION, eventDirection, EVT_START_DATE,
						evntStartDate, CELL_ID, cellID, IMSI, imsi, IMEI, imei, TRAFFICTYPENAME, trafficType);

				sbVert.append("vrtEntity = " + graphName + ".addVertex(T.label, \"entity" + entityID + "\",");
				sbVert.append("\"type\", \"entity\",");
				sbVert.append("\"" + ENTITYID + "\",\"" + entityID + "\",");
				sbVert.append("\"" + ANUMBER + "\",\"" + anumber + "\",");
				sbVert.append("\"" + BNUMBER + "\",\"" + bnumber + "\",");
				sbVert.append("\"" + EVENT_DIRECTION + "\",\"" + eventDirection + "\",");
				sbVert.append("\"" + EVT_START_DATE + "\",\"" + evntStartDate + "\",");
				sbVert.append("\"" + CELL_ID + "\",\"" + cellID + "\",");
				sbVert.append("\"" + IMSI + "\",\"" + imsi + "\",");
				sbVert.append("\"" + IMEI + "\",\"" + imsi + "\",");
				sbVert.append("\"" + TRAFFICTYPENAME + "\",\"" + trafficType + "\")");
				sbVert.append("\n");

				// create CELL vertex
				GraphTraversal<Vertex, Vertex> gtCell = tx.traversal().V().has("type", "cell").has(CELL_ID, cellID);
				Vertex vrtCell = null;
				Optional<Vertex> optVrtxCell = gtCell.tryNext();
				if (!optVrtxCell.isPresent()) {
					System.out.println("Adding Cell Vertex:");
					System.out.println("Entity: " + entityID);
					System.out.println("ANumber: " + anumber);
					System.out.println("BNumber: " + bnumber);
					System.out.println("Cell: " + cellID);
					vrtCell = tx.addVertex(T.label, "cell", "type", "cell", CELL_ID, cellID);

					sbVert.append("vrtCell = " + graphName + ".addVertex(T.label, \"cell\",");
					sbVert.append("\"type\", \"cell\",");
					sbVert.append("\"" + CELL_ID + "\",\"" + cellID + "\")");
					sbVert.append("\n");
				} else {
					System.out.println("Cell " + cellID + " already exists. With entity " + entityID);
					vrtCell = optVrtxCell.get();
					sbVert.append("vrtCell = " + graphName + ".traversal().V().has(\"type\", \"cell\").has(\"" + CELL_ID + "\",\"" + cellID + "\").next()");
					sbVert.append("\n");
				}
				vrtEntity.addEdge("at", vrtCell);
				sbVert.append("vrtEntity.addEdge(\"at\", vrtCell)");
				sbVert.append("\n");
				// end create CELL vertex

				// create IMEI vertex
				GraphTraversal<Vertex, Vertex> gtIMEI = tx.traversal().V().has("type", "imei").has(IMEI, imei);
				Vertex vrtIMEI = null;
				Optional<Vertex> optVrtxIMEI = gtIMEI.tryNext();
				if (!optVrtxIMEI.isPresent()) {
					System.out.println("Adding IMEI Vertex:");
					System.out.println("Entity: " + entityID);
					System.out.println("ANumber: " + anumber);
					System.out.println("BNumber: " + bnumber);
					System.out.println("IMEI: " + imei);
					vrtIMEI = tx.addVertex(T.label, "imei", "type", "imei", IMEI, imei);

					sbVert.append("vrtIMEI = " + graphName + ".addVertex(T.label, \"imei\",");
					sbVert.append("\"type\", \"imei\",");
					sbVert.append("\"" + IMEI + "\",\"" + imei + "\")");
					sbVert.append("\n");
				} else {
					System.out.println("IMEI " + imei + " already exists. For entity " + entityID);
					vrtIMEI = optVrtxIMEI.get();
					sbVert.append("vrtIMEI = " + graphName + ".traversal().V().has(\"type\", \"imei\").has(\"" + IMEI + "\",\"" + imei + "\").next()");
					sbVert.append("\n");
				}
				vrtEntity.addEdge("uses", vrtIMEI);
				sbVert.append("vrtEntity.addEdge(\"uses\", vrtIMEI)");
				sbVert.append("\n");
				// end create IMEI vertex
				count++;
				if (count == 140) {
					// commit changes
					sbVert.append(graphName + ".tx().commit();[]");
					sbVert.append("\n");
					Files.write(Paths.get("vertices" + fileNum + ".graph"), sbVert.toString().getBytes());
					// clear string buffer so it wont use any previous instruction in the next file
					sbVert = new StringBuffer();
					fileNum++;
				}
			}
			System.out.println("Rows " + count + " read from Database.");
			long e = System.currentTimeMillis();
			System.out.println("Created vertices in " + (e - s) + "ms.");
			System.out.println("--------------------------");
			System.out.println("- Adding Edges(calls)... -");
			System.out.println("--------------------------");
			rs.beforeFirst();
			s = System.currentTimeMillis();
			// reset file generator params
			count = 0;
			fileNum = 1;
			while (rs.next()) {
				System.out.println("*************************");
				String anumber = rs.getString("A_NUMBER");
				String bnumber = rs.getString("B_NUMBER");
				String entityID = rs.getString("ENTITY_ID");
				String eventDirection = rs.getString("EVENT_DIRECTION");
				Date evntStartDate = new Date(rs.getTimestamp("EVENT_START_DATE").getTime());

				System.out.println("Reading Entity: " + entityID);
				System.out.println("ANumber: " + anumber);
				System.out.println("BNumber: " + bnumber);
				System.out.println("Direction: " + eventDirection);
				System.out.println("EvtStartDate: " + evntStartDate.toString());
				Vertex entityVertex = tx.traversal().V().has(ANUMBER, anumber).has(BNUMBER, bnumber).has(ENTITYID, entityID).has(EVT_START_DATE, evntStartDate).next();
				sbEdges.append("entityVertex = " + graphName + ".traversal().V().has(\"" + ANUMBER + "\",\"" + anumber + "\").has(\"" + BNUMBER + "\",\"" + bnumber + "\").has(\"" + ENTITYID + "\",\""
						+ entityID + "\").has(\"" + EVT_START_DATE + "\",\"" + evntStartDate + "\").next()");
				sbEdges.append("\n");
				if ("IN".equals(eventDirection)) {

					Optional<Vertex> optInVertex = tx.traversal().V().has(ENTITYID, entityVertex.property(ANUMBER).value()).has(EVENT_DIRECTION, "OUT").has(EVT_START_DATE, evntStartDate).tryNext();
					if (optInVertex.isPresent()) {
						Vertex inVertex = optInVertex.get();
						entityVertex.addEdge("calls", inVertex);
						System.out.println(entityVertex.property(ENTITYID).value() + "->" + inVertex.property(ANUMBER).value());
						sbEdges.append("inVertex = " + graphName + ".traversal().V().has(\"" + ENTITYID + "\", entityVertex.property(\"" + ANUMBER + "\").value()).has(\"" + EVENT_DIRECTION
								+ "\", \"OUT\").has(\"" + EVT_START_DATE + "\",\"" + evntStartDate + "\").next()");
						sbEdges.append("\n");
						sbEdges.append("entityVertex.addEdge(\"calls\",inVertex)");
						sbEdges.append("\n");
					} else {
						System.out.println("Entity " + entityVertex.property(ANUMBER).value() + " with OUT direction, not found!");
					}
				} else {
					if ("OUT".equals(eventDirection)) {
						Optional<Vertex> optOutVertex = tx.traversal().V().has(ENTITYID, entityVertex.property(BNUMBER).value()).has(EVENT_DIRECTION, "IN").has(EVT_START_DATE, evntStartDate)
								.tryNext();
						if (optOutVertex.isPresent()) {
							Vertex outVertex = optOutVertex.get();
							outVertex.addEdge("calls", entityVertex);
							System.out.println(outVertex.property(ENTITYID).value() + "->" + entityVertex.property(BNUMBER).value());
							sbEdges.append("outVertex = " + graphName + ".traversal().V().has(\"" + ENTITYID + "\", entityVertex.property(\"" + BNUMBER + "\").value()).has(\"" + EVENT_DIRECTION
									+ "\", \"IN\").has(\"" + EVT_START_DATE + "\",\"" + evntStartDate + "\").next()");
							sbEdges.append("\n");
							sbEdges.append("outVertex.addEdge(\"calls\", entityVertex)");
							sbEdges.append("\n");
						} else {
							System.out.println("Entity " + entityVertex.property(BNUMBER).value() + " with IN direction, not found!");
						}
					} else {
						System.out.println("No Direction defined!");
					}
				}

				count++;
				if (count == 135) {
					sbEdges.append(graphName + ".tx().commit();[]");
					sbEdges.append("\n");
					Files.write(Paths.get("edges" + fileNum + ".graph"), sbEdges.toString().getBytes());

					// clear string buffer so it wont use any previous instruction in the next file
					sbEdges = new StringBuffer();
					fileNum++;
				}

			}
			e = System.currentTimeMillis();
			System.out.println("Created Edges in " + (e - s) + "ms");
			tx.commit();
			System.out.println("writing graph to disk");
			s = System.currentTimeMillis();
			GraphMLWriter.build().create().writeGraph(new FileOutputStream("events.graphml"), graph);
			e = System.currentTimeMillis();
			System.out.println("Graph written to 'events.graphml'.");

			// commit changes
			sbVert.append(graphName + ".tx().commit();[]");
			sbVert.append("\n");
			sbEdges.append(graphName + ".tx().commit();[]");
			sbEdges.append("\n");
			// write vertices
			Files.write(Paths.get("vertices" + fileNum + ".graph"), sbVert.toString().getBytes());
			Files.write(Paths.get("edges" + fileNum + ".graph"), sbEdges.toString().getBytes());
			// write edges
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}





	private static void connectGremlinServer(String commandSubmit) throws Exception {

		Cluster cluster = null;
		try {
			cluster = Cluster.open("conf/remote.yaml");

			Client client = cluster.connect();
			client.submit(commandSubmit);

			client.close();
			System.out.println("Created graph in remote gremlin server.");
		} finally {
			if (cluster != null) {
				cluster.close();
			}
		}
	}





	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// execute this command to clear graph vertices/edges
		// graph.traversal().V().drop().iterate()
		// graph.traversal().E().drop().iterate()
		if (args.length > 0) {
			String readfromdb = args[0];
			switch (readfromdb.toLowerCase()) {
				case "readdb":
					writeGraph();
					break;
				case "writeremote":
					StringBuffer sb = new StringBuffer();
					sb.append(new String(Files.readAllBytes(Paths.get("vertices1.graph"))));
					connectGremlinServer(sb.toString());

					System.out.println("Creating second file of vertices");
					sb = new StringBuffer();
					sb.append(new String(Files.readAllBytes(Paths.get("vertices2.graph"))));
					connectGremlinServer(sb.toString());

					sb = new StringBuffer();
					sb.append(new String(Files.readAllBytes(Paths.get("edges1.graph"))));
					connectGremlinServer(sb.toString());

					sb = new StringBuffer();
					sb.append(new String(Files.readAllBytes(Paths.get("edges2.graph"))));
					connectGremlinServer(sb.toString());
					break;
				case "both":
					writeGraph();
					sb = new StringBuffer();
					sb.append(new String(Files.readAllBytes(Paths.get("vertices1.graph"))));
					connectGremlinServer(sb.toString());

					System.out.println("Creating second file of vertices");
					sb = new StringBuffer();
					sb.append(new String(Files.readAllBytes(Paths.get("vertices2.graph"))));
					connectGremlinServer(sb.toString());

					System.out.println("Creating first file of edges");
					sb = new StringBuffer();
					sb.append(new String(Files.readAllBytes(Paths.get("edges1.graph"))));
					connectGremlinServer(sb.toString());

					System.out.println("Creating second file of edges");
					sb = new StringBuffer();
					sb.append(new String(Files.readAllBytes(Paths.get("edges2.graph"))));
					connectGremlinServer(sb.toString());
					break;
				default:
					System.out.println("Options are:");
					System.out.println("\treaddb -> read from DB and generate graph files");
					System.out.println("\twriteremote -> write graph files in remote gremlin server");
					System.out.println("\tboth -> the previous two steps in one");
					break;
			}
		} else {
			System.out.println("Options are:");
			System.out.println("\treaddb -> read from DB and generate graph files");
			System.out.println("\twriteremote -> write graph files in remote gremlin server");
			System.out.println("\tboth -> the previous two steps in one");
		}

	}

}
