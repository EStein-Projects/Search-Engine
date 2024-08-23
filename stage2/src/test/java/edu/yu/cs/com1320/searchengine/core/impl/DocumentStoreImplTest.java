package edu.yu.cs.com1320.searchengine.core.impl;

import edu.yu.cs.com1320.searchengine.core.Document;
import edu.yu.cs.com1320.searchengine.core.DocumentStore.DocumentFormat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStoreImplTest {
    private DocumentStoreImpl sample;
    @BeforeEach
    void setUp() throws IOException {
        sample = new DocumentStoreImpl();
        String s1 = "Hello World";
        ByteArrayInputStream doesThisWork = new ByteArrayInputStream(s1.getBytes());
        sample.put(doesThisWork, URI.create("HW"), DocumentFormat.TXT);
        String s2 = "Insert Image Here";
        ByteArrayInputStream thereWeGo = new ByteArrayInputStream(s2.getBytes());
        sample.put(thereWeGo, URI.create("Img"), DocumentFormat.BINARY);
        String s3 = "Hello World Hello";
        ByteArrayInputStream HelloIII = new ByteArrayInputStream(s3.getBytes());
        sample.put(HelloIII, URI.create("HIII"), DocumentFormat.TXT);
    }

    /**
     * This class started throwing NullPointerExceptions when it shouldn't,
     * but for some reason tearDown() fixes it.
     * It must be I had to reset the directory for it to work.
     */
    @AfterEach
    void tearDown() {
        for (File f : new File(System.getProperty("user.dir")).listFiles()){
            if (!f.getName().equals("pom.xml")) {
                f.delete();
            }
        }
    }

    @Test
    void setMetadata() throws IOException {
        assertNull(sample.setMetadata(URI.create("HW"), "Author", "Me"));
        assertEquals("Me", sample.setMetadata(URI.create("HW"), "Author", "ES"));
        assertNull(sample.setMetadata(URI.create("HW"), "Unrelated", "Bla"));
        assertNull(sample.setMetadata(URI.create("Img"), "Author", "Elchonon"));
        assertThrows(IllegalArgumentException.class, () -> sample.setMetadata(URI.create("Fake"), "bla", "bla"));
        assertThrows(IllegalArgumentException.class, () -> sample.setMetadata(URI.create(""), "bla", "bla"));
        assertThrows(IllegalArgumentException.class, () -> sample.setMetadata(null, "bla", "bla"));
        assertThrows(IllegalArgumentException.class, () -> sample.setMetadata(URI.create("HW"), "", "bla"));
        assertThrows(IllegalArgumentException.class, () -> sample.setMetadata(URI.create("HW"), null, "bla"));
    }

    // @Test
    // void getMetadata() {
    // }

    // @Test
    // void put() {
    // }

    @Test
    void get() throws IOException {
        assertEquals("Hello World", sample.get(URI.create("HW")).getDocumentTxt());
        assertNotEquals("Insert Image Here", sample.get(URI.create("Img")).getDocumentTxt());
        assertNull(sample.get(URI.create("Img")).getDocumentTxt());
        assertNull(sample.get(URI.create("HW")).getDocumentBinaryData());
        String s2 = "Insert Image Here";
        byte[] bytes = s2.getBytes();
        byte[] docTwoBytes = sample.get(URI.create("Img")).getDocumentBinaryData();
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], docTwoBytes[i]);
        }
    }

    @Test
    void delete() {
        assertFalse(sample.delete(URI.create("Hello")));
        assertFalse(sample.delete(null));
        assertTrue(sample.delete(URI.create("Img")));
    }

    @Test
    void undoLastSingle() throws IOException {
        sample.undo();
        sample.undo();
        assertEquals("Hello World", sample.get(URI.create("HW")).getDocumentTxt());
        sample.undo();
        assertNull(sample.get(URI.create("HW")));
        assertEquals(Collections.emptyList(), sample.search("Hello"));
        String s1 = "Hello World";
        ByteArrayInputStream doesThisWork = new ByteArrayInputStream(s1.getBytes());
        sample.put(doesThisWork, URI.create("HW"), DocumentFormat.TXT);
        String s2 = "Insert Image Here";
        ByteArrayInputStream thereWeGo = new ByteArrayInputStream(s2.getBytes());
        sample.put(thereWeGo, URI.create("Img"), DocumentFormat.BINARY);
        sample.setMetadata(URI.create("HW"), "Author", "Me");
        assertEquals("Me", sample.getMetadata(URI.create("HW"), "Author"));
        sample.undo();
        assertNull(sample.getMetadata(URI.create("HW"), "Author"));
        assertNull(sample.setMetadata(URI.create("HW"), "Author", "ES"));
        sample.setMetadata(URI.create("HW"), "Author", "Me");
        sample.undo();
        assertEquals("ES", sample.getMetadata(URI.create("HW"), "Author"));
    }

    @Test
    void undoParamSingle() throws IOException {
        sample.undo(URI.create("HW"));
        assertNull(sample.get(URI.create("HW")));
        List<Document> hello1 = sample.search("Hello");
        assertEquals(sample.get(URI.create("HIII")), hello1.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> hello1.get(1));
        String s1 = "Hello World";
        ByteArrayInputStream doesThisWork = new ByteArrayInputStream(s1.getBytes());
        sample.put(doesThisWork, URI.create("HW"), DocumentFormat.TXT);
        sample.setMetadata(URI.create("HW"), "Author", "Me");
        sample.undo(URI.create("HW"));
        assertNull(sample.getMetadata(URI.create("HW"), "Author"));
        assertEquals("Hello World", sample.get(URI.create("HW")).getDocumentTxt());
        sample.undo(URI.create("HW"));
        assertNull(sample.get(URI.create("HW")));
        List<Document> hello2 = sample.search("Hello");
        assertEquals(sample.get(URI.create("HIII")), hello2.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> hello2.get(1));
        String s2 = "Insert Image Here";
        byte[] bytes = s2.getBytes();
        byte[] docTwoBytes = sample.get(URI.create("Img")).getDocumentBinaryData();
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], docTwoBytes[i]);
        }
        assertThrows(IllegalStateException.class, () -> sample.undo(URI.create("")));
        assertThrows(IllegalStateException.class, () -> sample.undo(URI.create("Fake")));
        sample.undo();
        sample.undo();
        assertThrows(IllegalStateException.class, () -> sample.undo());
        assertThrows(IllegalStateException.class, () -> sample.undo(URI.create("Fake")));
    }

    @Test
    void undoOverwrite() throws IOException {
        assertEquals("Hello World", sample.get(URI.create("HW")).getDocumentTxt());
        String s3 = "ReplacementHW";
        ByteArrayInputStream thisWorks = new ByteArrayInputStream(s3.getBytes());
        sample.put(thisWorks, URI.create("HW"), DocumentFormat.TXT);
        assertEquals("ReplacementHW", sample.get(URI.create("HW")).getDocumentTxt());
        sample.undo();
        assertEquals("Hello World", sample.get(URI.create("HW")).getDocumentTxt());
    }

    @Test
    void undo() throws IOException {
        assertNull(sample.setMetadata(URI.create("HW"), "Author", "Me"));
        Set<URI> returned = new HashSet<>();
        returned.add(URI.create("HW"));
        returned.add(URI.create("HIII"));
        assertEquals(returned, sample.deleteAll("Hello"));
        String s4 = "H H H";
        ByteArrayInputStream H = new ByteArrayInputStream(s4.getBytes());
        sample.put(H, URI.create("H"), DocumentFormat.TXT);
        assertNull(sample.get(URI.create("HW")));
        assertEquals(Collections.emptyList(), sample.search("Hello"));
        sample.undo(URI.create("HW"));
        sample.undo(URI.create("HW"));
        assertNull(sample.setMetadata(URI.create("HW"), "Author", "Me"));
        sample.undo();
        sample.undo();
        assertEquals("Hello World", sample.get(URI.create("HW")).getDocumentTxt());
        assertNull(sample.get(URI.create("HIII")));
        sample.undo();
        assertEquals("Hello World", sample.get(URI.create("HW")).getDocumentTxt());
        assertEquals("Hello World Hello", sample.get(URI.create("HIII")).getDocumentTxt());
        sample.undo();
        assertEquals("Hello World", sample.get(URI.create("HW")).getDocumentTxt());
        assertNull(sample.get(URI.create("HIII")));
    }

    // @Test
    // void testUndo2() {
    // }

    @Test
    void search() throws IOException {
        assertEquals(Collections.emptyList(), sample.search(""));
        assertEquals(Collections.emptyList(), sample.search(" "));
        assertThrows(IllegalArgumentException.class, () -> sample.search(null));
        assertEquals(Collections.emptyList(), sample.search("H"));
        assertEquals(Collections.emptyList(), sample.search("Hello!"));
        assertEquals(Collections.emptyList(), sample.search("Hello "));
        List<Document> hello = sample.search("Hello");
        assertEquals(sample.get(URI.create("HIII")), hello.get(0));
        assertEquals(sample.get(URI.create("HW")), hello.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> hello.get(2));
    }

    @Test
    void searchByPrefix() throws IOException {
        String s4 = "H H H";
        ByteArrayInputStream H = new ByteArrayInputStream(s4.getBytes());
        sample.put(H, URI.create("H"), DocumentFormat.TXT);
        String s5 = "He He World He He";
        ByteArrayInputStream He = new ByteArrayInputStream(s5.getBytes());
        sample.put(He, URI.create("He"), DocumentFormat.TXT);
        assertEquals(Collections.emptyList(), sample.searchByPrefix(""));
        assertEquals(Collections.emptyList(), sample.searchByPrefix(" "));
        assertThrows(IllegalArgumentException.class, () -> sample.searchByPrefix(null));
        assertEquals(Collections.emptyList(), sample.searchByPrefix("Hello!"));
        assertEquals(Collections.emptyList(), sample.search("Hello "));
        List<Document> hello1 = sample.searchByPrefix("Hello");
        assertEquals(sample.get(URI.create("HIII")), hello1.get(0));
        assertEquals(sample.get(URI.create("HW")), hello1.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> hello1.get(2));
        List<Document> hello2 = sample.searchByPrefix("H");
        assertEquals(sample.get(URI.create("He")), hello2.get(0));
        assertEquals(sample.get(URI.create("H")), hello2.get(1));
        assertEquals(sample.get(URI.create("HIII")), hello2.get(2));
        assertEquals(sample.get(URI.create("HW")), hello2.get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> hello2.get(4));
    }

    // @Test
    // void deleteAll() {
    // }

    // @Test
    // void deleteAllWithPrefix() {
    // }

    // @Test
    // void searchByMetadata() {
    // }

    @Test
    void searchByKeywordAndMetadata() throws IOException {
        assertNull(sample.setMetadata(URI.create("HW"), "Author", "Me"));
        assertNull(sample.setMetadata(URI.create("HW"), "Unrelated", "Bla"));
        Map<String, String> metaMap = new HashMap<>();
        metaMap.put("Author", "Me");
        metaMap.put("Unrelated", "Bla");
        List<Document> hello = sample.searchByKeywordAndMetadata("Hello", metaMap);
        assertNotEquals(Collections.emptyList(), hello);
        assertEquals(sample.get(URI.create("HW")), hello.get(0));
        assertEquals(1, hello.size());
        assertEquals(Collections.emptyList(), sample.searchByKeywordAndMetadata("", metaMap));
    }

    @Test
    void searchByPrefixAndMetadata() throws IOException {
        assertNull(sample.setMetadata(URI.create("HW"), "Author", "Me"));
        assertNull(sample.setMetadata(URI.create("HW"), "Unrelated", "Bla"));
        Map<String, String> metaMap = new HashMap<>();
        metaMap.put("Author", "Me");
        metaMap.put("Unrelated", "Bla");
        List<Document> hello = sample.searchByPrefixAndMetadata("H", metaMap);
        assertNotEquals(Collections.emptyList(), hello);
        assertEquals(sample.get(URI.create("HW")), hello.get(0));
        assertEquals(1, hello.size());
        assertEquals(Collections.emptyList(), sample.searchByPrefixAndMetadata("", metaMap));
    }

    @Test
    void deleteAllWithMetadata() throws IOException {
        assertNull(sample.setMetadata(URI.create("HW"), "Author", "Me"));
        assertNull(sample.setMetadata(URI.create("HW"), "Unrelated", "Bla"));
        assertNull(sample.setMetadata(URI.create("HIII"), "Unrelated", "Bla"));
        assertNull(sample.setMetadata(URI.create("Img"), "Author", "Me"));
        assertNull(sample.setMetadata(URI.create("Img"), "Unrelated", "Bla"));
        String s4 = "H H H";
        ByteArrayInputStream H = new ByteArrayInputStream(s4.getBytes());
        sample.put(H, URI.create("H"), DocumentFormat.TXT);
        assertNull(sample.setMetadata(URI.create("H"), "Author", "Me"));
        assertNull(sample.setMetadata(URI.create("H"), "Unrelated", "Bla"));
        Map<String, String> metaMap = new HashMap<>();
        metaMap.put("Author", "Me");
        metaMap.put("Unrelated", "Bla");
        assertThrows(IllegalArgumentException.class, () ->
                sample.deleteAllWithMetadata(null));
        assertEquals(Collections.emptySet(), sample.deleteAllWithMetadata(new HashMap<>()));
        Set<URI> returned = new HashSet<>();
        returned.add(URI.create("HW"));
        returned.add(URI.create("Img"));
        returned.add(URI.create("H"));
        assertEquals(returned, sample.deleteAllWithMetadata(metaMap));
        assertNull(sample.get(URI.create("HW")));
        List<Document> hello = sample.search("Hello");
        assertEquals(sample.get(URI.create("HIII")), hello.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> hello.get(1));
    }

   /**
     * Completely remove any trace of any document which contains the given keyword AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * param keyword
     * param keysValues
     * return a Set of URIs of the documents that were deleted.
     */
    @Test
    void deleteAllWithKeywordAndMetadata() throws IOException {
        assertNull(sample.setMetadata(URI.create("HW"), "Author", "Me"));
        assertNull(sample.setMetadata(URI.create("HW"), "Unrelated", "Bla"));
        assertNull(sample.setMetadata(URI.create("HIII"), "Unrelated", "Bla"));
        assertNull(sample.setMetadata(URI.create("Img"), "Author", "Me"));
        assertNull(sample.setMetadata(URI.create("Img"), "Unrelated", "Bla"));
        String s4 = "H H H";
        ByteArrayInputStream H = new ByteArrayInputStream(s4.getBytes());
        sample.put(H, URI.create("H"), DocumentFormat.TXT);
        assertNull(sample.setMetadata(URI.create("H"), "Author", "Me"));
        assertNull(sample.setMetadata(URI.create("H"), "Unrelated", "Bla"));
        Map<String, String> metaMap = new HashMap<>();
        metaMap.put("Author", "Me");
        metaMap.put("Unrelated", "Bla");
        assertThrows(IllegalArgumentException.class, () ->
                sample.deleteAllWithKeywordAndMetadata(null, metaMap));
        assertThrows(IllegalArgumentException.class, () ->
                sample.deleteAllWithKeywordAndMetadata("Hello", null));
        assertEquals(Collections.emptySet(), sample.deleteAllWithKeywordAndMetadata("", metaMap));
        assertEquals(Collections.emptySet(), sample.deleteAllWithKeywordAndMetadata("Hello", new HashMap<>()));
        Set<URI> returned = new HashSet<>();
        returned.add(URI.create("HW"));
        assertEquals(returned, sample.deleteAllWithKeywordAndMetadata("Hello", metaMap));
        assertNull(sample.get(URI.create("HW")));
        List<Document> hello = sample.search("Hello");
        assertEquals(sample.get(URI.create("HIII")), hello.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> hello.get(1));
    }

    @Test
    void setMaxDocumentCount() throws IOException {
        sample.setMaxDocumentCount(2);
        assertNotNull(sample.get(URI.create("HW")));
        sample.setMaxDocumentCount(3);
        String s1 = "Hello World";
        sample.put(new ByteArrayInputStream(s1.getBytes()), URI.create("HW"), DocumentFormat.TXT);
        sample.delete(URI.create("Img"));
        sample.delete(URI.create("HW"));
        sample.delete(URI.create("HIII"));
        sample.setMaxDocumentCount(1);
        sample.undo();
        sample.undo();
        sample.undo();
        assertNotNull(sample.get(URI.create("HW")));
        assertNotNull(sample.get(URI.create("Img")));
        assertNotNull(sample.get(URI.create("HIII")));
        sample.setMaxDocumentCount(3);
        sample.put(new ByteArrayInputStream(s1.getBytes()), URI.create("HW"), DocumentFormat.TXT);
        sample.put(new ByteArrayInputStream("Hello World Hello".getBytes()), URI.create("HIII"), DocumentFormat.TXT);
        sample.deleteAllWithPrefix("H");
        sample.setMaxDocumentCount(1);
        sample.undo(URI.create("HW"));
        sample.undo();
        assertNotNull(sample.get(URI.create("HW")));
        assertNotNull(sample.get(URI.create("Img")));
        assertNotNull(sample.get(URI.create("HIII")));
        sample.undo();
        assertNotNull(sample.get(URI.create("HIII")));
    }

    @Test
    void setMaxDocumentBytes() throws IOException {
        int s1BytLn = "Hello World".getBytes().length;
        int s2BytLn = "Insert Image Here".getBytes().length;
        int s3BytLn = "Hello World Hello".getBytes().length;
        sample.setMaxDocumentBytes(s2BytLn + s3BytLn);
        assertNotNull(sample.get(URI.create("HW")));
        assertNotNull(sample.get(URI.create("Img")));
        assertNotNull(sample.get(URI.create("HIII")));
        sample.setMaxDocumentBytes(s1BytLn + s2BytLn + s3BytLn);
        sample.put(new ByteArrayInputStream("Hello World".getBytes()), URI.create("HW"), DocumentFormat.TXT);
        sample.deleteAllWithPrefix("H");
        assertNull(sample.get(URI.create("HW")));
        assertNotNull(sample.get(URI.create("Img")));
        assertNull(sample.get(URI.create("HIII")));
        sample.setMaxDocumentBytes(s1BytLn + s2BytLn -1);
        assertNull(sample.get(URI.create("HW")));
        assertNotNull(sample.get(URI.create("Img")));
        assertNull(sample.get(URI.create("HIII")));
        sample.undo(URI.create("HW"));
        sample.undo();
        assertNotNull(sample.get(URI.create("HW")));
        assertNotNull(sample.get(URI.create("Img")));
        assertNotNull(sample.get(URI.create("HIII")));
        sample.setMaxDocumentBytes(s1BytLn + s2BytLn + s3BytLn);
        sample.put(new ByteArrayInputStream("Hello World".getBytes()), URI.create("HW"), DocumentFormat.TXT);
        String s2 = "Insert Image Here";
        ByteArrayInputStream thereWeGo = new ByteArrayInputStream(s2.getBytes());
        sample.put(thereWeGo, URI.create("Img"), DocumentFormat.BINARY);
        sample.deleteAllWithPrefix("H");
        sample.setMaxDocumentBytes(s1BytLn + s3BytLn -1);
        sample.undo();
        assertFalse(sample.get(URI.create("HW")) == null || sample.get(URI.create("HIII")) == null);
        assertNotNull(sample.get(URI.create("Img")));
    }

    @Test
    void timeUpdater() throws IOException {
        sample.search("World");
        sample.setMaxDocumentCount(2);
        assertNotNull(sample.get(URI.create("HW")));
        assertNotNull(sample.get(URI.create("Img")));
        assertNotNull(sample.get(URI.create("HIII")));
        sample.get(URI.create("HIII"));
        sample.setMaxDocumentCount(1);
        assertNotNull(sample.get(URI.create("HW")));
        assertNotNull(sample.get(URI.create("Img")));
        assertNotNull(sample.get(URI.create("HIII")));
        sample.setMaxDocumentCount(3);
        String s1 = "Hello World";
        ByteArrayInputStream doesThisWork = new ByteArrayInputStream(s1.getBytes());
        sample.put(doesThisWork, URI.create("HW"), DocumentFormat.TXT);
        String s2 = "Insert Image Here";
        ByteArrayInputStream thereWeGo = new ByteArrayInputStream(s2.getBytes());
        sample.put(thereWeGo, URI.create("Img"), DocumentFormat.BINARY);
        sample.search("Hello");
        sample.setMaxDocumentCount(2);
        assertNotNull(sample.get(URI.create("HW")));
        assertNotNull(sample.get(URI.create("Img")));
        assertNotNull(sample.get(URI.create("HIII")));
        sample.setMetadata(URI.create("HIII"), "ES", "ROX");
        sample.setMaxDocumentCount(1);
        assertNotNull(sample.get(URI.create("HW")));
        assertNotNull(sample.get(URI.create("Img")));
        assertNotNull(sample.get(URI.create("HIII")));
        sample.setMaxDocumentCount(3);
        doesThisWork = new ByteArrayInputStream(s1.getBytes());
        sample.put(doesThisWork, URI.create("HW"), DocumentFormat.TXT);
        sample.getMetadata(URI.create("HIII"), "Blank");
        sample.setMaxDocumentCount(1);
        assertNotNull(sample.get(URI.create("HW")));
        assertNotNull(sample.get(URI.create("Img")));
        assertNotNull(sample.get(URI.create("HIII")));
    }
}