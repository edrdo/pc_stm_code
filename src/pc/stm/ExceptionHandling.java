package pc.stm;

import scala.concurrent.stm.Ref;
import scala.concurrent.stm.japi.STM;
import scala.util.control.ControlThrowable;

//
// Reference: https://nbronson.github.io/scala-stm/exceptions.html
//
@SuppressWarnings("serial")
public class ExceptionHandling {
  public static void main(String... args) {
    new ExceptionHandling();
  }

  ExceptionHandling() {
    example1(); 
    example2();
    example3();
    example4();
  }

  static class StandardException extends Exception {}
  static class ControlFlowException extends Exception implements ControlThrowable {}

  void example1() {
    System.out.println("--- Example 1 (rollback + rethrow) ---");

    Ref.View<Boolean> a = STM.newRef(false);
    Ref.View<Boolean> b = STM.newRef(false);

    try {
      STM.atomic(() -> {
        STM.atomic(() -> {
          a.set(true);
          // exception will roll back update of a
          throw new StandardException(); 
        });
        // not executed: exception is rethrown.
        b.set(true); 
      });
    }
    // exception is caught outside the transaction
    catch (Exception e) { } 
     
    System.out.println("a=" + a.get());
    System.out.println("b=" + b.get());
  }

  
  void example2() {
    System.out.println("--- Example 2 (commit + rethrow) ---");

    Ref.View<Boolean> a = STM.newRef(false);
    Ref.View<Boolean> b = STM.newRef(false);

    try {
      STM.atomic(() -> {
        STM.atomic(() -> {
          a.set(true);
          // exception thrown will commit update of a
          throw new ControlFlowException(); 
        });
      // not executed: exception is rethrown.
      b.set(true); 
      });
    }
    // exception is caught outside the transaction
    catch (Exception e) {} 
     
    System.out.println("a=" + a.get());
    System.out.println("b=" + b.get());
  }


  void example3() {
    System.out.println("--- Example 3 (rollback + catch + commit) ---");

    Ref.View<Boolean> a = STM.newRef(false);
    Ref.View<Boolean> b = STM.newRef(false);

    STM.atomic(() -> {
      try {
        STM.atomic(() -> {
          a.set(true);
          // exception will roll back update of a
          throw new StandardException(); 
        });
      } 
      // exception is caught by parent transaction
      catch (Exception e) {} 

      // update b and commit
      b.set(true); 
    });
     
    System.out.println("a=" + a.get());
    System.out.println("b=" + b.get());
  }

  void example4() {
    System.out.println("--- Example 4 (commit + catch + commit) ---");

    Ref.View<Boolean> a = STM.newRef(false);
    Ref.View<Boolean> b = STM.newRef(false);

    STM.atomic(() -> {
      try {
        STM.atomic(() -> {
          a.set(true);
          // exception thrown will commit update of a
          throw new ControlFlowException(); 
        });
      } 
      // exception is caught by parent transaction
      catch (Exception e) {} 

      // update b and commit
      b.set(true); 
    });
     
    System.out.println("a=" + a.get());
    System.out.println("b=" + b.get());
  }
}
