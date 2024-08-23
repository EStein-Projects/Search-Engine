package edu.yu.cs.com1320.searchengine.core.impl;

import edu.yu.cs.com1320.searchengine.core.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {
    private final URI uri;
    private String text;
    private byte[] binaryData;
    private HashMap<String, String> metadata;
    private long timeOfLastUse;
    private HashMap<String, Integer> wordCounts;

    //throws a java.lang.IllegalArgumentException if either argument is
    //null or empty/blank
    public DocumentImpl(URI uri, String text, Map<String, Integer> wordCountMap){
        if (uri == null || uri.toString().isBlank() || text == null || text.isBlank() ||
                (wordCountMap != null && wordCountMap.isEmpty())){
            throw new IllegalArgumentException("Invalid Document Parameter(s)");
        }
        this.uri = uri;
        this.text = text;
        this.binaryData = null;
        this.metadata = new HashMap<>();
        if (wordCountMap != null) {
            this.wordCounts = (HashMap<String, Integer>) wordCountMap;
        } else {
            this.wordCtMapGenerator(text);
        }
    }

    private void wordCtMapGenerator(String txt) {
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
        this.metadata = new HashMap<>();
        this.wordCounts = null;
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
    public HashMap<String, String> getMetadata() {
        return new HashMap<>(this.metadata);
    }

    @Override
    public void setMetadata(HashMap<String, String> metadata) {
        if (metadata == null){
            this.metadata = new HashMap<>();
        } else {
            this.metadata = metadata;
        }
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

    /**
     * return the last time this document was used, via put/get or via a search result
     */
    @Override
    public long getLastUseTime() {
        return this.timeOfLastUse;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.timeOfLastUse = timeInNanoseconds;
    }

    /**
     * @return a COPY of the word to count map so it can be serialized
     */
    @Override
    public HashMap<String, Integer> getWordMap() {
        return new HashMap<>(this.wordCounts);
    }

    /**
     * This must set the word to count map during deserialization
     *
     * @param wordMap
     */
    @Override
    public void setWordMap(HashMap<String, Integer> wordMap) {
        if (this.binaryData != null && wordMap != null && !wordMap.isEmpty()){
            throw new IllegalArgumentException("Binary docs have no words");
        } else if (this.text == null) {
            this.wordCounts = null;
        } else {
            this.wordCounts = wordMap;
        }
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

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure {@link Integer#signum
     * signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))} for
     * all {@code x} and {@code y}.  (This implies that {@code
     * x.compareTo(y)} must throw an exception if and only if {@code
     * y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code
     * x.compareTo(y)==0} implies that {@code signum(x.compareTo(z))
     * == signum(y.compareTo(z))}, for all {@code z}.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     * @apiNote It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     */
    @Override
    public int compareTo(Document o) {
        //Note: this class has a natural ordering that is inconsistent with equals.
        if (this.timeOfLastUse < o.getLastUseTime()){
            return -1;
        }
        if (this.timeOfLastUse == o.getLastUseTime()){
            return 0;
        }
        return 1;
    }
}
