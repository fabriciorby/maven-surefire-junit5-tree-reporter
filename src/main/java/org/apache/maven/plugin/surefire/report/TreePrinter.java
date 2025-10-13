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

package org.apache.maven.plugin.surefire.report;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;

import static org.apache.maven.surefire.shared.utils.logging.MessageUtils.buffer;

/**
 * Tree view printer.
 *
 * @author <a href="mailto:fabriciorby@hotmail.com">Fabrício Yamamoto</a>
 */
public class TreePrinter {

    private static final int $ = 36;
    private final ConsoleLogger consoleLogger;
    private final ReporterOptions options;

    public TreePrinter(ConsoleLogger consoleLogger, ReporterOptions options) {
        this.consoleLogger = consoleLogger;
        this.options = options;
    }

    public void printTests(Node node) {
        new ActualTreePrinter(node, consoleLogger, options).printAndRemoveChild();
    }
}
