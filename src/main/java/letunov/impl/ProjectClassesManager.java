package letunov.impl;

import letunov.exception.UnableToGetProjectURLException;
import letunov.impl.data.DependencyInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class ProjectClassesManager {
    private final String m2Repo;

    public ProjectClassesManager(String m2Repo) {
        this.m2Repo = m2Repo;
    }

    public ClassLoader getProjectClassLoader(MavenProject project) {
        log.debug("Build output directory: {}", project.getBuild().getOutputDirectory());
        var outputDirectory = new File(project.getBuild().getOutputDirectory());
        var projectUrl = getUrl(outputDirectory.toPath());

        var urls = getDependenciesURLs(project);
        urls.add(projectUrl);

        return URLClassLoader.newInstance(urls.toArray(URL[]::new), this.getClass().getClassLoader());
    }

    public Reflections getProjectReflections(ClassLoader classLoader) {
        return new Reflections(new ConfigurationBuilder()
            .forPackage("", classLoader));
    }

    public Map<DependencyInfo, ClassLoader> getDependenciesInfoWithClassloaders(MavenProject project) {
        return ((List<Dependency>)project.getDependencies()).stream()
            .collect(toMap(this::createDependencyInfo,
                dependency -> URLClassLoader.newInstance(new URL[]{getDependencyUrl(dependency)},
                    this.getClass().getClassLoader())));
    }

    //    =========================================================================
    //    Implementation
    //    =========================================================================

    private List<URL> getDependenciesURLs(MavenProject project) {
        return ((List<Dependency>)project.getDependencies()).stream()
            .peek(dep -> log.debug("[ProjectClassesManager] The dependency of the project found: {}", dep.toString()))
            .map(this::getDependencyUrl)
            .collect(toCollection(ArrayList::new));
    }

    private URL getDependencyUrl(Dependency dependency) {
        var path = Paths.get(m2Repo, dependency.getGroupId().replace(".", "/"),
            dependency.getArtifactId(), dependency.getVersion(),
            dependency.getArtifactId() + "-" + dependency.getVersion() + ".jar");
        log.debug("Dependency path created: {}", path);
        return getUrl(path);
    }

    private URL getUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new UnableToGetProjectURLException(path.toString(), e);
        }
    }

    private DependencyInfo createDependencyInfo(Dependency dependency) {
        return new DependencyInfo(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }
}
