package com.magerman.nrpc;
 
import lotus.domino.*;
import java.util.Vector;

/**
 * @author Bob Balaban, Looseleaf Software, Inc.
 */
public class DebugAgentContext implements AgentContext 
{
	// member vars
	Session m_Session = null;
	Database m_Database = null;
	DocumentCollection m_Doccoll = null;
	
	// default ctor
	public DebugAgentContext() {}
	// non-default ctor
	public DebugAgentContext(final Session s, final Database d)
	{ m_Session = s; m_Database = d; }
	public DebugAgentContext(final Session s, final Database d, final DocumentCollection dc)
	{ m_Session = s; m_Database = d; m_Doccoll = dc; }

	/*** 
	 * These methods are required by the AgentContext interface.
	 * If the debug environment requires more functionality, such 
	 * as DocumentContext, LastRun, etc., these could be faked up too
	***/
	
	public void updateProcessedDoc(final Document doc) 
		throws NotesException {}
		
	public final DocumentCollection unprocessedFTSearch(final String query, final int maxdocs) 
		throws NotesException 
	{ return m_Doccoll; }
	
	public final DocumentCollection unprocessedFTSearch(final String query, final int maxdocs, final int sortopt, final int otheropt) 
		throws NotesException
	{ return m_Doccoll; }
	
	public final DocumentCollection unprocessedFTSearchRange(final String query, final int maxdocs, final int sortopt) 
		throws NotesException
	{ return m_Doccoll; }
	public final DocumentCollection unprocessedFTSearchRange(final String query, final int maxdocs, final int sortopt, final int otheropt, final int arg1) 
		throws NotesException
	{ return m_Doccoll; }

	public final DocumentCollection unprocessedSearch(final String formula, final DateTime limit, final int maxdocs) 
		throws NotesException
	{ return m_Doccoll; }
	
	public final String getEffectiveUserName() throws NotesException
	{ return this.m_Session.getUserName(); }
	
	public final Agent getCurrentAgent() throws NotesException 
	{ return null; }
	
	public final Database getCurrentDatabase() throws NotesException
	{ return this.m_Database; }
	
	public final Document getDocumentContext() throws NotesException
	{ return null; }
	
	public final int getLastExitStatus() throws NotesException
	{ return 0; }
	
	public final DateTime getLastRun() throws NotesException
	{ return null; }
	
	public final Document getSavedData() throws NotesException
	{ return null; }
	
	public final DocumentCollection getUnprocessedDocuments() 
		throws NotesException
	{ return m_Doccoll; }	

	/*** 
	 * These 2 methods are required by our base class,
	 * where they are declared as "abstract"
	***/
	public void recycle() {}
	@SuppressWarnings("unchecked")
    public void recycle(final Vector v) {}
} // end DebugAgentContext
