package letunov.impl.data;

import java.util.List;

public record MicroserviceContractsInfo(
    String microserviceName,
    List<ProvidingContractInfo> providing,
    List<ConsumingContractInfo> consuming
) { }
