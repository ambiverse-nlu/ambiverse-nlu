package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative;

/**
 This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
 It is licensed under the Creative Commons Attribution License
 (see http://creativecommons.org/licenses/by/3.0) by
 the YAGO-NAGA team (see http://mpii.de/yago-naga).





 This class allows to trace where a program hangs. Use as follows: <PRE>
 Before calling a method in your program, call Tracer.signal():
 Tracer.signal("Calling blah");
 In your main method, state
 Tracer.start(number of milliseconds);

 Whenever the tracer does not receive a signal for the given number of milliseconds,
 it writes a warning, such as e.g.
 Calling blah hangs
 When it receives the next signal, it will say
 Hang resolved

 </PRE>
 Use the tracer for debugging purposes only.
 */
public class Tracer {

  /** The thread itself*/
  protected static class TracerThread extends Thread {

    /** TRUE i fthe thread should terminate*/
    protected boolean stop = false;

    /** Time in millseconds we received the last signal*/
    protected long lastSignalTime;

    /** Content of the last signal*/
    protected Object[] lastSignal;

    /** Time we give between two signals*/
    protected final long delay;

    /** Did we already print the warning?*/
    boolean announced = true;

    /** Receiving a signal*/
    public void signal(Object... s) {
      if (announced && lastSignalTime != 0) {
        StringBuilder b = new StringBuilder();
        for (Object o : s) b.append(o).append(' ');
        b.append("resolved hang after ").append(System.currentTimeMillis() - lastSignalTime).append(" ms");
        Announce.message(b);
      }
      lastSignal = s;
      lastSignalTime = System.currentTimeMillis();
      announced = false;
    }

    /** Constructor*/
    public TracerThread(long delay) {
      this.delay = delay;
      setDaemon(true);
      setName("Tracer");
    }

    @Override public void run() {
      while (!stop) {
        if (!announced && System.currentTimeMillis() - lastSignalTime > delay) {
          StringBuilder b = new StringBuilder();
          for (Object o : lastSignal) b.append(o).append(' ');
          Announce.message(b.append("hangs"));
          announced = true;
        }
      }
    }
  }

  /** Internal trace thread*/
  protected static TracerThread tracer;

  /** Start the tracer, accept millisDelay milliseconds between two signals before issuing a warning. Returns TRUE so that it can be used in an assert statement*/
  public static boolean start(long millisDelay) {
    tracer = new TracerThread(millisDelay);
    tracer.start();
    return (true);
  }

  /** Stop the tracer*/
  public static void stop() {
    if (tracer != null) {
      tracer.stop = true;
      tracer = null;
    }
  }

  /** Send a signal to the tracer. Always returns TRUE so that it can be used in assert*/
  public static boolean signal(Object... s) {
    if (tracer != null) tracer.signal(s);
    return (true);
  }

  // ------------------ Test Purposes ------------------

  /** Sleeps for 2 seconds. Tracer will not say anything*/
  public static void test1() {
    try {
      Thread.sleep(2000);
    } catch (Exception e) {
    }
  }

  /** Sleeps for 5 seconds. Tracer will write a warning*/
  public static void test2() {
    try {
      Thread.sleep(5000);
    } catch (Exception e) {
    }
  }

  /** Calls test1 and test2 in a loop. Tracer will complain
   * whenever some call takes longer than 3 seconds*/
  public static void main(String[] args) {
    Tracer.start(3000);
    while (true) {
      Tracer.signal("Test1");
      test1();
      Tracer.signal("Test2");
      test2();
    }
  }
}
