package exception;

// Checked exception thrown when user input violates validation rules.
public class InvalidInputException extends Exception {

    // Constructs the exception with a descriptive error message.
    public InvalidInputException(String msg) {
        super(msg);
    }
}
