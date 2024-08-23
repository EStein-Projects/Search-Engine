package edu.yu.cs.com1320.searchengine.core.impl;

import edu.yu.cs.com1320.searchengine.datastructures.impl.HashTableImpl;
import edu.yu.cs.com1320.searchengine.datastructures.impl.StackImpl;
import edu.yu.cs.com1320.searchengine.datastructures.impl.TrieImpl;
import edu.yu.cs.com1320.searchengine.core.Document;
import edu.yu.cs.com1320.searchengine.core.DocumentStore;
import edu.yu.cs.com1320.searchengine.undo.CommandSet;
import edu.yu.cs.com1320.searchengine.undo.Command;
import edu.yu.cs.com1320.searchengine.undo.Undoable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class DocumentStoreImpl implements DocumentStore {
    HashTableImpl<URI,Document> docs;
    private StackImpl<Undoable> actions;
    private TrieImpl<Document> wordTrie;

    /*The two document formats supported by this document store.
      Note that TXT means plain text, i.e. a String.

    enum DocumentFormat {
        TXT, BINARY
    }*/

    public DocumentStoreImpl(){
        this.docs = new HashTableImpl<>();
        this.actions = new StackImpl<>();
        this.wordTrie = new TrieImpl<>();
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
    public String setMetadata(URI uri, String key, String value) {
        if (uri == null || uri.toString().isBlank() || !this.docs.containsKey(uri)){
            throw new IllegalArgumentException("Invalid URI");
        }
        String old = this.docs.get(uri).setMetadataValue(key, value);
        this.actions.push(new Command<URI>(uri, u2 -> this.docs.get(uri).setMetadataValue(key, old)));
        return old;
    }

    /**
     * get the value corresponding to the given metadata key for the document at the given uri
     *
     * @param uri
     * @param key
     * @return the value, or null if there was no value
     * @throws IllegalArgumentException if the uri is null or blank,
     * if there is no document stored at that uri,
     * or if the key is null or blank
     */
    @Override
    public String getMetadata(URI uri, String key) {
        if (uri == null || uri.toString().isBlank() || !this.docs.containsKey(uri)){
            throw new IllegalArgumentException("Invalid URI");
        }
        return this.docs.get(uri).getMetadataValue(key);
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
        if (url == null /*|| uri.toString().isBlank()*/ || format == null){
            throw new IllegalArgumentException("Invalid File");
        }
        Document old;
        if (input != null) {
            byte[] in = input.readAllBytes();
            input.close();
            //Is it a text doc or a binary data doc?
            Document newDoc = (format == DocumentFormat.TXT) ?
                    new DocumentImpl(url, new String(in)) : new DocumentImpl(url, in);
            old = this.docs.put(url, newDoc);
            this.trieRemover(old);
            this.triePutter(newDoc);
            this.actions.push(new Command<URI>(url, u2 -> {
                this.docs.put(u2, old);
                this.trieRemover(newDoc);
                this.triePutter(old);
            }));
        } else {
            old = this.docs.put(url, null);
            this.trieRemover(old);
            if (old != null){
                this.actions.push(new Command<URI>(url, u2 -> {
                    this.docs.put(u2, old);
                    this.triePutter(old);
                }));
            }
        }
        return (old == null) ? 0 : old.hashCode();
    }

    private void trieRemover(Document old) {
        if (old == null){
            return;
        }
        for (String s : old.getWords()){
            this.wordTrie.delete(s, old);
        }
    }

    private void triePutter(Document newDoc) {
        if (newDoc == null){
            return;
        }
        for (String s : newDoc.getWords()){
            this.wordTrie.put(s, newDoc);
        }
    }

    /**
     * @param url the unique identifier of the document to get
     * @return the given document
     */
    @Override
    public Document get(URI url) {
        return this.docs.get(url);
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
        boolean contained = this.docs.containsKey(url);
        if (contained) {
            Document old = this.docs.put(url, null);
            this.trieRemover(old);
            //old can't be null
            this.actions.push(new Command<URI>(url, u2 -> {
                this.docs.put(u2, old);
                this.triePutter(old);
            }));
        }
        return contained;
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
        while (!this.hasURL(url)) {
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

    private boolean hasURL(URI url) {
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
    public List<Document> search(String keyword) {
        return this.wordTrie.getSorted(keyword, (d1, d2) -> {
            if (d1.wordCount(keyword) > d2.wordCount(keyword)) {
                return -1;
            } else if (d1.wordCount(keyword) == d2.wordCount(keyword)) {
                return 0;
            } else {
                return 1;
            }
        });
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     *
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        return this.wordTrie.getAllWithPrefixSorted(keywordPrefix, (d1, d2) -> {
            if (prefixWordCount(d1, keywordPrefix) > prefixWordCount(d2, keywordPrefix)) {
                return -1;
            } else if (prefixWordCount(d1, keywordPrefix) == prefixWordCount(d2, keywordPrefix)) {
                return 0;
            } else {
                return 1;
            }
        });
    }

    private int prefixWordCount(Document doc, String prefix){
        int total = 0;
        int len = prefix.length();
        for (String s : doc.getWords()){
            if (s.length() >= len && prefix.equals(s.substring(0, len))){
                total += doc.wordCount(s);
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
        Set<Document> deletedDocs = this.wordTrie.deleteAll(keyword);
        Set<URI> returningURIs = deleteSet(deletedDocs);
        return returningURIs;
    }

    private Set<URI> deleteSet(Set<Document> deleteDocs) {
        Set<URI> returningURIs = new HashSet<>();
        if (!deleteDocs.isEmpty()){
        CommandSet<URI> deletes = new CommandSet<>();
        for (Document d : deleteDocs){
            URI u = d.getKey();
            returningURIs.add(u);
            this.deleteSetDeleter(u, deletes);
        }
        this.actions.push(deletes);
        }
        return returningURIs;
    }

    private void deleteSetDeleter(URI url, CommandSet<URI> deletes) {
        Document old = this.docs.put(url, null);
        this.trieRemover(old);
        //old can't be null
        deletes.addCommand (new Command<URI>(url, u2 -> {
            this.docs.put(u2, old);
            this.triePutter(old);
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
        Set<Document> deletedDocs = this.wordTrie.deleteAllWithPrefix(keywordPrefix);
        Set<URI> returningURIs = deleteSet(deletedDocs);
        return returningURIs;
    }

    /**
     * @param keysValues metadata key-value pairs to search for
     * @return a List of all documents whose metadata contains ALL the given values for the given keys.
     *  If no documents contain all the given key-value pairs, return an empty list.
     */
    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) {
        if (keysValues == null) {
            throw new IllegalArgumentException("Invalid Metadata Request");
        } else if (keysValues.isEmpty()) {
            return Collections.emptyList();
        }
        List<Document> documents = new ArrayList<>(this.docs.values());
        this.hasMetadata(documents, keysValues);
        return documents;
    }

    private void hasMetadata(List<Document> documents, Map<String, String> keysValues) {
        List<Document> docsCopy = new ArrayList<>(documents);
        for (Document d : docsCopy) {
            for (Map.Entry<String, String> entry : keysValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (this.getMetadata(d.getKey(), key) == null || !this.getMetadata(d.getKey(), key).equals(value)) {
                    documents.remove(d);
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
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        if (keysValues == null) {
            throw new IllegalArgumentException("Invalid Metadata Request");
        } else if (keysValues.isEmpty()) {
            return Collections.emptyList();
        }
        List<Document> documents = this.search(keyword);
        this.hasMetadata(documents, keysValues);
        return documents;
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
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        if (keysValues == null) {
            throw new IllegalArgumentException("Invalid Metadata Request");
        } else if (keysValues.isEmpty()) {
            return Collections.emptyList();
        }
        List<Document> documents = this.searchByPrefix(keywordPrefix);
        this.hasMetadata(documents, keysValues);
        return documents;
    }

    /**
     * Completely remove any trace of any document which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     *
     * @param keysValues
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) {
        Set<Document> documents = new HashSet<>(this.searchByMetadata(keysValues));
        Set<URI> returningURIs = deleteSet(documents);
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
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        Set<Document> documents = new HashSet<>(this.searchByKeywordAndMetadata(keyword, keysValues));
        Set<URI> returningURIs = deleteSet(documents);
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
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        Set<Document> documents = new HashSet<>(this.searchByPrefixAndMetadata(keywordPrefix, keysValues));
        Set<URI> returningURIs = deleteSet(documents);
        return returningURIs;
    }
}