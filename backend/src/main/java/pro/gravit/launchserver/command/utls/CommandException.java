package pro.gravit.launchserver.command.utls;

public final class CommandException extends Exception {
    private static final long serialVersionUID = -6588814993972117772L;


    public CommandException(String message) {
        super(message, null, false, false);
    }


    public CommandException(Throwable exc) {
        super(exc);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
