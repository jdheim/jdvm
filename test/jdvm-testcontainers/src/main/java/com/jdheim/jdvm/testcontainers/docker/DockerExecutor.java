/*
 * © 2024-2025 JDHeim.com
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

package com.jdheim.jdvm.testcontainers.docker;

import static com.jdheim.jdvm.testcontainers.constant.TestConstants.IMAGE_USER;
import static com.jdheim.jdvm.testcontainers.constant.TestConstants.USER_HOME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.AbstractIntegerAssert;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ExecConfig;
import org.testcontainers.containers.GenericContainer;

/**
 * A utility class for executing shell commands inside a Docker container.
 * Provides a fluent API to configure the shell and command to be executed within a specified container.
 * This class is designed to facilitate command execution within containers while ensuring a clean API for users.
 */
public final class DockerExecutor {

    private DockerExecutor() {
        throw new AssertionError();
    }

    /**
     * Initializes the execution of a shell command within the specified container.
     */
    public static CommandStep in(GenericContainer<?> container) {
        return new Steps(container);
    }

    /**
     * A class that implements the steps to execute shell commands inside a running container.
     * Provides a fluent API for defining the command to be executed and executing it in a specified shell.
     */
    private static class Steps implements CommandStep, ExecStep {

        private static final Shell DEFAULT_SHELL = Shell.BASH;

        private final GenericContainer<?> container;

        private String command;

        private boolean shouldCheckExitCode;

        private boolean shouldFail;

        private Steps(GenericContainer<?> container) {
            this.container = container;
            shouldCheckExitCode = true;
            shouldFail = false;
        }

        /**
         * Sets the command to display the contents of the specified file.
         */
        @Override
        public ExecStep cat(String path) {
            command = "cat " + path;
            return this;
        }

        /**
         * Sets the command to retrieve the executable path of the specified program inside the container.
         * The command will use the syntax `command -v <exec>` to search for the program's location.
         */
        @Override
        public ExecStep executablePath(String executable) {
            command = "command -v " + executable;
            return this;
        }

        /**
         * Sets the command to list the contents of the specified directory inside the container.
         * The command uses the 'ls -1A' format to list all entries, including hidden ones, in a single column.
         */
        @Override
        public ExecStep ls(String path) {
            command = "ls -1A " + path;
            return this;
        }

        /**
         * Sets the command to fetch the value of the specified environment variable
         * inside the container.
         */
        @Override
        public ExecStep printenv(String envVariable) {
            command = "printenv " + envVariable;
            return this;
        }

        /**
         * Sets the command to be executed inside the container.
         * This method allows direct input of a custom command as a string.
         */
        @Override
        public ExecStep run(String command) {
            this.command = command;
            return this;
        }

        /**
         * Sets the command to retrieve the symlink path of the specified symlink inside the container.
         * The command will use the syntax `readlink <symlink>` to search for the symlink's location.
         */
        @Override
        public ExecStep symlinkPath(String symlink) {
            command = "readlink " + symlink;
            return this;
        }

        /**
         * Executes the previously defined shell command inside a container.
         * The command is executed using the bash shell and working directory.
         * Ensures that the command exits with a zero exit code; otherwise, an assertion error is thrown.
         */
        @Override
        public String exec() {
            return exec(DEFAULT_SHELL);
        }

        /**
         * Executes the previously defined shell command inside a container.
         * The command is executed using the defined shell and working directory.
         * Ensures that the command exits with a zero exit code; otherwise, an assertion error is thrown.
         */
        @Override
        public String exec(Shell shell) {
            Objects.requireNonNull(shell, "Shell cannot be null");
            ExecConfig execConfig = ExecConfig.builder().user(IMAGE_USER).workDir(USER_HOME).command(new String[]{
                    shell.toString().toLowerCase(), "-l", "-c", command
            }).build();
            try {
                Container.ExecResult execResult = container.execInContainer(execConfig);
                if (shouldCheckExitCode) {
                    AbstractIntegerAssert<?> assertThatExitCode = assertThat(execResult.getExitCode()).as("\n%s%s",
                            execResult.getStdout(), execResult.getStderr());
                    if (shouldFail) {
                        assertThatExitCode.isNotZero();
                        return null;
                    }
                    assertThatExitCode.isZero();
                }
                String stdOut = execResult.getStdout().trim();
                String stdErr = execResult.getStderr().trim();
                if (StringUtils.isEmpty(stdErr)) {
                    return stdOut;
                }
                return "%s%nErrors:%n%s".formatted(stdOut, stdErr);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while executing in container", e);
            }
        }

        /**
         * Executes the previously defined shell command inside a container.
         * The command is executed using the bash shell and working directory.
         * Ensures that the command exits with a non-zero exit code; otherwise, an assertion error is thrown.
         */
        @Override
        public void execShouldFail() {
            shouldFail = true;
            exec();
        }

        /**
         * Executes the previously defined shell command inside a container.
         * The command is executed using the defined shell and working directory.
         * Ensures that the command exits with a non-zero exit code; otherwise, an assertion error is thrown.
         */
        @Override
        public void execShouldFail(Shell shell) {
            shouldFail = true;
            exec(shell);
        }

        /**
         * Executes the previously defined shell command inside a container.
         * The command is executed using the bash shell and working directory.
         * Does not check command exit code.
         */
        @Override
        public String justExec() {
            shouldCheckExitCode = false;
            return exec();
        }

        /**
         * Executes the previously defined shell command inside a container.
         * The command is executed using the defined shell and working directory.
         * Does not check command exit code.
         */
        @Override
        public String justExec(Shell shell) {
            shouldCheckExitCode = false;
            return exec(shell);
        }

    }

    /**
     * Represents a step in a fluent API for defining commands to be executed inside a container.
     */
    public interface CommandStep {

        ExecStep cat(String path);

        ExecStep executablePath(String executable);

        ExecStep ls(String path);

        ExecStep printenv(String envVariable);

        ExecStep run(String command);

        ExecStep symlinkPath(String symlink);

    }

    /**
     * Represents the final step in a fluent API for executing a shell command inside a container.
     */
    public interface ExecStep {

        String exec();

        String exec(Shell shell);

        void execShouldFail();

        void execShouldFail(Shell shell);

        String justExec();

        String justExec(Shell shell);

        /**
         * Represents the available shell environments for command execution.
         */
        enum Shell {
            BASH,
            ZSH
        }

    }

}
