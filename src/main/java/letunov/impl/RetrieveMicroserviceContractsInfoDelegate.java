package letunov.impl;

import letunov.impl.data.MicroserviceContractsInfo;
import org.apache.maven.project.MavenProject;

public class RetrieveMicroserviceContractsInfoDelegate {
    public MicroserviceContractsInfo execute(String m2Repo, MavenProject project) {
        var classLoadingHelper = new ProjectClassesManager(m2Repo);
        var projectClassloader = classLoadingHelper.getProjectClassLoader(project);
        var reflections = classLoadingHelper.getProjectReflections(projectClassloader);
        var microserviceProvidingContractsScanner = new MicroserviceContractsScanner(reflections, projectClassloader, classLoadingHelper.getDependenciesInfoWithClassloaders(project));

        var providingContractsInfo = microserviceProvidingContractsScanner.getProvidingContractsInfo();
        var consumingContractsInfo = microserviceProvidingContractsScanner.getConsumingContractsInfo();

        return new MicroserviceContractsInfo(project.getName(), providingContractsInfo, consumingContractsInfo);
    }
}
