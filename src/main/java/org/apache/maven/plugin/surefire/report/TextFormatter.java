package org.apache.maven.plugin.surefire.report;

import static org.apache.maven.surefire.shared.lang3.StringUtils.abbreviate;
import static org.apache.maven.surefire.shared.lang3.StringUtils.normalizeSpace;

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
 * Text formatter to use when print to the console is needed.
 *
 * @author <a href="mailto:fabriciorby@hotmail.com">Fabrício Yamamoto</a>
 */
public class TextFormatter {

    public static final int MAX_WIDTH = 180;
    public static final String ABBREV_MARKER = "...";

    public static String abbreviateName(String text) {
        return abbreviate(normalizeSpace(text), ABBREV_MARKER, MAX_WIDTH);
    }
}
