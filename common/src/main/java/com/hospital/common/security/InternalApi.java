package com.hospital.common.security;

/**
 * Constants for calls between services.
 */
public final class InternalApi {

    /** Header that carries the shared internal secret. */
    public static final String SECRET_HEADER = "X-Internal-Secret";

    /** URL prefix used by all service-to-service endpoints. */
    public static final String PATH_PREFIX = "/internal";

    private InternalApi() {
    }
}
