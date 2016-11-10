package test.com.magerman.nrpc;

import java.util.ArrayList;

import junit.framework.TestCase;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import com.magerman.nrpc.FileParser;
import com.magerman.nrpc.Line;

public class LineParserTest extends TestCase {

    String testLineANotes9 = new String("[0F00:0002-0EA8] (4-48 [19]) ISDB2_RQST: 130 ms. [14+16=30]");
    Line lpANotes9 = new Line(testLineANotes9);

    String testLineA = new String("(4-48 [19]) ISDB2_RQST: 130 ms. [14+16=30]");
    Line lpA = new Line(testLineA);

    String testLineB = new String(
            "[0F00:0002-0EA8] (6-48 [21]) OPEN_NOTE(REP862572C6:006BEDBE-NTFFFF0010,03000400): 140 ms. [48+1596=1644]");
    Line lpB = new Line(testLineB);

    String testLineC = new String("(11-13 [13]) CLOSE_DB(REP80256592:003E0158): 0 ms. [14+0=14]");
    Line lpC = new Line(testLineC);

    String specialopendb = new String(
            "[0F00:0002-0EA8] (1-50 [1]) OPEN_DB(domino-77.prominic.net!!A55BAE\\mail\\Andrew_Magerman.nsf): (Connect to domino-77.prominic.net: 0 ms) (Exch names: 0 ms)(Authenticate: [0F3C:0006-0DB4:wrepl] (1-50 [1]) SERVER_AVAILABLE_LITE(domino-77.prominic.net): ");
    Line lpD = new Line(specialopendb);

    public void testspecialopendb() {
        assertEquals("domino-77.prominic.net", lpD.getCommonServerName());
        assertEquals("Opens database", lpD.getTranslatedCommand());
    }

    public void testFindCommand() {
        Line l = new Line("[0F00:0002-0EA8] (53-73 [122]) OPEN_NOTE(REP862572C6:006BEDBE-NT0001D4F2,00400000): 153 ms. [48+1658=1706]");
        l.parse();
        assertEquals("OPEN_NOTE", l.getCommand());
        assertEquals("Loads ", l.getTranslatedCommand());

        Line q = new Line("[0DD0:0002-0D1C] (5-52 [25]) SV_INFO_GET_RQST: 134 ms. [14+28=42]");
        q.parse();
        assertEquals("SV_INFO_GET_RQST", q.getCommand());

        Line s = new Line("[0F00:0002-0EA8] (321-153 [390]) GET LAST INDEX TIME(REP862572C6:006BEDBE): 134 ms. [14+76=90]");
        s.parse();
        assertEquals("GET LAST INDEX TIME", s.getCommand());
    }

    public void testFindThreadID() {
        Line l = new Line("[0F00:0002-0EA8] (53-73 [122]) OPEN_NOTE(REP862572C6:006BEDBE-NT0001D4F2,00400000): 153 ms. [48+1658=1706]");
        l.parse();
        assertEquals("[0F00:0002-0EA8]", l.getThreadID());

        Line q = new Line("(158-132 [163]) OPEN_NOTE(REPC1257B28:0056D462-NTFFFF0010,03000400): 0 ms. [56+2208=2264]");
        q.parse();
        assertEquals(Line.NO_THREAD_FOUND, q.getThreadID());

        Line r = new Line("[544+30=574] (Unsupported return flag(s))");
        r.parse();
        assertEquals(Line.NO_THREAD_FOUND, r.getThreadID());

    }

    @Override
    protected void setUp() {
        Session s;
        try {
            NotesThread.sinitThread();
            s = NotesFactory.createSession();
            lpA.parse();
            lpANotes9.parse();
            lpB.parse();
            lpC.parse();
            lpD.parse();

        } catch (NotesException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void tearDown() {
        NotesThread.stermThread();
    }

    public void testLOCATE_NOTE() {
        String b = "[0F90:0002-12EC] (133-51 [146]) LOCATE_NOTE(REPC125803E:004B8344): 16 ms. [42+34=76]";
        Line l = new Line(b);
        l.parse();
        assertEquals("LOCATE_NOTE", l.getCommand());
        assertEquals("Preparation for reading a NotesViewEntry", l.getTranslatedCommand());
    }

    public void testGETDBINFO() {
        String b = "[0F90:0002-12EC] (51-49 [64]) GET_DBINFO(REPC125803E:004B8344): 16 ms. [18+20=38]";
        Line l = new Line(b);
        l.parse();
        assertEquals("GET_DBINFO", l.getCommand());
        assertEquals("Gets database information", l.getTranslatedCommand());

    }

    public void testProfileDoc() {
        String b = "[0D30:0002-0C18] (26-26 [26]) OPEN_NOTE_UNIDNAME_RQST(REPC12575D0:001AD0CD-$profile_006config_cn=andrew magerman/ou=magerman/o=notesnet,00400020): 1 ms. [108+606=714]";
        Line l = new Line(b);

        assertEquals(
                "'config' , key:'andrew magerman/ou=magerman/o=notesnet'",
                l.getProfileDocumentDescriptor("REPC12575D0:001AD0CD-$profile_006config_cn=andrew magerman/ou=magerman/o=notesnet,00400020"));

        Line t = new Line(
                "[0D30:0002-0C18] (63-50 [143]) OPEN_NOTE_UNIDNAME_RQST(REP86257A75:00455282-$profile_014fa_translation_global~en,00400020): 34 ms. [94+3096=3190]");
        t.parse();
        assertEquals("OPEN_NOTE_UNIDNAME_RQST", t.getCommand());
        assertEquals("'fa_translation_global~en'", t.getProfileDocumentDescriptor(t.getArguments()));

    }

    public void testNewCombinedLine() {
        String test = "[0F3C:0007-0D3C:wrepl] (18-56 [70]) OPEN_DB(CN=domino-77.prominic.net/O=Prominic!!A55BAE\\mail\\Andrew_Magerman.nsf): [0F00:0002-0EA8] (25-56 [71]) DB_INFO_GET(REP862572C6:006BEDBE): 134 ms. [40+82=122]";
        Line andrew = new Line(test);
        andrew.parse();
        andrew.findServerNameFilePath();
        andrew.findReplicaIDAndNoteIDs();
        System.out.println(andrew);
        assertEquals("A55BAE\\mail\\Andrew_Magerman.nsf", andrew.getParsedDBFilePath());
        assertEquals("862572C6006BEDBE", andrew.getParsedDBReplicaID());
    }

    public void testcleanNotes9prefix() {

        assertEquals(testLineA, Line.cleanNotes9prependtext(testLineANotes9));
        assertEquals(testLineA, Line.cleanNotes9prependtext(testLineA));

        String weirdo = "135 ms. [26+222=248]";
        assertEquals(weirdo, Line.cleanNotes9prependtext(weirdo));

    }

    public void testFindServerFilepath() {
        Line l = new Line(
                "[0F00:0002-0EA8] (1-50 [1]) OPEN_DB(domino-77.prominic.net!!A55BAE\\mail\\Andrew_Magerman.nsf): (Connect to domino-77.prominic.net: 0 ms) (Exch names: 0 ms)(Authenticate: ");
        l.parse();
        assertEquals("domino-77.prominic.net", l.getParsedServerName());
        assertEquals("A55BAE\\mail\\Andrew_Magerman.nsf", l.getParsedDBFilePath());

    }

    public void testMysteryNumberOne() {
        assertEquals(4, lpA.getFirstMysteryNumber());
        assertEquals(4, lpANotes9.getFirstMysteryNumber());
        assertEquals(48, lpA.getSecondMysteryNumber());
        assertEquals(48, lpANotes9.getSecondMysteryNumber());
        assertEquals(19, lpA.getSequenceNumber());
        assertEquals(19, lpANotes9.getSequenceNumber());
        assertEquals(30, lpA.getBytesTotal());
        assertEquals(30, lpANotes9.getBytesTotal());
        assertEquals(16, lpA.getBytesOut());
        assertEquals(16, lpANotes9.getBytesOut());
        assertEquals(14, lpA.getBytesIn());
        assertEquals(14, lpANotes9.getBytesIn());
        assertEquals(130, lpA.getDurationInMilliseconds());
    }

    public void testSplitter() {

        String a = "[0F00:0002-0EA8] (28-57 [77]) OPEN_NOTE(REP862572C6:006BEDBE-NTFFFF0010,03000400): [0F3C:0007-0D3C:wrepl] (22-57 [78]) OPEN_DB(CN=domino-79.prominic.net/O=Prominic!!): (Connect to domino-79.prominic.net/Prominic: 0 ms) (Exch names: 1 ms)(Authenticate: 135 ms. [48+1908=1956]";
        ArrayList<String> splitted = FileParser.splitThisLineIntoCalls(a);
        assertEquals("[0F00:0002-0EA8] (28-57 [77]) OPEN_NOTE(REP862572C6:006BEDBE-NTFFFF0010,03000400): ", splitted.get(0));
        assertEquals(
                "[0F3C:0007-0D3C:wrepl] (22-57 [78]) OPEN_DB(CN=domino-79.prominic.net/O=Prominic!!): (Connect to domino-79.prominic.net/Prominic: 0 ms) (Exch names: 1 ms)(Authenticate: 135 ms. [48+1908=1956]",
                splitted.get(1));

        String b = "(181-133 [186]) OPEN_COLLECTION(REPC1257B28:0056D462-NTFFFF0020,0040,4000): (182-133 [187]) OPEN_DB(CN=albis/O=magerman!!Development\\HRE-Mail\\HRE-Mail2_0\\Hre-Mail_(2_0)_Dev.nsf): (Connect to albis/magerman: 180 ms) (Exch names: 0 ms)(Authenticate: 0 ms.)";
        ArrayList<String> splittedb = FileParser.splitThisLineIntoCalls(b);
        assertEquals("(181-133 [186]) OPEN_COLLECTION(REPC1257B28:0056D462-NTFFFF0020,0040,4000): ", splittedb.get(0));
        assertEquals(
                "(182-133 [187]) OPEN_DB(CN=albis/O=magerman!!Development\\HRE-Mail\\HRE-Mail2_0\\Hre-Mail_(2_0)_Dev.nsf): (Connect to albis/magerman: 180 ms) (Exch names: 0 ms)(Authenticate: 0 ms.)",
                splittedb.get(1));

        String c = "sdgfsdfgsdfg";
        assertEquals(c, FileParser.splitThisLineIntoCalls(c).get(0));

        String d = "[0DD0:0002-0D1C] (3-51 [19]) SV_INFO_GET_RQST: [0F3C:0007-0D3C:wrepl] (Opened: REP862572C6:006BEDBE) 137 ms. [134+290=424]";
        ArrayList<String> splittedd = FileParser.splitThisLineIntoCalls(d);
        assertEquals("[0DD0:0002-0D1C] (3-51 [19]) SV_INFO_GET_RQST: ", splittedd.get(0));
        assertEquals("[0F3C:0007-0D3C:wrepl] (Opened: REP862572C6:006BEDBE) 137 ms. [134+290=424]", splittedd.get(1));

        String e = "[0F00:0011-0E7C:newmail] (257-121 [326]) POLL_DEL_SEQNUM(CN=domino-77.prominic.net/O=Prominic!!A55BAE\\mail\\Andrew_Magerman.nsf): (258-121 [326]) OPEN_NOTE(REP862572C6:006BEDBE-NT0000D0C6,03400008): (Connect to domino-77.prominic.net/Prominic: 269 ms) (OPEN_SESSION: 134 ms)";
        ArrayList<String> splittede = FileParser.splitThisLineIntoCalls(e);
        assertEquals(
                "[0F00:0011-0E7C:newmail] (257-121 [326]) POLL_DEL_SEQNUM(CN=domino-77.prominic.net/O=Prominic!!A55BAE\\mail\\Andrew_Magerman.nsf): ",
                splittede.get(0));
        assertEquals(
                "(258-121 [326]) OPEN_NOTE(REP862572C6:006BEDBE-NT0000D0C6,03400008): (Connect to domino-77.prominic.net/Prominic: 269 ms) (OPEN_SESSION: 134 ms)",
                splittede.get(1));

    }

    public void testSearchDB() {
        String a = "[0F00:0002-0EA8] (68-76 [137]) OPEN_COLLECTION(REP862572C6:006BEDBE-NT0000D11A,0000,0000): 137 ms. [78+34=112]";
        Line l = new Line(a);
        l.findReplicaIDAndNoteIDs();
        assertEquals("0000D11A", l.getParsedNoteID());
    }

}
