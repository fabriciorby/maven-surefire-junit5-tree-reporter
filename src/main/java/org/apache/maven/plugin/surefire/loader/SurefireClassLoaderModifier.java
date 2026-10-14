package org.apache.maven.plugin.surefire.loader;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;

import org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoReporter;
import org.apache.maven.plugin.surefire.extensions.junit5.JUnit5StatelessTestsetInfoTreeReporter;

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
 * Needed for {@link JUnit5StatelessTestsetInfoReporter#clone()} method,
 * which is used when the unit tests are running using multiple threads.
 *
 * @author <a href="mailto:fabriciorby@hotmail.com">Fabrício Yamamoto (fabriciorby)</a>
 */
public class SurefireClassLoaderModifier {

    private final ClassLoader surefireClassLoader;
    private final Method addUrlMethod;

    public static final String ADD_URL_METHOD = "addURL";

    public SurefireClassLoaderModifier(ClassLoader surefireClassLoader) throws NoSuchMethodException {
        this.surefireClassLoader = surefireClassLoader;
        this.addUrlMethod = surefireClassLoader.getClass().getDeclaredMethod(ADD_URL_METHOD, URL.class);
        this.addUrlMethod.setAccessible(true);
    }

    public void addThisToSurefireClassLoader() throws ReflectiveOperationException {
        this.addUrlMethod.invoke(surefireClassLoader, getJarUrl());
    }

    /**
     * The jar or directory this reporter was loaded from. Matching the class loader URLs by name picks the wrong one
     * when the local repository or project path also contains the artifact name (issue #82).
     */
    URL getJarUrl() throws ReflectiveOperationException {
        CodeSource codeSource = JUnit5StatelessTestsetInfoTreeReporter.class.getProtectionDomain().getCodeSource();
        if (codeSource == null || codeSource.getLocation() == null) {
            throw new ClassNotFoundException("location of " + JUnit5StatelessTestsetInfoTreeReporter.class.getName());
        }
        return codeSource.getLocation();
    }
}
