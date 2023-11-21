package foo.other;

import java.util.Optional;
import java.util.function.BiFunction;

public interface BiLoadingCache<T> extends Cache<T> {

    Optional<T> get(Object firstParam, Object secondParam);

    Optional<T> get(Object firstParam, Object secondParam, BiFunction<Object, Object, Optional<T>> loader);

    Optional<T> load(Object firstParam, Object secondParam, BiFunction<Object, Object, Optional<T>> loader);
}
