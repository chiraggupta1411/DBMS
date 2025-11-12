// ----------------------------------------------------------------------
// Problem 16 (modified): Simulated MongoDB operations using in-memory structures.
// This version is standalone and does not require the MongoDB Java Driver.
// To run: javac MongoDBConnector.java && java MongoDBConnector
// ----------------------------------------------------------------------
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class MongoDBConnector {

    // Default (informational) connection string and names
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "java_mongo_db";
    private static final String COLLECTION_NAME = "TestCollection";

    public static void main(String[] args) {
        // Use an in-memory list to simulate a collection
        List<Document> collection = new ArrayList<>();

        System.out.println("Mock connection to MongoDB successful. Database: " + DATABASE_NAME);

        // --- C: CREATE (Insert) ---
        Document doc1 = new Document("name", "Chirag")
                .append("city", "Pune")
                .append("age", 28)
                .append("interests", Arrays.asList("Coding", "Music"));
        insertOne(collection, doc1);
        System.out.println("Inserted document for Chirag.");

        Document doc2 = new Document("name", "Aryan")
                .append("city", "Mumbai")
                .append("age", 22)
                .append("interests", Arrays.asList("Sports", "Reading"));
        insertOne(collection, doc2);
        System.out.println("Inserted document for Aryan.");

        // --- R: READ (Retrieve) ---
        System.out.println("\n--- All Documents ---");
        for (Document d : find(collection, null)) {
            System.out.println(d.toJson());
        }

        System.out.println("\n--- Document for Chirag (Filtered Read) ---");
        Document chiragDoc = findFirst(collection, Filters.eq("name", "Chirag"));
        if (chiragDoc != null) {
            System.out.println(chiragDoc.toJson());
        }

        // --- U: UPDATE ---
        updateOne(collection, Filters.eq("name", "Chirag"), new Document("$set", new Document("age", 29)));
        System.out.println("\nUpdated Chirag's age.");

        // --- D: DELETE ---
        deleteOne(collection, Filters.eq("name", "Aryan"));
        System.out.println("Deleted document for Aryan.");

        System.out.println("\n--- Documents after Update and Delete ---");
        for (Document d : find(collection, null)) {
            System.out.println(d.toJson());
        }
    }

    // Simple in-memory helpers

    private static void insertOne(List<Document> collection, Document doc) {
        // store a shallow copy to mimic DB behavior
        collection.add(new Document(doc));
    }

    private static List<Document> find(List<Document> collection, Predicate<Document> filter) {
        List<Document> results = new ArrayList<>();
        for (Document d : collection) {
            if (filter == null || filter.test(d)) {
                results.add(new Document(d));
            }
        }
        return results;
    }

    private static Document findFirst(List<Document> collection, Predicate<Document> filter) {
        for (Document d : collection) {
            if (filter == null || filter.test(d)) {
                return new Document(d);
            }
        }
        return null;
    }

    private static void updateOne(List<Document> collection, Predicate<Document> filter, Document update) {
        for (Document d : collection) {
            if (filter == null || filter.test(d)) {
                // Support only {$set: {...}} style for this mock
                Object setObj = update.get("$set");
                if (setObj instanceof Document) {
                    Document setDoc = (Document) setObj;
                    for (Map.Entry<String, Object> e : setDoc.map.entrySet()) {
                        d.map.put(e.getKey(), e.getValue());
                    }
                }
                return;
            }
        }
    }

    private static void deleteOne(List<Document> collection, Predicate<Document> filter) {
        Iterator<Document> it = collection.iterator();
        while (it.hasNext()) {
            Document d = it.next();
            if (filter == null || filter.test(d)) {
                it.remove();
                return;
            }
        }
    }

    // Minimal Document implementation (mimics org.bson.Document enough for this example)
    public static class Document {
        private final Map<String, Object> map = new LinkedHashMap<>();

        public Document() {}

        public Document(String key, Object value) {
            map.put(key, value);
        }

        // Copy constructor
        public Document(Document other) {
            this.map.putAll(other.map);
        }

        public Document append(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public Object get(String key) {
            return map.get(key);
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if (!first) sb.append(", ");
                first = false;
                sb.append("\"").append(e.getKey()).append("\": ");
                Object v = e.getValue();
                if (v instanceof String) {
                    sb.append("\"").append(v).append("\"");
                } else if (v instanceof List) {
                    sb.append(v.toString());
                } else {
                    sb.append(String.valueOf(v));
                }
            }
            sb.append("}");
            return sb.toString();
        }
    }

    // Minimal Filters helper to produce Predicate<Document>
    public static class Filters {
        public static Predicate<Document> eq(String key, Object value) {
            return doc -> Objects.equals(doc.get(key), value);
        }
    }
}