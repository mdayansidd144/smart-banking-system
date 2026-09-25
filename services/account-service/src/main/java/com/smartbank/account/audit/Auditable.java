package com.smartbank.account.audit;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    AuditAction action();
    String accountIdParam() default "";

    /** Optional: the name of the method parameter that contains the resource ID. */
    String resourceIdParam() default "";

    /** Optional: type of the resource being acted upon. */
    String resourceType() default "";
}