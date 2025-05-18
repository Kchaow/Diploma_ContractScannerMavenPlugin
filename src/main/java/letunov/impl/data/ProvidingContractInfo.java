package letunov.impl.data;

public record ProvidingContractInfo(
    String name,
    DependencyInfo dependency,
    String checksum
) { }
