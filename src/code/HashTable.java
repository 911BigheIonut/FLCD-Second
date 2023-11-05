package code;

import java.util.ArrayList;


public class HashTable<T> {
    private ArrayList<ArrayList<T>> items;
    private final int size;

    public HashTable(int size) {
        this.size = size;
        this.items = new ArrayList<>();
        for (int i = 0; i < size; i++)
            this.items.add(new ArrayList<>());
    }

    private int hash(int key) {
        return key % size;
    }

    private int hash(String key) {
        int s = 0;
        for (int i = 0; i < key.length(); i++) {
            s += key.charAt(i);
        }
        return s % size;
    }



    public KeyValue<Integer, Integer> add(T key) throws Exception {
        int hashValue = getHashValue(key);
        KeyValue<Integer, Integer> keyValue = new KeyValue<>(hashValue, items.get(hashValue).indexOf(key));
        if (!items.get(hashValue).contains(key)) {
            items.get(hashValue).add(key);
            return keyValue;
        }
        throw new Exception("Key " + key + " is already in the table!");
    }


    private int getHashValue(T key) {
        int hashValue = -1;
        if (key instanceof Integer) {
            hashValue = hash((int) key);
        } else if (key instanceof String) {
            hashValue = hash((String) key);
        }
        return hashValue;
    }

    public KeyValue<Integer, Integer> getPosition(T key) {
        if (this.contains(key)) {
            int hashValue = getHashValue(key);
            return new KeyValue<>(hashValue, items.get(hashValue).indexOf(key));
        }
        return new KeyValue<>(-1, -1);
    }

    public boolean contains(T key) {
        int hashValue = getHashValue(key);
        return items.get(hashValue).contains(key);
    }


    @Override
    public String toString() {
        return "HashTable{" + "items=" + items + '}';
    }
}