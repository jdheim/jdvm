/*
 * © 2024-2025 JDHeim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jdheim.jdvm.testcontainers.property;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

/**
 * Enum representing various file properties configurations.
 *
 * <p>Each constant in this enum corresponds to a specific properties file
 * that can be loaded and queried for property values.</p>
 *
 * <p>The enum lazily loads the properties file when the {@code getProperty} method is called.
 * If a system property with the same name exists, it overrides the value from the properties file.</p>
 */
public enum FileProperties {

    /** Properties from the maven.properties file generated with Maven execution id="generate-maven-properties" */
    MAVEN("maven.properties");

    private final String fileName;

    private Properties properties;

    FileProperties(String fileName) {
        this.fileName = fileName;
    }

    public String getProperty(String propertyName) {
        if (properties == null) {
            initProperties();
        }
        String systemProperty = System.getProperty(propertyName);
        if (StringUtils.isNotEmpty(systemProperty)) {
            return systemProperty;
        }
        return this.properties.getProperty(propertyName);
    }

    private void initProperties() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            this.properties = new Properties();
            this.properties.load(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
