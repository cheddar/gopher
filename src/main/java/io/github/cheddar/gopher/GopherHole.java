package io.github.cheddar.gopher;

import java.lang.reflect.InvocationTargetException;

/**
*/
public class GopherHole
{
  private final ClassLoader loader;

  public GopherHole(ClassLoader loader)
  {
    this.loader = loader;
  }

  public void loadAndRun(String mainClass, String[] args)
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
  {
    Thread.currentThread().setContextClassLoader(loader);

    final Class<?> theClass = loader.loadClass(mainClass);
    Object params = args;
    theClass.getMethod("main", String[].class).invoke(null, params);
  }
}
