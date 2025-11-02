package org.company.ecommerce.exceptions;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String msg) {
        super(msg);
    }
}