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

import static com.jdheim.jdvm.testcontainers.constant.TestConstants.IMAGE_USER;
import static com.jdheim.jdvm.testcontainers.constant.TestConstants.USER_HOME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import com.jdheim.jdvm.testcontainers.setup.JDVMContainer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Docker Entrypoint Tests
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class DockerEntrypointTest extends JDVMContainer {

    @Test
    void dockerEntrypointInitialization() throws IOException, InterruptedException {
        String dockerEntrypointLog = runDockerEntrypoint();
        assertThat(dockerEntrypointLog).contains("[STEP] ----- Start initialization -----")
                .contains("[INFO] Running /docker-entrypoint.d/01-setup-user-home.sh")
                .contains("[INFO] Running /docker-entrypoint.d/02-sync-user-templates.sh")
                .contains("[INFO] Running /docker-entrypoint.d/03-sync-skeletal-user-templates.sh")
                .contains("[INFO] Running /docker-entrypoint.d/04-sync-local-templates.sh")
                .contains("[INFO] Running /docker-entrypoint.d/05-init-oh-my-zsh.sh")
                .contains("[INFO] Running /docker-entrypoint.d/06-init-jmeter.sh")
                .contains("[INFO] Running /docker-entrypoint.d/07-save-docker-env-variables.sh")
                .doesNotContain("[INFO] Running /docker-entrypoint.d/08")
                .contains("[SUCCESS] Initialization complete")
                .doesNotContain("[STEP] ----- Start systemd -----");
    }

    private String runDockerEntrypoint() throws IOException, InterruptedException {
        getJDVMExecutor().run("sudo sed -i \"s/^  startSystemd/#&/\" \"/docker-entrypoint.d/docker-entrypoint.sh\"").exec();
        return getJDVMExecutor().run("/docker-entrypoint.d/docker-entrypoint.sh").exec();
    }

    @Test
    void testApps() throws IOException, InterruptedException {
        String homeDir = getJDVMExecutor().ls(USER_HOME).exec();
        assertThat(homeDir).contains("apps");
    }

    @Test
    void testFirefox() throws IOException, InterruptedException {
        String firefoxProfile = getJDVMExecutor().run("ls \"%s/.mozilla/firefox\" | grep \"%s\"".formatted(USER_HOME, IMAGE_USER))
                .exec();
        String firefoxProfilePath = "%s/.mozilla/firefox/%s".formatted(USER_HOME, firefoxProfile);
        String firefoxProfileDir = getJDVMExecutor().ls(firefoxProfilePath).exec();
        assertThat(firefoxProfileDir).contains("user.js");

        String userJsPath = getJDVMExecutor().symlinkPath(firefoxProfilePath + "/user.js").exec();
        assertThat(userJsPath).isEqualTo("/etc/jdvm-config/betterfox-user.js");

        String jdvmConfigDir = getJDVMExecutor().ls("/etc/jdvm-config").exec();
        assertThat(jdvmConfigDir).contains("betterfox-user.js");
    }

    @Test
    void testGnomeSettings() throws IOException, InterruptedException {
        String dconfDir = getJDVMExecutor().ls(USER_HOME + "/.config/dconf").exec();
        assertThat(dconfDir).isEqualTo("user");

        String dconfUserPath = getJDVMExecutor().symlinkPath(USER_HOME + "/.config/dconf/user").exec();
        assertThat(dconfUserPath).isEqualTo("/etc/jdvm-templates/base/dconf/user");

        String baseDconfDir = getJDVMExecutor().ls("/etc/jdvm-templates/base/dconf").exec();
        assertThat(baseDconfDir).isEqualTo("user");
    }

    @Test
    void testGnomeSettingsBackup() throws IOException, InterruptedException {
        String dconfUserFile = USER_HOME + "/.config/dconf/user";
        getJDVMExecutor().run("rm -f -- " + dconfUserFile).exec();
        getJDVMExecutor().run("touch " + dconfUserFile).exec();

        try {
            String dockerEntrypointLog = runDockerEntrypoint();
            assertThat(dockerEntrypointLog).contains("[SUCCESS] Initialization complete")
                    .doesNotContain("[STEP] ----- Start systemd -----");

            String dconfDir = getJDVMExecutor().ls(USER_HOME + "/.config/dconf").exec();
            assertThat(dconfDir).contains("user", "user.bak").hasLineCount(2);

            getJDVMExecutor().symlinkPath(dconfUserFile + ".bak").execShouldFail();
            String dconfUserPath = getJDVMExecutor().symlinkPath(dconfUserFile).exec();
            assertThat(dconfUserPath).isEqualTo("/etc/jdvm-templates/base/dconf/user");

            String baseDconfDir = getJDVMExecutor().ls("/etc/jdvm-templates/base/dconf").exec();
            assertThat(baseDconfDir).isEqualTo("user");
        } finally {
            getJDVMExecutor().run("rm -r -- " + dconfUserFile + ".bak").exec();
        }
    }

    @Test
    void testKittyConf() throws IOException, InterruptedException {
        String kittyConfigDir = getJDVMExecutor().ls(USER_HOME + "/.config/kitty").exec();
        assertThat(kittyConfigDir).isEqualTo("kitty.conf");

        String kittyConfPath = getJDVMExecutor().symlinkPath(USER_HOME + "/.config/kitty/kitty.conf").exec();
        assertThat(kittyConfPath).isEqualTo("/etc/jdvm-config/kitty.conf");

        String jdvmConfigDir = getJDVMExecutor().ls("/etc/jdvm-config").exec();
        assertThat(jdvmConfigDir).contains("kitty.conf");
    }

    @Test
    void testKubectlKrew() throws IOException, InterruptedException {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "index", "receipts", "store"
        };
        String krewDir = getJDVMExecutor().ls(USER_HOME + "/.krew").exec();
        assertThat(krewDir).contains(dirs).hasLineCount(dirs.length);

        for (CharSequence dir : dirs) {
            String homeKrewDirSymlink = "%s/.krew/%s".formatted(USER_HOME, dir);
            if ("index".contentEquals(dir)) {
                String homeKrewDirPath = getJDVMExecutor().symlinkPath(homeKrewDirSymlink).exec();
                assertThat(homeKrewDirPath).isEqualTo("/opt/krew/index");
            } else {
                getJDVMExecutor().symlinkPath(homeKrewDirSymlink).execShouldFail();
                String homeKrewDirPath = getJDVMExecutor().ls(homeKrewDirSymlink).exec();
                assertThat(homeKrewDirPath).isEmpty();
            }
        }
    }

    @Test
    void testKubectlKrewIndexBackup() throws IOException, InterruptedException {
        String krewIndexDir = USER_HOME + "/.krew/index";
        getJDVMExecutor().run("rm -rf -- " + krewIndexDir).exec();
        getJDVMExecutor().run("mkdir " + krewIndexDir).exec();
        getJDVMExecutor().run("touch " + krewIndexDir + "/test.txt").exec();

        try {
            String dockerEntrypointLog = runDockerEntrypoint();
            assertThat(dockerEntrypointLog).contains("[SUCCESS] Initialization complete")
                    .doesNotContain("[STEP] ----- Start systemd -----");

            CharSequence[] dirs = new CharSequence[]{
                    "bin", "index", "index.bak", "receipts", "store"
            };
            String krewDir = getJDVMExecutor().ls(USER_HOME + "/.krew").exec();
            assertThat(krewDir).contains(dirs).hasLineCount(dirs.length);

            for (CharSequence dir : dirs) {
                String homeKrewDirSymlink = "%s/.krew/%s".formatted(USER_HOME, dir);
                if ("index".contentEquals(dir)) {
                    String homeKrewDirPath = getJDVMExecutor().symlinkPath(homeKrewDirSymlink).exec();
                    assertThat(homeKrewDirPath).isEqualTo("/opt/krew/index");
                } else {
                    getJDVMExecutor().symlinkPath(homeKrewDirSymlink).execShouldFail();
                    String homeKrewDirPath = getJDVMExecutor().ls(homeKrewDirSymlink).exec();
                    if ("index.bak".contentEquals(dir)) {
                        assertThat(homeKrewDirPath).isEqualTo("test.txt");
                    } else {
                        assertThat(homeKrewDirPath).isEmpty();
                    }
                }
            }
        } finally {
            getJDVMExecutor().run("rm -rf -- " + krewIndexDir + ".bak").exec();
        }
    }

    @Test
    void testProjects() throws IOException, InterruptedException {
        String homeDir = getJDVMExecutor().ls(USER_HOME).exec();
        assertThat(homeDir).contains("projects");
    }

    @Test
    void testTealdeer() throws IOException, InterruptedException {
        String tealdeerConfigDir = getJDVMExecutor().ls(USER_HOME + "/.config/tealdeer").exec();
        assertThat(tealdeerConfigDir).isEqualTo("config.toml");

        String tealdeerConfigTomlPath = getJDVMExecutor().symlinkPath(USER_HOME + "/.config/tealdeer/config.toml").exec();
        assertThat(tealdeerConfigTomlPath).isEqualTo("/etc/jdvm-config/tealdeer-config.toml");

        String jdvmConfigDir = getJDVMExecutor().ls("/etc/jdvm-config").exec();
        assertThat(jdvmConfigDir).contains("tealdeer-config.toml");
    }

    @Test
    void testXdgUserDirs() throws IOException, InterruptedException {
        String configDir = getJDVMExecutor().ls(USER_HOME + "/.config").exec();
        assertThat(configDir).contains("user-dirs.dirs");

        String userDirsPath = getJDVMExecutor().symlinkPath(USER_HOME + "/.config/user-dirs.dirs").exec();
        assertThat(userDirsPath).isEqualTo("/etc/xdg/user-dirs.defaults");

        String xdgDir = getJDVMExecutor().ls("/etc/xdg").exec();
        assertThat(xdgDir).contains("user-dirs.defaults");
    }

}
