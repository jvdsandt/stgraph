package st.doit.stgraph.neoplugin;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;

public class StGroovyTest extends Neo4JTest {

    private static final String KEY_CITY_NAME = "name";
    private static final List<String> CITIES = Arrays.asList("Amsterdam", "Rotterdam", "Utrecht", "Den Haag", "Leeuwarden", "AMS2");
    
    private Index<Node> index;
    
    private StGroovy plugin = new StGroovy();

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
    
    protected Node nodeForCity(String city) {
        return index.get(KEY_CITY_NAME, city).getSingle();
    }

    @Test
    public void testCreateNode() {
    	String script = 
    		"def node = graphDb.createNode()\n" +
    		"node.setProperty(\"name\", \"pipo\")\n" +
    		"def list = [node]\n" +
    		"return list";
    	Iterable<Node> result = plugin.execNodesScript(getGraphDatabaseService(), script);
    	Assert.assertNotNull(result);
    	Assert.assertEquals("pipo", result.iterator().next().getProperty("name"));
    }
}	
