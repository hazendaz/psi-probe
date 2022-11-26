/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package psiprobe.tools.logging.logbackaccess;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.MethodUtils;

import psiprobe.tools.logging.DefaultAccessor;

/**
 * Wraps a Logback logger factory from a given web application class loader.
 *
 * <p>
 * All Logback classes are loaded via the given class loader and not via psi-probe's own class
 * loader. For this reasons, all methods on Logback objects are invoked via reflection.
 * </p>
 * <p>
 * This way, we can even handle different versions of Logback embedded in different WARs.
 * </p>
 */
public class LogbackAccessFactoryAccessor extends DefaultAccessor {

  /**
   * Attempts to initialize a Logback logger factory via the given class loader.
   *
   * @param cl the ClassLoader to use when fetching the factory
   * @throws ClassNotFoundException the class not found exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   */
  public LogbackAccessFactoryAccessor(ClassLoader cl)
      throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
    // Check if Logback Access exists
    Class<?> loggerFactoryClass = cl.loadClass("ch.qos.logback.access.spi.AccessContext");
    if (loggerFactoryClass == null) {
      logger.info("No logback access found");
    } else {
      setTarget(loggerFactoryClass);
    }
  }

  /**
   * Returns the Logback root logger.
   *
   * @return the root logger
   */
  public LogbackAccessLoggerAccessor getRootLogger() {
    // Logback has no dedicated getRootLogger() method, so we simply access the root logger
    // by its well-defined name.
    return getLogger("ROOT");
  }

  /**
   * Returns the Logback logger with a given name.
   *
   * @param name the name
   * @return the Logger with the given name
   */
  public LogbackAccessLoggerAccessor getLogger(String name) {
    try {
      Class<? extends Object> clazz = getTarget().getClass();
      Method getLogger = MethodUtils.getAccessibleMethod(clazz, "getLogger", String.class);

      Object logger = getLogger.invoke(getTarget(), name);
      if (logger == null) {
        throw new NullPointerException(getTarget() + ".getLogger(\"" + name + "\") returned null");
      }
      LogbackAccessLoggerAccessor accessor = new LogbackAccessLoggerAccessor();
      accessor.setTarget(logger);
      accessor.setApplication(getApplication());
      return accessor;

    } catch (Exception e) {
      logger.error("{}.getLogger('{}') failed", getTarget(), name, e);
    }
    return null;
  }

  /**
   * Returns a list of wrappers for all Logback appenders that have an associated logger.
   *
   * @return a list of {@link LogbackAccessAppenderAccessor}s representing all appenders that are in
   *         use
   */
  public List<LogbackAccessAppenderAccessor> getAppenders() {
    List<LogbackAccessAppenderAccessor> appenders = new ArrayList<>();
    try {
      Class<? extends Object> clazz = getTarget().getClass();
      Method getAppender = MethodUtils.getAccessibleMethod(clazz, "getAppender", String.class);

      appenders.add((LogbackAccessAppenderAccessor) getAppender.invoke(getTarget(), "ACCESS-LOG"));
    } catch (Exception e) {
      logger.error("{}.getAppender() failed", getTarget(), e);
    }
    return appenders;
  }

}
