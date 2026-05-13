package com.piggymetrics.account.service.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomUserInfoTokenServicesTest {

	private static final String USER_INFO_URI = "http://localhost/uaa/users/current";
	private static final String CLIENT_ID = "test-client";
	private static final String ACCESS_TOKEN = "access-token-value";

	@Mock
	private OAuth2RestOperations restTemplate;

	@Mock
	private OAuth2ClientContext clientContext;

	private CustomUserInfoTokenServices services;

	@Before
	public void setup() {
		services = new CustomUserInfoTokenServices(USER_INFO_URI, CLIENT_ID);
		services.setRestTemplate(restTemplate);
		when(restTemplate.getOAuth2ClientContext()).thenReturn(clientContext);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void stubGetForEntity(Map<String, Object> body) {
		ResponseEntity entity = new ResponseEntity<>(body, HttpStatus.OK);
		when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(entity);
	}

	private Map<String, Object> userInfoMap(String principalKey, String principalValue, String clientId, List<String> scopes) {
		Map<String, Object> map = new HashMap<>();
		map.put(principalKey, principalValue);

		Map<String, Object> oauth2Request = new HashMap<>();
		oauth2Request.put("clientId", clientId);
		if (scopes != null) {
			oauth2Request.put("scope", scopes);
		}
		map.put("oauth2Request", oauth2Request);
		return map;
	}

	@Test
	public void shouldLoadAuthenticationFromValidToken() {
		stubGetForEntity(userInfoMap("name", "alice", "ui-client", Arrays.asList("ui", "server")));

		OAuth2Authentication authentication = services.loadAuthentication(ACCESS_TOKEN);

		assertNotNull(authentication);
		assertEquals("alice", authentication.getPrincipal());
		OAuth2Request request = authentication.getOAuth2Request();
		assertEquals("ui-client", request.getClientId());
		assertTrue(request.getScope().contains("ui"));
		assertTrue(request.getScope().contains("server"));
		assertTrue(request.isApproved());
	}

	@Test
	public void shouldSetAccessTokenWhenClientContextHasNone() {
		when(clientContext.getAccessToken()).thenReturn(null);
		stubGetForEntity(userInfoMap("name", "alice", "ui-client", Collections.singletonList("ui")));

		services.loadAuthentication(ACCESS_TOKEN);

		verify(clientContext, atLeastOnce()).setAccessToken(any(OAuth2AccessToken.class));
	}

	@Test
	public void shouldNotResetAccessTokenWhenAlreadyMatching() {
		OAuth2AccessToken existing = new DefaultOAuth2AccessToken(ACCESS_TOKEN);
		when(clientContext.getAccessToken()).thenReturn(existing);
		stubGetForEntity(userInfoMap("name", "alice", "ui-client", Collections.singletonList("ui")));

		services.loadAuthentication(ACCESS_TOKEN);

		verify(clientContext, never()).setAccessToken(any(OAuth2AccessToken.class));
	}

	@Test(expected = InvalidTokenException.class)
	public void shouldThrowInvalidTokenWhenUserInfoReturnsErrorMap() {
		Map<String, Object> errorMap = new HashMap<>();
		errorMap.put("error", "invalid_token");
		stubGetForEntity(errorMap);

		services.loadAuthentication(ACCESS_TOKEN);
	}

	@Test(expected = InvalidTokenException.class)
	public void shouldThrowInvalidTokenWhenRestTemplateFails() {
		when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenThrow(new RuntimeException("network down"));

		services.loadAuthentication(ACCESS_TOKEN);
	}

	@Test
	public void shouldResolvePrincipalFromVariousKeys() {
		String[] keys = {"user", "username", "userid", "user_id", "login", "id", "name"};

		for (String key : keys) {
			CustomUserInfoTokenServices svc = new CustomUserInfoTokenServices(USER_INFO_URI, CLIENT_ID);
			OAuth2RestOperations rt = mock(OAuth2RestOperations.class);
			OAuth2ClientContext ctx = mock(OAuth2ClientContext.class);
			when(rt.getOAuth2ClientContext()).thenReturn(ctx);
			svc.setRestTemplate(rt);

			Map<String, Object> map = userInfoMap(key, "principal-" + key, "client", Collections.<String>emptyList());
			@SuppressWarnings({"unchecked", "rawtypes"})
			ResponseEntity entity = new ResponseEntity<>(map, HttpStatus.OK);
			when(rt.getForEntity(anyString(), eq(Map.class))).thenReturn(entity);

			OAuth2Authentication auth = svc.loadAuthentication(ACCESS_TOKEN);
			assertEquals("principal-" + key, auth.getPrincipal());
		}
	}

	@Test
	public void shouldDefaultPrincipalToUnknownWhenNoKeyMatches() {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> oauth2Request = new HashMap<>();
		oauth2Request.put("clientId", "ui-client");
		map.put("oauth2Request", oauth2Request);

		stubGetForEntity(map);

		OAuth2Authentication auth = services.loadAuthentication(ACCESS_TOKEN);
		assertEquals("unknown", auth.getPrincipal());
	}

	@Test
	public void shouldHandleMissingScopeKeyInOauth2Request() {
		stubGetForEntity(userInfoMap("name", "alice", "ui-client", null));

		OAuth2Authentication auth = services.loadAuthentication(ACCESS_TOKEN);
		assertNotNull(auth);
		assertTrue(auth.getOAuth2Request().getScope().isEmpty());
		assertEquals("ui-client", auth.getOAuth2Request().getClientId());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void readAccessTokenShouldThrowUnsupported() {
		services.readAccessToken(ACCESS_TOKEN);
	}

	@Test
	public void setterMethodsShouldRetainProvidedValues() {
		CustomUserInfoTokenServices svc = new CustomUserInfoTokenServices(USER_INFO_URI, CLIENT_ID);
		OAuth2RestOperations rt = mock(OAuth2RestOperations.class);
		AuthoritiesExtractor extractor = new AuthoritiesExtractor() {
			@Override
			public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
				return Collections.emptyList();
			}
		};

		svc.setRestTemplate(rt);
		svc.setTokenType("Bearer-Custom");
		svc.setAuthoritiesExtractor(extractor);

		OAuth2ClientContext ctx = mock(OAuth2ClientContext.class);
		when(rt.getOAuth2ClientContext()).thenReturn(ctx);
		stubGetForEntityOn(rt, userInfoMap("name", "alice", "ui-client", Collections.singletonList("ui")));

		OAuth2Authentication auth = svc.loadAuthentication(ACCESS_TOKEN);
		assertNotNull(auth);
		assertTrue(auth.getAuthorities().isEmpty());
	}

	@Test
	public void shouldUseCustomAuthoritiesExtractor() {
		AuthoritiesExtractor extractor = new AuthoritiesExtractor() {
			@Override
			public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
				return Collections.<GrantedAuthority>singletonList(new GrantedAuthority() {
					@Override
					public String getAuthority() {
						return "ROLE_TESTER";
					}
				});
			}
		};

		services.setAuthoritiesExtractor(extractor);
		stubGetForEntity(userInfoMap("name", "alice", "ui-client", Collections.singletonList("ui")));

		OAuth2Authentication auth = services.loadAuthentication(ACCESS_TOKEN);
		assertFalse(auth.getAuthorities().isEmpty());
		assertEquals("ROLE_TESTER", auth.getAuthorities().iterator().next().getAuthority());
	}

	@Test
	public void shouldCreateDefaultRestTemplateWhenNoneIsConfigured() {
		CustomUserInfoTokenServices svc = new CustomUserInfoTokenServices(USER_INFO_URI, CLIENT_ID);
		try {
			svc.loadAuthentication(ACCESS_TOKEN);
			fail("Expected InvalidTokenException because the default rest template cannot reach the user-info URI");
		} catch (InvalidTokenException expected) {
			assertNotNull(expected.getMessage());
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void stubGetForEntityOn(OAuth2RestOperations rt, Map<String, Object> body) {
		ResponseEntity entity = new ResponseEntity<>(body, HttpStatus.OK);
		when(rt.getForEntity(anyString(), eq(Map.class))).thenReturn(entity);
	}
}
