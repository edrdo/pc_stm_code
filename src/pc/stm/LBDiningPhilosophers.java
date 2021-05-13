package pc.stm;

import static pc.stm.DiningPhilosophers.*;

public class LBDiningPhilosophers implements DiningPhilosophers {

  public static void main(String[] args) {
    new LBDiningPhilosophers().onMain(args);
  }

  @Override
  public Fork createFork() {
    return new LBFork();
  }

  @Override
  public Philosopher createPhilosopher(String name, Fork leftFork, Fork rightFork) {
    return new LBPhilosopher(name, (LBFork) leftFork, (LBFork) rightFork);
  }
 
  private static class LBFork implements Fork {
    private Thread owner = null;
    public synchronized void pick() {
      try {
        Thread t = Thread.currentThread();
        if (owner == t) 
          throw new ForkAlreadyOwnedException();
        while (owner != null) 
          wait(); 
        owner = t;
      }
      catch(InterruptedException e) {
        throw new UnexpectedInterruptException();
      }
    }
 
    @Override   
    public synchronized Thread getOwner() {
      return owner;
    }
    
    @Override
    public synchronized void drop() {
      if (owner != Thread.currentThread()) 
        throw new ForkNotOwnedException();
      owner = null;
      notifyAll();
    }
  }

  static class LBPhilosopher implements Philosopher  {
    private final String name;
    private final LBFork leftFork;
    private final LBFork rightFork;
    private int meals = 0;
    private int thoughts = 0;
    private boolean leaveTheTable = false;
    
    LBPhilosopher(String name, LBFork leftFork, LBFork rightFork) {
      this.name = name;
      this.leftFork = leftFork;
      this.rightFork = rightFork;
    }
    
    @Override
    public String getName() {
      return name;
    }

    @Override
    public synchronized int meals() {
      return meals;
    }

    @Override
    public synchronized int thoughts() {
      return thoughts;
    }
    
    @Override
    public void eat() {
      leftFork.pick();
      rightFork.pick();
      randomDelay();
      synchronized(this) { meals++; }
      leftFork.drop();
      rightFork.drop();      
    }

    @Override
    public void think() {
      randomDelay();
      synchronized(this) { thoughts++; }
    }

    @Override
    public synchronized void pleaseLeaveTheTable() {
      leaveTheTable = true;
    }
    
    @Override
    public synchronized boolean shouldLeaveTheTable() {
      return leaveTheTable;
    }
  }
}
