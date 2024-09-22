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

package com.jdheim.jdvm.testcontainers;

import static com.jdheim.jdvm.testcontainers.constant.TestConstants.HOSTNAME;
import static com.jdheim.jdvm.testcontainers.constant.TestConstants.IMAGE_USER;
import static com.jdheim.jdvm.testcontainers.constant.TestConstants.USER_HOME;
import static com.jdheim.jdvm.testcontainers.property.FileProperties.MAVEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Year;
import java.util.concurrent.TimeUnit;
import com.jdheim.jdvm.testcontainers.setup.JDVMContainer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * JDVM General Tests
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class JDVMTest extends JDVMContainer {

    @Test
    void runningAndHealthy() {
        assertThat(getJDVM().isPrivilegedMode()).isTrue();
        assertThat(getJDVM().isRunning()).isTrue();
        assertThat(getJDVM().isHealthy()).isTrue();
    }

    @Test
    void testJdvmAptUpdateService() throws IOException, InterruptedException {
        await().atMost(20, TimeUnit.SECONDS).with().pollInterval(500, TimeUnit.MILLISECONDS).until(() -> {
            String isActive = getJDVMExecutor().run("systemctl is-active jdvm-apt-update.service").justExec();
            assertThat(isActive).isNotEmpty();
            return "inactive".equals(isActive);
        });

        String subState = getJDVMExecutor().run("systemctl show -p SubState jdvm-apt-update.service").exec();
        assertThat(subState).isEqualTo("SubState=dead");

        String execMainStatus = getJDVMExecutor().run("systemctl show -p ExecMainStatus jdvm-apt-update.service").exec();
        assertThat(execMainStatus).isEqualTo("ExecMainStatus=0");
    }

    @Test
    void testJdvmDBusSessionService() throws IOException, InterruptedException {
        String isActive = getJDVMExecutor().run("systemctl is-active jdvm-dbus-session.service").exec();
        assertThat(isActive).isEqualTo("active");

        String subState = getJDVMExecutor().run("systemctl show -p SubState jdvm-dbus-session.service").exec();
        assertThat(subState).isEqualTo("SubState=exited");

        String execMainStatus = getJDVMExecutor().run("systemctl show -p ExecMainStatus jdvm-dbus-session.service").exec();
        assertThat(execMainStatus).isEqualTo("ExecMainStatus=0");

        String xdgRuntimeDirName = "/run/user/%s".formatted(MAVEN.getProperty("image.user.uid"));
        String dbusDaemons = getJDVMExecutor().run("pgrep -a \"dbus-daemon\"").exec();
        assertThat(dbusDaemons).contains(
                        "@dbus-daemon --system --address=systemd: --nofork --nopidfile --systemd-activation --syslog-only")
                .contains("dbus-daemon --session --address=unix:path=%s/bus --nofork --nopidfile --syslog-only".formatted(
                        xdgRuntimeDirName))
                .hasLineCount(2);

        String xdgRuntimeDir = getJDVMExecutor().ls(xdgRuntimeDirName).exec();
        assertThat(xdgRuntimeDir).contains("bus");
    }

    @Test
    void testLauncheeService() throws IOException, InterruptedException {
        String isActive = getJDVMExecutor().run("systemctl is-active launchee.service").exec();
        assertThat(isActive).isEqualTo("active");

        String subState = getJDVMExecutor().run("systemctl show -p SubState launchee.service").exec();
        assertThat(subState).isEqualTo("SubState=running");
    }

    @Test
    void testLauncheeConfigAtHome() throws IOException, InterruptedException {
        String launcheeConfigDir = getJDVMExecutor().ls(USER_HOME + "/.config/launchee").exec();
        assertThat(launcheeConfigDir).contains("launchee").hasLineCount(1);
    }

    @Test
    void testLicenses() throws IOException, InterruptedException {
        CharSequence[] files = new CharSequence[]{
                "LICENSE", "NOTICE"
        };
        String licensesDir = getJDVMExecutor().ls("/licenses").exec();
        assertThat(licensesDir).contains(files).hasLineCount(files.length);

        String license = getJDVMExecutor().cat("/licenses/LICENSE").exec();
        String copyright = "© 2024-%s JDHeim".formatted(Year.now().getValue());
        assertThat(license).contains(copyright);
        String licenseType = "Apache License, Version 2.0";
        assertThat(license).contains(licenseType);

        String notice = getJDVMExecutor().cat("/licenses/NOTICE").exec();
        assertThat(notice).contains(copyright).contains(licenseType).contains("LicenseRef-ThirdParty");
    }

    @Test
    void testJdvmConfig() throws IOException, InterruptedException {
        CharSequence[] files = new CharSequence[]{
                "betterfox-policies.json", "betterfox-user.js", "docker-daemon.json", "kitty.conf", "tealdeer-config.toml"
        };
        String jdvmConfigDir = getJDVMExecutor().ls("/etc/jdvm-config").exec();
        assertThat(jdvmConfigDir).contains(files).hasLineCount(files.length);
    }

    @Test
    void testJdvmTemplatesBase() throws IOException, InterruptedException {
        CharSequence[] files = new CharSequence[]{
                "docker-env", "dconf", "env", "p10k.zsh"
        };
        String jdvmTemplatesBaseDir = getJDVMExecutor().ls("/etc/jdvm-templates/base").exec();
        assertThat(jdvmTemplatesBaseDir).contains(files).hasLineCount(files.length);
    }

    @Test
    void testDockerEnv() throws IOException, InterruptedException {
        String dockerEnv = getJDVMExecutor().cat("/etc/jdvm-templates/base/docker-env").exec();
        assertThat(dockerEnv).contains("export DEBCONF_NOWARNINGS=yes")
                .contains("export DEBIAN_FRONTEND=noninteractive")
                .contains("export PULSE_SERVER=")
                .contains("export DISPLAY=")
                .contains("export WAYLAND_DISPLAY=")
                .contains("export JDVM_USER=" + IMAGE_USER)
                .contains("export HOSTNAME=" + HOSTNAME)
                .hasLineCount(7);
    }

    @Test
    void testJdvmTemplatesLocal() throws IOException, InterruptedException {
        CharSequence[] files = new CharSequence[]{
                ".aliases.local", ".bash_aliases.local", ".bash_env.local", ".bash_logout.local", ".bashrc.local", ".env.local",
                ".profile.local", ".zlogout.local", ".zprofile.local", ".zshaliases.local", ".zshenv.local", ".zshrc.local"
        };
        String jdvmTemplatesLocalDir = getJDVMExecutor().ls("/etc/jdvm-templates/local").exec();
        assertThat(jdvmTemplatesLocalDir).contains(files).hasLineCount(files.length);
    }

    @Test
    void testJdvmTemplatesUser() throws IOException, InterruptedException {
        CharSequence[] files = new CharSequence[]{
                ".aliases", ".bash_aliases", ".bash_env", ".env", ".ps1", ".zlogout", ".zprofile", ".zshenv", ".zshrc"
        };
        String jdvmTemplatesUserDir = getJDVMExecutor().ls("/etc/jdvm-templates/user").exec();
        assertThat(jdvmTemplatesUserDir).contains(files).hasLineCount(files.length);
    }

    @Test
    void testWhoAmI() throws IOException, InterruptedException {
        String user = getJDVMExecutor().run("whoami").exec();
        assertThat(user).isEqualTo(IMAGE_USER);
    }

}
