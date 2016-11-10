/*
 * 
 */
package com.magerman.nrpc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

public class Line {

    /** The Constant NO_THREAD_FOUND. */
    public static final String NO_THREAD_FOUND = "[no thread ID found]";

    /**
     * Clean notes9prependtext.
     * 
     * @param inputline
     *            the inputline
     * @return the string
     */
    public static String cleanNotes9prependtext(final String inputline) {
        Pattern findNotes9prefix = Pattern.compile("^\\[.*?\\] ");
        Matcher m = findNotes9prefix.matcher(inputline);
        if (m.find()) {
            return m.replaceFirst("");
        }
        // If we didn't find this prefix then ignore;
        return inputline;
    }

    /** The arguments. */
    private String arguments = "";

    /** The bytes in. */
    private int bytesIn = 0;

    /** The bytes out. */
    private int bytesOut = 0;

    /** The bytes total. */
    private int bytesTotal = 0;

    /** The command. */
    private String command = "";

    /** The duration in milliseconds. */
    private int durationInMilliseconds = 0;

    /** The error message. */
    private String errorMessage = "";

    /** The first mystery number. */
    private int firstMysteryNumber = 0;

    /** The is slow. */
    private Boolean isSlow = false;

    /** The line count. */
    private long lineCount = 0;

    /** The matcher case. */
    private String matcherCase = "";

    /** The meta db. */
    private MetaDatabase metaDb = new MetaDatabase();

    /** The metanote. */
    private MetaNote metanote = new MetaNote();

    /** The note mystery number. */
    private String noteMysteryNumber = "";

    /** The nrpc line. */
    private String nrpcLine = "";

    /** The original line. */
    private String originalLine = "";
    /** The parsed db file path. */
    private String parsedDBFilePath = "";

    /** The parsed db replica id. */
    private String parsedDBReplicaID = "";

    /** The parsed note id. */
    private String parsedNoteID = "";

    /** The parsed server name. */
    private String parsedServerName = "";

    /** The second mystery number. */
    private int secondMysteryNumber;

    /** The sequence number. */
    private int sequenceNumber;

    /** The thread id. */
    private String threadID;

    /** The translation. */
    private String translation;

    /**
     * Instantiates a new line.
     * 
     * @param inputline
     *            the inputline
     */
    public Line(final String inputline) {
        // Since Notes 9 there is a different syntax for the debug lines. I am
        // not sure yet what these lines mean, but since they interfere with the
        // previous logic I am going to remove them by force

        originalLine = inputline;
        nrpcLine = cleanNotes9prependtext(inputline);

    }

    /**
     * Find bytes in bytes out.
     */
    public final void findBytesInBytesOut() {
        // Now we look if the RightOfSequenceNumbers *ends* with the
        // bytes-in-bytes-out construct [76+24614=24690]
        Pattern patternLookifEndsWithBytesInBytesout = Pattern.compile("^(.+) \\[(\\d+)\\+(\\d+)=(\\d+)\\](?: \\((.*)\\))??$");
        Matcher mx = patternLookifEndsWithBytesInBytesout.matcher(nrpcLine);
        if (mx.matches()) {
            bytesIn = Integer.parseInt(mx.group(2));
            bytesOut = Integer.parseInt(mx.group(3));
            bytesTotal = Integer.parseInt(mx.group(4));
            setErrorMessage(mx.group(5));
        }
    }

    /**
     * Find command.
     */
    public final void findCommand() {
        Pattern p = Pattern.compile("^.*\\) ([A-Z0-9_ ]+?)\\((.+)\\).*$");
        Matcher m = p.matcher(nrpcLine);
        if (m.matches()) {
            command = m.group(1);
            arguments = m.group(2);
            return;
        }

        // [0DD0:0002-0D1C] (5-52 [25]) SV_INFO_GET_RQST: 134 ms. [14+28=42]
        Pattern p2 = Pattern.compile("^.* (\\w+): \\d+ ms\\..*$");
        Matcher m2 = p2.matcher(nrpcLine);
        if (m2.matches()) {
            command = m2.group(1);
            return;
        }

    }

    /**
     * Find duration in milliseconds.
     */
    public final void findDurationInMilliseconds() {
        Pattern p = Pattern.compile("^.+ (\\d+) ms\\..+$");
        Matcher m = p.matcher(nrpcLine);
        if (m.matches()) {
            durationInMilliseconds = Integer.parseInt(m.group(1));
        }
    }

    /**
     * Find replica id and note ids.
     */
    public final void findReplicaIDAndNoteIDs() {

        // REPC1257635:004DDA90-NT00000226,0040,4008
        Pattern ViewOpen = Pattern.compile("^.*REP([A-F0-9]{8}):([A-F0-9]{8})-NT([A-F0-9]{8}),(\\d{4}),(\\d{4}).*$");
        Matcher s = ViewOpen.matcher(this.originalLine);
        if (s.find()) {
            setParsedDBReplicaID(s.group(1) + s.group(2));
            setParsedNoteID(s.group(3));
            // there are also the next two arguments, but I don't get them
            return;
        }

        // REPC12574EF:00319A6E-NT00001B2E,00000000
        Pattern OpenNote = Pattern.compile("^.*REP([A-F0-9]{8}):([A-F0-9]{8})-NT([A-F0-9]{8}),([A-F0-9]{8}).*$");
        Matcher t = OpenNote.matcher(this.originalLine);
        if (t.find()) {
            setParsedDBReplicaID(t.group(1) + t.group(2));
            setParsedNoteID(t.group(3));

            // there are also the next two arguments, but I don't get them
            return;
        }
        // REPC12574EF:00319A6E-NT00001B2E
        Pattern UpdateFilters = Pattern.compile("^.*REP([A-F0-9]{8}):([A-F0-9]{8})-NT([A-F0-9]{8}).*$");
        // I could be clever and start squeezing these all into a large
        // über-regular expression, but everything would be unreadable...
        Matcher u = UpdateFilters.matcher(this.originalLine);
        if (u.find()) {
            setParsedDBReplicaID(u.group(1) + u.group(2));
            setParsedNoteID(u.group(3));

            return;
            // there are also the next two arguments, but I don't get them
        }
        // REP80256592:003E0158-RRV0000011E,0xFFFFFFFF at 0x0
        Pattern Read_Object = Pattern.compile("^.*REP([A-F0-9]{8}):([A-F0-9]{8})-RRV([A-F0-9]{8}),0x([A-F0-9]+) at .*$");
        Matcher v = Read_Object.matcher(this.originalLine);
        if (v.find()) {

            setParsedDBReplicaID(v.group(1) + v.group(2));
            setParsedNoteID(v.group(3));

            // there are also the next two arguments, but I don't get them
            return;
        }
        // REPC12574EF:00319A6E-NT0000041E,Since:27.10.2009 06:35:35
        Pattern Read_Entries = Pattern.compile("^.*REP([A-F0-9]{8}):([A-F0-9]{8})-NT([A-F0-9]{8}),Since:.*$");
        Matcher w = Read_Entries.matcher(this.originalLine);
        if (w.find()) {
            setParsedDBReplicaID(w.group(1) + w.group(2));
            setParsedNoteID(w.group(3));

            // there are also the next two arguments, but I don't get them
            return;
        }

        // REPC1257635:004DDA90
        Pattern patternReplicaIDpat = Pattern.compile("^.*REP([A-F0-9]{8}):([A-F0-9]{8}).*$");
        Matcher o = patternReplicaIDpat.matcher(this.originalLine);
        if (o.find()) {
            setParsedDBReplicaID(o.group(1) + o.group(2));
            return;
        }
    }

    /**
     * Find server name file path.
     */
    public final void findServerNameFilePath() {
        // We are generally looking for some servers; this is important for
        // later. This routine comes at the end of everything to remove false
        // positives
        Pattern findserverandFileName = Pattern.compile("^.+?(?:OPEN_DB|POLL_DEL_SEQNUM)\\((CN=.+?)!!(.+?\\.n\\w\\w)\\).*$");
        Matcher q = findserverandFileName.matcher(originalLine);
        if (q.find()) {
            setParsedServerName(q.group(1));
            setParsedDBFilePath(q.group(2));
            return;
        }

        // [0F00:0002-0EA8] (1-50 [1])
        // OPEN_DB(domino-77.prominic.net!!A55BAE\mail\Andrew_Magerman.nsf):
        // (Connect to domino-77.prominic.net: 0 ms) (Exch names: 0
        // ms)(Authenticate:
        Pattern p = Pattern.compile("^.+?(?:OPEN_DB|POLL_DEL_SEQNUM)\\((.+?)!!(.+?\\.n\\w\\w)\\).*$");
        Matcher m = p.matcher(originalLine);
        if (m.find()) {
            setParsedServerName(m.group(1));
            setParsedDBFilePath(m.group(2));
            return;
        }

    }

    /**
     * Find start sequence.
     */
    public final void findStartSequence() {
        Pattern SequenceNumberExtract = Pattern.compile("^\\((\\d+)-(\\d+) \\[(\\d+)\\]\\) (.+)$");
        Matcher m = SequenceNumberExtract.matcher(nrpcLine);
        if (m.matches()) {
            firstMysteryNumber = Integer.parseInt(m.group(1));
            secondMysteryNumber = Integer.parseInt(m.group(2));
            sequenceNumber = Integer.parseInt(m.group(3));
        }
    }

    /**
     * Find thread id.
     */
    public final void findThreadID() {
        // [0DD0:0002-0D1C] (5-52 [25]) SV_INFO_GET_RQST: 134 ms. [14+28=42]
        Pattern p = Pattern.compile("^(\\[\\w+?:\\w+?-\\w+?.+?]).*$");
        Matcher m = p.matcher(this.originalLine);
        if (m.find()) {
            setThreadID(m.group(1));
            return;
        }
        setThreadID(NO_THREAD_FOUND);
    }

    /**
     * Gets the arguments.
     * 
     * @return the arguments
     */
    public final String getArguments() {
        return arguments;
    }

    /**
     * Gets the bytes in.
     * 
     * @return the bytes in
     */
    public final int getBytesIn() {
        return bytesIn;
    }

    /**
     * Gets the bytes out.
     * 
     * @return the bytes out
     */
    public final int getBytesOut() {
        return bytesOut;
    }

    /**
     * Gets the bytes total.
     * 
     * @return the bytes total
     */
    public final int getBytesTotal() {
        return bytesTotal;
    }

    /**
     * Gets the command.
     * 
     * @return the command
     */
    public final String getCommand() {
        return command;
    }

    /**
     * Gets the common server name.
     * 
     * @return the common server name
     */
    public final String getCommonServerName() {
        Session session;
        try {
            session = NotesFactory.createSession();
            Name servername = session.createName(this.getServerName());
            return servername.getCommon();
        } catch (NotesException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Gets the database title.
     * 
     * @return the database title
     */
    public final String getDatabaseTitle() {
        // return DatabaseName;
        String title = metaDb.getDatabaseTitle();
        if (!title.isEmpty()) {
            return title;
        }
        return metaDb.getFilePath();

    }

    /**
     * Gets the db.
     * 
     * @return the db
     */
    public final MetaDatabase getmetaDb() {
        return metaDb;
    }

    /**
     * Gets the dB replica id.
     * 
     * @return the dB replica id
     */
    public final String getDBReplicaID() {
        return metaDb.getReplicaID();
    }

    /**
     * Gets the duration in milliseconds.
     * 
     * @return the duration in milliseconds
     */
    public final int getDurationInMilliseconds() {
        return durationInMilliseconds;
    }

    /**
     * Gets the error message.
     * 
     * @return the error message
     */
    public final String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gets the first mystery number.
     * 
     * @return the first mystery number
     */
    public final int getFirstMysteryNumber() {
        return firstMysteryNumber;
    }

    /**
     * Gets the checks if is slow.
     * 
     * @return the checks if is slow
     */
    public final Boolean getIsSlow() {
        return isSlow;
    }

    /**
     * Gets the line count.
     * 
     * @return the line count
     */
    public final Long getLineCount() {
        return lineCount;
    }

    /**
     * Gets the matcher case.
     * 
     * @return the matcher case
     */
    public final String getMatcherCase() {
        return matcherCase;
    }

    /**
     * Gets the note.
     * 
     * @return the note
     */
    public final MetaNote getNote() {
        return metanote;
    }

    /**
     * Gets the note mystery number.
     * 
     * @return the note mystery number
     */
    public final String getNoteMysteryNumber() {
        return noteMysteryNumber;
    }

    /**
     * Gets the note type.
     * 
     * @return the note type
     */
    public final String getNoteType() {
        return metanote.getNoteType();
    }

    /**
     * Gets the original line.
     * 
     * @return the original line
     */
    public final String getOriginalLine() {
        return originalLine;
    }

    /**
     * Gets the parsed db file path.
     * 
     * @return the parsed db file path
     */
    public final String getParsedDBFilePath() {
        return parsedDBFilePath;
    }

    /**
     * Gets the parsed db replica id.
     * 
     * @return the parsed db replica id
     */
    public final String getParsedDBReplicaID() {
        return parsedDBReplicaID;
    }

    /**
     * Gets the parsed note id.
     * 
     * @return the parsed note id
     */
    public final String getParsedNoteID() {
        return parsedNoteID;
    }

    /**
     * Gets the parsed server name.
     * 
     * @return the parsed server name
     */
    public final String getParsedServerName() {
        return parsedServerName;
    }

    /**
     * Gets the second mystery number.
     * 
     * @return the second mystery number
     */
    public final int getSecondMysteryNumber() {
        return secondMysteryNumber;
    }

    /**
     * Gets the sequence number.
     * 
     * @return the sequence number
     */
    public final int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Gets the server name.
     * 
     * @return the server name
     */
    public final String getServerName() {
        return metaDb.getServerName();
    }

    /**
     * Gets the thread id.
     * 
     * @return the thread id
     */
    public final String getThreadID() {
        return threadID;
    }

    public final String getNotesURL() {
        return String.format("notes://%s/%s/0/%s?OpenDocument", this.getServerName(), this.getDBReplicaID(), this.getNote().getUNID());
    }

    public final String getProfileDocumentDescriptor(String inputArguments) {
        // REP86257A75:00455282-$profile_014fa_translation_global~en,00400020
        // REPC12575D0:001AD0CD-$profile_006config_cn=andrew magerman/ou=magerman/o=notesnet,00400020

        Pattern profileDocument = Pattern.compile(".*-\\$profile_\\d{0,3}(.*?)(?:_cn=(.*?))?,\\d{8}");
        Matcher s = profileDocument.matcher(inputArguments);
        if (s.find()) {
            String profileDocName = s.group(1);
            String profileDocKey = s.group(2);
            if (profileDocKey == null) {
                return "'" + profileDocName + "'";
            } else {
                return "'" + profileDocName + "' , key:'" + profileDocKey + "'";
            }

        }

        return inputArguments;
    }

    /**
     * Gets the translated command.Translates the command into something digestible. As a default, translate gives the original command
     * back.
     * 
     * @return the translated command
     */
    public final String getTranslatedCommand() {
        translation = command;
        try {
            if (command.equals("GET_SPECIAL_NOTE_ID")) {
                translation = "Retrieve info from the ACL";
            } else if (command.equals("GET_NAMED_OBJECT_ID")) {
                if (arguments.equals("$PrivateDesign")) {
                    translation = "Retrieves the NoteID of the $PrivateDesign view, which is a mini-view used to find private design elements";
                } else if (arguments.startsWith("$profile")) {
                    translation = "Retrieves the NoteID of the profile document";
                } else {
                    translation = "Retrieves the NoteID of a particular note";
                }
            } else if (command.equals("OPEN_SESSION")) {
                translation = "Authenticate with server and establish a session";
            } else if (command.equals("GET LAST INDEX TIME")) {
                translation = "Retrieves last index time of the full text index";
            } else if (command.equals("START_SERVER")) {
                translation = "Start User Session";
            } else if (command.equals("UPDATE_FILTERS")) {
                translation = "refreshes the view by making sure the selection formula has correctly taken all documents into the view";
            } else if (command.equals("OPEN_NOTE_UNIDNAME_RQST")) {
                translation = "Retrieves and caches a profile document: " + getProfileDocumentDescriptor(this.arguments);
                // TODO
            } else if (command.equals("DB_INFO_GET")) {
                translation = "Get database info buffer";
            } else if (command.equals("DB_REPLINFO_GET")) {
                translation = "Get replication information";
            } else if (command.equals("SET_UNREAD_NOTE_TABLE")) {
                translation = "This function merges changes between the original ID table and the modified table into the specified user's unread note list for the database.";

            } else if (command.equals("POLL_DEL_SEQNUM")) {
                translation = "Get delivery sequence number";
            } else if (command.equals("DB_GETSET_DEL_SEQNUM")) {
                translation = "Get or set delivery sequence number";
            } else if (command.equals("DB_MODIFIED_TIME")) {
                translation = "Checks last time database was modified";
            } else if (command.equals("SERVER_AVAILABLE_LITE")) {
                translation = "Server is available";
            } else if (command.equals("GET_COLLATION")) {
                translation = "Gets the collation for the view. A collation is a set of columns and sort specifications that determines how the view or folder will be sorted.";
            } else if (command.equals("GET_UNREAD_NOTE_TABLE")) {
                translation = "Retrieves the table of unread documents for the current database. Tip: Enable the database property 'Don't maintain unread marks' if you are not using them!";
            } else if (command.equals("ISDB2_RQST")) {
                translation = "Checking whether the database has an underlying DB2 Structure";
            } else if (command.equals("READ REPLICATION HISTORY")) {
                translation = "Reading the Replication History of the database";
            } else if (command.equals("STAMP_NOTES")) {
                translation = "Stamps the same value or flag on the current selection (could be a delete flag)";
            } else if (command.equals("GET_NOTE_INFO")) {
                translation = "Retrieves some note info";
            } else if (command.equals("DBGETREPLICAMATCHES")) {
                translation = "Scan the server for databases to replicate";
            } else if (command.equals("FINDDESIGN_NOTES")) {
                translation = "Finds a design note";
            } else if (command.equals("OPEN_NOTE")) {
                translation = "Loads " + metanote.getNoteType() + " '" + metanote.getNoteTitle() + "'";
            } else if (command.equals("OPEN_COLLECTION")) {
                translation = "Opens View " + metanote.getNoteTitle();
            } else if (command.equals("UPDATE_NOTE")) {
                translation = "Updates a note";
            } else if (command.equals("SEARCH")) {
                translation = "Search operation with Formula";
            } else if (command.equals("GET_COLLECTION_DATA")) {
                translation = "Gets collection (view) data";
            } else if (command.equals("READ_ENTRIES")) {
                translation = "Reads entries (sends data) from the view to the client";
            } else if (command.equals("CLOSE_COLLECTION")) {
                translation = "Close View " + metanote.getNoteTitle();
            } else if (command.equals("UPDATE_COLLECTION")) {
                translation = "Refreshes the view ";
            } else if (command.equals("FIND_BY_KEY")) {
                translation = "A view lookup via @DBLookup";
            } else if (command.equals("FIND_BY_KEY_EXTENDED2")) {
                translation = "A view lookup via @DBLookup";
            } else if (command.equals("GET_SERVER_STATS")) {
                translation = "Gets Server Statistics (?)";
            } else if (command.equals("GETOBJECT_RQST")) {
                translation = "Gets Object";
            } else if (command.equals("ALLOC_UPDATE_OBJECT")) {
                translation = "Create or update object";
            } else if (command.equals("WRITE_OBJECT")) {
                translation = "Writes into this note: " + metanote.getNoteTitle();
            } else if (command.equals("READ_OBJECT")) {
                translation = "Reads from this this note: " + metanote.getNoteTitle();
            } else if (command.equals("GET_ALLFOLDERCHANGES_RQST")) {
                translation = "Request all Changes in Folders (which document is present in which folder)";
            } else if (command.equals("GET_MODIFIED_NOTES")) {
                translation = "Get a list of modified notes";
            } else if (command.equals("NAME_LOOKUP32")) {
                translation = "Look up names in Address Books and get item values";
            } else if (command.equals("NAME_LOOKUP")) {
                translation = "Look up names in Address Books and get item values";
            } else if (command.equals("OPEN_DB")) {
                translation = "Opens database";
            } else if (command.equals("GET_OBJECT_SIZE")) {
                translation = "Get the size and class of an object in a database file. Used when updating an object to check that the size of the object is large enough to store the updated object data. ";
            } else if (command.equals("CLOSE_DB")) {
                if (arguments.equals("REP00000000:00000000")) {
                    translation = "close server session";
                } else {
                    translation = "close database";
                }
            } else if (command.equals("LOCATE_NOTE")) {
                translation = "Preparation for reading a NotesViewEntry";
            } else if (command.equals("GET_DBINFO")) {
                translation = "Gets database information";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return translation;

    }

    /**
     * Analyses a single line of the text file and tries to find out things like the sequence number, the time, etc.
     */
    public final void parse() {
        findServerNameFilePath();
        findBytesInBytesOut();
        findReplicaIDAndNoteIDs();
        findStartSequence();
        findDurationInMilliseconds();
        findCommand();
        findThreadID();
        return;
    }

    /**
     * Sets the db.
     * 
     * @param db
     *            the new db
     */
    public final void setDb(final MetaDatabase db) {
        metaDb = db;
    }

    /**
     * Sets the error message.
     * 
     * @param val
     *            the new error message
     */
    public final void setErrorMessage(final String val) {
        errorMessage = val;
    }

    /**
     * Sets the checks if is slow.
     * 
     * @param val
     *            the new checks if is slow
     */
    public final void setIsSlow(final Boolean val) {
        isSlow = val;
    }

    /**
     * Sets the line count.
     * 
     * @param val
     *            the new line count
     */
    public final void setLineCount(final long val) {
        lineCount = val;
    }

    /**
     * Sets the matcher case.
     * 
     * @param val
     *            the new matcher case
     */
    public final void setMatcherCase(final String val) {
        matcherCase = val;
    }

    /**
     * Sets the meta note.
     * 
     * @param note
     *            the new meta note
     */
    public final void setMetaNote(final MetaNote note) {
        this.metanote = note;
    }

    /**
     * Sets the note mystery number.
     * 
     * @param val
     *            the new note mystery number
     */
    public final void setNoteMysteryNumber(final String val) {
        noteMysteryNumber = val;
    }

    /**
     * Sets the parsed db file path.
     * 
     * @param val
     *            the new parsed db file path
     */
    public final void setParsedDBFilePath(final String val) {
        this.parsedDBFilePath = val;
    }

    /**
     * Sets the parsed db replica id.
     * 
     * @param string
     *            the new parsed db replica id
     */
    private void setParsedDBReplicaID(final String string) {
        parsedDBReplicaID = string;
    }

    /**
     * Sets the parsed note id.
     * 
     * @param val
     *            the new parsed note id
     */
    public final void setParsedNoteID(final String val) {
        this.parsedNoteID = val;
    }

    /**
     * Sets the parsed server name.
     * 
     * @param val
     *            the new parsed server name
     */
    public final void setParsedServerName(final String val) {
        this.parsedServerName = val;
    }

    /**
     * Sets the server name.
     * 
     * @param serverName
     *            the new server name
     */
    public void setServerName(final String serverName) {
    }

    /**
     * Sets the thread id.
     * 
     * @param val
     *            the new thread id
     */
    public final void setThreadID(final String val) {
        this.threadID = val;
    }

    @Override
    public final String toString() {
        return "Line [LineCount=" + lineCount + ", metanote=" + metanote + ", originalLine=" + originalLine + ", parsedDBFilePath="
                + parsedDBFilePath + ", parsedDBReplicaID=" + parsedDBReplicaID + ", parsedServerName=" + parsedServerName + "]";
    }

}
