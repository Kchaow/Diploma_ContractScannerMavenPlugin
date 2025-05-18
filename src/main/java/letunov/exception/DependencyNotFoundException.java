package letunov.exception;

public class DependencyNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Not found a dependency of the contract interface %s";

    public DependencyNotFoundException(Class<?> contractInterface) {
        super(MESSAGE_TEMPLATE.formatted(contractInterface.getTypeName()));
    }
}
