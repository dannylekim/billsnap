package proj.kedabra.billsnap.config;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import proj.kedabra.billsnap.utils.annotations.ObfuscateArgs;

@Aspect
@Component
@Slf4j
public class LogAspectConfiguration {

    //simply to log errors only once when they occur at the root
    private final ThreadLocal<Boolean> errorLoggedThread = new ThreadLocal<>();

    private static final String SEPARATOR = " | ";

    private static final String ENTRY_CALL_SEPARATOR = ">>>>>>>>>>>>>";

    private static final String EXIT_CALL_SEPARATOR = "<<<<<<<<<<<<<";

    private static final String CONTROLLER = "CONTROLLER";

    private static final String SERVICE = "SERVICE";

    private static final String DTO = "DTO";

    private static final String FACADE = "FACADE";

    private static final String REPOSITORY = "REPOSITORY";

    private static final String OTHER = "OTHER";

    private static final String ADVICE = "ADVICE";

    private static final String VOID = "VOID";

    private static final String NO_ARGS = "NO ARGS";

    private static final String NULL = "null";

    private final boolean isDev;

    @Autowired
    public LogAspectConfiguration(final Environment env) {
        isDev = Optional.ofNullable(env.getProperty("spring.active.profiles")).map(s -> s.contains("dev")).orElse(false);
    }

    @Around(value = "execution(* proj.kedabra.billsnap..* (..)) && !execution(* proj.kedabra.billsnap.BillSnapExceptionHandler..*(..))")
    public Object logMethodCall(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        final var signature = (MethodSignature) proceedingJoinPoint.getSignature();
        final var methodName = signature.toShortString();
        final String methodType = this.getMethodType(signature.getMethod().getDeclaringClass());
        final Class<?> returnType = signature.getReturnType();
        final String formattedArgsMsg = getFormattedArgsMsg(proceedingJoinPoint);
        final String entryMessage = getEntryMessage(formattedArgsMsg, methodName, returnType, methodType);

        log.info(entryMessage);

        final long start = System.currentTimeMillis();

        Object returnObject = null;
        //reset the errorLoggedThread
        errorLoggedThread.set(false);
        try {
            returnObject = proceedingJoinPoint.proceed();
            return returnObject;
        } catch (Throwable ex) {
            if (!errorLoggedThread.get()) {
                log.error("ERROR MESSAGE" + SEPARATOR + ex.getMessage(), ex);
                //set the thread to true and move all the way to the top without logging
                errorLoggedThread.set(true);
            }
            returnObject = ex.getClass().getSimpleName() + " : " + ex.getMessage();
            throw ex;
        } finally {
            final long executionTime = System.currentTimeMillis() - start;
            final String returnObjectMessage = getFormattedReturnObjectMessage(returnType, returnObject);
            final String exitMessage = createExitMessage(methodName, returnObjectMessage, executionTime, methodType, returnType);
            log.info(exitMessage);
        }


    }

    private String getEntryMessage(final String formattedMsgArgs, final String methodName, final Class<?> returnType, final String methodType) {
        return methodType +
                SEPARATOR +
                methodName +
                SEPARATOR +
                "RETURNS: " +
                returnType.getSimpleName() +
                SEPARATOR +
                ENTRY_CALL_SEPARATOR +
                SEPARATOR +
                formattedMsgArgs;
    }
    private String createExitMessage(final String methodName, final String returnObjectMessage, final long executionTime, final String methodType, final Class<?> returnType) {
        return methodType +
                SEPARATOR +
                methodName +
                SEPARATOR +
                "RETURNS: " +
                returnType.getSimpleName() +
                SEPARATOR +
                EXIT_CALL_SEPARATOR +
                SEPARATOR +
                returnObjectMessage +
                SEPARATOR +
                "execution time: " +
                executionTime
                + " ms";
    }

    private String getMethodType(final Class<?> declaringClass) {
        if (declaringClass.getAnnotation(Service.class) != null) {
            return SERVICE;
        }
        if (declaringClass.getAnnotation(Repository.class) != null) {
            return REPOSITORY;
        }
        if (declaringClass.getAnnotation(RestController.class) != null) {
            return CONTROLLER;
        }
        if (declaringClass.getAnnotation(RestControllerAdvice.class) != null) {
            return ADVICE;
        }
        final var className = declaringClass.getName().toUpperCase();
        if (className.contains(DTO)) {
            return DTO;
        }
        if (className.contains(FACADE)) {
            return FACADE;
        }
        return OTHER;

    }

    private String getFormattedReturnObjectMessage(final Class<?> returnType, Object returnObject) {
        String formattedReturnObjectMessage;

        if (returnType.equals(Void.class)) {
            return VOID;
        } else {
            final String formattedReturnObject = formatObject(returnObject);
            formattedReturnObjectMessage = "RETURNED: " + formattedReturnObject;
        }

        return formattedReturnObjectMessage;

    }

    private boolean shouldObfuscateArgs(final MethodSignature signature) {
        if (!isDev) {
            Method method = signature.getMethod();
            return Optional.ofNullable(method.getAnnotation(ObfuscateArgs.class)).isPresent();
        }

        return false;

    }

    private String getFormattedArgsMsg(final ProceedingJoinPoint proceedingJoinPoint) {
        final Object[] args = proceedingJoinPoint.getArgs();
        if (args.length == 0) {
            return NO_ARGS;
        }

        final boolean shouldObfuscateArgs = shouldObfuscateArgs((MethodSignature) proceedingJoinPoint.getSignature());

        if (shouldObfuscateArgs) {
            return Arrays.stream(args)
                    .map(Object::getClass)
                    //binding result specifically is a class that shouldn't be logged
                    .filter(Predicate.not(BindingResult.class::isAssignableFrom))
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(","));
        }
        return Arrays.stream(args)
                .map(argument -> (argument == null) ?
                        Objects.toString(null) : argument)
                .filter(Predicate.not(
                        arg -> BindingResult.class.isAssignableFrom(arg.getClass())))
                .map(this::formatObject)
                .collect(Collectors.joining(","));

    }

    private String formatObject(final Object object) {

        if (object == null) {
            return NULL;
        }

        final var packageName = object.getClass().getPackageName();

        //because of lazy loading, doing a object.toString() will force to get the entire row in the database, which is causes an exception. Either setting them to eager (which is slow for our purposes)
        //or just not logging entities would be the solutions. Here, we opted to not log entities
        if (packageName.contains("entities")) {
            return object.getClass().getSimpleName();
        }

        String formattedString = object.toString();

        if (formattedString.startsWith(packageName)) {
            formattedString = object.getClass().getSimpleName();
        }

        return formattedString;

    }


}
