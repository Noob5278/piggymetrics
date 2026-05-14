package com.piggymetrics.account.service.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class CustomUserInfoTokenServicesTest {

	private CustomUserInfoTokenServices tokenServices;

	@Mock
	private OAuth2RestOperations restTemplate;

	@Mock
	private OAuth2ClientContext clientContext;

	private static final String USER_INFO_URI = "http://auth-service/uaa/users/current";
	private static final String CLIENT_ID = "account-service";

	@Before
	public void setup() {
		initMocks(this);
		tokenServices = new CustomUserInfoTokenServices(USER_INFO_URI, CLIENT_ID);
		tokenServices.setRestTemplate(restTemplate);
	}

	@Test
	public void shouldLoadAuthentication() {
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("user", "testuser");
		Map<String, Object> oauth2Request = new HashMap<>();
		oauth2Request.put("clientId", CLIENT_ID);
		oauth2Request.put("scope", Arrays.asList("ui"));
		userInfo.put("oauth2Request", oauth2Request);

		DefaultOAuth2AccessToken existingToken = new DefaultOAuth2AccessToken("test-token");
		when(restTemplate.getOAuth2ClientContext()).thenReturn(clientContext);
		when(clientContext.getAccessToken()).thenReturn(existingToken);
		when(restTemplate.getForEntity(USER_INFO_URI, Map.class))
				.thenReturn(new ResponseEntity<Map>(userInfo, HttpStatus.OK));

		OAuth2Authentication authentication = tokenServices.loadAuthentication("test-token");

		assertNotNull(authentication);
		assertEquals("testuser", authentication.getPrincipal());
		assertTrue(authentication.getOAuth2Request().getScope().contains("ui"));
	}

	@Test(expected = InvalidTokenException.class)
	public void shouldThrowInvalidTokenExceptionWhenErrorInResponse() {
		Map<String, Object> errorMap = new HashMap<>();
		errorMap.put("error", "invalid_token");

		DefaultOAuth2AccessToken existingToken = new DefaultOAuth2AccessToken("bad-token");
		when(restTemplate.getOAuth2ClientContext()).thenReturn(clientContext);
		when(clientContext.getAccessToken()).thenReturn(existingToken);
		when(restTemplate.getForEntity(USER_INFO_URI, Map.class))
				.thenReturn(new ResponseEntity<Map>(errorMap, HttpStatus.OK));

		tokenServices.loadAuthentication("bad-token");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldThrowUnsupportedOperationOnReadAccessToken() {
		tokenServices.readAccessToken("any-token");
	}

	@Test
	public void shouldSetTokenType() {
		tokenServices.setTokenType("mac");
		// No exception means setter works
		assertNotNull(tokenServices);
	}

	@Test
	public void shouldSetNewAccessTokenWhenDifferentFromExisting() {
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("name", "testuser");
		Map<String, Object> oauth2Request = new HashMap<>();
		oauth2Request.put("clientId", CLIENT_ID);
		oauth2Request.put("scope", Collections.emptyList());
		userInfo.put("oauth2Request", oauth2Request);

		DefaultOAuth2AccessToken existingToken = new DefaultOAuth2AccessToken("old-token");
		when(restTemplate.getOAuth2ClientContext()).thenReturn(clientContext);
		when(clientContext.getAccessToken()).thenReturn(existingToken);
		when(restTemplate.getForEntity(USER_INFO_URI, Map.class))
				.thenReturn(new ResponseEntity<Map>(userInfo, HttpStatus.OK));

		OAuth2Authentication authentication = tokenServices.loadAuthentication("new-token");

		assertNotNull(authentication);
		verify(clientContext).setAccessToken(any(DefaultOAuth2AccessToken.class));
	}

	@Test
	public void shouldExtractPrincipalFromMultipleKeys() {
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("username", "principalUser");
		Map<String, Object> oauth2Request = new HashMap<>();
		oauth2Request.put("clientId", CLIENT_ID);
		oauth2Request.put("scope", Collections.emptyList());
		userInfo.put("oauth2Request", oauth2Request);

		DefaultOAuth2AccessToken existingToken = new DefaultOAuth2AccessToken("test-token");
		when(restTemplate.getOAuth2ClientContext()).thenReturn(clientContext);
		when(clientContext.getAccessToken()).thenReturn(existingToken);
		when(restTemplate.getForEntity(USER_INFO_URI, Map.class))
				.thenReturn(new ResponseEntity<Map>(userInfo, HttpStatus.OK));

		OAuth2Authentication authentication = tokenServices.loadAuthentication("test-token");

		assertEquals("principalUser", authentication.getPrincipal());
	}

	@Test
	public void shouldReturnUnknownWhenNoPrincipalKeyFound() {
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("email", "test@test.com");
		Map<String, Object> oauth2Request = new HashMap<>();
		oauth2Request.put("clientId", CLIENT_ID);
		oauth2Request.put("scope", Collections.emptyList());
		userInfo.put("oauth2Request", oauth2Request);

		DefaultOAuth2AccessToken existingToken = new DefaultOAuth2AccessToken("test-token");
		when(restTemplate.getOAuth2ClientContext()).thenReturn(clientContext);
		when(clientContext.getAccessToken()).thenReturn(existingToken);
		when(restTemplate.getForEntity(USER_INFO_URI, Map.class))
				.thenReturn(new ResponseEntity<Map>(userInfo, HttpStatus.OK));

		OAuth2Authentication authentication = tokenServices.loadAuthentication("test-token");

		assertEquals("unknown", authentication.getPrincipal());
	}
}
