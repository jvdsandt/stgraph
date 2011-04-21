package st.doit.stgraph.neoplugin;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

@Description( "An extension to the Neo4j Server for The Smalltalk Code Graph" )
public class StGraph extends ServerPlugin {
	
	private static final Collection<String> NO_KEYS = new ArrayList<String>();

	@Name( "get_nodes_with_type" )
    @Description( "Get all nodes from the Neo4j graph database" )
    @PluginTarget( GraphDatabaseService.class )
    public Iterable<Node> getNodesWithType( @Source GraphDatabaseService graphDb, @Parameter(name="type") String type )
    {
		List<Node> nodes = new ArrayList<Node>(1000);
		for (Node node : graphDb.getAllNodes()) {
			if (type.equals(node.getProperty("type", null))) {
				nodes.add(node);
			}
		}
		return nodes;
    }
	
	public Iterable<Node> getNodesWithOutRelationshipsTo( @Source GraphDatabaseService graphDb,
			@Parameter(name="nodeIds") long[] nodeIds,
			@Parameter(name="types") String[] relTypeNames) {
		
		RelationshipType[] relTypes = getTypes(graphDb.getRelationshipTypes(), relTypeNames);
		Set<Node> nodes = new HashSet<Node>();

		Node node = graphDb.getNodeById(nodeIds[0]);
		Iterable<Relationship> inRels = node.getRelationships(relTypes[0], Direction.INCOMING);
		Collection<Node> startNodes = getStartNodes(inRels);
		for(Node each : startNodes) {
			if (getSize(each.getRelationships(Direction.OUTGOING)) == nodeIds.length) {
				nodes.add(each);
			}
		}
		for (int i = 1 ; i < nodeIds.length; i++) {
			if (nodes.isEmpty()) {
				return Collections.emptyList();
			}
			node = graphDb.getNodeById(nodeIds[i]);
			inRels = node.getRelationships(relTypes[i], Direction.INCOMING);
			nodes.retainAll(getStartNodes(inRels));
		}
		return nodes;
	}
	
    @Name( "get_identical_nodes" )
    @Description( "Get all nodes that are identical to the argument node" )
    @PluginTarget( Node.class )
	public Iterable<Node> getIdenticalNodes(@Source Node node, @Parameter(name="ignoreNodeProperties", optional=true) String[] keys) {
		
		Iterable<Relationship> allOut = node.getRelationships(Direction.OUTGOING);
		HashSet<Node> nodes = null;
		Collection<String> keyColl = keys == null ? NO_KEYS : Arrays.asList(keys);
		
		for(Relationship rel : allOut) {
			List<Node> newNodes = getSimularStartNodes(rel);
			if (nodes == null) {
				nodes = new HashSet<Node>();
				for (Node newNode : newNodes) {
					if (areEqueal(node, newNode, keyColl)) {
						nodes.add(newNode);
					}
				}
			} else {
				nodes.retainAll(newNodes);
				if (nodes.isEmpty()) {
					return Collections.emptyList();
				}
			}
		}
		if (nodes == null) {
			return Collections.emptyList();
		} else {
			return nodes;
		}
	}
	
    @Name( "delete_with_out" )
    @Description( "Delete a node with all its outgoing relationships" )
    @PluginTarget( Node.class )
    public void deleteWithOutgoing(@Source Node node) {
   		Iterable<Relationship> allOut = node.getRelationships(Direction.OUTGOING);
		for (Relationship rel : allOut) {
			rel.delete();
		}
		node.delete();
    } 
    
	protected List<Node> getSimularStartNodes(Relationship relationship) {
		List<Node> nodes = new ArrayList<Node>();
		Iterable<Relationship> allIn = relationship.getEndNode().getRelationships(relationship.getType(), Direction.INCOMING);
		for (Relationship rel : allIn) {
			if (!relationship.getStartNode().equals(rel.getStartNode())) {
				if (areEqueal(relationship, rel)) {
					nodes.add(rel.getStartNode());
				}
			}
		}
		return nodes;
	}
	
	protected boolean areEqueal(PropertyContainer pc1, PropertyContainer pc2) {
		return areEqueal(pc1, pc2, NO_KEYS);
	}

	protected boolean areEqueal(PropertyContainer pc1, PropertyContainer pc2, Collection<String> ignoreKeys) {
		HashSet<String> keys = new HashSet<String>();
		for (String key : pc1.getPropertyKeys()) {
			if (!ignoreKeys.contains(key) && !pc1.getProperty(key).equals(pc2.getProperty(key, null))) {
				return false;
			}
			keys.add(key);
		}
		for (String key : pc2.getPropertyKeys()) {
			if (!keys.contains(key)) {
				return false;
			}
		}
		return true;
	}
	
	protected RelationshipType[] getTypes(Iterable<RelationshipType> relTypes, String[] names) {
		/*	map := relTypes groupedBy: [ :each | each name ]	*/
		Map<String, RelationshipType> map = new HashMap<String, RelationshipType>();
		for (RelationshipType type : relTypes) {
			map.put(type.name(), type);
		}
		/*	^ names collect: [ :each | map at: each ]		*/
		RelationshipType[] result = new RelationshipType[names.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = map.get(names[i]);
		}
		return result;
	}
	
	protected Collection<Node> getStartNodes(Iterable<Relationship> rels) {
		List<Node> startNodes = new ArrayList<Node>();
		for (Relationship rel : rels) {
			startNodes.add(rel.getStartNode());
		}
		return startNodes;
	}
	
	@SuppressWarnings({ "rawtypes", "unused" })
	protected long getSize(Iterable iter) {
		long size = 0;
		for (Object o : iter) {
			size++;
		}
		return size;
	}
}
