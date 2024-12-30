package ru.ricardocraft.client.commands;

public final class CommandException extends Exception {
    private static final long serialVersionUID = -6588814993972117772L;


    public CommandException(String message) {
        super(message, null, false, false);
    }


    @Override
    public String toString() {
        return getMessage();
    }
}
