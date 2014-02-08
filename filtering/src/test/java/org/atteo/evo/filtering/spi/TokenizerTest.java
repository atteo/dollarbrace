/*
 * Copyright 2012 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.evo.filtering.spi;

import java.util.List;

import org.atteo.evo.filtering.spi.Tokenizer.Token;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TokenizerTest {
	@Test
	public void simple() {
		List<Token> tokens = Tokenizer.splitIntoTokens("${a}");
		assertEquals(1, tokens.size());
		assertTrue(tokens.get(0).isProperty());
	}

	@Test
	public void nested() {
		List<Token> tokens = Tokenizer.splitIntoTokens("${a_${b}}");
		assertEquals(1, tokens.size());
		assertTrue(tokens.get(0).isProperty());
		assertEquals("a_${b}", tokens.get(0).getValue());
	}

	@Test
	public void multiple() {
		List<Token> tokens = Tokenizer.splitIntoTokens("${a} ${b}");
		assertEquals(3, tokens.size());
		assertFalse(tokens.get(1).isProperty());
		assertTrue(tokens.get(2).isProperty());
		assertEquals("b", tokens.get(2).getValue());
	}

	@Test
	public void incorrect() {
		List<Token> tokens = Tokenizer.splitIntoTokens("${abc} }");
		assertEquals(" }", tokens.get(1).getValue());

		tokens = Tokenizer.splitIntoTokens("${abc} ${a");
		assertEquals(" ${a", tokens.get(1).getValue());
	}
}
