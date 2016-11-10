package com.magerman.nrpc;

import lotus.domino.*;

/**
 * @author Bob Balaban, Looseleaf Software, Inc.
 */
public class BaseClass extends AgentBase {
    // member vars for session and database
    Session m_Session = null;
    AgentContext m_Ctx = null;

    public BaseClass() {
    }

    public BaseClass(final Session s, final AgentContext c) {
	m_Session = s;
	m_Ctx = c;
    }

    public static void main(final String[] args) {
	Session s = null;
	Database d = null;
	DocumentCollection dc = null;
	AgentContext ctx = null;

	NotesThread.sinitThread();
	try {
	    s = NotesFactory.createSession();
	    d = s.getDatabase("albis/magerman",
		    "Development\\NRPC Parser\\NRPC Parser1_0\\Nrpc_Parser_(1_0)_Dev.nsf");

	    // get all the documents in the database
	    dc = d.getAllDocuments();

	    // create "fake" AgentContext
	    // if the agent is doing a search, we could
	    // create the DocumentCollection here too, and
	    // pass it in to the constructor
	    ctx = new DebugAgentContext(s, d, dc);

	    // create the agent object, invoke it on
	    // NotesMain(), the way Domino does
	    BaseClass a = new BaseClass(s, ctx);
	    a.NotesMain();
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    try {
		if (s != null) {
		    s.recycle();
		}
	    } catch (Exception x) {
	    }
	    NotesThread.stermThread();
	}
    } // end main

    // the real entry point
    public final void NotesMain() {
	Session s;
	Database db;
	AgentContext ac;

	// do we get Session from debug member var,
	// or from agent context?
	try {
	    if (this.m_Session != null) {
		s = this.m_Session;
		ac = this.m_Ctx;
	    } else {
		s = this.getSession();
		ac = s.getAgentContext();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    return;
	}

	// from this point on, agent code is common to
	// both debug and "real agent" environments

	try {
	    FileParser fp = new FileParser();
	    db = ac.getCurrentDatabase();
	    fp.parseFile(db);
	} catch (Exception x) {
	    x.printStackTrace();
	}
    } // end NotesMain
}
