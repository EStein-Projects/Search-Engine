package edu.yu.cs.com1320.searchengine.core.impl;

import edu.yu.cs.com1320.searchengine.core.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class DocumentPersistenceManagerTest {
    private DocumentPersistenceManager defaultMngr;
    private DocumentPersistenceManager manager;
    private File defaultBaseDir;
    private File baseDir;

    @BeforeEach
    void setUp() {
        this.defaultBaseDir = new File(System.getProperty("user.dir"));
        this.defaultMngr = new DocumentPersistenceManager(null);
        this.baseDir = new File(System.getProperty("user.dir") + "/test");
        this.baseDir.mkdir();
        this.manager = new DocumentPersistenceManager(baseDir);
    }

    @AfterEach
    void tearDown() {
        /*for (File file : baseDir.listFiles()) {
            file.delete();
        }*/
        baseDir.delete();
    }

    @Test
    void serialize() {
        URI uri1 = URI.create("http://www.yu.edu/documents/doc1");
        Document byteDoc = new DocumentImpl(uri1, "This is a test document.".getBytes());
        URI uri2 = URI.create("http://www.yu.edu/documents/doc2");
        Document wordDoc = new DocumentImpl(uri2, "Hello World", null);
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("Author", "Me");
        metadata.put("Date", "Pesach Sheini");
        wordDoc.setMetadata(metadata);
        Document deserializedByteDoc;
        Document deserializedWordDoc;
        try {
            this.manager.serialize(uri1, byteDoc);
            this.manager.serialize(uri2, wordDoc);
            deserializedByteDoc = this.manager.deserialize(uri1);
            deserializedWordDoc = this.manager.deserialize(uri2);
            this.manager.delete(uri1);
            this.manager.delete(uri2);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
        this.docEqualityTester(byteDoc, deserializedByteDoc, wordDoc, deserializedWordDoc);
        try {
            this.defaultMngr.serialize(uri1, byteDoc);
            this.defaultMngr.serialize(uri2, wordDoc);
            deserializedByteDoc = this.defaultMngr.deserialize(uri1);
            deserializedWordDoc = this.defaultMngr.deserialize(uri2);
            this.defaultMngr.delete(uri1);
            this.defaultMngr.delete(uri2);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
        this.docEqualityTester(byteDoc, deserializedByteDoc, wordDoc, deserializedWordDoc);
    }

    private void docEqualityTester(Document byteDoc, Document deserializedByteDoc, Document wordDoc, Document deserializedWordDoc) {
        assertEquals(byteDoc, deserializedByteDoc);
        assertEquals(wordDoc, deserializedWordDoc);
        assertEquals(byteDoc.getDocumentTxt(), deserializedByteDoc.getDocumentTxt());
        assertArrayEquals(byteDoc.getDocumentBinaryData(), deserializedByteDoc.getDocumentBinaryData());
        assertEquals(byteDoc.getMetadata(), deserializedByteDoc.getMetadata());
        assertEquals(wordDoc.getDocumentTxt(), deserializedWordDoc.getDocumentTxt());
        assertArrayEquals(wordDoc.getDocumentBinaryData(), deserializedWordDoc.getDocumentBinaryData());
        assertEquals(wordDoc.getMetadata(), deserializedWordDoc.getMetadata());
    }

    // @Test
    // void deserialize() {
    // }

    @Test
    void delete() throws IOException{
        URI uri = URI.create("http://www.yu.edu/documents/doc1");
        Document doc = new DocumentImpl(uri, "This is a test document.".getBytes());
        this.manager.serialize(uri, doc);
        assertTrue(this.manager.delete(uri));
        String fileName = this.baseDir.getPath() + "/" + uri.getHost() + uri.getPath() + ".json";
        assertFalse(new File(fileName).exists());
        this.defaultMngr.serialize(uri, doc);
        assertTrue(this.defaultMngr.delete(uri));
        fileName = this.defaultBaseDir.getPath() + "/" + uri.getHost() + uri.getPath() + ".json";
        assertFalse(new File(fileName).exists());
    }
}