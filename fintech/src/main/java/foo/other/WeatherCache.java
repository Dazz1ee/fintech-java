package foo.other;

import foo.models.Weather;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.com.google.common.base.Function;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
@ConfigurationProperties(prefix = "cache.course")
public class WeatherCache {
    @Setter
    private Integer size;

    @Setter
    private Long validTime;

    private final Map<Object, ValidPair> container;

    private final ConcurrentDoublyLinkedList<Weather> order;

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
            ValidPair weatherNode = container.get(key);
            if (weatherNode == null) {
                return Optional.empty();
            } else if (Instant.now().isAfter(weatherNode.getTime().plusSeconds(validTime))) {
                removeFromCache(key);
                return Optional.empty();
            }

            order.removeInnerNode(weatherNode.weatherNode);
            order.addFirst(weatherNode.weatherNode.getElement());
            return Optional.of(weatherNode.getWeatherNode().getElement());
        } finally {
            readLock.unlock();
        }
    }

    public void addWeatherToCache(Weather weather, Function<Weather, Object> keyExtractor) {
        writeLock.lock();
        try {
            if (container.containsKey(keyExtractor.apply(weather))) {
                order.removeInnerNode(container.get(keyExtractor.apply(weather)).weatherNode);
            } else if (size == order.getSize().get()) {
                Weather removed = order.removeLast();
                container.remove(removed.getCity().getId());
                container.remove(removed.getCity().getName());
            }

            ConcurrentDoublyLinkedList.Node<Weather> weatherNode = order.addFirst(weather);
            container.put(keyExtractor.apply(weather), new ValidPair(Instant.now(), weatherNode));
        } finally {
            writeLock.unlock();
        }
    }

    public void removeFromCache(Object key) {
        writeLock.lock();
        try {
            ValidPair weather = container.remove(key);
            if (weather != null) {
                order.removeInnerNode(weather.getWeatherNode());
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void removeFromCache(Weather weather1) {
        writeLock.lock();
        try {
            ValidPair weather = container.remove(weather1.getCity().getName());
            if (weather != null) {
                order.removeInnerNode(weather.getWeatherNode());
            }

            weather = container.remove(weather1.getCity().getId());
            if (weather != null) {
                order.removeInnerNode(weather.getWeatherNode());
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

    @Getter
    private static class ValidPair {
        @Setter
        private volatile Instant time;
        private final ConcurrentDoublyLinkedList.Node<Weather> weatherNode;

        private ValidPair(Instant time, ConcurrentDoublyLinkedList.Node<Weather> weatherNode) {
            this.time = time;
            this.weatherNode = weatherNode;
        }
    }

}
