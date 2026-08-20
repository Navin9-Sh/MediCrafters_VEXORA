package com.mediwise.common.exception;

public class SlotConflictException extends RuntimeException {
    public SlotConflictException() {
        super("This time slot is no longer available. Please select another slot.");
    }
    public SlotConflictException(String message) {
        super(message);
    }
}
