package test.com.magerman.nrpc;

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
import com.magerman.nrpc.MetaDatabase;

public class TestRecognitionOfDesignNotes extends TestCase {

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
	    s = NotesFactory.createSession();
	    db = s.getDatabase("albis/magerman",
		    "Development\\HRE-Mail\\HRE-Mail2_0\\Hre-Mail_(2_0)_Dev.nsf");
	    myMailFile = s.getDatabase("", "A55BAE\\mail\\Andrew_Magerman.nsf");
	    DocumentCollection dc = db.getAllDocuments();
	    ac = new DebugAgentContext(s, db, dc);

	    System.out.println(db.getTitle());

	} catch (NotesException e) {
	    e.printStackTrace();
	}
    }

    protected void tearDown() {
    }

    public void testGeneratingInfoForNotesInMyMailFile() {
	MetaDatabase md = new MetaDatabase();
	md.setDb(myMailFile);
//	run note recognition on all notes and have this printed out to system.out
	md.loadAllDesignNotesFromActualDatabase();
	md.printAllNoteTypes();
	
    }

    public static void main(String[] args) {
	TestRunner.run(TestRecognitionOfDesignNotes.class);
    }

}
