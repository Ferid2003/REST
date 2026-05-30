package farid.aghazada.core.Component;

import java.util.UUID;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class TransactionLoggerFilter implements Filter{

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        MDC.put("transactionId", UUID.randomUUID().toString());

        try {
            chain.doFilter(req, res);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            MDC.clear();
        }
    }

}
