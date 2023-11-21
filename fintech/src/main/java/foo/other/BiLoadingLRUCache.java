package foo.other;

import foo.exceptions.CacheClassCastException;
import foo.exceptions.ListEmptyException;
import lombok.Getter;
import lombok.Setter;

import java.lang.ref.SoftReference;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;

public class BiLoadingLRUCache<T> implements BiLoadingCache<T> {
    private final Integer size;

    private final Long validTime;

    private final Map<Object, HashMapValue<T>> container;

    private final ConcurrentDoublyLinkedList<LinkedListElement<T>> order;

    private final ReentrantReadWriteLock reentrantReadWriteLock;

    private final Lock readLock;

    private final Lock writeLock;

    private final BiFunction<Object, Object, Optional<T>> loader;

    public BiLoadingLRUCache(Integer size, Long validTime, BiFunction<Object, Object,Optional<T>> loader) {
        this.size = size;
        this.validTime = validTime;
        container = new ConcurrentHashMap<>();
        order = new ConcurrentDoublyLinkedList<>();
        reentrantReadWriteLock = new ReentrantReadWriteLock(true);
        readLock = reentrantReadWriteLock.readLock();
        writeLock = reentrantReadWriteLock.writeLock();
        this.loader = loader;
    }

    public void removeFromCache(Object key) {
        writeLock.lock();
        try {
            HashMapValue<T> value = container.remove(key);
            if (value != null) {
                order.unlink(value.linkedListElement);
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

    @Override
    public Optional<T> get(Object key) {
        readLock.lock();
        try {
            HashMapValue<T> valueMap = container.get(key);
            if (valueMap == null ||
                    valueMap.linkedListElement().getElement().getValue().get() == null) {
                return Optional.empty();
            } else if (Instant.now().isAfter(valueMap.time().plusSeconds(validTime))) {
                removeFromCache(key);
                return Optional.empty();
            }

            replaceAndRise(valueMap.linkedListElement(), valueMap.linkedListElement().getElement().getValue().get());
            return Optional.ofNullable(valueMap.linkedListElement().getElement().getValue().get());
        } catch (ClassCastException ex) {
              throw new CacheClassCastException(ex);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Optional<T> get(Object key, Object param) {
        return get(key, param, loader);
    }

    @Override
    public Optional<T> get(Object key, Object param, BiFunction<Object, Object, Optional<T>> loader) {
        writeLock.lock();
        try {
            HashMapValue<T> valueMap = container.get(key);
            if (valueMap == null || valueMap.linkedListElement().getElement().getValue().get() == null) {
                Optional<T> loaded = load(key, param, loader);
                loaded.ifPresent(value -> put(key, value));
                return loaded;
            } else if (Instant.now().isAfter(valueMap.time().plusSeconds(validTime))) {
                removeFromCache(key);
                Optional<T> loaded = load(key, param, loader);
                loaded.ifPresent(value -> put(key, value));
                return loaded;
            }

            replaceAndRise(valueMap.linkedListElement(), valueMap.linkedListElement().getElement().getValue().get());
            return Optional.ofNullable(valueMap.linkedListElement().getElement().getValue().get());
        } catch (ClassCastException ex) {
            throw new CacheClassCastException(ex);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Optional<T> load(Object firstParam,
                            Object secondParam,
                            BiFunction<Object, Object, Optional<T>> loader) {
        return loader.apply(firstParam, secondParam);
    }

    @Override
    public void put(Object key, T value) {
        writeLock.lock();
        try {
            ConcurrentDoublyLinkedList.Node<LinkedListElement<T>> node = null;

            if (container.containsKey(key)) {
                node = replaceAndRise(container.get(key).linkedListElement(), value);
            } else if (size.equals(order.getSize())) {
                container.remove(order.getTail().getElement().getKey());
                order.getTail();
                node = replaceAndRise(order.getTail(), value);
            } else {
                node = order.addFirst(new LinkedListElement<>(key, value));
            }

            container.put(key, new HashMapValue<>(Instant.now(), node));
        } finally {
            writeLock.unlock();
        }
    }

    private ConcurrentDoublyLinkedList.Node<LinkedListElement<T>> replaceAndRise(ConcurrentDoublyLinkedList.Node<LinkedListElement<T>> node, T value) {
        order.unlink(node);
        node.setElement(new LinkedListElement<T>(node.getElement().getKey(), value));
        order.addFirst(node);
        return node;
    }


    private record HashMapValue<T>(Instant time,
                                BiLoadingLRUCache.ConcurrentDoublyLinkedList.Node<LinkedListElement<T>> linkedListElement) {
    }

    @Getter
    private static class LinkedListElement<T> {
        private final Object key;
        private final SoftReference<T> value;

        private LinkedListElement(Object key, T value) {
            this.value = new SoftReference<>(value);
            this.key = key;
        }
    }
    private static class ConcurrentDoublyLinkedList<E> {
        private volatile Node<E> head;

        private volatile Node<E> tail;

        @Getter
        private volatile Integer size;

        private final ReentrantLock reentrantLock;

        public ConcurrentDoublyLinkedList() {
            head = null;
            tail = null;
            size = 0;
            reentrantLock = new ReentrantLock(true);
        }

        public Node<E> getHead() {
            reentrantLock.lock();

            try {
                if (head.element == null) {
                    throw new ListEmptyException();
                }

                return head;
            } finally {
                reentrantLock.unlock();
            }
        }

        public Node<E> getTail() {
            reentrantLock.lock();

            try {
                if (tail.getElement() == null) {
                    throw new ListEmptyException();
                }

                return tail;
            } finally {
                reentrantLock.unlock();
            }
        }

        public Node<E> addLast(E element) {
            reentrantLock.lock();
            try {
                if (head == null) {
                    return init(element);
                }

                size++;
                Node<E> newNode = new Node<>(element, tail, null);
                tail.next = newNode;
                tail = newNode;
                return newNode;
            } finally {
                reentrantLock.unlock();
            }

        }

        public Node<E> addFirst(E element) {
            reentrantLock.lock();

            try {
                if (head == null) {
                    return init(element);
                }

                size++;
                Node<E> newNode = new Node<>(element, null, head);
                head.prev = newNode;
                head = newNode;

                return newNode;
            } finally {
                reentrantLock.unlock();
            }
        }

        public void addFirst(Node<E> element) {
            reentrantLock.lock();

            try {
                size++;

                if (head == null) {
                    this.head = element;
                    this.tail = element;
                    return;
                }

                head.prev = element;
                element.next = head;
                head = element;

            } finally {
                reentrantLock.unlock();
            }
        }

        public E removeFirst() {
            if (head == null) {
                throw new ListEmptyException();
            }

            reentrantLock.lock();
            try {
                size--;
                E element = head.element;
                head = head.next;

                if (head != null) {
                    head.prev = null;
                } else {
                    tail = null;
                }

                return element;
            } finally {
                reentrantLock.unlock();
            }
        }

        public E removeLast() {
            if (tail == null) {
                throw new ListEmptyException();
            }

            reentrantLock.lock();
            try {
                size--;
                E element = tail.element;
                tail = tail.prev;

                if (tail != null) {
                    tail.next = null;
                } else {
                    head = null;
                }

                return element;
            } finally {
                reentrantLock.unlock();
            }
        }

        public void clearAll() {
            reentrantLock.lock();
            try {
                head = null;
                tail = null;
                size = 0;
            } finally {
                reentrantLock.unlock();
            }
        }

        private Node<E> init(E element) {
            size++;
            head = new Node<>(element, null, null);
            tail = head;
            return head;
        }

        public void unlink(Node<E> node) {
            if (node == null) {
                throw new ListEmptyException();
            }
            reentrantLock.lock();
            try {
                if (node.prev != null) {
                    node.prev.next = node.next;
                } else {
                    head = node.next;
                }

                if (node.next != null) {
                    node.next.prev = node.prev;
                } else {
                    tail = node.prev;
                }

                size--;
                node.prev = null;
                node.next = null;
            } finally {
                reentrantLock.unlock();
            }
        }

        @Getter
        @Setter
        private static class Node<E> {
            private volatile E element;
            private volatile Node<E> next;
            private volatile Node<E> prev;

            private Node(E element, Node<E> prev, Node<E> next) {
                this.element = element;
                this.next = next;
                this.prev = prev;
            }


        }
    }
}