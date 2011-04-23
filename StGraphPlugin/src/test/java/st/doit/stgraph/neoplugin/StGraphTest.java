package st.doit.stgraph.neoplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;

public class StGraphTest extends Neo4JTest {

    private static final String KEY_CITY_NAME = "name";
    private static final List<String> CITIES = Arrays.asList("Amsterdam", "Rotterdam", "Utrecht", "Den Haag", "Leeuwarden", "AMS2");
    
    private Index<Node> index;
    
    private StGraph plugin = new StGraph();

	@Before
    public void createTestData() {
        index = getGraphDatabaseService().index().forNodes("cities");
        // Create a node for each city (see above)
        for (String city : CITIES) {
            Node cityNode = getGraphDatabaseService().createNode();
            cityNode.setProperty("type", "City");
            cityNode.setProperty(KEY_CITY_NAME, city);

            // Index the city name
            index.add(cityNode, KEY_CITY_NAME, city);
        }

        // Create relationships (or roads) between the cities
        roadFrom("Amsterdam", "Rotterdam", 50, 120);
        roadFrom("Amsterdam", "Leeuwarden", 50, 130);		
        
        roadFrom("AMS2", "Rotterdam", 50, 120);
        roadFrom("AMS2", "Leeuwarden", 50, 130);		
        
    }

    protected void roadFrom(String cityA, String cityB, double distanceKm, double speedLimit) {

        Node nodeA = nodeForCity(cityA);
        Node nodeB = nodeForCity(cityB);

        if (nodeA == null || nodeB == null) {
            throw new IllegalArgumentException("Cannot build a road to or from non existant city!");
        }

        // Set the type of this relationship to be ROAD
        Relationship relationship = nodeA.createRelationshipTo(nodeB, DynamicRelationshipType.withName("ROAD"));
        relationship.setProperty("distance", distanceKm);
        relationship.setProperty("speedlimit", speedLimit);
    }
    
    @Test
    public void testGetCityNodes() {
    	for (String city : CITIES) {
    		Node node = index.get(KEY_CITY_NAME, city).getSingle();
    		Assert.assertNotNull(node);
    	}
    }
    
    @Test
    public void testGetNodesWithType() {
    	Iterable<Node> nodes = plugin.getNodesWithType(getGraphDatabaseService(), "City");
    	List<Node> nodeList = new ArrayList<Node>();
    	for (Node node : nodes) {
    		nodeList.add(node);
    	}
    	Assert.assertEquals(CITIES.size(), nodeList.size());
    }
    
    @Test
    public void testGetIdenticalNodes() {
    	Node node = nodeForCity("Amsterdam");
    	Iterable<Node> nodes = plugin.getIdenticalNodes(node, new String[] { KEY_CITY_NAME });
    	
    	Assert.assertTrue(nodes.iterator().hasNext());
    	Node result = nodes.iterator().next();
    	Assert.assertEquals("AMS2", result.getProperty(KEY_CITY_NAME));
    }
    
    @Test
    public void testDeleteWithOutgoing() {
    	Node node = nodeForCity("Amsterdam");
    	long id = node.getId();
    	plugin.deleteWithOutgoing(node);
    	
        getTransaction().success();
        getTransaction().finish();

    	try {
    		getGraphDatabaseService().getNodeById(id);
    		Assert.assertTrue("Exception expected", false);
    	} catch (NotFoundException ex) {
    		Assert.assertNotNull(ex);
    	}
    }
    
    @Test
    public void testGetNodesWithOutRelationshipsTo() {
    	Node node1 = nodeForCity("Leeuwarden");
    	Node node2 = nodeForCity("Rotterdam");
    	    	
    	Iterable<Node> nodes = plugin.getNodesWithOutRelationshipsTo(getGraphDatabaseService(), 
    			new long[] { node1.getId(), node2.getId() }, 
    			new String[] { "ROAD", "ROAD" });
    	Assert.assertTrue(nodes.iterator().hasNext());
   
    }
    
    protected Node nodeForCity(String city) {
        return index.get(KEY_CITY_NAME, city).getSingle();
    }
	
}	
