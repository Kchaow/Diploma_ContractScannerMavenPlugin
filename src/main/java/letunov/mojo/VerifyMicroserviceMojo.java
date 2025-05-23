package letunov.mojo;

import letunov.impl.MicroserviceIntegrityServerClient;
import letunov.impl.RetrieveMicroserviceContractsInfoDelegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "verifyMicroservice")
@Slf4j
public class VerifyMicroserviceMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() {
        var delegate = new RetrieveMicroserviceContractsInfoDelegate();
        var m2Repo = System.getProperty("M2_REPO");
        log.debug("m2Repo received: {}", m2Repo);
        var microserviceContractsInfo = delegate.execute(m2Repo, project);
        log.info("Microservice contracts information retrieved: {}", microserviceContractsInfo);

        var changeGraphId = System.getProperty("changeGraphId");
        log.debug("changeGraphId received: {}", changeGraphId);
        var microserviceIntegrityServerBaseURL = System.getProperty("microserviceIntegrityServerURL");
        var microserviceIntegrityServerClient = new MicroserviceIntegrityServerClient(microserviceIntegrityServerBaseURL);
        microserviceIntegrityServerClient.verifyMicroservice(microserviceContractsInfo, changeGraphId);
    }
}
