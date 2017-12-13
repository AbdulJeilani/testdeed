package com.heapbrain.core.testdeed.annotations;

/**
 * @author AbdulJeilani
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.HttpStatus;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestDeedApiOperation {
	String name() default "";
	String description() default "";
	HttpStatus httpStatus() default HttpStatus.OK;
}
