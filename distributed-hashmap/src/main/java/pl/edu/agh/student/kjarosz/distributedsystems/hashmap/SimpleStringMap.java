package pl.edu.agh.student.kjarosz.distributedsystems.hashmap;

public interface SimpleStringMap {
    boolean containsKey(String key);

    Integer get(String key);

    void put(String key, Integer value);

    Integer remove(String key);
}
