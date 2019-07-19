package proj.kedabra.billsnap.security;

class SecurityConstants {

    static final String AUTH_LOGIN_URL = "/login";

    static final String TOKEN_HEADER = "Authorization";

    static final String TOKEN_PREFIX = "Bearer ";

    static final String TOKEN_TYPE = "JWT";

    static final String TOKEN_ISSUER = "secure-api";

    static final String TOKEN_AUDIENCE = "secure-app";

    private SecurityConstants() {
        throw new IllegalStateException("Cannot create instance of static util class");
    }
}