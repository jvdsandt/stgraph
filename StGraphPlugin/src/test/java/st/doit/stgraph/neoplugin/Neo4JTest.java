package st.doit.stgraph.neoplugin;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;

public abstract class Neo4JTest {

    private GraphDatabaseService graphDatabaseService;
    private File dbDir;
    private Transaction transaction;

    @Before
    public void setUp() throws Exception {
        dbDir = File.createTempFile("stgraph", "test");
        dbDir.delete();
        dbDir.mkdir();

        graphDatabaseService = new EmbeddedGraphDatabase(dbDir.getAbsolutePath());
        transaction = graphDatabaseService.beginTx();
    }

    @After
    public void tearDown() throws Exception {
        transaction.success();
        transaction.finish();
        graphDatabaseService.shutdown();
        FileUtils.forceDelete(dbDir);
    }

    protected GraphDatabaseService getGraphDatabaseService() {
        return graphDatabaseService;
    }
    
    protected Transaction getTransaction() {
    	return transaction;
    }
}
