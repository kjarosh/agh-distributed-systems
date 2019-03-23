package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;

public class DistributedMap implements SimpleStringMap, AutoCloseable {
    private final HashMap<String, Integer> localCopy = new HashMap<>();
    private final MapSynchronizationService syncService;

    public DistributedMap(String clusterName, String address) throws Exception {
        syncService = new MapSynchronizationService(clusterName, address);
        syncService.setRemoveListener(localCopy::remove);
        syncService.setPutListener(localCopy::put);
        syncService.setStateSerializer(this::serializeMap);
        syncService.setStateDeserializer(this::deserializeMap);
        syncService.setMerger(inputStream -> merge(localCopy, inputStream));
    }

    private void serializeMap(OutputStream outputStream) {
        try {
            new ObjectOutputStream(outputStream)
                    .writeObject(localCopy);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deserializeMap(InputStream inputStream) {
        HashMap<String, Integer> received = new HashMap<>();
        merge(received, inputStream);

        localCopy.clear();
        localCopy.putAll(received);
    }

    private void merge(HashMap<String, Integer> map, InputStream inputStream) {
        try {
            Object obj = new ObjectInputStream(inputStream).readObject();
            ((HashMap<?, ?>) obj).forEach((key, value) ->
                    map.put((String) key, (Integer) value));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean containsKey(String key) {
        return localCopy.containsKey(key);
    }

    @Override
    public Integer get(String key) {
        return localCopy.get(key);
    }

    @Override
    public void put(String key, Integer value) {
        localCopy.put(key, value);
        syncService.synchronizePut(key, value);
    }

    @Override
    public Integer remove(String key) {
        Integer oldValue = localCopy.remove(key);
        syncService.synchronizeRemove(key);
        return oldValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DistributedMap that = (DistributedMap) o;
        return Objects.equals(localCopy, that.localCopy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localCopy);
    }

    @Override
    public String toString() {
        return localCopy.toString();
    }

    @Override
    public void close() {
        syncService.close();
    }
}
