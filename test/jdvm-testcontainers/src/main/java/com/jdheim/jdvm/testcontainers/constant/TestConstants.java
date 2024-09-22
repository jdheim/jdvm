/*
 * © 2024-2026 JDHeim.com
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

package com.jdheim.jdvm.testcontainers.constant;

import static com.jdheim.jdvm.testcontainers.property.FileProperties.MAVEN;

/**
 * Common constants for Tests
 */
public final class TestConstants {

    /** Hostname for Tests */
    public static final String HOSTNAME = "%s-test".formatted(MAVEN.getProperty("image.name"));

    /** Image Tag */
    public static final String IMAGE_TAG = "%s/%s:%s%s".formatted(MAVEN.getProperty("image.namespace"),
            MAVEN.getProperty("image.name"), MAVEN.getProperty("image.version"), MAVEN.getProperty("image.tag.ea"));

    /** Image User */
    public static final String IMAGE_USER = MAVEN.getProperty("image.user");

    /** Root User */
    public static final String ROOT_USER = "root";

    /** Image User Id */
    public static final String IMAGE_USER_ID = MAVEN.getProperty("image.user.uid");

    /** Root User Id */
    public static final String ROOT_USER_ID = "0";

    /** User Home directory */
    public static final String USER_HOME = "/home/%s".formatted(IMAGE_USER);

    /** Root Home directory */
    public static final String ROOT_HOME = "/root";

    private TestConstants() {
        throw new AssertionError();
    }

}
