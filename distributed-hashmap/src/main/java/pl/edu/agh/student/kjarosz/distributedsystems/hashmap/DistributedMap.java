package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

import java.util.HashMap;
import java.util.Objects;

public class DistributedMap implements SimpleStringMap {
    private final HashMap<String, Integer> localCopy = new HashMap<>();
    private final MapSynchronizationService syncService;

    public DistributedMap(String clusterName) throws Exception {
        syncService = new MapSynchronizationService(clusterName);
        syncService.setRemoveListener(localCopy::remove);
        syncService.setPutListener(localCopy::put);
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
        syncService.synchronizeMap(new SynchronizedAction.Put(key, value));
    }

    @Override
    public Integer remove(String key) {
        Integer oldValue = localCopy.remove(key);
        syncService.synchronizeMap(new SynchronizedAction.Remove(key));
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
}
