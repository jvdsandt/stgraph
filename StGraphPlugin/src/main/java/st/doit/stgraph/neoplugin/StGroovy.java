package st.doit.stgraph.neoplugin;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

@SuppressWarnings("unchecked")
public class StGroovy extends ServerPlugin {

	@Name( "exec_nodes_script" )
    @Description( "Execute a Groovy script that returns a collection of nodes" )
    @PluginTarget( GraphDatabaseService.class )
    public Iterable<Node> execNodesScript( @Source GraphDatabaseService graphDb, @Parameter(name="script") String script ) {
		
		Binding binding = new Binding();
		binding.setVariable("graphDb", graphDb);
		GroovyShell shell = new GroovyShell(binding);

		Object value = shell.evaluate(script);
		return (Iterable<Node>) value;
	}

	@Name( "exec_nodes_script_on_node" )
    @Description( "Execute a Groovy script that takes a single Node as input and returns a collection of nodes" )
    @PluginTarget( Node.class )
    public Iterable<Node> execNodesScriptOnNode( @Source Node node, @Parameter(name="script") String script ) {
		
		Binding binding = new Binding();
		binding.setVariable("node", node);
		GroovyShell shell = new GroovyShell(binding);

		Object value = shell.evaluate(script);
		return (Iterable<Node>) value;
	}

}
