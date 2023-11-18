package foo.other;

import foo.models.Weather;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.lang.ref.SoftReference;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

@Component
@ConfigurationProperties(prefix = "cache.course")
public class WeatherCache {
    @Setter
    private Integer size;

    @Setter
    private Long validTime;

    private final Map<Object, HashMapValue> container;

    private final ConcurrentDoublyLinkedList<KeyAndWeather> order;

    private final ReentrantReadWriteLock reentrantReadWriteLock;

    private final Lock readLock;

    private final Lock writeLock;

    public WeatherCache() {
        container = new ConcurrentHashMap<>();
        order = new ConcurrentDoublyLinkedList<>();
        reentrantReadWriteLock = new ReentrantReadWriteLock(true);
        readLock = reentrantReadWriteLock.readLock();
        writeLock = reentrantReadWriteLock.writeLock();
    }

    public Optional<Weather> getWeatherFromCache(Object key) {
        readLock.lock();

        try {
            HashMapValue weatherNode = container.get(key);
            if (weatherNode == null || weatherNode.keyAndWeather.getElement().weather.get() == null) {
                return Optional.empty();
            } else if (Instant.now().isAfter(weatherNode.time.plusSeconds(validTime))) {
                removeFromCache(key);
                return Optional.empty();
            }

            order.removeInnerNode(weatherNode.keyAndWeather);
            order.addFirst(weatherNode.keyAndWeather.getElement());
            return Optional.ofNullable(weatherNode.keyAndWeather.getElement().weather.get());
        } finally {
            readLock.unlock();
        }
    }

    public void addWeatherToCache(Weather weather, Function<Weather, Object> keyExtractor) {
        writeLock.lock();
        try {
            if (container.containsKey(keyExtractor.apply(weather))) {
                order.removeInnerNode(container.get(keyExtractor.apply(weather)).keyAndWeather);
            } else if (size == order.getSize().get()) {
                KeyAndWeather removed = order.removeLast();
                container.remove(removed.key);
            }

            ConcurrentDoublyLinkedList.Node<KeyAndWeather> weatherNode = order.addFirst(new KeyAndWeather(weather, keyExtractor.apply(weather)));

            container.put(keyExtractor.apply(weather), new HashMapValue(Instant.now(), weatherNode));
        } finally {
            writeLock.unlock();
        }
    }

    public void removeFromCache(Object key) {
        writeLock.lock();
        try {
            HashMapValue weather = container.remove(key);
            if (weather != null) {
                order.removeInnerNode(weather.keyAndWeather);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void removeFromCache(Weather weather1) {
        writeLock.lock();
        try {
            HashMapValue weather = container.remove(weather1.getCity().getName());
            if (weather != null) {
                order.removeInnerNode(weather.keyAndWeather);
            }

            weather = container.remove(weather1.getCity().getId());
            if (weather != null) {
                order.removeInnerNode(weather.keyAndWeather);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void removeAll() {
        writeLock.lock();
        try {
            container.clear();
            order.clearAll();
        } finally {
            writeLock.unlock();
        }
    }

    public Integer getSize() {
        readLock.lock();
        try {
            return container.size();
        } finally {
            readLock.unlock();
        }
    }

    private static class HashMapValue {
        private final Instant time;
        private final ConcurrentDoublyLinkedList.Node<KeyAndWeather> keyAndWeather;

        private HashMapValue(Instant time, ConcurrentDoublyLinkedList.Node<KeyAndWeather> keyAndWeather) {
            this.time = time;
            this.keyAndWeather = keyAndWeather;
        }
    }

    private static class KeyAndWeather {
        private final Object key;
        private final SoftReference<Weather> weather;

        private KeyAndWeather(Weather weather, Object key) {
            this.weather = new SoftReference<>(weather);
            this.key = key;
        }
    }

}
