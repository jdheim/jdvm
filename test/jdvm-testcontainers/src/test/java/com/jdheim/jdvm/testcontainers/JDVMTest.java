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

package com.jdheim.jdvm.testcontainers;

import static com.jdheim.jdvm.testcontainers.constant.TestConstants.HOSTNAME;
import static com.jdheim.jdvm.testcontainers.constant.TestConstants.IMAGE_USER;
import static com.jdheim.jdvm.testcontainers.constant.TestConstants.IMAGE_USER_ID;
import static com.jdheim.jdvm.testcontainers.constant.TestConstants.USER_HOME;
import static com.jdheim.jdvm.testcontainers.property.FileProperties.MAVEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Year;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.jdheim.jdvm.testcontainers.setup.JDVMContainer;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.images.builder.Transferable;

/**
 * JDVM General Tests
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class JDVMTest extends JDVMContainer {

    static final int MODE_755 = Integer.parseInt("755", 8);

    @Test
    void runningAndHealthy() {
        assertThat(getJDVM().isPrivilegedMode()).isTrue();
        assertThat(getJDVM().isRunning()).isTrue();
        assertThat(getJDVM().isHealthy()).isTrue();
    }

    @Test
    void testNoBrokenSymlinks() {
        String homeBrokenLinks = getJDVMExecutor().run("sudo find \"/home\" -xtype l").justExec();
        assertThat(homeBrokenLinks).isEmpty();

        String optBrokenLinks = getJDVMExecutor().run("sudo find \"/opt\" -xtype l").justExec();
        assertThat(optBrokenLinks).isEmpty();
    }

    @Test
    void testJdvmAptUpdateService() {
        await().atMost(30, TimeUnit.SECONDS).with().pollInterval(500, TimeUnit.MILLISECONDS).until(() -> {
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
    void testJdvmDBusSessionService() {
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
    void testLauncheeService() {
        String isActive = getJDVMExecutor().run("systemctl is-active launchee.service").exec();
        assertThat(isActive).isEqualTo("active");

        String subState = getJDVMExecutor().run("systemctl show -p SubState launchee.service").exec();
        assertThat(subState).isEqualTo("SubState=running");
    }

    @Test
    void testLauncheeConfigAtHome() {
        String launcheeConfigDir = getJDVMExecutor().ls(USER_HOME + "/.config/launchee").exec();
        assertThat(launcheeConfigDir).contains("launchee").hasLineCount(1);
    }

    @Test
    void testLicenses() {
        CharSequence[] files = new CharSequence[]{
                "LICENSE", "NOTICE"
        };
        String licensesDir = getJDVMExecutor().ls("/licenses").exec();
        assertThat(licensesDir).contains(files).hasLineCount(files.length);

        String license = getJDVMExecutor().cat("/licenses/LICENSE").exec();
        String copyright = "© 2024-%s JDHeim.com".formatted(Year.now().getValue());
        assertThat(license).contains(copyright);
        String licenseType = "Apache License, Version 2.0";
        assertThat(license).contains(licenseType);

        String notice = getJDVMExecutor().cat("/licenses/NOTICE").exec();
        assertThat(notice).contains(copyright).contains(licenseType).contains("LicenseRef-ThirdParty");
    }

    @Test
    void testJdvmConfig() {
        CharSequence[] files = new CharSequence[]{
                "betterfox-policies.json", "betterfox-user.js", "docker-daemon.json", "kitty.conf", "tealdeer-config.toml"
        };
        String jdvmConfigDir = getJDVMExecutor().ls("/etc/jdvm-config").exec();
        assertThat(jdvmConfigDir).contains(files).hasLineCount(files.length);
    }

    @Test
    void testJdvmTemplatesBase() {
        CharSequence[] files = new CharSequence[]{
                "docker-env", "dconf", "env", "p10k.zsh"
        };
        String jdvmTemplatesBaseDir = getJDVMExecutor().ls("/etc/jdvm-templates/base").exec();
        assertThat(jdvmTemplatesBaseDir).contains(files).hasLineCount(files.length);
    }

    @Test
    void testDockerEnv() {
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
    void testBinaries() {
        String binDir = getJDVMExecutor().ls("/usr/local/bin").exec();
        Set<String> binaryPaths = Arrays.stream(StringUtils.split(binDir, StringUtils.LF))
                .map("/usr/local/bin/"::concat)
                .collect(Collectors.toSet());
        assertThat(binaryPaths).as("Binaries not owned by root").filteredOn(this::isNotOwnedByRoot).isEmpty();

        CharSequence[] binaries = new CharSequence[]{
                "bat", "bun", "callgrind_annotate", "callgrind_control", "cg_annotate", "cg_diff", "cg_merge", "dive", "firefox",
                "gh", "git-filter-repo", "git-lfs", "hadolint", "helm", "jdvm-launcher", "jdvm-versions", "k3d", "kubectl",
                "launchee", "mint", "mint-sensor", "ms_print", "slim", "slim-sensor", "start-dbus-session", "terraform", "upx",
                "uv", "uvx", "valgrind", "valgrind-di-server", "valgrind-listener", "vgdb", "vgstack", "wasmtime", "wasmtime-min",
                "zig"
        };
        assertThat(binDir).contains(binaries).hasLineCount(binaries.length);
    }

    private boolean isNotOwnedByRoot(String binaryPath) {
        String ownership = getJDVMExecutor().run("stat -c %u:%g " + binaryPath).exec();
        return !"0:0".equals(ownership);
    }

    @Test
    void testJdvmTemplatesLocal() {
        CharSequence[] files = new CharSequence[]{
                ".aliases.local", ".bash_aliases.local", ".bash_env.local", ".bash_logout.local", ".bashrc.local", ".env.local",
                ".profile.local", ".zlogout.local", ".zprofile.local", ".zshaliases.local", ".zshenv.local", ".zshrc.local"
        };
        String jdvmTemplatesLocalDir = getJDVMExecutor().ls("/etc/jdvm-templates/local").exec();
        assertThat(jdvmTemplatesLocalDir).contains(files).hasLineCount(files.length);
    }

    @Test
    void testJdvmTemplatesUser() {
        CharSequence[] files = new CharSequence[]{
                ".aliases", ".bash_aliases", ".bash_env", ".env", ".ps1", ".zlogout", ".zprofile", ".zshenv", ".zshrc"
        };
        String jdvmTemplatesUserDir = getJDVMExecutor().ls("/etc/jdvm-templates/user").exec();
        assertThat(jdvmTemplatesUserDir).contains(files).hasLineCount(files.length);
    }

    @Test
    void testWhoAmI() {
        String user = getJDVMExecutor().run("whoami").exec();
        assertThat(user).isEqualTo(IMAGE_USER);

        String userId = getJDVMExecutor().run("id -u").exec();
        assertThat(userId).isEqualTo(IMAGE_USER_ID);

        String groupId = getJDVMExecutor().run("id -g").exec();
        assertThat(groupId).isEqualTo(IMAGE_USER_ID);

        String dockerGroupId = getJDVMExecutor().run("getent group docker | cut -d: -f3").exec();
        assertThat(dockerGroupId).isNotBlank();
        String groupsId = getJDVMExecutor().run("id -G").exec();
        assertThat(groupsId).isEqualTo("%s %s".formatted(IMAGE_USER_ID, dockerGroupId));
    }

    /// See [Linux Kernel Requirements](https://github.com/bpftrace/bpftrace/blob/master/INSTALL.md#linux-kernel-requirements)
    @Test
    void testBpftraceRequirements() throws Exception {
        String bpftraceVersion = getJDVMExecutor().run("bpftrace --version | sed \"s/.*v//\"").exec();
        assertThat(bpftraceVersion).matches("0\\.\\d+\\.\\d+");
        String checkKernelFeaturesScript = downloadCheckKernelFeaturesScript(bpftraceVersion);
        getJDVM().copyFileToContainer(Transferable.of(checkKernelFeaturesScript.getBytes(StandardCharsets.UTF_8), MODE_755),
                "/tmp/check_kernel_features.sh");
        String mode = getJDVMExecutor().run("stat -c %a /tmp/check_kernel_features.sh").exec();
        assertThat(mode).isEqualTo("755");

        String output = getJDVMExecutor().run("/tmp/check_kernel_features.sh").justExec();
        assumeFalse(output.contains("Could not find kernel config"), "Kernel config not available in this environment");
        assertThat(output).contains("All required features present!");
    }

    private String downloadCheckKernelFeaturesScript(String version) throws Exception {
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://raw.githubusercontent.com/bpftrace/bpftrace/refs/tags/v%s/scripts/check_kernel_features.sh".formatted(
                                    version)))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response).extracting(HttpResponse::statusCode).isEqualTo(200);
            return response.body();
        }
    }

}
