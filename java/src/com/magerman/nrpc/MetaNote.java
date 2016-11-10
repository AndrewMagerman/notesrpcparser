package com.magerman.nrpc;

import lotus.domino.Document;
import lotus.domino.NotesException;

// TODO: Auto-generated Javadoc
/**
 * The Class MetaNote.
 */
public class MetaNote {

    /** The actual doc for notes. */
    private Document actualDocForNotes;

    /** The Actual note flags string item value. */
    private String actualNoteFlagsStringItemValue;

    /** The note id. */
    private String noteID;

    /** The Note title. */
    private String noteTitle;

    /** The Note type. */
    private String noteType;

    /** The Parent database. */
    private MetaDatabase parentDatabase;

    private NoteCategory noteCategory;

    public String getUNID() {
	return UNID;
    }

    private String UNID;

    /**
     * Instantiates a new meta note.
     */
    public MetaNote() {
	noteID = "";
	noteTitle = "";
	noteType = "";
	actualNoteFlagsStringItemValue = "";
    }

    /**
     * Discover note type with help from flags.
     */
    public final void discoverNoteTypeWithHelpFromFlags() {
	try {
	    if (actualDocForNotes.hasItem("$Flags")) {
		actualNoteFlagsStringItemValue = actualDocForNotes
			.getItemValueString("$Flags");

		if (flagsContains("^")) {
		    noteType = "Shared Column";
		    return;
		} else if ((!flagsContains("U"))
			&& actualDocForNotes.hasItem("$DesignerVersion")
			&& actualDocForNotes.hasItem("$Fields")) {
		    noteType = "Form";
		} else if (actualDocForNotes.hasItem("$FormulaClass")
			&& actualDocForNotes.hasItem("$Comment")) {
		    if (flagsContains("V")) {
			noteType = "private View";
		    } else {
			noteType = "shared View";
		    }
		} else if (flagsContains("F") && flagsContains("p")) {
		    if (flagsContains("o")) {
			noteType = "Shared Folder, desktop private on first use";
		    } else if (flagsContains("V")) {
			noteType = "Shared Folder, private on first use (private version)";
		    } else {
			noteType = "Shared, private on first use folders(shared version)";
		    }
		} else if (flagsContains("F") && flagsContains("V")) {
		    noteType = "private Folder";
		} else if (flagsContains("F")) {
		    noteType = "shared Folder";

		} else if (flagsContains("U")) {
		    noteType = "Subform";
		} else if (flagsContains("m")) {
		    noteType = "Outline";
		} else if (flagsContains("G")) {
		    noteType = "Navigator";
		} else if (flagsContains("W")) {
		    noteType = "Page";
		} else if (flagsContains("s")) {
		    noteType = "LotusScript Library";
		} else if (flagsContains("t")) {
		    noteType = "Database script";
		} else if (flagsContains("#")) {
		    noteType = "Frameset";
		} else if (actualDocForNotes.hasItem("$AssistType")) {
		    double assistType = actualDocForNotes
			    .getItemValueDouble("$AssistType");
		    if (assistType == -1) {
			noteType = "Simple actions Agent";
		    } else if (assistType == 65412 || assistType == 65426) {
			noteType = "Formula Language Agent";
		    } else if (assistType == 65413) {
			noteType = "LotusScript agent";
		    } else if (assistType == 65427) {
			noteType = "Imported Java";
		    } else if (assistType == 65428) {
			noteType = "Java (with source)";
		    }
		    if (flagsContains("V")) {
			noteType = noteType + " (private)";
		    }
		} else if (flagsContains("X")) {
		    noteType = "Agent data note";
		} else if (flagsContains("g") && flagsContains("K")) {
		    noteType = "XPages";
		} else if (flagsContains("g") && flagsContains(";")) {
		    noteType = "Custom Controls";
		} else if (flagsContains("U")
			&& actualDocForNotes.hasItem("$Fields")
			&& !actualDocForNotes.hasItem("$Comment")) {
		    noteType = "Shared fields";
		} else if (flagsContains("y")) {
		    noteType = "Shared Actions";
		} else if (flagsContains("h")) {
		    noteType = "JavaScript Library";
		} else if (flagsContains(".")) {
		    noteType = "Server JavaScript Library";
		} else if (flagsContains("s")
			&& flagsContains("j")
			&& actualDocForNotes.getItemValueString("$FlagsExt")
				.indexOf("W") == -1) {
		    noteType = "Java Library";
		} else if (flagsContains("s")
			&& actualDocForNotes.getItemValueString("$FlagsExt")
				.indexOf("W") >= 0) {
		    noteType = "Web service consumer";
		} else if (flagsContains("{")) {
		    noteType = "Web service provider";
		} else if (flagsContains("k")) {
		    noteType = "Data connections";
		} else if (flagsContains("z")) {
		    noteType = "DB2 access views";
		} else if (flagsContains("i")) {
		    noteType = "Images";
		} else if (flagsContains("g")
			&& flagsContains("~")
			&& !actualNoteFlagsStringItemValue
				.matches("~|K|\\[|\\]|;|`")) {
		    noteType = "Hidden files (Eclipse project)";
		} else if (flagsContains("g")
			&& !actualNoteFlagsStringItemValue
				.matches("~|K|\\[|\\]|;|`")) {
		    noteType = "Files";
		} else if (flagsContains("@")) {
		    noteType = "Applets";
		} else if (flagsContains("=")) {
		    noteType = "Style sheets";
		} else if (flagsContains("g") && flagsContains("`")) {
		    noteType = "Themes";
		} else if (flagsContains(":")) {
		    noteType = "Wiring properties";
		} else if (flagsContains("|")) {
		    noteType = "Composite applications";
		} else if (flagsContains("O")) {
		    noteType = "Stored full-text query";
		} else if (actualDocForNotes.hasItem("$ReplFormula")) {
		    noteType = "Replication Formula";
		} else if (actualDocForNotes.hasItem("$ACLDigest")) {
		    noteType = "Replication Formula";
		} else if (actualDocForNotes.hasItem("$FormulaClass")
			& !actualDocForNotes.hasItem("$Comment")) {
		    noteType = "Design note collection";
		}
	    }
	} catch (NotesException e) {
	    e.printStackTrace();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public final boolean equals(final Object aNote) {
	if (aNote == null) {
	    return false;
	}
	MetaNote notetocompare = (MetaNote) aNote;
	return getNoteID().equals(notetocompare.getNoteID());
    }

    /**
     * Flags contains the input string.
     * 
     * @param inputString
     *            the input string
     * @return true, if successful
     */
    public final boolean flagsContains(final String inputString) {
	if (inputString.isEmpty()) {
	    return false;
	} else {
	    return (actualNoteFlagsStringItemValue.indexOf(inputString) >= 0);
	}

    }

    /**
     * Gets the actual note.
     * 
     * @return the actual note
     */
    public final Document getActualNote() {
	return actualDocForNotes;
    }

    /**
     * Gets the note id.
     * 
     * @return the note id
     */
    public final String getNoteID() {
	return noteID;
    }

    /**
     * Gets the note title.
     * 
     * @return the note title
     */
    public final String getNoteTitle() {
	return noteTitle;
    }

    /**
     * Gets the note type.
     * 
     * @return the note type
     */
    public final String getNoteType() {
	return noteType;
    }

    /**
     * Gets the parent database.
     * 
     * @return the parent database
     */
    public final MetaDatabase getParentDatabase() {
	return parentDatabase;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public final int hashCode() {
	return noteID.hashCode();
    }

    /**
     * Checks for unid.
     * 
     * @return true, if successful
     */
    public final boolean hasUNID() {
	return (!noteID.equals(""));
    }

    /**
     * Sets the actual note.
     * 
     * @param actualNote
     *            the new actual note
     */
    public final void setActualNote(final Document actualNote) {
	actualDocForNotes = actualNote;
	try {
	    noteID = actualNote.getNoteID();
	    UNID = actualNote.getUniversalID();
	} catch (NotesException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Sets the note id.
     * 
     * @param noteUNID
     *            the new note id
     */
    public final void setNoteID(final String noteUNID) {
	noteID = noteUNID;
    }

    /**
     * Sets the note title.
     * 
     * @param var
     *            the new note title
     */
    public final void setNoteTitle(final String var) {
	this.noteTitle = var;
    }

    /**
     * Sets the note type.
     * 
     * @param val
     *            the new note type
     */
    public final void setNoteType(final String val) {
	noteType = val;
    }

    /**
     * Sets the parent database.
     * 
     * @param val
     *            the new parent database
     */
    public final void setParentDatabase(final MetaDatabase val) {
	parentDatabase = val;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public final String toString() {
	return ("Note ID :" + noteID + " | Note Type " + noteType
		+ " | Note Title " + noteTitle);
    }

    /**
     * Find note type and note title.
     */
    public final void findNoteTypeAndNoteTitle() {

	if (noteID.equals("FFFF0002")) {
	    noteTitle = "the About this database document";
	    noteType = "special design element";
	    noteCategory = NoteCategory.DESIGN;
	    return;
	} else if (noteID.equals("FFFF0004")) {
	    noteTitle = "the default form";
	    noteType = "Form";
	    noteCategory = NoteCategory.DESIGN;
	    return;
	} else if (noteID.equals("FFFF0008")) {
	    noteTitle = "the default View";
	    noteType = "View";
	    noteCategory = NoteCategory.DESIGN;
	    return;
	} else if (noteID.equals("FFFF0010")) {
	    noteTitle = "the database Icon";
	    noteType = "Database Icon";
	    noteCategory = NoteCategory.DESIGN;
	    return;
	} else if (noteID.equals("FFFF0020")) {
	    noteTitle = "the database Design Collection";
	    noteType = "Design Collection";
	    noteCategory = NoteCategory.DESIGN;
	    return;
	} else if (noteID.equals("FFFF0040")) {
	    noteTitle = "the database ACL";
	    noteType = "ACL";
	    noteCategory = NoteCategory.DESIGN;
	    return;
	} else if (noteID.equals("FFFF0100")) {
	    noteTitle = "the Using This Database Document";
	    noteType = "special design element";
	    noteCategory = NoteCategory.DESIGN;
	    return;
	} else if (noteID.equals("FFFF0800")) {
	    noteTitle = "the Replication Formula";
	    noteType = "Replication Formula note";
	    noteCategory = NoteCategory.DESIGN;
	    return;
	} else {
	    try {
		if (actualDocForNotes != null) {
		    if (actualDocForNotes.hasItem("$Title")) {
			noteTitle = actualDocForNotes
				.getItemValueString("$Title");
			noteType = "Design note";
			noteCategory = NoteCategory.DESIGN;
			discoverNoteTypeWithHelpFromFlags();
		    } else if (actualDocForNotes.hasItem("$Name")) {
			noteCategory = NoteCategory.DESIGN;
			noteTitle = actualDocForNotes
				.getItemValueString("$Name");
			if (noteTitle.equals("$PrivateDesign")) {
			    noteType = "a special mini-view";
			} else {
			    noteType = "profile document";
			}
		    } else if (actualDocForNotes.hasItem("Form")) {
			noteCategory = NoteCategory.DATA;
			noteType = "normal document";
			noteTitle = " a document whose form is '"
				+ actualDocForNotes.getItemValueString("Form")
				+ "'";
		    }
		}
	    } catch (NotesException e) {
		noteTitle = "Could not find note (perhaps deleted?)";
	    }
	}
    }

    public String getCategory() {
	if (noteCategory != null) {
	    return noteCategory.toString();
	}
	return "";
    }
}
