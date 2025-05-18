package letunov.exception;

public class UnableToMakeRequestException extends RuntimeException {
    public UnableToMakeRequestException(String url, Exception e) {
        super("Unable to make request url %s".formatted(url), e);
    }

    public UnableToMakeRequestException(String url, int code) {
        super("Unable to make request url %s, code %s".formatted(url, code));
    }
}
