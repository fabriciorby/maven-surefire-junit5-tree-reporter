package org.apache.maven.plugin.surefire.loader;

import org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporterBase;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Stream;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Class created to modify the Surefire ClassLoader during Runtime.
 * Needed for {@link JUnit5StatelessTestsetInfoTreeReporterBase#clone()} method,
 * which is used when the unit tests are running using multiple threads.
 *
 * @author <a href="mailto:fabriciorby@hotmail.com">Fabrício Yamamoto (fabriciorby)</a>
 */
public class SurefireClassLoaderModifier {

    private final ClassLoader surefireClassLoader;
    private final Method addUrlMethod;

    public static final String MAVEN_SUREFIRE_JUNIT_5_TREE_REPORTER = "maven-surefire-junit5-tree-reporter";
    public static final String ADD_URL_METHOD = "addURL";

    public SurefireClassLoaderModifier(ClassLoader surefireClassLoader) throws NoSuchMethodException {
        this.surefireClassLoader = surefireClassLoader;
        this.addUrlMethod = surefireClassLoader.getClass().getDeclaredMethod(ADD_URL_METHOD, URL.class);
        this.addUrlMethod.setAccessible(true);
    }

    public void addThisToSurefireClassLoader() throws ReflectiveOperationException {
        this.addUrlMethod.invoke(surefireClassLoader, getJarUrl());
    }

    private URL getJarUrl() throws ReflectiveOperationException {
        return getThreadContextClassLoaderURLs()
                .filter(this::isMavenSurefireTreeReporterJar)
                .findFirst()
                .orElseThrow(ReflectiveOperationException::new);
    }

    private Stream<URL> getThreadContextClassLoaderURLs() {
        return Stream.of(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs());
    }

    private boolean isMavenSurefireTreeReporterJar(URL i) {
        return i.getFile().contains(MAVEN_SUREFIRE_JUNIT_5_TREE_REPORTER);
    }
}
