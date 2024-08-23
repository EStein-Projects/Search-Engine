package edu.yu.cs.com1320.searchengine.core.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class DocumentImplTest {
    DocumentImpl sampleDoc;
    DocumentImpl sampleImg;
    @BeforeEach
    void setUp() {
        sampleDoc = new DocumentImpl(URI.create("HW"), "Hello World", null);
        sampleDoc.setLastUseTime(System.nanoTime());
        sampleDoc.setMetadataValue("Status", "Imaginary");
        sampleImg = new DocumentImpl(URI.create("Bla"), new byte[0xd]);
        sampleImg.setLastUseTime(System.nanoTime());
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
        assertEquals(sampleDoc.getMetadata().get("Status"), sampleDoc.getMetadataValue("Status"));
        assertNull(sampleImg.getMetadataValue("Status"));
    }

    @Test
    void getMetadata() {
        HashMap<String,String> sampleMap = sampleDoc.getMetadata();
        assertNull(sampleMap.get("Author"));
        assertEquals("Imaginary", sampleMap.get("Status"));
        assertEquals(sampleMap, sampleDoc.getMetadata());
        sampleMap.put("Status", "Fictional");
        assertNotEquals(sampleMap.get("Status"), sampleDoc.getMetadataValue("Status"));
        assertNotEquals(sampleMap, sampleDoc.getMetadata());
    }

    @Test
    void setMetadata() {
    }

    @Test
    void getDocumentTxt() {
        assertNotNull(sampleDoc.getDocumentTxt());
        assertFalse(sampleDoc.getDocumentTxt().isBlank());
        assertEquals(sampleDoc.getDocumentTxt(), "Hello World");
        assertNull(sampleImg.getDocumentTxt());
    }

    @Test
    void getDocumentBinaryData() {
        assertNotNull(sampleImg.getDocumentBinaryData());
        assertNotEquals(0, sampleImg.getDocumentBinaryData().length);
        assertArrayEquals(sampleImg.getDocumentBinaryData(), new byte[0xd]);
        assertNull(sampleDoc.getDocumentBinaryData());
    }

    @Test
    void getKey() {
        assertNotNull(sampleDoc.getKey());
        assertNotNull(sampleImg.getKey());
        assertFalse(sampleDoc.getKey().toString().isBlank());
        assertEquals(sampleDoc.getKey(), URI.create("HW"));
        assertEquals(sampleDoc.getKey().toString(), "HW");
    }

    @Test
    void wordCount() {
        assertEquals(0, sampleImg.wordCount("Hello"));
        assertEquals(0, sampleDoc.wordCount("He"));
        assertEquals(0, sampleDoc.wordCount(""));
        assertEquals(0, sampleDoc.wordCount("Hello World"));
        assertEquals(1, sampleDoc.wordCount("Hello"));
        assertEquals(1, sampleDoc.wordCount("World"));
        DocumentImpl newSample = new DocumentImpl(URI.create("HW"), "Hello World Hello", null);
        assertEquals(2, newSample.wordCount("Hello"));
    }

    @Test
    void getWords() {assertEquals(Collections.emptySet(), sampleImg.getWords());
        HashSet<String> docWords = new HashSet<>();
        docWords.add("Hello");
        docWords.add("World");
        assertEquals(docWords, sampleDoc.getWords());
        assertEquals(Collections.emptySet(),
                new DocumentImpl(URI.create("Empty"), " !@#$%^&*_< >,. ", null).getWords());
    }

    @Test
    void getLastUseTime() {
        assertNotEquals(0, sampleDoc.getLastUseTime());
        assertNotEquals(0, sampleImg.getLastUseTime());
    }

    @Test
    void setLastUseTime() {
        sampleDoc.setLastUseTime(3);
        sampleImg.setLastUseTime(3);
        assertEquals(3, sampleDoc.getLastUseTime());
        assertEquals(3, sampleImg.getLastUseTime());
    }

    // @Test
    // void getWordMap() {
    // }

    // @Test
    // void setWordMap() {
    // }

    // @Test
    // void testHashCode() {
    // }

    @Test
    void testEquals() {
        DocumentImpl newSample = sampleDoc;
        assertEquals(newSample, sampleDoc);
        assertNotEquals(sampleDoc, sampleImg);
    }

    @Test
    void compareTo() {
        assertEquals(-1, sampleDoc.compareTo(sampleImg));
        assertEquals(1, sampleImg.compareTo(sampleDoc));
        assertEquals(0, sampleDoc.compareTo(sampleDoc));
    }
}