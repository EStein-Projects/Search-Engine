package edu.yu.cs.com1320.searchengine.core.impl;

import edu.yu.cs.com1320.searchengine.datastructures.impl.HashTableImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class DocumentImplTest {
    DocumentImpl sampleDoc;
    DocumentImpl sampleImg;

    @BeforeEach
    void setUp() {
        sampleDoc = new DocumentImpl(URI.create("HW"), "Hello World");
        sampleDoc.setMetadataValue("Status", "Imaginary");
        sampleImg = new DocumentImpl(URI.create("Bla"), new byte[0xd]);
    }

    @Test
    void setMetadataValue() {
        assertNull(sampleDoc.setMetadataValue("Author", "Me"));
        assertEquals("Me", sampleDoc.setMetadataValue("Author", "ES"));
        assertNull(sampleDoc.setMetadataValue("Unrelated", "Bla"));
        assertNull(sampleImg.setMetadataValue("Author", "Elchonon"));
    }

    @Test
    void getMetadataValue() {
        assertNull(sampleDoc.getMetadataValue("Author"));
        assertEquals("Imaginary", sampleDoc.getMetadataValue("Status"));
        assertEquals(sampleDoc.metadata.get("Status"), sampleDoc.getMetadataValue("Status"));
        assertNull(sampleImg.getMetadataValue("Status"));
    }

    @Test
    void getMetadata() {
        HashTableImpl<String,String> sampleMap = (HashTableImpl<String, String>) sampleDoc.getMetadata();
        assertNull(sampleMap.get("Author"));
        assertEquals("Imaginary", sampleMap.get("Status"));

        // Not equivalent bec HashTableImpl#equals returns false when copied bec not overridden
        assertNotEquals(sampleMap, sampleDoc.metadata);
        sampleMap.put("Status", "Fictional");
        assertNotEquals(sampleMap.get("Status"), sampleDoc.getMetadataValue("Status"));
        assertNotEquals(sampleMap, sampleDoc.metadata);
    }

    @Test
    void getDocumentTxt() {
        assertNotNull(sampleDoc.getDocumentTxt());
        assertFalse(sampleDoc.getDocumentTxt().isBlank());
        assertEquals(sampleDoc.getDocumentTxt(), sampleDoc.text);
        assertNull(sampleImg.getDocumentTxt());
    }

    @Test
    void getDocumentBinaryData() {
        assertNotNull(sampleImg.getDocumentBinaryData());
        assertNotEquals(0, sampleImg.getDocumentBinaryData().length);
        assertEquals(sampleImg.getDocumentBinaryData(), sampleImg.binaryData);
        assertNull(sampleDoc.getDocumentBinaryData());
    }

    @Test
    void getKey() {
        assertNotNull(sampleDoc.getKey());
        assertNotNull(sampleImg.getKey());
        assertFalse(sampleDoc.getKey().toString().isBlank());
        assertEquals(sampleDoc.getKey(), sampleDoc.uri);
        assertEquals(sampleDoc.getKey().toString(), "HW");
    }

//    @Test
//    void testHashCode() {
//    }

    @Test
    void testEquals() {
        DocumentImpl newSample = sampleDoc;
        assertEquals(newSample, sampleDoc);
        assertNotEquals(sampleDoc, sampleImg);
    }

    /**
     * how many times does the given word appear in the document?
     * param word
     * return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    @Test
    void wordCount() {
        assertEquals(0, sampleImg.wordCount("Hello"));
        assertEquals(0, sampleDoc.wordCount("He"));
        assertEquals(0, sampleDoc.wordCount(""));
        assertEquals(0, sampleDoc.wordCount("Hello World"));
        assertEquals(1, sampleDoc.wordCount("Hello"));
        assertEquals(1, sampleDoc.wordCount("World"));
        DocumentImpl newSample = new DocumentImpl(URI.create("HW"), "Hello World Hello");
        assertEquals(2, newSample.wordCount("Hello"));
    }

    /**
     * return all the words that appear in the document
     */
    @Test
    void getWords() {
        assertEquals(Collections.emptySet(), sampleImg.getWords());
        HashSet <String> docWords = new HashSet<>();
        docWords.add("Hello");
        docWords.add("World");
        assertEquals(docWords, sampleDoc.getWords());
        assertEquals(Collections.emptySet(), new DocumentImpl(URI.create("Empty"), " !@#$%^&*_< >,. ").getWords());
    }
}