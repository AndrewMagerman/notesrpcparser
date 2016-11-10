package com.magerman.nrpc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntryCollection;

/**
 * The Class FileParser.
 */
public class FileParser {

    /** The Output debug file. */
    private File outputDebugFile;

    /** The this db. */
    private Database thisDB;

    /**
     * Gets the output debug file.
     * 
     * @return the output debug file
     */
    public final File getOutputDebugFile() {
	// I make a lazy loading here because I want to set it externally for
	// testing.
	if (this.outputDebugFile == null) {
	    try {
		Session session;
		session = NotesFactory.createSession();
		Document configurationDocument;
		configurationDocument = thisDB.getProfileDocument("Config",
			session.getUserName());
		outputDebugFile = new File(
			configurationDocument
				.getItemValueString("txt_ParseThisFile"));

	    } catch (NotesException e) {
		e.printStackTrace();
		return null;
	    }
	}

	return outputDebugFile;
    }

    /**
     * Parses the file.
     * 
     * @param inputThisDB
     *            the input this db
     */
    public final void parseFile(final Database inputThisDB) {

	try {
	    Session session;
	    session = NotesFactory.createSession();
	    thisDB = inputThisDB;
	    View latestParsingView = thisDB.getView("LatestParsing");
	    latestParsingView.setAutoUpdate(false);
	    ViewEntryCollection allentries = latestParsingView.getAllEntries();
	    allentries.stampAll("ShowInLatestParsingViewNO", 0);	    
	    String strUserName = session.getCommonUserName();
	    DateTime dtParseTime = session.createDateTime("Today");
	    dtParseTime.setNow();

	    try {
		BufferedReader input = new BufferedReader(new FileReader(
			getOutputDebugFile()));
		try {
		    String line = null;
		    MetaDebugOutputFile lol = new MetaDebugOutputFile();
		    // reads the text file line by line;
		    while ((line = input.readLine()) != null) {
			// some commands have more than one NRPC Call per
			// physical line; these need to be split up
			ArrayList<String> splitlines = splitThisLineIntoCalls(line);
			for (String splitline : splitlines) {
			    Line lp = new Line(splitline);
			    lp.parse();
			    lol.addLine(lp);
			}
		    }
		    // Then we do here actions which depend on the whole list
		    // (as opposed to single lines)
		    lol.discoverDatabasesAndNotes();
		    lol.openDatabasesAndDiscoverNatureOfNotes();
		    lol.markSlowestEntries();

		    for (Line entry : lol.getAllLines()) {
			createNotesDoc(strUserName, dtParseTime, entry);
		    }
		} finally {
		    input.close();
		    latestParsingView.setAutoUpdate(true);
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	} catch (NotesException n) {
	    n.printStackTrace();
	}
    }

    /**
     * Creates the notes doc.
     * 
     * @param strUserName
     *            the str user name
     * @param dtParseTime
     *            the dt parse time
     * @param entry
     *            the entry
     */
    private void createNotesDoc(final String strUserName,
	    final DateTime dtParseTime, final Line entry) {
	Document docLine;
	try {
	    docLine = thisDB.createDocument();

	    docLine.replaceItemValue("Form", "RPCLine");
	    docLine.replaceItemValue("ShowInLatestParsingViewNO", 1);
	    
	    docLine.replaceItemValue("txt_ThreadID", entry.getThreadID());
	    docLine.replaceItemValue("txt_UserName", strUserName);
	    docLine.replaceItemValue("dt_ParseTime", dtParseTime);
	    docLine.replaceItemValue("num_lineNumber", entry.getLineCount());
	    docLine.replaceItemValue("txt_NRPCLine", entry.getOriginalLine());
	    docLine.replaceItemValue("num_SequentialNumber",
		    entry.getSequenceNumber());
	    docLine.replaceItemValue("txt_Command",
		    entry.getTranslatedCommand());
	    docLine.replaceItemValue("num_ElapsedTime",
		    entry.getDurationInMilliseconds());
	    docLine.replaceItemValue("num_BytesSent", entry.getBytesOut());
	    docLine.replaceItemValue("num_BytesReceived", entry.getBytesIn());

	    docLine.replaceItemValue("txt_ServerName",
		    entry.getCommonServerName());

	    docLine.replaceItemValue("txt_DBFilePath", entry.getmetaDb()
		    .getFilePath());

	    docLine.replaceItemValue("txt_NoteUNID", entry.getNote()
		    .getUNID());

	    docLine.replaceItemValue("txt_DBTitle", entry.getDatabaseTitle());
	    docLine.replaceItemValue("txt_NotesURL", entry.getNotesURL());
	    docLine.replaceItemValue("txt_Category", entry.getNote().getCategory());
	    
	    
	    if (entry.getNote() != null) {

		docLine.replaceItemValue("txt_Type", entry.getNote()
			.getNoteType());
		docLine.replaceItemValue("txt_Title", entry.getNote()
			.getNoteTitle());
	    }
	    if (entry.getIsSlow()) {
		docLine.replaceItemValue("ShowSlow", 1);
		docLine.markUnread();
	    } else {
		docLine.markRead();
	    }
		
	    docLine.save(true, true);
	    if (entry.getIsSlow()) {
		docLine.markUnread();
	    } else {
		docLine.markRead();
	    }

	} catch (NotesException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Split this line into calls.
     * 
     * @param line
     *            the line
     * @return the array list
     */
    public static ArrayList<String> splitThisLineIntoCalls(final String line) {

	ArrayList<String> r = new ArrayList<String>();

	// First try to split the line if there is a thread id
	// [0F00:0002-0EA8] (28-57 [77])
	// OPEN_NOTE(REP862572C6:006BEDBE-NTFFFF0010,03000400):
	// [0F3C:0007-0D3C:wrepl] (22-57 [78])
	// OPEN_DB(CN=domino-79.prominic.net/O=Prominic!!): (Connect to
	// domino-79.prominic.net/Prominic: 0 ms) (Exch names: 1
	// ms)(Authenticate: 135 ms. [48+1908=1956]
	Pattern p = Pattern
		.compile("(\\[.*\\] \\(\\d+-\\d+ \\[\\d+\\]\\).+)(\\[.*\\] \\(\\d+-\\d+ \\[\\d+\\]\\).+)");
	Matcher m = p.matcher(line);
	if (m.matches()) {
	    r.add(m.group(1));
	    r.add(m.group(2));
	    return r;
	}

	// try pattern with two threads but one sequence number
	// [0DD0:0002-0D1C] (3-51 [19]) SV_INFO_GET_RQST: [0F3C:0007-0D3C:wrepl]
	// (Opened: REP862572C6:006BEDBE) 137 ms. [134+290=424]

	Pattern p3 = Pattern
		.compile("(\\[.*\\] \\(\\d+-\\d+ \\[\\d+\\]\\).+)(\\[.*\\] .+)");
	Matcher m3 = p3.matcher(line);
	if (m3.matches()) {
	    r.add(m3.group(1));
	    r.add(m3.group(2));
	    return r;
	}

	// try pattern with one thread but two sequence numbers
	// [0F00:0011-0E7C:newmail] (257-121 [326])
	// POLL_DEL_SEQNUM(CN=domino-77.prominic.net/O=Prominic!!A55BAE\\mail\\Andrew_Magerman.nsf):
	// (258-121 [326]) OPEN_NOTE(REP862572C6:006BEDBE-NT0000D0C6,03400008):
	// (Connect to domino-77.prominic.net/Prominic: 269 ms) (OPEN_SESSION:
	// 134 ms)
	Pattern p4 = Pattern
		.compile("(\\[.*\\] \\(\\d+-\\d+ \\[\\d+\\]\\).+ )(\\(\\d+-\\d+ \\[\\d+\\]\\).+)");
	Matcher m4 = p4.matcher(line);
	if (m4.matches()) {
	    r.add(m4.group(1));
	    r.add(m4.group(2));
	    return r;
	}

	// then try the pattern without a thread
	// (181-133 [186])
	// OPEN_COLLECTION(REPC1257B28:0056D462-NTFFFF0020,0040,4000): (182-133
	// [187])
	// OPEN_DB(CN=albis/O=magerman!!Development\\HRE-Mail\\HRE-Mail2_0\\Hre-Mail_(2_0)_Dev.nsf):
	// (Connect to albis/magerman: 180 ms) (Exch names: 0 ms)(Authenticate:
	// 0 ms.)
	Pattern p2 = Pattern
		.compile("(\\(\\d+-\\d+ \\[\\d+\\]\\).+)(\\(\\d+-\\d+ \\[\\d+\\]\\).+)");
	Matcher m2 = p2.matcher(line);
	if (m2.matches()) {
	    r.add(m2.group(1));
	    r.add(m2.group(2));
	    return r;
	}

	// only then return the original string

	r.add(line);
	return r;
    }

    /**
     * Sets the output debug file.
     * 
     * @param val
     *            the new output debug file
     */
    public final void setOutputDebugFile(final File val) {
	outputDebugFile = val;
    }

}
