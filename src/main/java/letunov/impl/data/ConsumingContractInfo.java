package letunov.impl.data;

public record ConsumingContractInfo(
    String name,
    String serviceName,
    DependencyInfo dependency,
    String checksum
) { }
