package letunov.exception;

import java.net.MalformedURLException;

public class UnableToGetProjectURLException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Unable to get project URL from output directory path %s";

    public UnableToGetProjectURLException(String outputDirectoryPath, MalformedURLException e) {
        super(MESSAGE_TEMPLATE.formatted(outputDirectoryPath), e);
    }
}
