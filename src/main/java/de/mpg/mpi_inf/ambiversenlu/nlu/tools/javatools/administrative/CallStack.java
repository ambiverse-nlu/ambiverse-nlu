package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative;

import java.util.Stack;

/**
 This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
 It is licensed under the Creative Commons Attribution License
 (see http://creativecommons.org/licenses/by/3.0) by
 the YAGO-NAGA team (see http://mpii.de/yago-naga).





 This class represents the current position of a program, i.e. the stack of methods that
 have been called together with the line numbers.<BR>
 Example:<BR>
 <PRE>
 public class Blah {
 public m2() {
 System.out.println(new CallStack());   // (1)
 System.out.println(CallStack.here());  // (2)
 }

 public m1() {
 m2();
 }

 public static void main(String[] args) {
 m1();
 }
 }
 -->
 Blah.main(12)->Blah.m1(8)->Blah.m2(2)  // Call Stack at (1)
 Blah.m2(3)                             // Method and line number at (2)
 </PRE>
 */
public class CallStack {

  /** Holds the call stack */
  protected Stack<StackTraceElement> callstack = new Stack<StackTraceElement>();

  /** Constructs a call stack from the current program position (without the constructor call)*/
  public CallStack() {
    try {
      throw new Exception();
    } catch (Exception e) {
      StackTraceElement[] s = e.getStackTrace();
      for (int i = s.length - 1; i != 0; i--) callstack.push(s[i]);
    }
  }

  /** Returns TRUE if the two call stacks have the same elements*/
  public boolean equals(Object o) {
    return (o instanceof CallStack && ((CallStack) o).callstack.equals(callstack));
  }

  /** Returns a nice String for a Stacktraceelement*/
  public static String toString(StackTraceElement e) {
    String cln = e.getClassName();
    if (cln.lastIndexOf('.') != -1) cln = cln.substring(cln.lastIndexOf('.') + 1);
    return (cln + "." + e.getMethodName() + '(' + e.getLineNumber() + ')');
  }

  /** Returns "method(line)->method(line)->..." */
  public String toString() {
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < callstack.size() - 1; i++) {
      s.append(toString(callstack.get(i))).append("->");
    }
    s.append(toString(callstack.get(callstack.size() - 1)));
    return (s.toString());
  }

  /** Gives the calling position as a StackTraceElement */
  public StackTraceElement top() {
    return (callstack.peek());
  }

  /** Gives the calling position */
  public static StackTraceElement here() {
    CallStack p = new CallStack();
    p.callstack.pop();
    return (p.callstack.peek());
  }

  /** Returns the callstack */
  public Stack<StackTraceElement> getCallstack() {
    return callstack;
  }

  /** Pops the top level of this callstack, returns this callstack */
  public CallStack ret() {
    callstack.pop();
    return (this);
  }

  /** Test routine */
  public static void main(String[] args) {
    D.p(new CallStack());
    D.p(here());
  }

}
