package com.melli.hub.access;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class AccessCheck {


    public Object doAccessCheck(ProceedingJoinPoint joinPoint) throws Throwable{
        log.debug("--------------------------- start check parameter -------------------------");
        log.info("method==> ({})", joinPoint.getSignature().getName());

        switch (joinPoint.getSignature().getName()){

            default:
                log.info("can not find any method for method ===> ({})" , joinPoint.getSignature().getName());
                return joinPoint.proceed();
        }

    }
}
