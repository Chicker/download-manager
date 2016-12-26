package ru.chicker.exception;

public class InvalidFileStructureException extends Exception {
    public InvalidFileStructureException(String fileName, Throwable exCause) {
        super(String.format("File %s has invalid format!", fileName), exCause);
    }
}
