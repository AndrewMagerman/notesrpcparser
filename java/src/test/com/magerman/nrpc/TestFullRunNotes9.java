package test.com.magerman.nrpc;

import java.io.File;
import java.net.URL;

import javax.security.auth.login.Configuration;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import com.magerman.nrpc.DebugAgentContext;
import com.magerman.nrpc.FileParser;

public class TestFullRunNotes9 extends TestCase {

    URL url;
    Document doceFSRTest;
    Database db;
    DebugAgentContext ac;
    Configuration config;
    Database myMailFile;

    protected void setUp() {
	NotesThread.sinitThread();
	Session s;
	try {
	    NotesThread.sinitThread();
	    s = NotesFactory.createSession();
	    db = s.getDatabase("albis/magerman",
		    "Development\\NRPC Parser\\NRPC Parser1_0\\Nrpc_Parser_(1_0)_Dev.nsf");
	    myMailFile = s.getDatabase("", "A55BAE\\mail\\Andrew_Magerman.nsf");
	    DocumentCollection dc = db.getAllDocuments();
	    ac = new DebugAgentContext(s, db, dc);

	    System.out.println(db.getTitle());

	} catch (NotesException e) {
	    e.printStackTrace();
	}
    }

    protected void tearDown() {
	NotesThread.stermThread();
    }

    public void testwithNotes9Outputfile() {
//	File test = new File("H:\\Current Projects\\NRPC\\RPC_SKAFFEN_2013_10_02@10_08_45.txt");
	File test = new File("H:\\Current Projects\\NRPC\\RPC_Notes_7.txt");
//	File test = new File("H:\\Current Projects\\NRPC\\RPC_SKAFFEN_COMPACT.txt");
	
	
	FileParser fp = new FileParser();
	fp.setOutputDebugFile(test);
	long tStart = System.currentTimeMillis();
	fp.parseFile(db);
	

	long tEnd = System.currentTimeMillis();
	long tDelta = tEnd - tStart;
	double elapsedSeconds = tDelta / 1000.0;
	System.out.println("Time to complete in seconds : "  + elapsedSeconds);

    }

    public static void main(String[] args) {
	TestRunner.run(TestFullRunNotes9.class);
    }

}
