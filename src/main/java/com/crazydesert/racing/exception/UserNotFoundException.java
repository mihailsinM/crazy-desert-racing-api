package com.crazydesert.racing.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException (String message){
        super(message);
    }
}
