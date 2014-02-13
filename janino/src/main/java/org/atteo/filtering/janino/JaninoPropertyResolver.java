/*
 * Copyright 2011 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.filtering.janino;

import java.lang.reflect.InvocationTargetException;

import org.atteo.filtering.PrefixedPropertyResolver;
import org.atteo.filtering.PropertyFilter;
import org.atteo.filtering.PropertyNotFoundException;
import org.atteo.filtering.PropertyResolver;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;

/**
 * Java expression evaluation {@link PropertyResolver}.
 *
 * <p>
 * By default not recognized properties are silently ignored. To force to treat
 * the expression as Java prefix it with 'java:'.
 * </p>
 */
public class JaninoPropertyResolver implements PrefixedPropertyResolver {
	private static final String prefix = "java:";
	private boolean useWithoutPrefix = false;

	public JaninoPropertyResolver() {
	}

	/**
	 * If useWithoutPrefix will be set to false, this resolver will try to compile and execute every
	 * property. It will not report errors for properties not prefixed with 'java:'.
	 * @param useWithoutPrefix If false, 'java:' prefix will not be needed.
	 */
	public JaninoPropertyResolver(boolean useWithoutPrefix) {
		this.useWithoutPrefix = useWithoutPrefix;
	}

	@Override
	public String getPrefix() {
		if (useWithoutPrefix) {
			return null;
		}
		return prefix;
	}

	@Override
	public String resolveProperty(String name, PropertyFilter resolver) throws PropertyNotFoundException {
		boolean throwErrors = false;
		if (name.startsWith(prefix)) {
			name = name.substring(prefix.length());
			throwErrors = true;
		} else if (!useWithoutPrefix) {
			throw new PropertyNotFoundException(name);
		}
		name = resolver.filter(name);
		name = name.trim();
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		evaluator.setExpressionType(Object.class);
		evaluator.setThrownExceptions(new Class[] { Exception.class });
		try {
			evaluator.cook(name);
			return evaluator.evaluate(new Object[] {}).toString();
		} catch (CompileException e) {
			if (!throwErrors) {
				throw new PropertyNotFoundException(name);
			}
			// TODO: new SyntaxException?
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			if (!throwErrors) {
				throw new PropertyNotFoundException(name);
			}
			throw new RuntimeException(e);
		}
	}
}
