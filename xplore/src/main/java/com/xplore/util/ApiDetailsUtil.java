package com.xplore.util;

import jakarta.servlet.http.HttpServletRequest;

public class ApiDetailsUtil {
    public static String getApiName(HttpServletRequest request) {
        return request.getMethod() + " " + request.getRequestURI();
    }
}
