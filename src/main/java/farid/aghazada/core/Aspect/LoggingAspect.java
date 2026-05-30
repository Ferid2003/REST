package farid.aghazada.core.Aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Around("execution(* farid.aghazada.core.Controller.*.*(..))")
    public Object logRestCall(ProceedingJoinPoint joinPoint) throws Throwable {
        return logCall("REST", joinPoint);
    }

    @Around("execution(* farid.aghazada.core.Service.*.*(..))")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        return logCall("SERVICE", joinPoint);
    }

    private Object logCall(String layer, ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();

        logger.info("[{}] [{}] -> {}", MDC.get("transactionId"), layer, method);

        try {
            Object result = joinPoint.proceed();
            logger.info("[{}] [{}] <- {}", MDC.get("transactionId"), layer, method);
            return result;
        } catch (Exception e) {
            logger.error("[{}] [{}] <- {} threw: {}", MDC.get("transactionId"), layer, method, e.getMessage());
            throw e;
        }
    }

}
