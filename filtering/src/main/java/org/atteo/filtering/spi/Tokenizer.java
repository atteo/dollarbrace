/*
 * Copyright 2014 Atteo.
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

package org.atteo.filtering.spi;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
	public static final class Token {
		private final String value;
		private final boolean property;

		public Token(String value, boolean property) {
			this.value = value;
			this.property = property;
		}

		public String getValue() {
			return value;
		}

		public boolean isProperty() {
			return property;
		}
	}


    /**
     * Splits given string into {@link Token tokens}.
     * <p>
     * Token is ordinary text or property placeholder: <code>${name}</code>.
     * For instance the string: "abc${abc}abc" will be split
     * into three tokens: text "abc", property "abc" and text "abc".
     * </p>
     * @param input input string to split into tokens
     * @return list of tokens
     */
    public static List<Token> splitIntoTokens(String input) {
        List<Token> parts = new ArrayList<>();
        int index = 0;
        while (true) {
            int startPosition = input.indexOf("${", index);
            if (startPosition == -1) {
                break;
            }
            // find '${' and '}' pair, correctly handle nested pairs
            boolean lastDollar = false;
            int count = 1;
            int countBrace = 0;
            int endposition;
            for (endposition = startPosition + 2; endposition < input.length(); endposition++) {
                if (input.charAt(endposition) == '$') {
                    lastDollar = true;
                    continue;
                }
                if (input.charAt(endposition) == '{') {
                    if (lastDollar) {
                        count++;
                    } else {
                        countBrace++;
                    }
                } else if (input.charAt(endposition) == '}') {
                    if (countBrace > 0) {
                        countBrace--;
                    } else {
                        count--;
                        if (count == 0) {
                            break;
                        }
                    }
                }
                lastDollar = false;
            }
            if (count > 0) {
                break;
            }
            if (index != startPosition) {
                parts.add(new Token(input.substring(index, startPosition), false));
            }
            String propertyName = input.substring(startPosition + 2, endposition);
            index = endposition + 1;
            parts.add(new Token(propertyName, true));
        }
        if (index != input.length()) {
            parts.add(new Token(input.substring(index), false));
        }
        return parts;
    }

}
