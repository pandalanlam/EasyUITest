package com.codeborne.selenide.logevents;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.logevents.LogEvent.EventStatus.FAIL;

/**
 * Logs Selenide test steps and notifies all registered LogEventListener about it
 */
public class SelenideLogger {
  protected static ThreadLocal<Map<String, LogEventListener>> listeners = new ThreadLocal<>();

  /**
   * Add a listener (to the current thread).
   * @param name unique name of this listener (per thread). 
   *             Can be used later to remove listener using method {@link #removeListener(String)}
   * @param listener event listener
   */
  public static void addListener(String name, LogEventListener listener) {
    Map<String, LogEventListener> threadListeners = listeners.get();
    if (threadListeners == null) {
      threadListeners = new HashMap<>();
    }

    threadListeners.put(name, listener);
    listeners.set(threadListeners);
  }

  public static SelenideLog beginStep(String source, String methodName, Object... args) {
    return beginStep(source, readableMethodName(methodName) + "(" + readableArguments(args) + ")");
  }

  static String readableMethodName(String methodName) {
    return methodName.replaceAll("([A-Z])", " $1").toLowerCase();
  }

  static String readableArguments(Object... args) {
    return args == null ? "" :
        (args[0] instanceof Object[]) ? arrayToString((Object[]) args[0]) :
            arrayToString(args);
  }

  private static String arrayToString(Object[] args) {
    return args.length == 1 ? String.valueOf(args[0]) : Arrays.toString(args);
  }

  public static SelenideLog beginStep(String source, String subject) {
    return new SelenideLog(source, subject);
  }

  public static void commitStep(SelenideLog log, Throwable error) {
    log.setError(error);
    commitStep(log, FAIL);
  }
  
  public static void commitStep(SelenideLog log, LogEvent.EventStatus status) {
    log.setStatus(status);

    Collection<LogEventListener> listeners = getEventLoggerListeners();
    for (LogEventListener listener : listeners) {
      listener.onEvent(log);
    }
  }

  private static Collection<LogEventListener> getEventLoggerListeners() {
    if (listeners.get() == null) {
      listeners.set(new HashMap<String, LogEventListener>());
    }
    return listeners.get().values();
  }

  /**
   * Remove listener (from the current thread).
   * @param name unique name of listener added by method {@link #addListener(String, LogEventListener)}
   * @param <T> class of listener to be returned
   * @return the listener being removed
   */
  @SuppressWarnings("unchecked")
  public static <T extends LogEventListener> T removeListener(String name) {
    Map<String, LogEventListener> listeners = SelenideLogger.listeners.get();
    return listeners == null ? null : (T) listeners.remove(name);
  }
  
  public static void removeAllListeners() {
    SelenideLogger.listeners.remove();
  }

  /**
   * If listener with given name is bound (added) to the current thread.
   *
   * @param name unique name of listener added by method {@link #addListener(String, LogEventListener)}
   * @return true iff method {@link #addListener(String, LogEventListener)} with 
   *              corresponding name has been called in current thread. 
   */
  public static boolean hasListener(String name) {
    Map<String, LogEventListener> listeners = SelenideLogger.listeners.get();
    return listeners != null && listeners.containsKey(name);
  }
}
