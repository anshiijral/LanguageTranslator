package exception;

// Checked exception thrown when an attempt is made to insert or update a phrase that already exists.
public class DuplicatePhraseException extends Exception {

    // Constructs the exception with a descriptive error message.
    public DuplicatePhraseException(String msg) {
        super(msg);
    }
}
