package foo.other;

import java.util.Optional;

public interface Cache<T> {
    Optional<T> get(Object key);
    void put(Object key, T value);
}