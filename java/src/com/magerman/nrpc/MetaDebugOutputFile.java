package com.magerman.nrpc;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

// TODO: Auto-generated Javadoc
/**
 * The Class MetaDebugOutputFile.
 */
public class MetaDebugOutputFile {

    /** The Total duration in milliseconds. */
    private long totalDurationInMilliseconds;

    /** The linecounter. */
    private Long linecounter;

    /** The All lines. */
    private HashMap<Long, Line> allLines = new HashMap<Long, Line>();

    /** The databases. */
    private Map<String, MetaDatabase> databasesWithReplicaID = new HashMap<String, MetaDatabase>();

    /** The databases with server and file name. */
    private Map<String, MetaDatabase> databasesWithServerAndFileName = new HashMap<String, MetaDatabase>();

    /** The session. */
    private Session session;

    /**
     * Gets the all lines.
     * 
     * @return the all lines
     */
    public final Collection<Line> getAllLines() {
	return allLines.values();
    }

    /**
     * Gets the total duration in milliseconds.
     * 
     * @return the total duration in milliseconds
     */
    public final long getTotalDurationInMilliseconds() {
	return totalDurationInMilliseconds;
    }

    /**
     * Gets the linecounter.
     * 
     * @return the linecounter
     */
    public final long getLinecounter() {
	return linecounter;
    }

    /**
     * Instantiates a new meta debug output file.
     */
    public MetaDebugOutputFile() {
	linecounter = Long.valueOf(0);
    }

    /**
     * Discover databases and notes. This does a parsing of all the lines and
     * inputs the result in a tree Database --> Note --> MetaLine. There is no
     * call to the server at this point.
     */
    public final void discoverDatabasesAndNotes() {
	for (Line line : getAllLines()) {
	    if (!line.getParsedDBReplicaID().isEmpty()) {
		String currentparsedDBreplicaID = line.getParsedDBReplicaID();
		// There can be more than one per line!

		// we avoid the entries which correspond to server entries
		if (!currentparsedDBreplicaID.equals("0000000000000000")) {
		    MetaDatabase metaDB = databasesWithReplicaID
			    .get(currentparsedDBreplicaID);
		    if (metaDB == null) {
			metaDB = new MetaDatabase();
			databasesWithReplicaID.put(currentparsedDBreplicaID,
				metaDB);
		    }
		    metaDB.setReplicaID(currentparsedDBreplicaID);
		    metaDB.setFilePath(line.getParsedDBFilePath());
		    metaDB.setServerName(line.getParsedServerName());
		    // there are some lines which refer only to the database,
		    // not to the note
		    line.setDb(metaDB);
		    if (!line.getParsedNoteID().isEmpty()) {
			MetaNote metanote = metaDB.getListOfNotes().get(
				line.getParsedNoteID());
			if (metanote == null) {
			    MetaNote newnote = new MetaNote();
			    newnote.setNoteID(line.getParsedNoteID());
			    metaDB.addNote(newnote);
			    metanote = newnote;
			}

			// below is the magic moment when I a matching a
			// metadatabase and a metanote to a line
			line.setMetaNote(metanote);
		    }
		}
	    }
	    // we also look for the databases that have a clear server and
	    // filename. These will be matched to the databases for whom we only
	    // have a replicaID reference
	    if (!line.getParsedDBFilePath().isEmpty()) {

		MetaDatabase currentServerAndFilePath = databasesWithServerAndFileName
			.get(line.getParsedServerName() + "!!"
				+ line.getParsedDBFilePath());
		if (currentServerAndFilePath == null) {

		    currentServerAndFilePath = new MetaDatabase();
		    currentServerAndFilePath.setServerName(line
			    .getParsedServerName());
		    currentServerAndFilePath.setFilePath(line
			    .getParsedDBFilePath());

		    databasesWithServerAndFileName.put(
			    line.getParsedServerName() + "!!"
				    + line.getParsedDBFilePath(),
			    currentServerAndFilePath);
		}
		currentServerAndFilePath.addLineNumber(line.getLineCount());
	    }
	}

    }

    /**
     * Open databases and discover nature of notes. I loop through the list of
     * databases which we found with a filepath and a server name, and using its
     * newly discovered ReplicaID, update the list of databases with a replica
     * ID. It's this second list which is the 'master list', the other list with
     * servers and filenames is no longer useful after this. Once the database
     * is opened, I read the metanotes and try to discover more information.
     */
    public final void openDatabasesAndDiscoverNatureOfNotes() {
	for (MetaDatabase metadbWithServerAndFileName : databasesWithServerAndFileName
		.values()) {

	  
	    Database currentRealDatabase;
	    try {
		session = NotesFactory.createSession();
		currentRealDatabase = session.getDatabase(
			metadbWithServerAndFileName.getServerName(),
			metadbWithServerAndFileName.getFilePath());

		if (currentRealDatabase != null) {
		    if (currentRealDatabase.isOpen()) {

			MetaDatabase currentMetaDb = this.databasesWithReplicaID
				.get(currentRealDatabase.getReplicaID());

			// There are cases when a database is opened (I had an
			// example with a poll_del_seqnum) but reveals nothing
			// about it's replicaID, i.e. there are no calls in the
			// subsequent data file containing replica ids. In this case
			// the database will not be seen, so I have to check for
			// its presence. Thanks to Lars Bentrop-Bos for pointing
			// this out to me.
			if (currentMetaDb != null) {
			    currentMetaDb
				    .importInfoFromRealDatabase(currentRealDatabase);

			    switchLinkedLinesFromFirstMetaDBToSecondMetaDB(
				    metadbWithServerAndFileName, currentMetaDb);

			    for (MetaNote currentMetaNote : currentMetaDb
				    .getListOfNotes().values()) {
				Document actualNote = currentRealDatabase
					.getDocumentByID(currentMetaNote
						.getNoteID());
				if (actualNote == null) {
				    currentMetaNote
					    .setNoteTitle("note not found - perhaps deleted?");
				} else {
				    currentMetaNote.setActualNote(actualNote);
				    currentMetaNote.findNoteTypeAndNoteTitle();
				}
			    }
			}
		    }
		}
	    } catch (NotesException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Assign linked lines to current meta db. I link a line to a particular
     * metadatabase. When I am trying to merge the list of databases identified
     * through the filepath-Server with the list of databases identified through
     * the replica id (which is the list I am interested in), I need to switch
     * the 'parent metadatabase'.
     * 
     * @param metadbWithServerAndFileName
     *            the metadb with server and file name
     * @param currentMetaDb
     *            the current meta db
     */
    private void switchLinkedLinesFromFirstMetaDBToSecondMetaDB(
	    final MetaDatabase metadbWithServerAndFileName,
	    final MetaDatabase currentMetaDb) {
	for (Long lineNumber : metadbWithServerAndFileName
		.getListOfLineNumbers()) {
	    Line x = allLines.get(lineNumber);
	    if (x != null) {
		x.setDb(currentMetaDb);
	    }
	}
    }

    /**
     * Mark slowest entries.
     */
    public final void markSlowestEntries() {
	long cumulativeBarrier = getTotalDurationInMilliseconds() * 40 / 100;

	long cumulativeCount = 0;
	ValueComparator bvc = new ValueComparator(allLines);
	TreeMap<Long, Line> sortedMap = new TreeMap<Long, Line>(bvc);

	sortedMap.putAll(allLines);

	OUTER: for (Line entry : sortedMap.values()) {
	    cumulativeCount = cumulativeCount
		    + entry.getDurationInMilliseconds();
	    entry.setIsSlow(true);
	    if (cumulativeCount > cumulativeBarrier) {
		break OUTER;
	    }
	}
    }

    /**
     * Adds the line.
     * 
     * @param inputlp
     *            the inputlp
     */
    public final void addLine(final Line inputlp) {
	if (inputlp != null) {
	    allLines.put(linecounter, inputlp);
	    totalDurationInMilliseconds = totalDurationInMilliseconds
		    + inputlp.getDurationInMilliseconds();
	    inputlp.setLineCount(linecounter);
	    linecounter = linecounter + 1;
	}
    }

    /**
     * The Class CompareByLineNumber.
     */
    class CompareByLineNumber implements Comparator<Line> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(final Line one, final Line two) {
	    return one.getLineCount().compareTo(two.getLineCount());

	}
    }

    /**
     * The Class LineParserCompare.
     */
    class LineParserCompare implements Comparator<Line> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(final Line one, final Line two) {
	    return Double.compare(two.getDurationInMilliseconds(),
		    one.getDurationInMilliseconds());
	}
    }

}
