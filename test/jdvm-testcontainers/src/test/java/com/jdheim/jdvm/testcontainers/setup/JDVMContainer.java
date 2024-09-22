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

package com.jdheim.jdvm.testcontainers.setup;

import static com.jdheim.jdvm.testcontainers.constant.TestConstants.HOSTNAME;
import static com.jdheim.jdvm.testcontainers.constant.TestConstants.IMAGE_TAG;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.jdheim.jdvm.testcontainers.docker.DockerExecutor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.utility.DockerImageName;

/**
 * The JDVMContainer class provides a pre-configured singleton Docker container.
 * It ensures that the necessary setup is applied when the container is started.
 */
public class JDVMContainer {

    private static final String CONTAINERD_VOLUME = "jdvm-testcontainers-containerd-" + UUID.randomUUID();

    private static final String DOCKER_VOLUME = "jdvm-testcontainers-docker-" + UUID.randomUUID();

    @SuppressWarnings("resource")
    private static final GenericContainer<?> JDVM = new GenericContainer<>(DockerImageName.parse(IMAGE_TAG)).withPrivilegedMode(
                    true)
            .withCreateContainerCmdModifier(cmd -> {
                cmd.withHostName(HOSTNAME);
                cmd.getHostConfig().withMounts(List.of(
                        new Mount().withType(MountType.VOLUME).withSource(CONTAINERD_VOLUME).withTarget("/var/lib/containerd"),
                        new Mount().withType(MountType.VOLUME).withSource(DOCKER_VOLUME).withTarget("/var/lib/docker")));
            })
            .withEnv("HEADLESS", Boolean.TRUE.toString())
            .waitingFor(Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(5 * 60)));

    static {
        getJDVM().start();
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(JDVMContainer::stopAndRemoveVolumes));
    }

    private static void stopAndRemoveVolumes() {
        try {
            getJDVM().stop();
        } finally {
            try (DockerClient dockerClient = DockerClientFactory.instance().client()) {
                try {
                    dockerClient.removeVolumeCmd(CONTAINERD_VOLUME).exec();
                } finally {
                    dockerClient.removeVolumeCmd(DOCKER_VOLUME).exec();
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Provides access to the pre-configured JDVM container.
     * This container is a singleton instance and is started during class initialization to
     * ensure it is running and ready for use in related operations and tests.
     */
    protected static GenericContainer<?> getJDVM() {
        return JDVM;
    }

    /**
     * Provides a command execution step for interacting with the pre-configured JDVM container.
     * This method initializes a fluent API for defining and executing shell commands within the container.
     */
    protected static DockerExecutor.CommandStep getJDVMExecutor() {
        return DockerExecutor.in(getJDVM());
    }

}
