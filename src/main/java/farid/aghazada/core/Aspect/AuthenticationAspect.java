package farid.aghazada.core.Aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import farid.aghazada.core.Service.HelperService;

@Aspect
@Component
public class AuthenticationAspect {
    
    private HelperService helperService;

    @Autowired
    public void setHelperService(HelperService helperService) {
        this.helperService = helperService;
    }

    @Around("@annotation(farid.aghazada.core.Aspect.Annotation.RequiresAuth)")
    public Object authenticate(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        String username = (String) args[0];
        String password = (String) args[1];

        helperService.authenticate(username, password);


        return joinPoint.proceed();
    }
}
