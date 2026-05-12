package com.piggymetrics.account.service.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CustomUserInfoTokenServicesTest {

	private static final String USER_INFO_URI = "http://auth-service/uaa/users/current";
	private static final String CLIENT_ID = "test-client";
	private static final String TOKEN = "valid-access-token";

	private OAuth2RestOperations restTemplate;
	private OAuth2ClientContext clientContext;
	private CustomUserInfoTokenServices tokenServices;

	@Before
	public void setup() {
		restTemplate = mock(OAuth2RestOperations.class);
		clientContext = mock(OAuth2ClientContext.class);
		when(restTemplate.getOAuth2ClientContext()).thenReturn(clientContext);
		when(clientContext.getAccessToken()).thenReturn(null);

		tokenServices = new CustomUserInfoTokenServices(USER_INFO_URI, CLIENT_ID);
		tokenServices.setRestTemplate(restTemplate);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void stubUserInfoResponse(Map<String, Object> body) {
		ResponseEntity<Map> response = new ResponseEntity<Map>(body, HttpStatus.OK);
		when(restTemplate.getForEntity(anyString(), any(Class.class))).thenReturn((ResponseEntity) response);
	}

	private Map<String, Object> validUserInfoMap(String principalKey, String principalValue) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put(principalKey, principalValue);

		Map<String, Object> oauth2Request = new LinkedHashMap<String, Object>();
		oauth2Request.put("clientId", "ui");
		oauth2Request.put("scope", Arrays.asList("server", "ui"));
		map.put("oauth2Request", oauth2Request);

		return map;
	}

	@Test
	public void shouldLoadAuthenticationFromUserInfo() {
		stubUserInfoResponse(validUserInfoMap("user", "alice"));

		OAuth2Authentication authentication = tokenServices.loadAuthentication(TOKEN);

		assertNotNull(authentication);
		assertEquals("alice", authentication.getPrincipal());
		assertTrue(authentication.isAuthenticated());

		OAuth2Request request = authentication.getOAuth2Request();
		assertEquals("ui", request.getClientId());
		assertTrue(request.getScope().contains("server"));
		assertTrue(request.getScope().contains("ui"));
		assertTrue(request.isApproved());
	}

	@Test
	public void shouldResolvePrincipalUsingEachKnownKey() {
		String[] keys = new String[] { "user", "username", "userid", "user_id", "login", "id", "name" };
		for (String key : keys) {
			stubUserInfoResponse(validUserInfoMap(key, "alice-" + key));

			OAuth2Authentication authentication = tokenServices.loadAuthentication(TOKEN);

			assertEquals("alice-" + key, authentication.getPrincipal());
		}
	}

	@Test
	public void shouldFallbackToUnknownPrincipalWhenNoKeyMatches() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("something-else", "ignored");
		Map<String, Object> oauth2Request = new LinkedHashMap<String, Object>();
		oauth2Request.put("clientId", "ui");
		oauth2Request.put("scope", Collections.singletonList("ui"));
		map.put("oauth2Request", oauth2Request);

		stubUserInfoResponse(map);

		OAuth2Authentication authentication = tokenServices.loadAuthentication(TOKEN);

		assertEquals("unknown", authentication.getPrincipal());
	}

	@Test
	public void shouldHandleMissingScopeAsEmptySet() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("user", "alice");
		Map<String, Object> oauth2Request = new LinkedHashMap<String, Object>();
		oauth2Request.put("clientId", "ui");
		map.put("oauth2Request", oauth2Request);

		stubUserInfoResponse(map);

		OAuth2Authentication authentication = tokenServices.loadAuthentication(TOKEN);

		assertNotNull(authentication.getOAuth2Request().getScope());
		assertTrue(authentication.getOAuth2Request().getScope().isEmpty());
	}

	@Test(expected = InvalidTokenException.class)
	public void shouldThrowInvalidTokenWhenUserInfoReturnsError() {
		Map<String, Object> errorMap = new HashMap<String, Object>();
		errorMap.put("error", "invalid_token");
		stubUserInfoResponse(errorMap);

		tokenServices.loadAuthentication("bad-token");
	}

	@Test(expected = InvalidTokenException.class)
	public void shouldTreatRestTemplateExceptionAsErrorMap() {
		when(restTemplate.getOAuth2ClientContext()).thenReturn(clientContext);
		when(restTemplate.getForEntity(anyString(), any(Class.class)))
				.thenThrow(new RuntimeException("server unreachable"));

		tokenServices.loadAuthentication(TOKEN);
	}

	@Test
	public void shouldUseProvidedAuthoritiesExtractor() {
		final GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_TEST");
		tokenServices.setAuthoritiesExtractor(new AuthoritiesExtractor() {
			@Override
			public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
				return Collections.singletonList(authority);
			}
		});

		stubUserInfoResponse(validUserInfoMap("user", "alice"));

		OAuth2Authentication authentication = tokenServices.loadAuthentication(TOKEN);

		assertEquals(1, authentication.getAuthorities().size());
		assertEquals("ROLE_TEST", authentication.getAuthorities().iterator().next().getAuthority());
	}

	@Test
	public void shouldDefaultToFixedAuthoritiesExtractor() throws Exception {
		java.lang.reflect.Field field = CustomUserInfoTokenServices.class.getDeclaredField("authoritiesExtractor");
		field.setAccessible(true);
		Object extractor = field.get(new CustomUserInfoTokenServices(USER_INFO_URI, CLIENT_ID));

		assertTrue(extractor instanceof FixedAuthoritiesExtractor);
	}

	@Test
	public void shouldPushAccessTokenIntoClientContextWhenAbsent() {
		stubUserInfoResponse(validUserInfoMap("user", "alice"));

		tokenServices.loadAuthentication(TOKEN);

		ArgumentCaptor<OAuth2AccessToken> captor = ArgumentCaptor.forClass(OAuth2AccessToken.class);
		verify(clientContext, times(1)).setAccessToken(captor.capture());
		assertEquals(TOKEN, captor.getValue().getValue());
	}

	@Test
	public void shouldReuseExistingAccessTokenWhenAlreadyPresent() {
		OAuth2AccessToken existing = mock(OAuth2AccessToken.class);
		when(existing.getValue()).thenReturn(TOKEN);
		when(clientContext.getAccessToken()).thenReturn(existing);

		stubUserInfoResponse(validUserInfoMap("user", "alice"));

		tokenServices.loadAuthentication(TOKEN);

		verify(clientContext, times(0)).setAccessToken(any(OAuth2AccessToken.class));
	}

	@Test
	public void shouldRespectCustomTokenType() {
		tokenServices.setTokenType("MAC");
		stubUserInfoResponse(validUserInfoMap("user", "alice"));

		tokenServices.loadAuthentication(TOKEN);

		ArgumentCaptor<OAuth2AccessToken> captor = ArgumentCaptor.forClass(OAuth2AccessToken.class);
		verify(clientContext).setAccessToken(captor.capture());
		assertEquals("MAC", captor.getValue().getTokenType());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void readAccessTokenShouldBeUnsupported() {
		tokenServices.readAccessToken("anything");
	}

	@Test
	public void authenticationShouldBeAuthenticatedAndCarryAuthoritiesList() {
		stubUserInfoResponse(validUserInfoMap("user", "alice"));

		OAuth2Authentication authentication = tokenServices.loadAuthentication(TOKEN);

		assertNotNull(authentication.getAuthorities());
		assertFalse(authentication.getOAuth2Request().getScope().isEmpty()
				&& authentication.getOAuth2Request().getClientId() == null);
	}
}
