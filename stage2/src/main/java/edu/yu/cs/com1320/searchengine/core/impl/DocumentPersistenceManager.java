package edu.yu.cs.com1320.searchengine.core.impl;

import com.google.gson.*;
import edu.yu.cs.com1320.searchengine.core.Document;
import edu.yu.cs.com1320.searchengine.core.PersistenceManager;
import jakarta.xml.bind.DatatypeConverter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;

public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private File directory;
    private Gson gson;
    public DocumentPersistenceManager(File baseDir){
        if (baseDir != null){
            this.directory = baseDir;
        }
        else {
            this.directory = new File(System.getProperty("user.dir"));
        }
        GsonBuilder builder = new GsonBuilder().registerTypeHierarchyAdapter(Document.class, new DocSerializers());
        this.gson = builder.create();
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        String filePath = this.uriToFileName(uri);
        String json = this.gson.toJson(val);
        File file = new File(filePath);
        file.getParentFile().mkdirs(); // Create directories if they don't exist
        file.createNewFile();
        try (FileWriter writer = new FileWriter(file)){
            writer.write(json);
        }
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        String filePath = this.uriToFileName(uri);
        StringBuilder json = new StringBuilder();
        try (FileReader reader = new FileReader(filePath)) {
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }
        }
        return gson.fromJson(json.toString(), Document.class);
    }

    /**
     * delete the file stored on disk that corresponds to the given URI
     *
     * @return true or false to indicate if deletion occured or not
     * @throws IOException
     */
    @Override
    public boolean delete(URI uri) throws IOException {
        File file = new File(this.uriToFileName(uri));
        // Delete the file
        boolean deleted = file.delete();
        // Delete parent directories if empty
        for(File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()){
            if (!parent.delete()){
                break;
            }
        }
        return deleted;
    }

    private String uriToFileName (URI uri) {
        return directory.getPath() + "/" + uri.getHost() + uri.getPath() + ".json";
    }

    private class DocSerializers implements JsonSerializer<Document>, JsonDeserializer <Document> {

        @Override
        public Document deserialize(JsonElement j, Type t, JsonDeserializationContext c) throws JsonParseException {
            JsonObject jsonDoc = j.getAsJsonObject();
            URI uri = URI.create(jsonDoc.get("uri").getAsString());
            HashMap<String, String> metadata = new HashMap<>();
            JsonObject metaMap = jsonDoc.get("metadata map").getAsJsonObject();
            for (String s : metaMap.keySet()) {
                JsonObject pair = metaMap.get(s).getAsJsonObject();
                String key = pair.get("key").getAsString();
                String val = pair.get("value").getAsString();
                metadata.put(key, val);
            }
            Document doc;
            if (jsonDoc.has("text")){
                String text = jsonDoc.get("text").getAsString();
                HashMap<String, Integer> wordCounts = new HashMap<>();
                JsonObject wordMap = jsonDoc.get("word count map").getAsJsonObject();
                for (String s : wordMap.keySet()){
                    JsonObject pair = wordMap.get(s).getAsJsonObject();
                    String key = pair.get("word").getAsString();
                    int val = pair.get("number").getAsInt();
                    wordCounts.put(key, val);
                }
                doc = new DocumentImpl(uri, text, wordCounts);
            } else {
                byte[] bytes = DatatypeConverter.parseBase64Binary(jsonDoc.get("bytes").getAsString());
                doc = new DocumentImpl(uri, bytes);
            }
            doc.setMetadata(metadata);
            doc.setLastUseTime(System.nanoTime());
            return doc;
        }

        @Override
        public JsonElement serialize(Document d, Type t, JsonSerializationContext c) {
            JsonObject j = new JsonObject();
            j.addProperty("uri", d.getKey().toASCIIString());
            JsonObject metadata = new JsonObject();
            for (String s : d.getMetadata().keySet()){
                JsonObject pair = new JsonObject();
                pair.addProperty("key", s);
                pair.addProperty("value", d.getMetadataValue(s));
                metadata.add("metadata pair" + s, pair);
            }
            j.add("metadata map", metadata);
            if (d.getDocumentTxt() != null){
                j.addProperty("text", d.getDocumentTxt());
                JsonObject wordCounts = new JsonObject();
                HashMap<String, Integer> docWordMap = d.getWordMap();
                for (String s : docWordMap.keySet()){
                    JsonObject pair = new JsonObject();
                    pair.addProperty("word", s);
                    pair.addProperty("number", docWordMap.get(s));
                    wordCounts.add("word count " + s, pair);
                }
                j.add("word count map", wordCounts);
            } else {
                j.addProperty("bytes", DatatypeConverter.printBase64Binary(d.getDocumentBinaryData()));
            }
            return j;
        }
    }
}
