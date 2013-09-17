package io.github.cheddar.gopher;

import com.google.common.base.Throwables;
import com.google.common.collect.Ordering;
import com.google.common.io.ByteStreams;
import io.tesla.aether.TeslaAether;
import io.tesla.aether.internal.DefaultTeslaAether;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public class Gopher
{
  public static DefaultTeslaAether makeDefaultAetherClient(final String localRepo, List<String> remoteRepos)
  {
    DefaultTeslaAether aetherClient;
    PrintStream systemOut = System.out;
    try {
      System.setOut(new PrintStream(ByteStreams.nullOutputStream()));
      aetherClient = new DefaultTeslaAether(localRepo, remoteRepos);
    }
    finally {
      System.setOut(systemOut);
    }
    return aetherClient;
  }

  private final TeslaAether aether;

  public Gopher(
      TeslaAether aether
  )
  {
    this.aether = aether;
  }

  public GopherHole dig(String coordinates) throws DependencyResolutionException
  {
    return dig(new DefaultArtifact(coordinates));
  }

  public GopherHole dig(Artifact loadArtifact) throws DependencyResolutionException
  {
    final CollectRequest collectRequest = new CollectRequest();
    collectRequest.setRoot(new Dependency(loadArtifact, JavaScopes.RUNTIME));
    DependencyRequest dependencyRequest = new DependencyRequest(
        collectRequest, DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME)
    );

    final List<Artifact> artifacts = aether.resolveArtifacts(dependencyRequest);
    Collections.sort(artifacts, Ordering.usingToString());
    final List<URL> dependencyUrls = new ArrayList<>(artifacts.size());
    for (Artifact artifact : artifacts) {
      try {
        dependencyUrls.add(artifact.getFile().toURI().toURL());
      }
      catch (MalformedURLException e) {
        throw Throwables.propagate(e);
      }
    }

    return new GopherHole(new URLClassLoader(dependencyUrls.toArray(new URL[dependencyUrls.size()]), null));
  }
}
