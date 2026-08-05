package com.financebuddy.backend.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JwtErrorResponseTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @MethodSource("invalidJwtExceptions")
    void invalidJwtExceptionsReturnUnauthorizedJson(RuntimeException jwtException) throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        FilterChain filterChain = mock(FilterChain.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtService,
                userDetailsService,
                objectMapper
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/profile");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("invalid-token")).thenThrow(jwtException);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));

        JsonNode json = objectMapper.readTree(response.getContentAsString());
        assertEquals("Unauthorized", json.get("error").asText());
        assertEquals(
                "JWT token is invalid or expired. Please login again.",
                json.get("message").asText()
        );
        assertEquals("/api/profile", json.get("path").asText());
        verifyNoInteractions(userDetailsService, filterChain);
    }

    @Test
    void authenticationEntryPointReturnsConsistentUnauthorizedJson() throws Exception {
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(objectMapper);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/profile");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("Authentication is required")
        );

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));

        JsonNode json = objectMapper.readTree(response.getContentAsString());
        assertEquals("Unauthorized", json.get("error").asText());
        assertEquals("Authentication is required.", json.get("message").asText());
        assertEquals("/api/profile", json.get("path").asText());
    }

    private static Stream<RuntimeException> invalidJwtExceptions() {
        return Stream.of(
                mock(ExpiredJwtException.class),
                new MalformedJwtException("Malformed token"),
                new SignatureException("Invalid signature"),
                new UnsupportedJwtException("Unsupported token"),
                new IllegalArgumentException("Token is blank")
        );
    }
}
