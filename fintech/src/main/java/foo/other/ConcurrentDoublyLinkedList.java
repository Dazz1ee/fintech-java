package foo.other;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentDoublyLinkedList<E> {

    private Node<E> head;

    private Node<E> tail;

    @Getter
    private AtomicInteger size;

    private ReentrantLock reentrantLock;

    public ConcurrentDoublyLinkedList() {
        head = null;
        tail = null;
        size = new AtomicInteger(0);
        reentrantLock = new ReentrantLock(true);
    }

    public E getHead() {
        return head.element;
    }

    public E getTail() {
        return tail.element;
    }

    public Node<E> addLast(E element) {
        reentrantLock.lock();
        try {
            if (head == null) {
                return init(element);
            }

            size.incrementAndGet();
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

            size.incrementAndGet();
            Node<E> newNode = new Node<>(element, null, head);
            head.prev = newNode;
            head = newNode;

            return newNode;
        } finally {
            reentrantLock.unlock();
        }
    }

    public E removeFirst() {
        if (head == null) {
            return null;
        }

        reentrantLock.lock();
        try {
            size.decrementAndGet();
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
            return null;
        }

        reentrantLock.lock();
        try {
            size.decrementAndGet();
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

    public E removeInnerNode(Node<E> node) {
        if (node == null) {
            return null;
        }
        reentrantLock.lock();
        try {
            if (node.prev != null) {
                node.prev.next = node.next;
            } else  {
                head = node.next;
            }

            if (node.next != null) {
                node.next.prev = node.prev;
            } else  {
                tail = node.prev;
            }
            size.decrementAndGet();


            return node.element;
        } finally {
            reentrantLock.unlock();
        }
    }

    public void clearAll() {
        reentrantLock.lock();
        try {
            head = null;
            tail = null;
            size.set(0);
        } finally {
            reentrantLock.unlock();
        }
    }

    private Node<E> init(E element) {
        size.incrementAndGet();
        head = new Node<>(element, null, null);
        tail = head;
        return head;
    }

    public static class Node<E> {
        @Setter
        @Getter
        private E element;
        private Node<E> next;
        private Node<E> prev;

        private Node(E element, Node<E> prev, Node<E> next) {
            this.element = element;
            this.next = next;
            this.prev = prev;
        }


    }
}
