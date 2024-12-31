package com.xplore.filter;

import com.xplore.util.ApiDetailsUtil;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Component
public class LoggingFilter implements Filter{

    private static final String CORRELATION_ID = "correlationId";
    private static final String USERNAME = "username";
    private static final String API_NAME = "apiName";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Generate Correlation ID
        String correlationId = UUID.randomUUID().toString();

//        // Extract Username
//        String username = httpRequest.getHeader("X-Username");
//        if (username == null || username.isEmpty()) {
//            username = "Anonymous";
//        }
        String username = "TEST";

        // Extract API Name dynamically
        String apiName = ApiDetailsUtil.getApiName(httpRequest);

        // Add to MDC
        MDC.put(CORRELATION_ID, correlationId);
        MDC.put(USERNAME, username);
        MDC.put(API_NAME, apiName);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID);
            MDC.remove(USERNAME);
            MDC.remove(API_NAME);
        }
    }

}
