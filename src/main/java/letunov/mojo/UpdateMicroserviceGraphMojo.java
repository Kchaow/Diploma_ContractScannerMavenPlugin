package letunov.mojo;

import letunov.impl.MicroserviceIntegrityServerClient;
import letunov.impl.MicroserviceContractsScanner;
import letunov.impl.ProjectClassesManager;
import letunov.impl.RetrieveMicroserviceContractsInfoDelegate;
import letunov.impl.data.MicroserviceContractsInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "updateMicroserviceGraph")
@Slf4j
public class UpdateMicroserviceGraphMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() {
        var delegate = new RetrieveMicroserviceContractsInfoDelegate();
        var m2Repo = System.getProperty("M2_REPO");
        var microserviceContractsInfo = delegate.execute(m2Repo, project);
        log.info("Microservice contracts information retrieved: {}", microserviceContractsInfo);

        var microserviceIntegrityServerBaseURL = System.getProperty("microserviceIntegrityServerURL");
        var microserviceIntegrityServerClient = new MicroserviceIntegrityServerClient(microserviceIntegrityServerBaseURL);
        microserviceIntegrityServerClient.updateMicroserviceGraph(microserviceContractsInfo);
        log.info("Graph updated successfully!");
    }
}
