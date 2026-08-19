package com.crazydesert.racing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return errors;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public Map<String, String> handleUserNotFoundException(UserNotFoundException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RaceCarNotFoundException.class)
    public Map<String,String> handleRaceCarNotFoundException(RaceCarNotFoundException ex){

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RaceNotFoundException.class)
    public Map<String, String> handleRaceNotFoundException(RaceNotFoundException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(RaceCarOwnershipException.class)
    public Map<String, String> handleRaceCarOwnershipException(
            RaceCarOwnershipException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LicenseNotVerifiedException.class)
    public Map<String, String> handleLicenseNotVerifiedException(
            LicenseNotVerifiedException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicateRaceRegistrationException.class)
    public Map<String, String> handleDuplicateRaceRegistrationException(
            DuplicateRaceRegistrationException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RaceCapacityExceededException.class)
    public Map<String, String> handleRaceCapacityExceededException(
            RaceCapacityExceededException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EmailAlreadyInUseException.class)
    public Map<String, String> handleEmailAlreadyInUseException(
            EmailAlreadyInUseException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidAvatarException.class)
    public Map<String, String> handleInvalidAvatarException(
            InvalidAvatarException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AvatarNotFoundException.class)
    public Map<String, String> handleAvatarNotFoundException(
            AvatarNotFoundException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(AvatarStorageException.class)
    public Map<String, String> handleAvatarStorageException() {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to store avatar image");

        return error;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            DesertLiveItemNotFoundException.class,
            DesertLiveImageNotFoundException.class,
            MediaImageNotFoundException.class
    })
    public Map<String, String> handleDesertLiveNotFoundException(
            RuntimeException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(DesertLiveAccessDeniedException.class)
    public Map<String, String> handleDesertLiveAccessDeniedException(
            DesertLiveAccessDeniedException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            InvalidDesertLiveItemException.class,
            InvalidImageFocusException.class,
            InvalidImageFramingException.class,
            InvalidImageException.class
    })
    public Map<String, String> handleInvalidDesertLiveRequest(
            RuntimeException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return error;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ImageStorageException.class)
    public Map<String, String> handleImageStorageException() {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Failed to store image");

        return error;
    }

    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Map<String, String> handleMaxUploadSizeExceededException() {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Image must be 2 MB or smaller");

        return error;
    }
}
