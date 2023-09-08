package dev.darkknights;

import static dev.darkknights.Bots.getTime;

public class WrongGuildException extends Exception {

    public WrongGuildException(String message) {
        System.err.println("WrongGuildException: " + message + getTime());
    }

    public void setLocation(String location) {
        System.err.println("WrongGuildException in " + location + getTime());
    }
}
