/*
 * Copyright 2012-2019 the original author or authors.
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

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.UrlHandlerFilter;

@Configuration(proxyBeanMethods = false)
class WebConfiguration {

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE + 1)
	UrlHandlerFilter trailingSlashFilter() {
		return UrlHandlerFilter.trailingSlashHandler("/owners", "/owners/find", "/owners/new", "/owners/{ownerId}",
				"/owners/{ownerId}/edit", "/owners/{ownerId}/pets/new", "/owners/{ownerId}/pets/{petId}/edit",
				"/owners/{ownerId}/pets/{petId}/visits/new", "/vets", "/vets.html", "/oups", "/resources/**",
				"/webjars/**").wrapRequest().build();
	}

	@Bean
	WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> javascriptMimeMapping() {
		return factory -> {
			MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
			mappings.add("js", "application/javascript");
			factory.setMimeMappings(mappings);
		};
	}

}
