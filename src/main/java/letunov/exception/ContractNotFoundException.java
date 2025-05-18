package letunov.exception;

public class ContractNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "The provider or consumer %s doesn't implement contract interface";

    public ContractNotFoundException(Class<?> providerOrConsumer) {
        super(MESSAGE_TEMPLATE.formatted(providerOrConsumer.getTypeName()));
    }
}
