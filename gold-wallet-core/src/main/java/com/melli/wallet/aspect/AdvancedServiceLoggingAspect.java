package com.melli.wallet.aspect;

import com.melli.wallet.annotation.LogExecutionTime;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Advanced aspect for logging all service method executions with configurable options
 * This aspect automatically logs the start and end of all methods in service classes
 * Handles both public and private methods, and prevents StackOverflowError from circular references
 */
@Aspect
@Component
@Log4j2
public class AdvancedServiceLoggingAspect {

    @Value("${logging.aspect.log-parameters:true}")
    private boolean logParameters;
    
    @Value("${logging.aspect.log-return-values:true}")
    private boolean logReturnValues;
    
    @Value("${logging.aspect.log-execution-time:true}")
    private boolean logExecutionTime;

    /**
     * Around advice that logs all service method executions
     * This advice will be applied to all methods in classes annotated with @Service
     * 
     * @param joinPoint the join point representing the method execution
     * @return the result of the method execution
     * @throws Throwable if the method throws an exception
     */

    @Around("@annotation(com.melli.wallet.annotation.LogExecutionTime)")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // Get method information
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogExecutionTime annotation = method.getAnnotation(LogExecutionTime.class);
        
        String methodName = signature.getName();
        String className = signature.getDeclaringType().getSimpleName();
        String customMessage = annotation.value();
        String accessModifier = getAccessModifier(method);
        Object[] args = joinPoint.getArgs();
        
        // Log method start safely
        logMethodStart(className, methodName, customMessage, accessModifier, args);
        
        try {
            // Execute the method
            Object result = joinPoint.proceed();
            
            // Calculate execution time
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // Log method end with success safely
            logMethodEnd(className, methodName, customMessage, accessModifier, executionTime, result);
            
            return result;
            
        } catch (Throwable throwable) {
            // Calculate execution time
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // Log method end with error safely
            logMethodError(className, methodName, customMessage, accessModifier, executionTime, throwable);
            
            // Re-throw the exception
            throw throwable;
        }
    }
    
    /**
     * Gets the access modifier of the method
     */
    private String getAccessModifier(Method method) {
        if (java.lang.reflect.Modifier.isPrivate(method.getModifiers())) {
            return "private";
        } else if (java.lang.reflect.Modifier.isProtected(method.getModifiers())) {
            return "protected";
        } else if (java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
            return "public";
        } else {
            return "package-private";
        }
    }
    
    /**
     * Safely converts objects to string, handling circular references
     */
    private String safeToString(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        try {
            // For arrays, use Arrays.toString
            if (obj.getClass().isArray()) {
                return Arrays.toString((Object[]) obj);
            }
            
            // For collections, use safe collection toString
            if (obj instanceof java.util.Collection) {
                return safeCollectionToString((java.util.Collection<?>) obj);
            }
            
            // For JPA entities, use safe entity toString
            if (isJpaEntity(obj)) {
                return safeEntityToString(obj);
            }
            
            // For other objects, use toString but catch StackOverflowError
            return obj.toString();
            
        } catch (StackOverflowError e) {
            return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode()) + " [circular reference]";
        } catch (Exception e) {
            return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode()) + " [error: " + e.getMessage() + "]";
        }
    }
    
    /**
     * Safely converts collections to string
     */
    private String safeCollectionToString(java.util.Collection<?> collection) {
        try {
            if (collection.isEmpty()) {
                return "[]";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            int count = 0;
            for (Object item : collection) {
                if (count > 0) sb.append(", ");
                if (count >= 10) { // Limit to first 10 items
                    sb.append("... and ").append(collection.size() - 10).append(" more");
                    break;
                }
                sb.append(safeToString(item));
                count++;
            }
            sb.append("]");
            return sb.toString();
        } catch (StackOverflowError e) {
            return collection.getClass().getSimpleName() + " [circular reference, size: " + collection.size() + "]";
        }
    }
    
    /**
     * Safely converts JPA entities to string
     */
    private String safeEntityToString(Object entity) {
        try {
            // Try to get ID and class name
            String className = entity.getClass().getSimpleName();
            
            // Try to get ID field if it exists
            try {
                java.lang.reflect.Field idField = entity.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                Object id = idField.get(entity);
                return className + "{id=" + id + "}";
            } catch (Exception e) {
                // If no ID field, just return class name with hash
                return className + "@" + Integer.toHexString(entity.hashCode());
            }
        } catch (Exception e) {
            return entity.getClass().getSimpleName() + "@" + Integer.toHexString(entity.hashCode());
        }
    }
    
    /**
     * Checks if an object is a JPA entity
     */
    private boolean isJpaEntity(Object obj) {
        if (obj == null) {
            return false;
        }
        
        try {
            // Check for Jakarta Persistence Entity annotation
            if (obj.getClass().isAnnotationPresent(jakarta.persistence.Entity.class)) {
                return true;
            }
            
            // Check if class is in entity package (fallback)
            return obj.getClass().getPackage().getName().contains("entity");
            
        } catch (Exception e) {
            // If any error occurs, assume it's not a JPA entity
            return false;
        }
    }
    
    /**
     * Safely converts method arguments to string
     */
    private String safeArgsToString(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(safeToString(args[i]));
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Logs the start of method execution safely
     */
    private void logMethodStart(String className, String methodName, String customMessage, 
                               String accessModifier, Object[] args) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("START - ").append(className).append(".").append(methodName);
        logMessage.append(" (").append(accessModifier).append(")");
        
        if (!customMessage.isEmpty()) {
            logMessage.append(" - ").append(customMessage);
        }
        
        if (logParameters && args != null && args.length > 0) {
            logMessage.append(" - Parameters: ").append(safeArgsToString(args));
        }
        
        log.info(logMessage.toString());
    }
    
    /**
     * Logs the successful end of method execution safely
     */
    private void logMethodEnd(String className, String methodName, String customMessage, 
                             String accessModifier, long executionTime, Object result) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("END - ").append(className).append(".").append(methodName);
        logMessage.append(" (").append(accessModifier).append(")");
        
        if (!customMessage.isEmpty()) {
            logMessage.append(" - ").append(customMessage);
        }
        
        if (logExecutionTime) {
            logMessage.append(" - Execution time: ").append(executionTime).append("ms");
        }
        
        if (logReturnValues && result != null) {
            logMessage.append(" - Return value: ").append(safeToString(result));
        }
        
        log.info(logMessage.toString());
    }
    
    /**
     * Logs the error end of method execution safely
     */
    private void logMethodError(String className, String methodName, String customMessage, 
                               String accessModifier, long executionTime, Throwable throwable) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("ERROR - ").append(className).append(".").append(methodName);
        logMessage.append(" (").append(accessModifier).append(")");
        
        if (!customMessage.isEmpty()) {
            logMessage.append(" - ").append(customMessage);
        }
        
        if (logExecutionTime) {
            logMessage.append(" - Execution time: ").append(executionTime).append("ms");
        }

        logMessage.append(" - Exception: ").append(throwable.getMessage());
        log.error(logMessage.toString());
    }
}
