package com.eazybrew.vend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Enumeration;

@Component
@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip logging for swagger/static resources to reduce noise
        String path = request.getRequestURI();
        if (path.contains("/swagger-ui") || path.contains("/v3/api-docs") || path.contains("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        try {
            // We need to pass the wrappers to the chain so that subsequent filters/servlets
            // read from them
            // AND write to them.
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long timeTaken = System.currentTimeMillis() - startTime;

            // For request body, we must read from the wrapper's cache.
            // NOTE: The request body is only available here IF the controller/servlet
            // actually read the stream.
            // If nothing read the stream, the cache is empty.
            // To fix this, we might need to eagerly read the stream if it's empty, but
            // that's risky.
            // Usually Spring controllers read the body for @RequestBody.
            String requestBody = getStringValue(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding());
            String responseBody = getStringValue(responseWrapper.getContentAsByteArray(),
                    response.getCharacterEncoding());

            log.info("\n================================= REQUEST START =================================");
            log.info("URI         : {}", request.getRequestURI());
            log.info("Method      : {}", request.getMethod());
            // log.info("Headers : {}", getHeaders(request)); // Headers can be noisy
            log.info("Request Body: {}", requestBody);
            log.info("================================== REQUEST END ===================================");

            log.info("\n================================ RESPONSE START ================================");
            log.info("Status      : {}", response.getStatus());
            log.info("Time Taken  : {} ms", timeTaken);
            log.info("Response Body: {}", responseBody);
            log.info("================================= RESPONSE END =================================\n");

            responseWrapper.copyBodyToResponse();
        }
    }

    private String getStringValue(byte[] contentAsByteArray, String characterEncoding) {
        try {
            return new String(contentAsByteArray, 0, contentAsByteArray.length, characterEncoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.append(headerName).append(": ").append(request.getHeader(headerName)).append(", ");
        }
        return headers.toString();
    }
}
