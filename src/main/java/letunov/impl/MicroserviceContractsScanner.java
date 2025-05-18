package letunov.impl;

import letunov.contract.Contract;
import letunov.contract.ContractConsumer;
import letunov.contract.ContractProvider;
import letunov.exception.ContractNotFoundException;
import letunov.exception.DependencyNotFoundException;
import letunov.impl.data.ConsumingContractInfo;
import letunov.impl.data.DependencyInfo;
import letunov.impl.data.ProvidingContractInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.*;
import java.util.Map.Entry;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.reflections.scanners.Scanners.TypesAnnotated;

@RequiredArgsConstructor
@Slf4j
public class MicroserviceContractsScanner {
    private final Reflections reflections;
    private final ClassLoader projectClassLoader;
    private final Map<DependencyInfo, ClassLoader> dependencies;

    public List<ProvidingContractInfo> getProvidingContractsInfo() {
        var contracts = getProvidingContracts();
        log.info("[MicroserviceProvidingContractsScanner] Providing contracts found: {}", contracts);

        return contracts.stream()
            .map(contract -> new ProvidingContractInfo(contract.getTypeName(),
                getDependencyInfoOfContract(contract),
                ChecksumUtil.getContractChecksum(contract)))
                .toList();
    }

    public List<ConsumingContractInfo> getConsumingContractsInfo() {
        var contracts = getConsumingContracts();
        log.info("[MicroserviceProvidingContractsScanner] Consuming contract interfaces found: {}", contracts);

        return contracts.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                .map(contract -> new ConsumingContractInfo(contract.getTypeName(), entry.getKey(),
                    getDependencyInfoOfContract(contract),
                    ChecksumUtil.getContractChecksum(contract))))
            .toList();
    }

    //    =========================================================================
    //    Implementation
    //    =========================================================================

    private Set<Class<?>> getProvidingContracts() {
        var controllers = reflections.get(TypesAnnotated.with(ContractProvider.class.getTypeName()).asClass(projectClassLoader));
        log.info("[MicroserviceProvidingContractsScanner] Controllers providing contracts found: {}", controllers);

        Set<Class<?>> contracts = new HashSet<>();
        controllers.forEach(controller -> {
            var interfaces = controller.getInterfaces();
            log.debug("[MicroserviceProvidingContractsScanner] Found interfaces implemented by {} controller: {}", controller.getTypeName(), interfaces);

            var contractsOfController = stream(interfaces)
                .filter(this::hasContractSuperInterface)
                .toList();

            if (contractsOfController.isEmpty()) {
                throw new ContractNotFoundException(controller);
            }

            contracts.addAll(contractsOfController);
        });
        return contracts;
    }

    private Map<String, Set<Class<?>>> getConsumingContracts() {
        var consumers = reflections.get(TypesAnnotated.with(ContractConsumer.class.getTypeName()).asClass(projectClassLoader));
        log.info("[MicroserviceProvidingContractsScanner] Consumers found: {}", consumers);

        Map<String, Set<Class<?>>> contractsWithServiceNames = new HashMap<>();
        consumers.forEach(consumer -> {
            var serviceName = consumer.getAnnotation(ContractConsumer.class).serviceName();
            log.debug("[MicroserviceProvidingContractsScanner] The consumer {} consuming a contract provided by {}", consumer.getTypeName(), serviceName);
            var interfaces = consumer.getInterfaces();
            log.debug("[MicroserviceProvidingContractsScanner] Found interfaces implemented by {} consumer: {}", consumer.getTypeName(), interfaces);

            var contractsOfConsumer = stream(interfaces)
                .filter(this::hasContractSuperInterface)
                .collect(toSet());

            if (contractsOfConsumer.isEmpty()) {
                throw new ContractNotFoundException(consumer);
            }

            contractsWithServiceNames.merge(serviceName, contractsOfConsumer, (v1, v2) -> {
                v1.addAll(v2);
                return v1;
            });
        });
        return contractsWithServiceNames;
    }

    private DependencyInfo getDependencyInfoOfContract(Class<?> contractInterface) {
        return dependencies.entrySet().stream()
            .filter(entry -> {
                try {
                    entry.getValue().loadClass(contractInterface.getTypeName());
                    return true;
                } catch (ClassNotFoundException e) {
                    return false;
                }
            })
            .findFirst()
            .map(Entry::getKey)
            .orElseThrow(() -> new DependencyNotFoundException(contractInterface));
    }

    private boolean hasContractSuperInterface(Class<?> _interface) {

        return stream(_interface.getInterfaces())
            .anyMatch(superInterface ->
                superInterface.getTypeName().equals(Contract.class.getTypeName()));
    }
}
