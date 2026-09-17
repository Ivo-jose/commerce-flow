package br.com.ivogoncalves.catalog_service.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ivo Gonçalves
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProductImageLimitExceededException extends RuntimeException {
    public ProductImageLimitExceededException(String message) {
        super(message);
    }
}
