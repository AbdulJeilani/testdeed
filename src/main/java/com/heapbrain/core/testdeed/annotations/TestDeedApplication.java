package com.heapbrain.core.testdeed.annotations;

/**
 * @author AbdulJeilani
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestDeedApplication {
	String name() default "";
}
