package edu.yu.cs.com1320.searchengine.core.impl;

import edu.yu.cs.com1320.searchengine.datastructures.HashTable;
import edu.yu.cs.com1320.searchengine.datastructures.impl.HashTableImpl;
import edu.yu.cs.com1320.searchengine.core.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {
    URI uri;
    String text;
    byte[] binaryData;
    HashTable<String,String> metadata;
    HashMap<String, Integer> wordCounts;

    //throws a java.lang.IllegalArgumentException if either argument is
    //null or empty/blank
    public DocumentImpl(URI uri, String txt) {
        if (uri == null || uri.toString().isBlank() || txt == null || txt.isBlank()){
            throw new IllegalArgumentException("Invalid Document Parameter(s)");
        }
        this.uri = uri;
        this.text = txt;
        this.binaryData = null;
        this.metadata = new HashTableImpl<>();
        this.wordCounts = new HashMap<>();
        String temp = "";
        for (int c = 0; c < txt.length(); c++){
            if (Character.isLetterOrDigit(txt.charAt(c))){
                temp += txt.charAt(c);
                if (c != txt.length()-1){
                    continue;
                }
            }
            if ((Character.isSpaceChar(txt.charAt(c)) || Character.isWhitespace(txt.charAt(c))
                    || c == txt.length()-1)
                    && !temp.isEmpty()) {
                if (!this.wordCounts.containsKey(temp)){
                    this.wordCounts.put(temp, 1);
                } else {
                    int newCount = this.wordCounts.get(temp) + 1;
                    this.wordCounts.put(temp, newCount);
                }
                temp = "";
            }
        }
    }
    public DocumentImpl(URI uri, byte[] binaryData) {
        if (uri == null || uri.toString().isBlank() || binaryData == null || binaryData.length == 0){
            throw new IllegalArgumentException("Invalid Image Parameter(s)");
        }
        this.uri = uri;
        this.text = null;
        this.binaryData = binaryData;
        metadata = new HashTableImpl<>();
    }

    /**
     * @param key   key of document metadata to store a value for
     * @param value value to store
     * @return old value, or null if there was no old value
     * @throws IllegalArgumentException if the key is null or blank
     */
    @Override
    public String setMetadataValue(String key, String value) {
        if (key == null || key.isBlank()){
            throw new IllegalArgumentException("Invalid Metadata Entry");
        }
        return this.metadata.put(key, value);
    }

    /**
     * @param key metadata key whose value we want to retrieve
     * @return corresponding value, or null if there is no such key
     * @throws IllegalArgumentException if the key is null or blank
     */
    @Override
    public String getMetadataValue(String key) {
        if (key == null || key.isBlank()){
            throw new IllegalArgumentException("Invalid Metadata Request");
        }
        return this.metadata.get(key);
    }

    /**
     * @return a COPY of the metadata saved in this document
     */
    @Override
    public HashTable<String, String> getMetadata() {
        HashTableImpl <String, String> copy = new HashTableImpl<>();
        HashSet<String> keys = new HashSet<>(this.metadata.keySet());
        for (String k : keys) {
            copy.put(k, this.getMetadataValue(k));
        }
        return copy;
    }

    /**
     * @return content of text document
     */
    @Override
    public String getDocumentTxt() {
        return this.text;
    }

    /**
     * @return content of binary data document
     */
    @Override
    public byte[] getDocumentBinaryData() {
        return this.binaryData;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    @Override
    public URI getKey() {
        return this.uri;
    }

    /**
     * how many times does the given word appear in the document?
     *
     * @param word
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    @Override
    public int wordCount(String word) {
        if (this.text == null){
            return 0;
        }
        String checker = "";
        for (int c = 0; c < word.length(); c++){
            if (Character.isLetterOrDigit(word.charAt(c))){
                checker += word.charAt(c);
            }
        }
        if (this.wordCounts.containsKey(checker)){
            return this.wordCounts.get(checker);
        }
        return 0;
    }

    /**
     * @return all the words that appear in the document
     */
    @Override
    public Set<String> getWords() {
        if (this.binaryData == null){
            return this.wordCounts.keySet();
        }
        return Collections.emptySet();
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }

    @Override
    public boolean equals(Object obj) {
        //if it's not the same class, can't be equal
        if(!(obj instanceof DocumentImpl other)){
            return false;
        }
        //if it's the exact same object, of course equal
        if(obj == this){
            return true;
        }
        //test logical equivalence to another object
        //of the same class
        return this.uri.equals(other.uri);
    }
}
