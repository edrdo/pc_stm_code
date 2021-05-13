package pc.stm;

import scala.concurrent.stm.japi.STM;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestAccount {

  public static void main(String[] args) throws Exception {
    concurrentTest();
    sequentialTest();
  }

  private static void concurrentTest() throws Exception {
    Account a = new Account("a", 100);
    Account b = new Account("b", 100);

    System.out.println("== INITIAL STATE ==");
    System.out.println(a);
    System.out.println(b);
    
    ExecutorService exec = Executors.newFixedThreadPool(2);

    Callable<Boolean> t1 = () -> {
      return Account.transfer(a, b, 100);
    };

    Callable<Integer> t2 = () -> {
      return STM.atomic(() -> a.balance() + b.balance());
    };
    
    Future<Boolean> r1 = exec.submit(t1);
    Future<Integer> r2 = exec.submit(t2);
    System.out.printf("Result for t1: %s%n", r1.get());
    System.out.printf("Result for t2: %s%n", r2.get());

    System.out.println("== FINAL STATE ==");
    System.out.println(a);
    System.out.println(b);

    exec.shutdown();
  }

  private static void sequentialTest() {
    Account a = new Account("a", 100);
    Account b = new Account("b", 100);
    
    System.out.println("== INITIALLY ==");
    System.out.println(a);
    System.out.println(b);
    
    // Transfer ok
    Account.transfer(a, b, 100);
    System.out.println("== AFTER TRANSFER 1 OF 100 FROM a to b ==");
    System.out.println(a);
    System.out.println(b);

    // Transfer will not be done
    Account.transfer(a, b, 100);

    System.out.println("== AFTER TRANSFER 2 OF 100 FROM a to b ==");
    System.out.println(a);
    System.out.println(b);
  }

}
