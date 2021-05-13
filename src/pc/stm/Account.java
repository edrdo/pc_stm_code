package pc.stm;

import scala.concurrent.stm.Ref;
import scala.concurrent.stm.japi.STM;

public class Account {

  private final String id;
  private final Ref.View<Integer> balance;

  public Account(String id, int initialValue) {
    if (initialValue < 0) 
      throw new IllegalArgumentException();
    this.id = id;
    balance = STM.newRef(initialValue); 
  }

  public String id() {
    return id;
  }

  public int balance() {
    return balance.get();
  }

  @Override
  public String toString() {
    return id + " " + balance.get();
  }

  public void deposit(int value) {
    STM.atomic(() -> {
      if (value <= 0) 
        throw new IllegalArgumentException(); // abort
      STM.increment(balance, value);
    });
  }

  public void withdraw(int value) {
    STM.atomic(() -> {
      if (value > balance.get()) 
        throw new IllegalArgumentException(); // abort
      STM.increment(balance, - value);
    });
  }


  static boolean transfer(Account a, Account b, int value) {
    try {
      STM.atomic(() -> {
        b.deposit(value);
        // System.out.println(b); 
        // Intermediate state will never be commited
        // if transaction fails in withdraw
        a.withdraw(value);
      });
      return true;
    } 
    catch (Exception e) {
      return false;
    }
  }
}
