/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.api.internal.tasks.options;

import org.gradle.internal.typeconversion.FromStringNotationParserFactory;
import org.gradle.internal.reflect.JavaMethod;
import org.gradle.internal.reflect.JavaReflectionUtil;
import org.gradle.internal.typeconversion.NotationParser;

import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractOptionElement implements OptionElement {
    private final String optionName;
    private final String description;
    private final Class<?> optionType;
    private final NotationParser notationParser;

    public AbstractOptionElement(String optionName, Option option, Class<?> optionType, Class<?> declaringClass) {
        this.description = readDescription(option, optionName, declaringClass);
        this.optionName = optionName;
        this.optionType = optionType;
        notationParser = new FromStringNotationParserFactory(optionType).toComposite();

    }

    protected static Class<?> calculateOptionType(Class<?> type) {
        //we don't want to support "--flag true" syntax
        if (type == Boolean.class || type == Boolean.TYPE) {
            return Void.TYPE;
        } else {
            return type;
        }
    }

    public List<String> getAvailableValues() {
        List<String> describes = new ArrayList<String>();
        notationParser.describe(describes);
        return describes;
    }

    public Class<?> getOptionType() {
        return optionType;
    }

    private String readDescription(Option option, String optionName, Class<?> declaringClass) {
        try {
            return option.description();
        } catch (IncompleteAnnotationException ex) {
            throw new OptionValidationException(String.format("No description set on option '%s' at for class '%s'.", optionName, declaringClass.getName()));
        }
    }

    protected Object getParameterObject(String value) {
        NotationParser<?> notationParser = new FromStringNotationParserFactory(getOptionType()).toComposite();
        return notationParser.parseNotation(value);
    }



    protected Object invokeMethod(Object object, Method method, Object... parameterValues) {
        final JavaMethod<Object, Object> javaMethod = JavaReflectionUtil.method(Object.class, Object.class, method);
        return javaMethod.invoke(object, parameterValues);
    }

    public String getOptionName() {
        return optionName;
    }

    public String getDescription() {
        return description;
    }
}