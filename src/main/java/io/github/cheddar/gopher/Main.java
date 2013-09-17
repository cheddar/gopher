package io.github.cheddar.gopher;

import com.google.common.base.Charsets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 */
public class Main
{

  private static final String DEFAULT_LOCAL_REPO = String.format(
      "%s%s%s",
      System.getProperty("user.home"),
      File.separator,
      ".m2/repository"
  );

  public static void main(String[] args) throws Exception
  {
    Properties props = loadProperties("gopher.properties");

    final String coordinates = props.getProperty("gopher.coordinates");
    if (coordinates == null) {
      System.out.printf("Property[%s] must be set.%n", "gopher.coordinates");
      System.exit(1);
    }

    String mainClass = props.getProperty("gopher.mainClass");
    if (mainClass == null && args.length > 0) {
      mainClass = args[0];
      args = Arrays.copyOfRange(args, 1, args.length);
    }

    if (mainClass == null) {
      System.out.printf(
          "Property[%s] must be set or the main class must be passed in as the first argument.%n", mainClass
      );
      System.exit(2);
    }

    final String localRepository = props.getProperty("gopher.localRepo", DEFAULT_LOCAL_REPO);
    final List<String> remoteRepos = Arrays.asList(
        props.getProperty("gopher.remoteRepos", "http://repo1.maven.org/maven2/").split(",")
    );

    Gopher gopher = new Gopher(Gopher.makeDefaultAetherClient(localRepository, remoteRepos));

    final GopherHole gopherHole = gopher.dig(coordinates);

    try {
      gopherHole.loadAndRun(mainClass, args);
    }
    catch (ClassNotFoundException e) {
      System.out.printf("Could not find main class[%s] in artifact[%s]%n", mainClass, coordinates);
      System.exit(3);
    }
    catch (NoSuchMethodException e) {
      System.out.printf("No main method on mainClass[%s] in artifact[%s]%n", mainClass, coordinates);
      System.exit(4);
    }
  }

  private static Properties loadProperties(String filename) throws IOException
  {
    Properties retVal = null;
    final URL resource = Main.class.getClassLoader().getResource(filename);
    if (resource != null) {
      try (InputStream in = resource.openStream()) {
        retVal = loadProperties(retVal, in);
      }
    }

    final File workingPathFile = new File(filename);
    if (workingPathFile.exists()) {
      try (InputStream in = new FileInputStream(workingPathFile)) {
        retVal = loadProperties(retVal, in);
      }
    }

    final String propertyPathFile = System.getProperty(filename);
    if (propertyPathFile != null && new File(propertyPathFile).exists()) {
      try (InputStream in = new FileInputStream(propertyPathFile)) {
        retVal = loadProperties(retVal, in);
      }
    }

    retVal = new Properties(retVal);
    final Properties sysProps = System.getProperties();
    for (String propertyName : sysProps.stringPropertyNames()) {
      retVal.setProperty(propertyName, sysProps.getProperty(propertyName));
    }

    return retVal;
  }

  private static Properties loadProperties(Properties retVal, InputStream in) throws IOException
  {
    Properties resourceProperties = new Properties(retVal);
    resourceProperties.load(new InputStreamReader(in, Charsets.UTF_8));
    retVal = resourceProperties;
    return retVal;
  }
}
