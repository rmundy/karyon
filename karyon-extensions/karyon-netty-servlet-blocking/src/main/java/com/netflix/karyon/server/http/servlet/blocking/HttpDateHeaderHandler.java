package com.netflix.karyon.server.http.servlet.blocking;

import io.netty.handler.codec.http.HttpHeaders;

/**
 * @author Nitesh Kant
 */
public interface HttpDateHeaderHandler {

    /**
     * Date Header value as millis since epoch as specified by the HTTP spec.
     *
     * @param headers All headers.
     * @param headerName Name of the header, value of which has to be returned.
     *
     * @return Header value as miilis since epoch or -1 if header is not available.
     */
    long getDateAsMillisFromEpoch(HttpHeaders headers, String headerName);

    /**
     * Converts the passed time value as millis since epoch to a date header as specified by the HTTP spec.
     *
     * @param millisSinceEpoch Time in millis since epoch.
     *
     * @return Date header value for the passed time.
     */
    String convertToDateHeader(long millisSinceEpoch);
}
