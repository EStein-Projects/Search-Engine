package edu.yu.cs.com1320.searchengine.core.impl;

import edu.yu.cs.com1320.searchengine.datastructures.impl.BTreeImpl;
import edu.yu.cs.com1320.searchengine.datastructures.impl.MinHeapImpl;
import edu.yu.cs.com1320.searchengine.datastructures.impl.StackImpl;
import edu.yu.cs.com1320.searchengine.datastructures.impl.TrieImpl;
import edu.yu.cs.com1320.searchengine.core.Document;
import edu.yu.cs.com1320.searchengine.core.DocumentStore;
import edu.yu.cs.com1320.searchengine.undo.Command;
import edu.yu.cs.com1320.searchengine.undo.CommandSet;
import edu.yu.cs.com1320.searchengine.undo.Undoable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class DocumentStoreImpl implements DocumentStore {
    private BTreeImpl<URI, Document> docs;
    private StackImpl<Undoable> actions;
    private TrieImpl<DocGetter> wordTrie;
    private MinHeapImpl<DocGetter> usageHeap;
    private HashMap<URI, DocGetter> placeholders;
    private int maxDocCt;
    private int maxByteCt;

    private class DocGetter implements Comparable<DocGetter> {
        private final URI uri;
        private boolean isInMemory;
        private HashMap<String, Integer> wordMap;
        private HashMap<String, String> metadata;
        private DocGetter(URI uri){
            this.uri = uri;
            this.isInMemory = true;
        }
        private Document getDoc(){
            return docs.get(this.uri);
        }
        @Override
        public int compareTo(DocGetter dog) {
            return this.getDoc().compareTo(dog.getDoc());
        }
    }

    /*The two document formats supported by this document store.
      Note that TXT means plain text, i.e. a String.

    enum DocumentFormat {
        TXT, BINARY
    }*/

    public DocumentStoreImpl(){
        this.docs = new BTreeImpl<>();
        this.docs.setPersistenceManager(new DocumentPersistenceManager(null));
        this.actions = new StackImpl<>();
        this.wordTrie = new TrieImpl<>();
        this.usageHeap = new MinHeapImpl<>();
        this.maxDocCt = 0;
        this.maxByteCt = 0;
        this.placeholders = new HashMap<>();
    }

    public DocumentStoreImpl(File baseDir){
        this.docs = new BTreeImpl<>();
        this.docs.setPersistenceManager(new DocumentPersistenceManager(baseDir));
        this.actions = new StackImpl<>();
        this.wordTrie = new TrieImpl<>();
        this.usageHeap = new MinHeapImpl<>();
        this.maxDocCt = 0;
        this.maxByteCt = 0;
        this.placeholders = new HashMap<>();
    }

    private void memoryChecker () throws IOException {
        this.docChecker();
        this.byteChecker();
    }

    private void byteChecker() throws IOException {
        if (this.maxByteCt == 0){
            return;
        }
        int totalBytesUsed = 0;
        for (DocGetter dog : this.placeholders.values()){
            if (dog.isInMemory) {
                totalBytesUsed += this.getDocBytes(dog.getDoc()).length;
            }
        }
        if (totalBytesUsed <= this.maxByteCt){
            return;
        }
        do {
            DocGetter oldest = this.usageHeap.remove();
            oldest.isInMemory = false;
            totalBytesUsed -= getDocBytes(oldest.getDoc()).length;
            this.docs.moveToDisk(oldest.uri);
        } while (totalBytesUsed > this.maxByteCt);
    }

    private void docChecker() throws IOException{
        if (this.maxDocCt == 0){
            return;
        }
        int curDocCt = 0;
        for (DocGetter dog : this.placeholders.values()){
            if (dog.isInMemory) {
                curDocCt++;
            }
        }
        if (curDocCt <= this.maxDocCt){
            return;
        }
        do {
            DocGetter oldest = this.usageHeap.remove();
            oldest.isInMemory = false;
            curDocCt--;
            this.docs.moveToDisk(oldest.uri);
        } while (curDocCt > this.maxDocCt);
    }

    /**
     * set the given key-value metadata pair for the document at the given uri
     *
     * @param uri
     * @param key
     * @param value
     * @return the old value, or null if there was no previous value
     * @throws IllegalArgumentException if the uri is null or blank,
     * if there is no document stored at that uri,
     * or if the key is null or blank
     */
    @Override
    public String setMetadata(URI uri, String key, String value) throws IOException {
        if (uri == null || uri.toString().isBlank()){
            throw new IllegalArgumentException("Invalid URI");
        }
        Document doc = this.docs.get(uri);
        if (doc == null){
            throw new IllegalArgumentException("Invalid URI");
        }
        String old = doc.setMetadataValue(key, value);
        DocGetter dog = this.placeholders.get(uri);
        dog.metadata.put(key, value);
        this.singleDocUsageUpdater(doc);
        this.actions.push(new Command<>(uri, u2 -> {
            Document returningDoc = this.docs.get(uri);
            if (!dog.isInMemory){
                try {
                    this.memoryPutter(returningDoc, dog);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            returningDoc.setMetadataValue(key, old);
            dog.metadata.put(key, old);
        }));
        return old;
    }

    private void singleDocUsageUpdater(Document doc) throws IOException {
        doc.setLastUseTime(System.nanoTime());
        URI key = doc.getKey();
        DocGetter dog = this.placeholders.get(key);
        if (dog.isInMemory){
            this.usageHeap.reHeapify(dog);
        } else {
            this.memoryPutter(doc, dog);
        }
    }

    private void memoryPutter(Document doc, DocGetter dog) throws IOException {
        this.bytesHugeDetector(this.getDocBytes(doc));
        this.usageHeap.insert(dog);
        dog.isInMemory = true;
        this.memoryChecker();
    }

    /**
     * get the value corresponding to the given metadata key for the document at the given uri
     *
     * @param uri
     * @param key
     * @return the value, or null if there was no value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    @Override
    public String getMetadata(URI uri, String key) throws IOException {
        if (uri == null || uri.toString().isBlank()){
            throw new IllegalArgumentException("Invalid URI");
        }
        Document doc = this.docs.get(uri);
        if (doc == null){
            throw new IllegalArgumentException("Invalid URI");
        }
        this.singleDocUsageUpdater(doc);
        return doc.getMetadataValue(key);
    }

    /**
     * @param input  the document being put
     * @param url    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0.
     * If there is a previous doc, return the hashCode of the previous doc.
     * If InputStream is null, this is a delete,
     *  and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException              if there is an issue reading input
     * @throws IllegalArgumentException if url or format are null
     */
    @Override
    public int put(InputStream input, URI url, DocumentFormat format) throws IOException {
        if (url == null /*|| uri.toString().isBlank()*/ || format == null) {
            throw new IllegalArgumentException("Invalid File");
        }
        int oldHashCode;
        if (input != null) {
            byte[] in = input.readAllBytes();
            input.close();
            this.bytesHugeDetector(in);
            //Is it a text doc or a binary data doc?
            Document newDoc;
            if (format == DocumentFormat.TXT) {
                newDoc = new DocumentImpl(url, new String(in), null);
            } else {
                newDoc = new DocumentImpl(url, in);
            }
            oldHashCode = putPutter(url, newDoc);
        } else {
            oldHashCode = deleter(url);
        }
        return oldHashCode;
    }

    private int putPutter (URI uri, Document newDoc) throws IOException {
        Document old = this.storeDeleter(uri, newDoc);
        this.storePutter(newDoc);
        this.actions.push(new Command<>(uri, u2 -> {
            if (this.maxByteCt == 0 || this.getDocBytes(old).length <= this.maxByteCt) {
                this.storeDeleter(u2, old);
                try {
                    this.storePutter(old);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }));
        return (old == null) ? 0 : old.hashCode();
    }

    private Document storeDeleter(URI uri, Document newDoc){
        Document old = this.docs.get(uri);
        if (old == null){
            if (newDoc == null) {
                return null;
            }
            return this.docs.put(uri, newDoc);
        }
        DocGetter dog = this.placeholders.remove(uri);
        for (String s : old.getWords()){
            this.wordTrie.delete(s, dog);
        }
        if (dog.isInMemory){
            old.setLastUseTime(0);
            this.usageHeap.reHeapify(dog);
            this.usageHeap.remove();
        }
        return this.docs.put(uri, newDoc);
    }

    private void storePutter(Document newDoc) throws IOException {
        if (newDoc == null){
            return;
        }
        DocGetter dog = new DocGetter(newDoc.getKey());
        for (String s : newDoc.getWords()){
            this.wordTrie.put(s, dog);
        }
        this.placeholders.put(newDoc.getKey(), dog);
        newDoc.setLastUseTime(System.nanoTime());
        this.memoryPutter(newDoc, dog);
        if (newDoc.getDocumentTxt() != null) {
            dog.wordMap = newDoc.getWordMap();
        }
        dog.metadata = newDoc.getMetadata();
    }

    private int deleter (URI uri){
        Document old = this.storeDeleter(uri, null);
        if (old != null){
            this.actions.push(new Command<URI>(uri, u2 -> {
                if (this.maxByteCt == 0 || this.getDocBytes(old).length <= this.maxByteCt) {
                    this.docs.put(u2, old);
                    try {
                        this.storePutter(old);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }));
        }
        return (old == null) ? 0 : old.hashCode();
    }

    private void bytesHugeDetector(byte[] in) {
        if (this.maxByteCt != 0 && in.length > this.maxByteCt){
            throw new IllegalArgumentException("File is too big");
        }
    }

    /**
     * @param url the unique identifier of the document to get
     * @return the given document
     */
    @Override
    public Document get(URI url) throws IOException {
        Document doc = this.docs.get(url);
        if (doc != null){
            this.singleDocUsageUpdater(doc);
        }
        return doc;
    }

    /**
     * @param url the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean delete(URI url) {
        //null is impossible to have in the table
        if (url == null){
            return false;
        }
        return this.deleter(url) != 0;
    }

    /**
     * undo the last put or delete command
     *
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public void undo() throws IllegalStateException {
        if (this.actions.peek() == null){
            throw new IllegalStateException("Nothing to undo");
        }
        this.actions.pop().undo();
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     *
     * @param url
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    @Override
    public void undo(URI url) throws IllegalStateException {
        if (this.actions.peek() == null){
            throw new IllegalStateException("Nothing to undo");
        }
        if (url == null){
            throw new IllegalArgumentException("null URL");
        }
        StackImpl<Undoable> temp = new StackImpl<>();
        while (!this.nextHasURL(url)) {
            temp.push(this.actions.pop());
            if (this.actions.peek() == null) {
                this.stackRestorer(temp);
                throw new IllegalStateException("Command does not exist");
            }
        }
        Undoable c = this.actions.peek();
        if (c instanceof Command<?>) {
            this.actions.pop().undo();
        } else {
            CommandSet<URI> castC = (CommandSet<URI>) c;
            castC.undo(url);
            if (castC.isEmpty()){
                this.actions.pop();
            }
        }
        this.stackRestorer(temp);
    }

    private boolean nextHasURL(URI url) {
        if (this.actions.peek() instanceof Command<?> other){
            if (other.getTarget().equals(url)) {
                return true;
            }
        } else {
            CommandSet<URI> castSet = (CommandSet<URI>) this.actions.peek();
            if (castSet.containsTarget(url)) {
                return true;
            }
        }
        return false;
    }

    private void stackRestorer(StackImpl<Undoable> temp) {
        while (temp.size() > 0){
            this.actions.push(temp.pop());
        }
    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> search(String keyword) throws IOException {
        List<DocGetter> sorted = this.getSorted(keyword);
        return this.docListConverter(sorted);
    }

    private List<DocGetter> getSorted(String keyword) {
        List<DocGetter> sorted = this.wordTrie.getSorted(keyword, (d1, d2) -> {
            int d1ct = this.wordCt(d1, keyword);
            int d2ct = this.wordCt(d2, keyword);
            if (d1ct > d2ct){
                return -1;
            } else if (d1ct == d2ct){
                return 0;
            } else {
                return 1;
            }
        });
        return sorted;
    }

    private int wordCt(DocGetter dog, String word){
        for (String s : dog.wordMap.keySet()){
            if (word.equals(s)){
                return dog.wordMap.get(word);
            }
        }
        return 0;
    }

    private List<Document> docListConverter(List<DocGetter> sorted) throws IOException {
        List<Document> documents = new ArrayList<>();
        long timeNow = System.nanoTime();
        for (DocGetter dog : sorted){
            Document doc = (dog.getDoc());
            doc.setLastUseTime(timeNow);
            if (dog.isInMemory){
                this.usageHeap.reHeapify(dog);
            } else {
                this.memoryPutter(doc, dog);
            }
            documents.add(doc);
        }
        return documents;
    }

    /**
     * Retrieve all documents containing a word that starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) throws IOException {
        List<DocGetter> sorted = this.getPrefixSorted(keywordPrefix);
        return this.docListConverter(sorted);
    }

    private List<DocGetter> getPrefixSorted(String keywordPrefix) {
        List<DocGetter> sorted = this.wordTrie.getAllWithPrefixSorted(keywordPrefix, (d1, d2) -> {
            int d1ct = this.prefixWordCt(d1, keywordPrefix);
            int d2ct = this.prefixWordCt(d2, keywordPrefix);
            if (d1ct > d2ct){
                return -1;
            } else if (d1ct == d2ct){
                return 0;
            } else {
                return 1;
            }
        });
        return sorted;
    }

    private int prefixWordCt(DocGetter dog, String prefix){
        int total = 0;
        int len = prefix.length();
        for (String s : dog.wordMap.keySet()){
            if (s.length() >= len && prefix.equals(s.substring(0, len))){
                total += dog.wordMap.get(s);
            }
        }
        return total;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * Search is CASE SENSITIVE.
     *
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<DocGetter> deletedDocs = this.wordTrie.deleteAll(keyword);
        Set<URI> returningURIs = deleteSet(deletedDocs);
        return returningURIs;
    }

    private Set<URI> deleteSet(Set<DocGetter> deleteDocs) {
        Set<URI> returningURIs = new HashSet<>();
        if (!deleteDocs.isEmpty()){
            CommandSet<URI> deletes = new CommandSet<>();
            for (DocGetter dog : deleteDocs){
                URI u = dog.uri;
                returningURIs.add(u);
                this.deleteSetDeleter(u, deletes);
            }
            this.actions.push(deletes);
        }
        return returningURIs;
    }

    private void deleteSetDeleter(URI url, CommandSet<URI> deletes) {
        Document old = this.storeDeleter(url, null);
        //old can't be null
        deletes.addCommand (new Command<URI>(url, u2 -> {
            if (this.maxByteCt == 0 || this.getDocBytes(old).length <= this.maxByteCt) {
                this.docs.put(u2, old);
                try {
                    this.storePutter(old);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }));
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<DocGetter> deletedDocs = this.wordTrie.deleteAllWithPrefix(keywordPrefix);
        Set<URI> returningURIs = deleteSet(deletedDocs);
        return returningURIs;
    }

    /**
     * @param keysValues metadata key-value pairs to search for
     * @return a List of all documents whose metadata contains ALL OF the given values for the given keys. If no documents contain all the given key-value pairs, return an empty list.
     */
    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) throws IOException {
        if (keysValues == null) {
            throw new IllegalArgumentException("Invalid Metadata Request");
        } else if (keysValues.isEmpty()) {
            return Collections.emptyList();
        }
        List<DocGetter> allMetaDogs = new ArrayList<>(this.placeholders.values());
        this.hasMetadata(allMetaDogs, keysValues);
        return this.docListConverter(allMetaDogs);
    }

    private void hasMetadata(Collection<DocGetter> allDogs, Map<String, String> keysValues) {
        List<DocGetter> cats = new ArrayList<>(allDogs);
        for (DocGetter d : cats) {
            for (Map.Entry<String, String> entry : keysValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                String dogMeta = d.metadata.get(key);
                if (dogMeta == null || !dogMeta.equals(value)) {
                    allDogs.remove(d);
                    break;
                }
            }
        }
    }

    /**
     * Retrieve all documents whose text contains the given keyword AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keyword
     * @param keysValues
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException {
        if (keysValues == null) {
            throw new IllegalArgumentException("Invalid Metadata Request");
        } else if (keysValues.isEmpty()) {
            return Collections.emptyList();
        }
        List<DocGetter> sorted = new ArrayList<>(this.getSorted(keyword));
        this.hasMetadata(sorted, keysValues);
        return this.docListConverter(sorted);
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @param keysValues
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException {
        if (keysValues == null) {
            throw new IllegalArgumentException("Invalid Metadata Request");
        } else if (keysValues.isEmpty()) {
            return Collections.emptyList();
        }
        List<DocGetter> sorted = new ArrayList<>(this.getPrefixSorted(keywordPrefix));
        this.hasMetadata(sorted, keysValues);
        return this.docListConverter(sorted);
    }

    /**
     * Completely remove any trace of any document which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @param keysValues
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) throws IOException {
        if (keysValues == null) {
            throw new IllegalArgumentException("Invalid Metadata Request");
        } else if (keysValues.isEmpty()) {
            return Collections.emptySet();
        }
        Set<DocGetter> allMetaDogs = new HashSet<>(this.placeholders.values());
        this.hasMetadata(allMetaDogs, keysValues);
        Set<URI> returningURIs = deleteSet(allMetaDogs);
        return returningURIs;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @param keyword
     * @param keysValues
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException {
        if (keysValues == null) {
            throw new IllegalArgumentException("Invalid Metadata Request");
        } else if (keysValues.isEmpty()) {
            return Collections.emptySet();
        }
        Set<DocGetter> allMetaDogs = new HashSet<>(this.getSorted(keyword));
        this.hasMetadata(allMetaDogs, keysValues);
        Set<URI> returningURIs = deleteSet(allMetaDogs);
        return returningURIs;
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @param keysValues
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException {
        if (keysValues == null) {
            throw new IllegalArgumentException("Invalid Metadata Request");
        } else if (keysValues.isEmpty()) {
            return Collections.emptySet();
        }
        Set<DocGetter> allMetaDogs = new HashSet<>(this.getPrefixSorted(keywordPrefix));
        this.hasMetadata(allMetaDogs, keysValues);
        Set<URI> returningURIs = deleteSet(allMetaDogs);
        return returningURIs;
    }

    /**
     * set maximum number of documents that may be stored
     *
     * @param limit
     * @throws IllegalArgumentException if limit < 1
     */
    @Override
    public void setMaxDocumentCount(int limit) {
        this.numberChecker(limit);
        this.maxDocCt = limit;
        try {
            this.docChecker();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents in memory combined
     *
     * @param limit
     * @throws IllegalArgumentException if limit < 1
     */
    @Override
    public void setMaxDocumentBytes(int limit) {
        this.numberChecker(limit);
        this.maxByteCt = limit;
        try {
            this.byteChecker();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void numberChecker(int limit) {
        if (limit < 1){
            throw new IllegalArgumentException("Invalid number");
        }
    }

    private byte[] getDocBytes(Document d) {
        if (d == null){
            return new byte[0];
        }
        if (d.getDocumentBinaryData() != null){
            return d.getDocumentBinaryData();
        }
        return d.getDocumentTxt().getBytes();
    }
}
