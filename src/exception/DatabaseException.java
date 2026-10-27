package exception;

// Unchecked exception used to wrap low-level database and SQL errors.
public class DatabaseException extends RuntimeException {

    // Constructs the exception with a message and the underlying cause.
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
