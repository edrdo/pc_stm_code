package pc.stm;

import scala.concurrent.stm.Ref;
import scala.concurrent.stm.japi.STM;

public class BlockingQueue<E> {

  private static class Node<T> {
    T value;
    Ref.View<Node<T>> next = STM.newRef(null);
  }

  private final Ref.View<Integer> size; 
  private final Ref.View<Node<E>> head;
  private final Ref.View<Node<E>> tail;

  public BlockingQueue() {
    size = STM.newRef(0);
    head = STM.newRef(null);
    tail = STM.newRef(null);
  }

  public int size() {
    return size.get();
  }

  public void add(E elem) {
    STM.atomic(() -> {
      Node<E> prevTail = tail.get();
      Node<E> newNode = new Node<>();
      newNode.value = elem;
      tail.set(newNode);
      if (prevTail != null)
        prevTail.next.set(newNode);
      else 
        head.set(newNode);
      STM.increment(size, 1);
    });
  }

  public E remove() {
    return STM.atomic(() -> {
      if (size.get() == 0) 
        STM.retry();
      Node<E> node = head.get();
      Node<E> nextHead = node.next.get();
      E value = node.value;
      head.set(nextHead);
      if (nextHead != null)
        node.next.set(null);
      else 
        tail.set(null);
      STM.increment(size, -1);
      return value;
    });
  }

  public void dump() {
    STM.atomic(() -> {
      StringBuilder sb = new StringBuilder("[");
      Node<E> node = head.get();
      while (node != null) {
        sb.append(' ').append(node.value);
        node = node.next.get();
      }
      sb.append(" ]");
      STM.afterCommit(() -> System.out.println(sb.toString()));
    });
  }
  
  public void buggyDump() {
    STM.atomic(() -> {
      // I/O actions should not be performed within a transaction
      System.out.print("[");
      Node<E> node = head.get();
      while (node != null) {
        System.out.print(' ');
        System.out.print(node.value);
        node = node.next.get();
      }
      System.out.println(" ]");
    });
  }
  
}
