package proj.kedabra.billsnap.config;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import lombok.extern.slf4j.Slf4j;

import proj.kedabra.billsnap.utils.annotations.ObfuscateArgs;

@Aspect
@Component
@Slf4j
public class LogAspectConfiguration {

    private static final String SEPARATOR = " | ";

    private static final String ENTRY_CALL_SEPARATOR = ">>>>>>>>>>>>>";

    private static final String EXIT_CALL_SEPARATOR = "<<<<<<<<<<<<<<";

    @Around(value = "execution(* proj.kedabra.billsnap..* (..)) && !execution(* proj.kedabra.billsnap.BillSnapExceptionHandler..*(..))")
    public Object logMethodCall(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        final var signature = (MethodSignature) proceedingJoinPoint.getSignature();
        final var methodName = signature.toShortString();
        final Class returnType = signature.getReturnType();
        final String formattedArgsMsg = getFormattedArgsMsg(proceedingJoinPoint);
        final String entryMessage = getEntryMessage(formattedArgsMsg, methodName, returnType);

        log.info(entryMessage);

        final long start = System.currentTimeMillis();

        Object returnObject = null;

        try {
            returnObject = proceedingJoinPoint.proceed();
            return returnObject;
        } catch (Throwable ex) {
            log.error("ERROR MESSAGE" + SEPARATOR + ex.getMessage());
            throw ex;
        } finally {
            final long executionTime = System.currentTimeMillis() - start;
            final String returnObjectMessage = getFormattedReturnObjectMessage(returnType, returnObject);
            final String exitMessage = createExitMessage(methodName, returnObjectMessage, executionTime);
            log.info(exitMessage);
        }


    }
    private String getEntryMessage(final String formattedMsgArgs, final String methodName, final Class returnType) {
        return methodName +
                SEPARATOR +
                "RETURNS: " +
                returnType.getSimpleName() +
                SEPARATOR +
                formattedMsgArgs +
                SEPARATOR + ENTRY_CALL_SEPARATOR;
    }
    private String createExitMessage(final String methodName, final String returnObjectMessage, final long executionTime) {
        return EXIT_CALL_SEPARATOR +
                SEPARATOR +
                methodName +
                SEPARATOR +
                returnObjectMessage +
                SEPARATOR +
                "execution time: " +
                executionTime
                + " ms";

    }

    private String getFormattedReturnObjectMessage(final Class returnType, Object returnObject) {
        String formattedReturnObjectMessage;

        if (returnType.equals(Void.class)) {
            final String formattedReturnObject = formatObject(returnObject);
            formattedReturnObjectMessage = "returned: " + formattedReturnObject;
        } else {
            formattedReturnObjectMessage = "VOID";
        }

        return formattedReturnObjectMessage;

    }

    private boolean shouldObfuscateArgs(final MethodSignature signature) {
        Method method = signature.getMethod();
        return Optional.ofNullable(method.getAnnotation(ObfuscateArgs.class)).isPresent();
    }

    private String getFormattedArgsMsg(final ProceedingJoinPoint proceedingJoinPoint) {
        final Object[] args = proceedingJoinPoint.getArgs();
        if (args.length == 0) {
            return "NO ARGS";
        }

        final boolean shouldObfuscateArgs = shouldObfuscateArgs((MethodSignature) proceedingJoinPoint.getSignature());

        if (shouldObfuscateArgs) {
            return Arrays.stream(args)
                    .map(Object::getClass)
                    .filter(Predicate.not(BindingResult.class::isAssignableFrom))
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(","));
        }
        return Arrays.stream(args)
                .filter(Predicate.not(
                        arg -> BindingResult.class.isAssignableFrom(arg.getClass())))
                .map(this::formatObject)
                .collect(Collectors.joining(","));

    }

    private String formatObject(final Object object) {

        String formattedString = object.toString();

        if (formattedString.startsWith(object.getClass().getPackageName())) {
            formattedString = object.getClass().getName();
        }

        return formattedString;

    }

}
