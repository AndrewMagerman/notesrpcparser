package com.magerman.nrpc;

import java.util.HashMap;
import java.util.HashSet;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

// TODO: Auto-generated Javadoc
//import java.io.*;

/**
 * The Class MetaDatabase.
 */
public class MetaDatabase {

    /** The Can be opened. */
    private boolean canBeOpened;

    /** The Database title. */
    private String databaseTitle = "";

    /** The db. */
    private Database db;

    /** The File path. */
    private String filePath = "";

    /** The List of notes. */
    private HashMap<String, MetaNote> listOfNotes = new HashMap<String, MetaNote>();

    /** The Replica id. */
    private String replicaID = "";

    /** The Server name. */
    private String serverName = "";

    /** The List of line numbers. */
    private HashSet<Long> ListOfLineNumbers = new HashSet<Long>();

    /**
     * Instantiates a new meta database.
     */
    public MetaDatabase() {
    }

    /**
     * Adds the note.
     * 
     * @param notetoadd
     *            the notetoadd
     */
    final void addNote(final MetaNote notetoadd) {
	if (!notetoadd.getNoteID().isEmpty()) {
	    listOfNotes.put(notetoadd.getNoteID(), notetoadd);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public final boolean equals(final Object aDatabase) {
	MetaDatabase adb = (MetaDatabase) aDatabase;
	boolean replicaIDisSame = false;
	boolean filePathAreSame = false;
	if (getReplicaID() != null) {
	    if (adb.getReplicaID() != null) {
		if (getReplicaID().equals(adb.getReplicaID())) {
		    replicaIDisSame = true;
		}
	    }
	}
	if (getFilePath() != null) {
	    if (adb.getFilePath() != null) {
		if (getFilePath().equals(adb.getFilePath())) {
		    filePathAreSame = true;
		}
	    }
	}
	return ((replicaIDisSame || filePathAreSame));
    }

    /**
     * Gets the database title.
     * 
     * @return the databaseTitle
     */
    public final String getDatabaseTitle() {
	return databaseTitle;
    }

    /**
     * Gets the db.
     * 
     * @return the db
     */
    public final Database getDb() {
	return db;
    }

    /**
     * Gets the file path.
     * 
     * @return the filePath
     */
    public final String getFilePath() {
	return filePath;
    }

    /**
     * Gets the list of notes.
     * 
     * @return the listOfNotes
     */
    public final HashMap<String, MetaNote> getListOfNotes() {
	return listOfNotes;
    }

    /**
     * Gets the replica id.
     * 
     * @return the replicaID
     */
    public final String getReplicaID() {
	return replicaID;
    }

    /**
     * Gets the serverand filepath.
     * 
     * @return the serverand filepath
     */
    public final String getServerandFilepath() {
	return serverName + "!!" + filePath;
    }

    /**
     * Gets the server name.
     * 
     * @return the serverName
     */
    public final String getServerName() {
	return serverName;
    }

    /**
     * Checks for file path.
     * 
     * @return true, if successful
     */
    public final boolean hasFilePath() {
	return (getFilePath() != null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public final int hashCode() {
	return replicaID.hashCode();
    }

    /**
     * Import info (db title, FilePath, and Server) from real database.
     * 
     * @param val
     *            the val
     */
    public final void importInfoFromRealDatabase(final Database val) {
	this.db = val;
	try {
	    setDatabaseTitle(this.db.getTitle());
	    setFilePath(this.db.getFilePath());
	    setServerName(this.db.getServer());
	} catch (NotesException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Checks if is can be opened.
     * 
     * @return true, if is can be opened
     */
    public final boolean isCanBeOpened() {
	return canBeOpened;
    }

    /**
     * Checks if is defined.
     * 
     * @return true, if is defined
     */
    public final boolean isDefined() {
	// returns true if either filepath or replicaID is not null
	return !(getFilePath() == null && getReplicaID() == null);
    }

    /**
     * Load all design notes from actual database.
     */
    public final void loadAllDesignNotesFromActualDatabase() {

	// Get a notesnotecollection full of the design elements
	NoteCollection nc;
	try {
	    nc = db.createNoteCollection(false);
	    nc.selectAllDesignElements(true);
	    nc.buildCollection();
	    // loop through this notecollection and fill the metadatabase full
	    // of
	    // MetaNotes.

	    if (nc.getCount() > 0) {
		String id = nc.getFirstNoteID();
		while (id.length() > 0) {
		    String idZap = id;
		    // Get next doc before zapping current
		    id = nc.getNextNoteID(id);
		    Document doc = db.getDocumentByID(idZap);
		    MetaNote mn = new MetaNote();
		    mn.setParentDatabase(this);
		    mn.setActualNote(doc);
		    mn.findNoteTypeAndNoteTitle();
		    this.addNote(mn);
		}
	    }

	} catch (NotesException e) {
	    e.printStackTrace();
	}

    }

    /**
     * Prints the all note types.
     */
    public final void printAllNoteTypes() {
	for (MetaNote mnote : listOfNotes.values()) {
	    System.out.println(mnote);
	}

    }

    /**
     * Sets the can be opened.
     * 
     * @param val
     *            the new can be opened
     */
    public final void setCanBeOpened(final boolean val) {
	canBeOpened = val;
    }

    /**
     * Sets the database title.
     * 
     * @param val
     *            the new database title
     */
    public final void setDatabaseTitle(final String val) {
	if (val.isEmpty()) {
	    return;
	}
	databaseTitle = val;
    }

    /**
     * Sets the db. Used for testing purposes, I hope only.
     * 
     * @param myMailFile
     *            the new db
     */
    public final void setDb(final Database myMailFile) {
	this.db = myMailFile;

    }

    /**
     * Sets the file path.
     * 
     * @param val
     *            the filePath to set
     */
    public final void setFilePath(final String val) {
	filePath = val;
    }

    /**
     * Sets the replica id.
     * 
     * @param val
     *            the new replica id
     */
    public final void setReplicaID(final String val) {
	if (val.isEmpty()) {
	    return;
	}
	replicaID = val;
    }

    /**
     * Sets the server name.
     * 
     * @param val
     *            the new server name
     */
    public final void setServerName(final String val) {
	if (val.isEmpty()) {
	    return;
	}
	try {
	    Session session = NotesFactory.createSession();
	    Name abbreviatedName = session.createName(val);
	    serverName = abbreviatedName.getAbbreviated();
	} catch (NotesException e) {
	    serverName = val;
	    e.printStackTrace();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
	return "MetaDatabase [CanBeOpened=" + canBeOpened + ", DatabaseTitle="
		+ databaseTitle + ", db=" + db + ", FilePath=" + filePath
		+ ", ReplicaID=" + replicaID + ", ServerName=" + serverName
		+ "]";
    }

    /**
     * Adds the line number.
     * 
     * @param long1
     *            the long1
     */
    public final void addLineNumber(final Long long1) {
        ListOfLineNumbers.add(long1);
    }

    /**
     * Gets the list of line numbers.
     * 
     * @return the list of line numbers
     */
    public final HashSet<Long> getListOfLineNumbers() {
        return ListOfLineNumbers;
    }

}
