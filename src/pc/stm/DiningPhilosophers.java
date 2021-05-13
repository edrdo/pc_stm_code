package pc.stm;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@SuppressWarnings("serial")
public interface DiningPhilosophers {

  class ForkNotOwnedException extends RuntimeException {} 
  class ForkAlreadyOwnedException extends RuntimeException {}
  class UnexpectedInterruptException extends RuntimeException {}

  Fork createFork();

  Philosopher createPhilosopher(String name, Fork leftFork, Fork rightFork);

  default DinnerState getDinnerState(Fork[] forks, Philosopher[] philosophers) {
    // This is overriden for the STM implementation
    // with memory consistency guarantees.
    return new DinnerState(forks, philosophers);
  }
 
  interface Fork { 
    void pick() throws ForkAlreadyOwnedException;
    void drop() throws ForkNotOwnedException;
    Thread getOwner();
    default boolean isTaken() {
      return getOwner() != null; 
    }
  }

  interface Philosopher extends Runnable {
    String getName();
    void eat();
    void think(); 
    int meals();
    int thoughts();
    void pleaseLeaveTheTable(); 
    boolean shouldLeaveTheTable();
    default void run() {
      System.out.println(getName() + " joined the table.");
      while (!shouldLeaveTheTable()) {
        System.out.println(getName() + " will now eat.");
        eat();
        System.out.println(getName() + " will now think.");
        think(); 
      } 
      System.out.println(getName() + " left the table.");
    }
    default Thread createThread() {
      return new Thread(this, getName());
    }
  }
  


  default void onMain(String[] args) {
    String[] names;
    if (args.length == 0) {
      names = new String[] { 
        "Aristotle", "Hippocrates", "Plato", "Pythagoras", "Socrates"
      };
    } else {
      names = args;
    }
    // Setup
    int N = names.length;
    Fork[] forks = buildArray(new Fork[N], i -> createFork());
    Philosopher[] philosophers = 
      buildArray(new Philosopher[N], 
                 i -> createPhilosopher(names[i], forks[i], forks[(i+1) % N]));
    Thread[] threads = 
      buildArray(new Thread[N], philosophers, p -> p.createThread());

    // Start dinner
    for (Thread t : threads) {  
      t.start(); 
    }

    long closingTime = System.currentTimeMillis() + 10_000;

    do {
      randomDelay();
      System.out.println(getDinnerState(forks, philosophers));
    } while( System.currentTimeMillis() < closingTime);

    for (Philosopher p : philosophers) {
      p.pleaseLeaveTheTable();
    }
    for (Thread t : threads) {
      try {
        t.join();
      } 
      catch(InterruptedException e) {
        throw new UnexpectedInterruptException();
      }
    }
    System.out.println("The dinner is over!");
    for (Philosopher p : philosophers) {
      System.out.printf("%s had %d meals and %d thoughts!%n",
        p.getName(), p.meals(), p.thoughts()); 
    }
    System.out.println(getDinnerState(forks, philosophers));
  }

  final class DinnerState {
    private static class PState {
      final String name;
      final int meals, thoughts;
      PState(String n, int m, int t) {
        name = n; meals = m; thoughts = t;
      }
    }
    private final Thread[] forkOwners;
    private final PState[] pstates;
    public DinnerState(Fork[] forks, Philosopher[] philosophers) {
      int N = forks.length;
      forkOwners = buildArray(new Thread[N], forks, f -> f.getOwner()); 
      pstates = buildArray(new PState[N], philosophers, 
                 p -> new PState(p.getName(), p.meals(), p.thoughts()));
    }
 
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("F: [");
      for (Thread t : forkOwners) 
        sb.append(' ').append(t != null ? t.getName() : "free");
      sb.append(" ]\n");
      sb.append("P: [");
      for (PState ps : pstates) 
        sb.append(' ').append(ps.name)
          .append('/').append(ps.meals)
          .append('/').append(ps.thoughts);
      sb.append(" ]");
      return sb.toString();
    } 
  }

  static void randomDelay() {
    try {
      // Let calling thread sleep for at most one second
      int delay = ThreadLocalRandom.current().nextInt(1_000);
      Thread.sleep(delay);
    } 
    catch(InterruptedException e) {
      throw new UnexpectedInterruptException();
    }
  }


  static <T> T[] buildArray(T[] array, Function<Integer,T> builder) {
    for (int i = 0; i < array.length; i++) {
      array[i] = builder.apply(i);
    }
    return array;
  } 

  static <T,U> T[] buildArray(T[] array, U[] array2, Function<U,T> builder) {
    for (int i = 0; i < array.length; i++) {
      array[i] = builder.apply(array2[i]);
    }
    return array;
  }
}
