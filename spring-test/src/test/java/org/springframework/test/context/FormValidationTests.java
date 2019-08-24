/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.test.context;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.Locale;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test for issues related to special validation messages.
 */
class FormValidationTests
{
	@Test
	void testWithoutValidationErrors ()
			throws Exception
	{
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController()).build();

		mockMvc.perform(get("/").param("id", "abcde")
								.param("username", "john"))
			   .andExpect(content().string("OK"));
	}

	@Test
	void testWithIdValidationError ()
			throws Exception
	{
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController()).build();

		mockMvc.perform(get("/").param("id", "abc")
								.param("username", "john"))
			   .andExpect(content().string("id must match \"\\w{5}\"."));
	}

	@Test
	void testWithUsernameValidationError ()
			throws Exception
	{
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController()).build();

		mockMvc.perform(get("/").param("id", "abcde")
								.param("username", "j"))
			   .andExpect(content().string("username must match \"\\w{2,10}\"."));
	}

	@RestController
	private static class TestController
	{
		@GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
		public String test (@Valid TestForm form,
							BindingResult bindingResult,
							Locale locale)
		{
			FieldError fieldError = bindingResult.getFieldError();
			if (fieldError == null) {
				return "OK";
			}

			ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
			return messageSource.getMessage(fieldError, locale);
		}
	}

	private static class TestForm
	{
		private static final String VALIDATION_ERROR_MESSAGE = "{0} must match \"{regexp}\".";

		@Pattern(regexp = "\\w{5}", message = VALIDATION_ERROR_MESSAGE)
		private String id;

		@Pattern(regexp = "\\w{2,10}", message = VALIDATION_ERROR_MESSAGE)
		private String username;

		public String getId ()
		{
			return id;
		}

		public void setId (String id)
		{
			this.id = id;
		}

		public String getUsername ()
		{
			return username;
		}

		public void setUsername (String username)
		{
			this.username = username;
		}
	}
}
