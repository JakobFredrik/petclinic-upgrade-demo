/*
 * Copyright 2012-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.system;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.endpoint.SanitizableData;
import org.springframework.boot.actuate.endpoint.SanitizingFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Key-based masking of the values shown by the {@code env} and {@code configprops}
 * actuator endpoints: values whose key looks sensitive (password, secret, key, token,
 * credentials, ...) are replaced with {@value SanitizableData#SANITIZED_VALUE}, and for
 * URI-like keys only the password part of the user info is masked.
 */
@Configuration(proxyBeanMethods = false)
class ActuatorSanitizingConfiguration {

	private static final List<String> SENSITIVE_KEYS = List.of("password", "secret", "key", "token", ".*credentials.*",
			"vcap_services", "^vcap\\.services.*$", "sun.java.command", "^spring[._]application[._]json$");

	private static final List<String> URI_USERINFO_KEYS = List.of("uri", "uris", "url", "urls", "address", "addresses");

	private static final Pattern URI_USERINFO_PATTERN = Pattern
			.compile("^\\[?[A-Za-z][A-Za-z0-9\\+\\.\\-]+://.+:(.*)@.+$");

	private static final List<Pattern> SENSITIVE_KEY_PATTERNS = SENSITIVE_KEYS.stream()
			.map(ActuatorSanitizingConfiguration::keyPattern).toList();

	private static final List<Pattern> URI_KEY_PATTERNS = URI_USERINFO_KEYS.stream()
			.map(ActuatorSanitizingConfiguration::keyPattern).toList();

	@Bean
	SanitizingFunction keyBasedSanitizingFunction() {
		return (data) -> {
			if (data.getValue() == null) {
				return data;
			}
			String key = data.getKey();
			if (matchesAny(SENSITIVE_KEY_PATTERNS, key)) {
				return data.withSanitizedValue();
			}
			if (matchesAny(URI_KEY_PATTERNS, key)) {
				return data.withValue(sanitizeUris(data.getValue().toString()));
			}
			return data;
		};
	}

	private static Pattern keyPattern(String key) {
		boolean regex = key.contains("*") || key.contains("$") || key.contains("^") || key.contains("+");
		return Pattern.compile(regex ? key : ".*" + key + "$", Pattern.CASE_INSENSITIVE);
	}

	private static boolean matchesAny(List<Pattern> patterns, String key) {
		return patterns.stream().anyMatch((pattern) -> pattern.matcher(key).matches());
	}

	private static String sanitizeUris(String value) {
		return Arrays.stream(value.split(",")).map(ActuatorSanitizingConfiguration::sanitizeUri)
				.collect(Collectors.joining(","));
	}

	private static String sanitizeUri(String value) {
		Matcher matcher = URI_USERINFO_PATTERN.matcher(value);
		if (!matcher.matches()) {
			return value;
		}
		String password = matcher.group(1);
		return StringUtils.replace(value, ":" + password + "@", ":" + SanitizableData.SANITIZED_VALUE + "@");
	}

}
