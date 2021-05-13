package pc.stm;

import scala.concurrent.stm.Ref;
import scala.concurrent.stm.japi.STM;

import static pc.stm.DiningPhilosophers.*;

public class STMDiningPhilosophers implements DiningPhilosophers {

  public static void main(String[] args) {
    new STMDiningPhilosophers().onMain(args);
  }

  @Override
  public Fork createFork() {
    return new STMFork();
  }

  @Override
  public DinnerState
  getDinnerState(Fork[] forks, Philosopher[] philosophers) {
     return STM.atomic( () -> new DinnerState(forks, philosophers));
  }

  @Override
  public Philosopher createPhilosopher(String name, Fork leftFork, Fork rightFork) {
    return new STMPhilosopher(name, (STMFork) leftFork, (STMFork) rightFork);
  }

  private static class STMFork implements Fork {
    private final Ref.View<Thread> owner = STM.newRef(null);

    @Override
    public void pick() {
      STM.atomic(() -> {
        Thread t = Thread.currentThread();
        if (owner.get() == t) 
          throw new ForkAlreadyOwnedException();
        while (owner.get() != null) 
          STM.retry(); 
        owner.set(t);
      });
    }
    
    @Override
    public Thread getOwner() {
      return owner.get();
    }
    
    @Override
    public void drop() {
      STM.atomic(() -> {
        if (owner.get() != Thread.currentThread()) 
          throw new ForkNotOwnedException();
        owner.set(null);
      });
    }
  }

  private static class STMPhilosopher implements Philosopher  {
    private final String name;
    private final STMFork leftFork; 
    private final STMFork rightFork; 
    private Ref.View<Integer> meals = STM.newRef(0);
    private Ref.View<Integer> thoughts = STM.newRef(0);
    private Ref.View<Boolean> leaveTheTable = STM.newRef(false);
    
    STMPhilosopher(String name, STMFork leftFork, STMFork rightFork) {
      this.name = name;
      this.leftFork = leftFork;
      this.rightFork = rightFork;
    }
    
    @Override
    public String getName() {
      return name;
    }

    @Override
    public int meals() {
      return meals.get();
    }

    @Override
    public int thoughts() {
      return thoughts.get();
    }
    
    @Override
    public void eat() {
      STM.atomic(() -> {
        leftFork.pick();
        rightFork.pick();
      });
      randomDelay();
      STM.atomic(() -> {
        STM.increment(meals, 1);
        leftFork.drop();
        rightFork.drop();      
      });
    }

    @Override
    public void think() {
      randomDelay();
      STM.increment(thoughts, 1);
    }

    @Override
    public void pleaseLeaveTheTable() {
      leaveTheTable.set(true);
    }
    
    @Override
    public boolean shouldLeaveTheTable() {
      return leaveTheTable.get();
    }
  }
}
