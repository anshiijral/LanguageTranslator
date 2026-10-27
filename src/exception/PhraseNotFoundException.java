package exception;

// Checked exception thrown when a phrase cannot be found by its ID.
public class PhraseNotFoundException extends Exception {

    // Constructs the exception with a descriptive error message.
    public PhraseNotFoundException(String msg) {
        super(msg);
    }
}
