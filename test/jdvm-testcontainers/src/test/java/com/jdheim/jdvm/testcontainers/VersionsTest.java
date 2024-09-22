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

import static com.jdheim.jdvm.testcontainers.constant.TestConstants.IMAGE_USER;
import static com.jdheim.jdvm.testcontainers.constant.TestConstants.USER_HOME;
import static com.jdheim.jdvm.testcontainers.property.FileProperties.MAVEN;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Stream;
import com.jdheim.jdvm.testcontainers.setup.JDVMContainer;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * JDVM Versions Tests
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class VersionsTest extends JDVMContainer {

    @Test
    void runningAndHealthy() {
        assertThat(getJDVM().isPrivilegedMode()).isTrue();
        assertThat(getJDVM().isRunning()).isTrue();
        assertThat(getJDVM().isHealthy()).isTrue();
    }

    @Test
    void testJDVMVersion() {
        String jdvmVersion = getJDVMExecutor().cat("/etc/versions/%s.version".formatted(MAVEN.getProperty("image.name"))).exec();
        assertThat(jdvmVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("image.version"));
    }

    @Test
    void testUbuntu() {
        String ubuntuVersion = getJDVMExecutor().run("grep \"VERSION=\" \"/etc/os-release\" | sed \"s/.*=\\\"//;s/ .*//\"")
                .exec();
        assertThat(ubuntuVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("ubuntu.version"));
    }

    @Test
    void testKitty() {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "lib"
        };
        String kittyDir = getJDVMExecutor().ls("/opt/kitty").exec();
        assertThat(kittyDir).contains(dirs).hasLineCount(dirs.length);

        String kittyConf = getJDVMExecutor().cat(USER_HOME + "/.config/kitty/kitty.conf").exec();
        assertThat(kittyConf).contains("font_family MesloLGS NF");

        String kittyVersion = getJDVMExecutor().run("kitty --version | sed \"s/kitty //;s/ .*//\"").exec();
        assertThat(kittyVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("kitty.version"));

        String kittenVersion = getJDVMExecutor().run("kitten --version | sed \"s/kitten //;s/ .*//\"").exec();
        assertThat(kittenVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("kitty.version"));
    }

    @Test
    void testTmux() {
        String tmuxPath = getJDVMExecutor().executablePath("tmux").exec();
        assertThat(tmuxPath).isEqualTo("/usr/bin/tmux");

        String tmuxVersion = getJDVMExecutor().run("tmux -V | sed \"s/tmux //\"").exec();
        assertThat(tmuxVersion).isNotEmpty().matches("3\\.\\d+[a-z]?");
    }

    @Test
    void testFirefox() {
        String firefoxPath = getJDVMExecutor().executablePath("firefox").exec();
        assertThat(firefoxPath).isEqualTo("/usr/local/bin/firefox");

        String env = getJDVMExecutor().cat("/etc/jdvm-templates/base/env").exec();
        assertThat(env).contains("export FIREFOX_PROFILE=\"%s\"".formatted(IMAGE_USER));

        String firefoxVersion = getJDVMExecutor().run("firefox --version | sed \"s/.* //\"").exec();
        assertThat(firefoxVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("firefox.version"));
    }

    @Test
    void testUpx() {
        String upxPath = getJDVMExecutor().executablePath("upx").exec();
        assertThat(upxPath).isEqualTo("/usr/local/bin/upx");

        String upxVersion = getJDVMExecutor().run("upx --version | grep -m 1 \"upx\" | sed \"s/.* //\"").exec();
        assertThat(upxVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("upx.version"));
    }

    @Test
    void testGit() {
        String gitPath = getJDVMExecutor().executablePath("git").exec();
        assertThat(gitPath).isEqualTo("/usr/bin/git");

        String bashCompletionDir = getJDVMExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("git-prompt");

        String gitVersion = getJDVMExecutor().run("git version | sed \"s/.*version //\"").exec();
        assertThat(gitVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("git.version"));
    }

    @Test
    void testGitFilterRepo() {
        String gitFilterRepoPath = getJDVMExecutor().executablePath("git-filter-repo").exec();
        assertThat(gitFilterRepoPath).isEqualTo("/usr/local/bin/git-filter-repo");

        String gitFilterRepoVersion = getJDVMExecutor().cat("/etc/versions/git-filter-repo.version").exec();
        assertThat(gitFilterRepoVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("git-filter-repo.version"));
    }

    @Test
    void testGitLFS() {
        String gitLfsPath = getJDVMExecutor().executablePath("git-lfs").exec();
        assertThat(gitLfsPath).isEqualTo("/usr/local/bin/git-lfs");

        String gitLfsVersion = getJDVMExecutor().run("git lfs version | sed \"s/.*\\///;s/ (.*//\"").exec();
        assertThat(gitLfsVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("git-lfs.version"));
    }

    @Test
    void testGitHubCLI() {
        String ghPath = getJDVMExecutor().executablePath("gh").exec();
        assertThat(ghPath).isEqualTo("/usr/local/bin/gh");

        String bashCompletionDir = getJDVMExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("gh");

        String ghVersion = getJDVMExecutor().run("gh --version | grep gh | sed \"s/.*version //;s/ (.*//\"").exec();
        assertThat(ghVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("github-cli.version"));
    }

    @Test
    void testSdkMan() {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "candidates", "contrib", "etc", "ext", "libexec", "src", "tmp", "var"
        };
        String sdkManDir = getJDVMExecutor().ls("/opt/sdkman").exec();
        assertThat(sdkManDir).contains(dirs).hasLineCount(dirs.length);

        String homeSdkManDir = getJDVMExecutor().ls(USER_HOME + "/.sdkman").exec();
        assertThat(homeSdkManDir).contains(dirs).hasLineCount(dirs.length);

        for (CharSequence dir : dirs) {
            String homeSdkManDirSymlink = "%s/.sdkman/%s".formatted(USER_HOME, dir);
            if ("candidates".contentEquals(dir)) {
                getJDVMExecutor().symlinkPath(homeSdkManDirSymlink).execShouldFail();
                String homeSdkManDirPath = getJDVMExecutor().ls(homeSdkManDirSymlink).exec();
                assertThat(homeSdkManDirPath).as(homeSdkManDirSymlink).isEmpty();
            } else {
                String homeSdkManDirPath = getJDVMExecutor().symlinkPath(homeSdkManDirSymlink).exec();
                assertThat(homeSdkManDirPath).isEqualTo("/opt/sdkman/" + dir);
            }
        }

        String sdkManDirEnv = getJDVMExecutor().printenv("SDKMAN_DIR").exec();
        assertThat(sdkManDirEnv).isEqualTo(USER_HOME + "/.sdkman");

        String sdkManConfig = getJDVMExecutor().cat(USER_HOME + "/.sdkman/etc/config").exec();
        assertThat(sdkManConfig).contains("sdkman_auto_answer=true")
                .contains("sdkman_auto_env=true")
                .contains("sdkman_colour_enable=false")
                .contains("sdkman_curl_connect_timeout=10")
                .contains("sdkman_curl_max_time=120")
                .contains("sdkman_selfupdate_feature=false");

        Set<String> executables = Set.of(USER_HOME + "/.sdkman/bin/sdkman-init.sh", USER_HOME + "/.sdkman/src/sdkman-list.sh",
                USER_HOME + "/.sdkman/src/sdkman-upgrade.sh");
        for (String executable : executables) {
            String sdkManExecutable = getJDVMExecutor().cat(executable).exec();
            assertThat(sdkManExecutable).containsOnlyOnce("$(find").containsOnlyOnce("$(find -L");
        }

        String sdkManVersion = getJDVMExecutor().cat(USER_HOME + "/.sdkman/var/version").exec();
        assertThat(sdkManVersion).isNotEmpty().matches("5\\.\\d+\\.\\d+");

        String sdkVersion = getJDVMExecutor().run("sdk version | grep \"script\" | sed \"s/.* //\"").exec();
        assertThat(sdkVersion).isNotEmpty().matches("5\\.\\d+\\.\\d+").isEqualTo(sdkManVersion);
    }

    @Test
    void testJdk() {
        CharSequence jdkLtsVersionWithDistribution =
                MAVEN.getProperty("jdk-lts.version") + "-" + MAVEN.getProperty("jdk.distribution");
        CharSequence jdkStsVersionWithDistribution =
                MAVEN.getProperty("jdk-sts.version") + "-" + MAVEN.getProperty("jdk.distribution");
        CharSequence jdkLtsLegacyVersionWithDistribution =
                MAVEN.getProperty("jdk-lts-legacy.version") + "-" + MAVEN.getProperty("jdk.distribution");
        CharSequence[] dirs = new CharSequence[]{
                "current", jdkLtsVersionWithDistribution, jdkStsVersionWithDistribution, jdkLtsLegacyVersionWithDistribution
        };
        String jdkDir = getJDVMExecutor().ls("/opt/java").exec();
        assertThat(jdkDir).contains(dirs);

        String sdkmanCandidatesDir = getJDVMExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("java");

        String jdkCurrentPath = getJDVMExecutor().symlinkPath("/opt/java/current").exec();
        assertThat(jdkCurrentPath).isNotEmpty().isEqualTo(jdkLtsVersionWithDistribution);

        String jdkLtsVersion = getJDVMExecutor().run("java --version | grep \"openjdk\" | sed \"s/openjdk \\([^ ]*\\) .*/\\1/\"")
                .exec();
        assertThat(jdkLtsVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("jdk-lts.version"));

        String jdkStsVersion = getJDVMExecutor().run("/opt/java/" + jdkStsVersionWithDistribution +
                "/bin/java --version | grep \"openjdk\" | sed \"s/openjdk \\([^ ]*\\) .*/\\1/\"").exec();
        if (StringUtils.isNotEmpty(jdkStsVersion)) {
            if (!jdkStsVersion.contains("Errors")) {
                assertThat(jdkStsVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("jdk-sts.version"));
            } else {
                jdkStsVersion = StringUtils.EMPTY;
            }
        }

        String jdkLtsLegacyVersion = getJDVMExecutor().run("/opt/java/" + jdkLtsLegacyVersionWithDistribution +
                "/bin/java --version | grep \"openjdk\" | sed \"s/openjdk \\([^ ]*\\) .*/\\1/\"").exec();
        if (StringUtils.isNotEmpty(jdkLtsLegacyVersion)) {
            if (!jdkLtsLegacyVersion.contains("Errors")) {
                assertThat(jdkLtsLegacyVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("jdk-lts-legacy.version"));
            } else {
                jdkLtsLegacyVersion = StringUtils.EMPTY;
            }
        }

        assertThat(Stream.of(jdkStsVersion, jdkLtsLegacyVersion)).filteredOn(StringUtils::isNotEmpty)
                .hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void testJBang() {
        String jbangPath = getJDVMExecutor().executablePath("jbang").exec();
        assertThat(jbangPath).isEqualTo("/opt/jbang/bin/jbang");

        String sdkmanCandidatesDir = getJDVMExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("jbang");

        String jbangVersion = getJDVMExecutor().run("jbang version").exec();
        assertThat(jbangVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("jbang.version"));
    }

    @Test
    void testJMeter() {
        String jmeterPath = getJDVMExecutor().executablePath("jmeter").exec();
        assertThat(jmeterPath).isEqualTo("/opt/jmeter/bin/jmeter");

        String jmeterDir = getJDVMExecutor().ls(USER_HOME + "/.java/.userPrefs/org/apache/jmeter").exec();
        assertThat(jmeterDir).contains("prefs.xml");

        String homeDir = getJDVMExecutor().ls(USER_HOME).exec();
        assertThat(homeDir).doesNotContain("jmeter.log");

        String sdkmanCandidatesDir = getJDVMExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("jmeter");

        String jmeterVersion = getJDVMExecutor().run(
                        "jmeter -n --version -j /dev/null 2>/dev/null | grep -m 1 -E \"[0-9]+\" | sed \"s/.* \\([0-9]\\+\\)/\\1/\"")
                .exec();
        assertThat(jmeterVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("jmeter.version"));
    }

    @Test
    void testJReleaser() {
        String jreleaserPath = getJDVMExecutor().executablePath("jreleaser").exec();
        assertThat(jreleaserPath).isEqualTo("/opt/jreleaser/bin/jreleaser");

        String sdkmanCandidatesDir = getJDVMExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("jreleaser");

        String jreleaserVersion = getJDVMExecutor().run("jreleaser --version | grep jreleaser | sed \"s/.* //\"").exec();
        assertThat(jreleaserVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("jreleaser.version"));
    }

    @Test
    void testGradle() {
        String gradlePath = getJDVMExecutor().executablePath("gradle").exec();
        assertThat(gradlePath).isEqualTo("/opt/gradle/bin/gradle");

        String sdkmanCandidatesDir = getJDVMExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("gradle");

        String gradleVersion = getJDVMExecutor().run(
                "gradle --version | grep -m 1 \"Gradle \" | sed -e \"s/.* //\" -e \"s/\\!//\"").exec();
        assertThat(gradleVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("gradle.version"));
    }

    @Test
    void testMaven() {
        String mvnPath = getJDVMExecutor().executablePath("mvn").exec();
        assertThat(mvnPath).isEqualTo("/opt/maven/bin/mvn");

        String mavenBinDir = getJDVMExecutor().ls("/opt/maven/bin").exec();
        assertThat(mavenBinDir).doesNotContain("mvn.cmd");

        String sdkmanCandidatesDir = getJDVMExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("maven");

        String m2Dir = getJDVMExecutor().ls(USER_HOME + "/.m2").exec();
        assertThat(m2Dir).contains("repository");

        String mvnVersion = getJDVMExecutor().run("mvn -B -v | grep \"Apache Maven\" | sed \"s/Apache Maven \\([^ ]*\\).*/\\1/\"")
                .exec();
        assertThat(mvnVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("mvn.version"));
    }

    @Test
    void testSpringBootCLI() {
        String springPath = getJDVMExecutor().executablePath("spring").exec();
        assertThat(springPath).isEqualTo("/opt/springboot/bin/spring");

        String sdkmanCandidatesDir = getJDVMExecutor().ls("/opt/sdkman/candidates").exec();
        assertThat(sdkmanCandidatesDir).doesNotContain("springboot");

        String springVersion = getJDVMExecutor().run("spring --version | sed \"s/.*v//\"").exec();
        assertThat(springVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("spring-boot-cli.version"));
    }

    @Test
    void testAsyncProfiler() {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "lib"
        };
        String asyncProfilerDir = getJDVMExecutor().ls("/opt/async-profiler").exec();
        assertThat(asyncProfilerDir).contains(dirs).hasLineCount(dirs.length);

        String asprofPath = getJDVMExecutor().executablePath("asprof").exec();
        assertThat(asprofPath).isEqualTo("/opt/async-profiler/bin/asprof");

        String jfrconvPath = getJDVMExecutor().executablePath("jfrconv").exec();
        assertThat(jfrconvPath).isEqualTo("/opt/async-profiler/bin/jfrconv");

        String sysctlDir = getJDVMExecutor().ls("/etc/sysctl.d").exec();
        assertThat(sysctlDir).contains("999-async-profiler.conf");

        String kernelPerfEventParanoid = getJDVMExecutor().run("sysctl kernel.perf_event_paranoid").exec();
        assertThat(kernelPerfEventParanoid).isEqualTo("kernel.perf_event_paranoid = 1");

        String kernelKptrRestrict = getJDVMExecutor().run("sysctl kernel.kptr_restrict").exec();
        assertThat(kernelKptrRestrict).isEqualTo("kernel.kptr_restrict = 0");

        String asyncProfilerVersion = getJDVMExecutor().run("asprof --version | sed \"s/.*profiler //;s/ .*//\"").exec();
        assertThat(asyncProfilerVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("async-profiler.version"));
    }

    @Test
    void testKafka() {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "config", "libs"
        };
        String kafkaDir = getJDVMExecutor().ls("/opt/kafka").exec();
        assertThat(kafkaDir).contains(dirs).hasLineCount(dirs.length);

        String kafkaBinDir = getJDVMExecutor().ls("/opt/kafka/bin").exec();
        assertThat(kafkaBinDir).doesNotContain("windows");

        String kafkaTopicsPath = getJDVMExecutor().executablePath("kafka-topics.sh").exec();
        assertThat(kafkaTopicsPath).isEqualTo("/opt/kafka/bin/kafka-topics.sh");

        String kafkaVersion = getJDVMExecutor().run(
                "ls \"/opt/kafka/libs\" | grep -m 1 \"kafka-server\" | sed \"s/.*-//;s/.jar//\"").exec();
        assertThat(kafkaVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("kafka.version"));
    }

    @Test
    void testBun() {
        String bunPath = getJDVMExecutor().executablePath("bun").exec();
        assertThat(bunPath).isEqualTo("/usr/local/bin/bun");

        String bunVersion = getJDVMExecutor().run("bun --version").exec();
        assertThat(bunVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("bun.version"));
    }

    @Test
    void testNvm() {
        CharSequence[] files = new CharSequence[]{
                "nvm.sh", "nvm-exec"
        };
        String nvmDir = getJDVMExecutor().ls("/opt/nvm").exec();
        assertThat(nvmDir).contains(files).hasLineCount(files.length);

        String homeNvmDir = getJDVMExecutor().ls(USER_HOME + "/.nvm").exec();
        assertThat(homeNvmDir).contains(files).hasLineCount(files.length);

        for (CharSequence file : files) {
            String homeNvmDirSymlink = "%s/.nvm/%s".formatted(USER_HOME, file);
            String homeNvmDirPath = getJDVMExecutor().symlinkPath(homeNvmDirSymlink).exec();
            assertThat(homeNvmDirPath).isEqualTo("/opt/nvm/%s".formatted(file));
        }

        String nvmPath = getJDVMExecutor().executablePath("nvm").exec();
        assertThat(nvmPath).isEqualTo("nvm");

        String nvmDirEnv = getJDVMExecutor().printenv("NVM_DIR").exec();
        assertThat(nvmDirEnv).isEqualTo(USER_HOME + "/.nvm");

        String bashCompletionDir = getJDVMExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("nvm");

        String nvmVersion = getJDVMExecutor().run("nvm --version").exec();
        assertThat(nvmVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("nvm.version"));
    }

    @Test
    void testNode() {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "etc", "include", "lib"
        };
        String nodeDir = getJDVMExecutor().ls("/opt/node").exec();
        assertThat(nodeDir).contains(dirs).hasLineCount(dirs.length);

        String nodePath = getJDVMExecutor().executablePath("node").exec();
        assertThat(nodePath).isEqualTo("/opt/node/bin/node");

        String nodeVersion = getJDVMExecutor().run("node --version | sed \"s/v//\"").exec();
        assertThat(nodeVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("node.version"));
    }

    @Test
    void testNpm() {
        String npmPath = getJDVMExecutor().executablePath("npm").exec();
        assertThat(npmPath).isEqualTo("/opt/node/bin/npm");

        String npxPath = getJDVMExecutor().executablePath("npx").exec();
        assertThat(npxPath).isEqualTo("/opt/node/bin/npx");

        String npmrcSystem = getJDVMExecutor().cat("/opt/node/etc/npmrc").exec();
        assertThat(npmrcSystem).contains("ignore-scripts=true");

        String npmrcHome = getJDVMExecutor().cat(USER_HOME + "/.npmrc").exec();
        assertThat(npmrcHome).contains("ignore-scripts=true");

        String bashCompletionDir = getJDVMExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("npm");

        String npmVersion = getJDVMExecutor().run("npm --version").exec();
        assertThat(npmVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("npm.version"));

        String npxVersion = getJDVMExecutor().run("npx --version").exec();
        assertThat(npxVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("npm.version"));
    }

    @Test
    void testNpq() {
        String npqPath = getJDVMExecutor().executablePath("npq").exec();
        assertThat(npqPath).isEqualTo("/opt/node/bin/npq");

        String npqVersion = getJDVMExecutor().run("npq --version").exec();
        assertThat(npqVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("npq.version"));
    }

    @Test
    void testPnpm() {
        String pnpmPath = getJDVMExecutor().executablePath("pnpm").exec();
        assertThat(pnpmPath).isEqualTo("/opt/node/bin/pnpm");

        String pnpxPath = getJDVMExecutor().executablePath("pnpx").exec();
        assertThat(pnpxPath).isEqualTo("/opt/node/bin/pnpx");

        String bashCompletionDir = getJDVMExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("pnpm");

        String pnpmVersion = getJDVMExecutor().run("pnpm --version").exec();
        assertThat(pnpmVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("pnpm.version"));
    }

    @Test
    void testYarn() {
        String yarnPath = getJDVMExecutor().executablePath("yarn").exec();
        assertThat(yarnPath).isEqualTo("/opt/node/bin/yarn");

        String yarnpkgPath = getJDVMExecutor().executablePath("yarnpkg").exec();
        assertThat(yarnpkgPath).isEqualTo("/opt/node/bin/yarnpkg");

        String yarnRc = getJDVMExecutor().cat(USER_HOME + "/.yarnrc.yml").exec();
        assertThat(yarnRc).contains("enableTelemetry: false", "enableScripts: false");

        String yarnVersion = getJDVMExecutor().run("yarn --version").exec();
        assertThat(yarnVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("yarn.version"));

        String yarnpkgVersion = getJDVMExecutor().run("yarnpkg --version").exec();
        assertThat(yarnpkgVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("yarn.version"));
    }

    @Test
    void testGulpCLI() {
        String gulpPath = getJDVMExecutor().executablePath("gulp").exec();
        assertThat(gulpPath).isEqualTo("/opt/node/bin/gulp");

        String gulpVersion = getJDVMExecutor().run("gulp --version | grep \"CLI\" | sed \"s/.*: //\"").exec();
        assertThat(gulpVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("gulp-cli.version"));
    }

    @Test
    void testPython() {
        String pythonPath = getJDVMExecutor().executablePath("python3").exec();
        assertThat(pythonPath).isEqualTo("/usr/bin/python3");

        String pythonVersion = getJDVMExecutor().run("python3 --version | sed \"s/.* //\"").exec();
        assertThat(pythonVersion).isNotEmpty().matches("3\\.\\d+\\.\\d+");
    }

    @Test
    void testPip() {
        String pipPath = getJDVMExecutor().executablePath("pip").exec();
        assertThat(pipPath).isEqualTo("/usr/bin/pip");

        String pipVersion = getJDVMExecutor().run("pip --version | awk '{print $2}'").exec();
        assertThat(pipVersion).isNotEmpty().matches("25\\.\\d+\\.\\d+");
    }

    @Test
    void testUv() {
        String uvPath = getJDVMExecutor().executablePath("uv").exec();
        assertThat(uvPath).isEqualTo("/usr/local/bin/uv");

        String uvxPath = getJDVMExecutor().executablePath("uvx").exec();
        assertThat(uvxPath).isEqualTo("/usr/local/bin/uvx");

        String uvVersion = getJDVMExecutor().run("uv --version | sed \"s/.* //\"").exec();
        assertThat(uvVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("uv.version"));
    }

    @Test
    void testGCC() {
        String gccPath = getJDVMExecutor().executablePath("gcc").exec();
        assertThat(gccPath).isEqualTo("/usr/bin/gcc");

        String gPlusPlusPath = getJDVMExecutor().executablePath("g++").exec();
        assertThat(gPlusPlusPath).isEqualTo("/usr/bin/g++");

        String gccVersion = getJDVMExecutor().run("gcc --version | grep -m1 -i gcc | sed \"s/.* //\"").exec();
        assertThat(gccVersion).isNotEmpty().matches("15\\.\\d+\\.\\d+");

        String gPlusPlusVersion = getJDVMExecutor().run("g++ --version | grep -m1 -i g++ | sed \"s/.* //\"").exec();
        assertThat(gPlusPlusVersion).isNotEmpty().matches("15\\.\\d+\\.\\d+").isEqualTo(gccVersion);
    }

    @Test
    void testGDB() {
        String gdbPath = getJDVMExecutor().executablePath("gdb").exec();
        assertThat(gdbPath).isEqualTo("/usr/bin/gdb");

        String gdbVersion = getJDVMExecutor().run("gdb --version | grep -m1 -i gdb | sed \"s/.* //\"").exec();
        assertThat(gdbVersion).isNotEmpty().matches("16\\.\\d+");
    }

    @Test
    void testMake() {
        String makePath = getJDVMExecutor().executablePath("make").exec();
        assertThat(makePath).isEqualTo("/usr/bin/make");

        String makeVersion = getJDVMExecutor().run("make --version | grep -m1 -i make | sed \"s/.* //\"").exec();
        assertThat(makeVersion).isNotEmpty().matches("4\\.\\d+\\.\\d+");
    }

    @Test
    void testCMake() {
        String cmakePath = getJDVMExecutor().executablePath("cmake").exec();
        assertThat(cmakePath).isEqualTo("/usr/bin/cmake");

        String cmakeVersion = getJDVMExecutor().run("cmake --version | grep -m1 -i cmake | sed \"s/.* //\"").exec();
        assertThat(cmakeVersion).isNotEmpty().matches("3\\.\\d+\\.\\d+");
    }

    @Test
    void testValgrind() {
        String valgrindPath = getJDVMExecutor().executablePath("valgrind").exec();
        assertThat(valgrindPath).isEqualTo("/usr/local/bin/valgrind");

        String valgrindDiServerPath = getJDVMExecutor().executablePath("valgrind-di-server").exec();
        assertThat(valgrindDiServerPath).isEqualTo("/usr/local/bin/valgrind-di-server");

        String valgrindListenerPath = getJDVMExecutor().executablePath("valgrind-listener").exec();
        assertThat(valgrindListenerPath).isEqualTo("/usr/local/bin/valgrind-listener");

        String callgrindAnnotatePath = getJDVMExecutor().executablePath("callgrind_annotate").exec();
        assertThat(callgrindAnnotatePath).isEqualTo("/usr/local/bin/callgrind_annotate");

        String callgrindControlPath = getJDVMExecutor().executablePath("callgrind_control").exec();
        assertThat(callgrindControlPath).isEqualTo("/usr/local/bin/callgrind_control");

        String cgAnnotatePath = getJDVMExecutor().executablePath("cg_annotate").exec();
        assertThat(cgAnnotatePath).isEqualTo("/usr/local/bin/cg_annotate");

        String cgDiffPath = getJDVMExecutor().executablePath("cg_diff").exec();
        assertThat(cgDiffPath).isEqualTo("/usr/local/bin/cg_diff");

        String cgMergePath = getJDVMExecutor().executablePath("cg_merge").exec();
        assertThat(cgMergePath).isEqualTo("/usr/local/bin/cg_merge");

        String msPrintPath = getJDVMExecutor().executablePath("ms_print").exec();
        assertThat(msPrintPath).isEqualTo("/usr/local/bin/ms_print");

        String vgdbPath = getJDVMExecutor().executablePath("vgdb").exec();
        assertThat(vgdbPath).isEqualTo("/usr/local/bin/vgdb");

        String vgstackPath = getJDVMExecutor().executablePath("vgstack").exec();
        assertThat(vgstackPath).isEqualTo("/usr/local/bin/vgstack");

        String valgrindVersion = getJDVMExecutor().run("valgrind --version | sed \"s/.*-//\"").exec();
        assertThat(valgrindVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("valgrind.version"));
    }

    @Test
    void testRustToolchainManager() {
        CharSequence[] files = new CharSequence[]{
                "downloads", "settings.toml", "tmp", "toolchains", "update-hashes"
        };
        String rustupDir = getJDVMExecutor().ls("/opt/rustup").exec();
        assertThat(rustupDir).contains(files).hasLineCount(files.length);

        String homeRustupDir = getJDVMExecutor().ls(USER_HOME + "/.rustup").exec();
        assertThat(homeRustupDir).contains(files).hasLineCount(files.length);

        for (CharSequence file : files) {
            String homeRustupDirSymlink = "%s/.rustup/%s".formatted(USER_HOME, file);
            if ("settings.toml".contentEquals(file)) {
                String homeRustupDirPath = getJDVMExecutor().symlinkPath(homeRustupDirSymlink).exec();
                assertThat(homeRustupDirPath).isEqualTo("/opt/rustup/%s".formatted(file));
            } else {
                getJDVMExecutor().symlinkPath(homeRustupDirSymlink).execShouldFail();
                String homeRustupDirPath = getJDVMExecutor().ls(homeRustupDirSymlink).exec();
                if ("toolchains".contentEquals(file) || "update-hashes".contentEquals(file)) {
                    String rustToolchainName = MAVEN.getProperty("rust.version") + "-x86_64-unknown-linux-gnu";
                    assertThat(homeRustupDirPath).as(homeRustupDirSymlink).isEqualTo(rustToolchainName);
                    String rustToolchainPath = getJDVMExecutor().symlinkPath(
                            "%s/%s".formatted(homeRustupDirSymlink, rustToolchainName)).exec();
                    assertThat(rustToolchainPath).isEqualTo("/opt/rustup/%s/%s".formatted(file, rustToolchainName));
                } else {
                    assertThat(homeRustupDirPath).as(homeRustupDirSymlink).isEmpty();
                }
            }
        }

        String rustupPath = getJDVMExecutor().executablePath("rustup").exec();
        assertThat(rustupPath).isEqualTo(USER_HOME + "/.cargo/bin/rustup");

        String rustupTargetPath = getJDVMExecutor().symlinkPath(rustupPath).exec();
        assertThat(rustupTargetPath).isEqualTo("/opt/cargo/bin/rustup");

        String cargoBinaries = getJDVMExecutor().ls(USER_HOME + "/.cargo/bin").exec();
        assertThat(cargoBinaries).isEqualTo("rustup");

        String rustupHomeEnv = getJDVMExecutor().printenv("RUSTUP_HOME").exec();
        assertThat(rustupHomeEnv).isEqualTo(USER_HOME + "/.rustup");

        String bashCompletionDir = getJDVMExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("rustup");

        String rustupVersion = getJDVMExecutor().run("rustup --version 2>/dev/null | awk '{print $2}'").exec();
        assertThat(rustupVersion).isNotEmpty().matches("1\\.\\d+\\.\\d+");
    }

    @Test
    void testRust() {
        CharSequence[] files = new CharSequence[]{
                "bin", "env"
        };
        String cargoDir = getJDVMExecutor().ls("/opt/cargo").exec();
        assertThat(cargoDir).contains(files).hasLineCount(files.length);

        String homeCargoDir = getJDVMExecutor().ls(USER_HOME + "/.cargo").exec();
        assertThat(homeCargoDir).contains(files).hasLineCount(files.length);

        String cargoEnvPath = getJDVMExecutor().symlinkPath(USER_HOME + "/.cargo/env").exec();
        assertThat(cargoEnvPath).isEqualTo("/opt/cargo/env");

        String rustcPath = getJDVMExecutor().executablePath("rustc").exec();
        assertThat(rustcPath).isEqualTo("/opt/cargo/bin/rustc");

        String cargoPath = getJDVMExecutor().executablePath("cargo").exec();
        assertThat(cargoPath).isEqualTo("/opt/cargo/bin/cargo");

        String cargoHomeEnv = getJDVMExecutor().printenv("CARGO_HOME").exec();
        assertThat(cargoHomeEnv).isEqualTo(USER_HOME + "/.cargo");

        String bashCompletionDir = getJDVMExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("cargo");

        String rustcVersion = getJDVMExecutor().run("rustc --version | awk '{print $2}'").exec();
        assertThat(rustcVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("rust.version"));

        String cargoVersion = getJDVMExecutor().run("cargo --version | awk '{print $2}'").exec();
        assertThat(cargoVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("rust.version"));
    }

    @Test
    void testZig() {
        CharSequence[] files = new CharSequence[]{
                "lib", "LICENSE", "zig"
        };
        String zigDir = getJDVMExecutor().ls("/opt/zig").exec();
        assertThat(zigDir).contains(files).hasLineCount(files.length);

        String zigPath = getJDVMExecutor().executablePath("zig").exec();
        assertThat(zigPath).isEqualTo("/usr/local/bin/zig");

        String zigVersion = getJDVMExecutor().run("zig version").exec();
        assertThat(zigVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("zig.version"));
    }

    @Test
    void testGo() {
        String goPath = getJDVMExecutor().executablePath("go").exec();
        assertThat(goPath).isEqualTo("/opt/go/bin/go");

        String goPathEnv = getJDVMExecutor().printenv("GOPATH").exec();
        assertThat(goPathEnv).isEqualTo(USER_HOME + "/.go");

        String goTelemetryMode = getJDVMExecutor().cat(USER_HOME + "/.config/go/telemetry/mode").exec();
        assertThat(goTelemetryMode).isEqualTo("off");

        String goVersion = getJDVMExecutor().run("go version | sed \"s/.* go//;s/ .*//\"").exec();
        assertThat(goVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("go.version"));

        String sysctlDir = getJDVMExecutor().ls("/etc/sysctl.d").exec();
        assertThat(sysctlDir).contains("999-ptrace.conf");

        String kernelYamaPtraceScope = getJDVMExecutor().run("sysctl kernel.yama.ptrace_scope").exec();
        assertThat(kernelYamaPtraceScope).isEqualTo("kernel.yama.ptrace_scope = 0");
    }

    @Test
    void testContainerd() {
        String containerdPath = getJDVMExecutor().executablePath("containerd").exec();
        assertThat(containerdPath).isEqualTo("/usr/bin/containerd");

        String containerdVersion = getJDVMExecutor().run("containerd --version | awk '{print $3}' | sed \"s/v//\"").exec();
        assertThat(containerdVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("containerd.io.version"));
    }

    @Test
    void testDocker() {
        String dockerPath = getJDVMExecutor().executablePath("docker").exec();
        assertThat(dockerPath).isEqualTo("/usr/bin/docker");

        String bashCompletionDir = getJDVMExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("docker");

        String dockerClientVersion = getJDVMExecutor().run("docker version --format \"{{.Client.Version}}\"").exec();
        assertThat(dockerClientVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("docker.version"));

        String dockerServerVersion = getJDVMExecutor().run("docker version --format \"{{.Server.Version}}\"").exec();
        assertThat(dockerServerVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("docker.version"));

        String dockerDriver = getJDVMExecutor().run("docker system info --format \"{{.Driver}}\"").exec();
        assertThat(dockerDriver).isEqualTo("overlayfs");
    }

    @Test
    void testDockerBuildx() {
        String dockerCliPluginsDir = getJDVMExecutor().ls("/usr/libexec/docker/cli-plugins").exec();
        assertThat(dockerCliPluginsDir).contains("docker-buildx");

        String dockerBuildxVersion = getJDVMExecutor().run("docker buildx version | sed \"s/.* v//;s/ .*//\"").exec();
        assertThat(dockerBuildxVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("docker-buildx.version"));
    }

    @Test
    void testDockerCompose() {
        String dockerCliPluginsDir = getJDVMExecutor().ls("/usr/libexec/docker/cli-plugins").exec();
        assertThat(dockerCliPluginsDir).contains("docker-compose");

        String dockerComposeVersion = getJDVMExecutor().run("docker compose version --short").exec();
        assertThat(dockerComposeVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("docker-compose.version"));
    }

    @Test
    void testDockerScout() {
        String dockerCliPluginsDir = getJDVMExecutor().ls("/usr/local/lib/docker/cli-plugins").exec();
        assertThat(dockerCliPluginsDir).contains("docker-scout");

        String dockerScoutVersion = getJDVMExecutor().run("docker scout version | grep version | sed \"s/.* v//;s/ (.*//\"")
                .exec();
        assertThat(dockerScoutVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("docker-scout.version"));
    }

    @Test
    void testDive() {
        String divePath = getJDVMExecutor().executablePath("dive").exec();
        assertThat(divePath).isEqualTo("/usr/local/bin/dive");

        String diveVersion = getJDVMExecutor().run("dive --version | sed \"s/.* //\"").exec();
        assertThat(diveVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("dive.version"));
    }

    @Test
    void testHadolint() {
        String hadolintPath = getJDVMExecutor().executablePath("hadolint").exec();
        assertThat(hadolintPath).isEqualTo("/usr/local/bin/hadolint");

        String hadolintVersion = getJDVMExecutor().run("hadolint --version | sed \"s/.* //\"").exec();
        assertThat(hadolintVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("hadolint.version"));
    }

    @Test
    void testSlim() {
        String mintPath = getJDVMExecutor().executablePath("mint").exec();
        assertThat(mintPath).isEqualTo("/usr/local/bin/mint");

        String mintSensorPath = getJDVMExecutor().executablePath("mint-sensor").exec();
        assertThat(mintSensorPath).isEqualTo("/usr/local/bin/mint-sensor");

        String slimPath = getJDVMExecutor().executablePath("slim").exec();
        assertThat(slimPath).isEqualTo("/usr/local/bin/slim");

        String slimSensorPath = getJDVMExecutor().executablePath("slim-sensor").exec();
        assertThat(slimSensorPath).isEqualTo("/usr/local/bin/slim-sensor");

        String slimVersion = getJDVMExecutor().run("slim --version | sed \"s/.*version [^|]*|[^|]*|.\\.\\([^|]*\\)|.*/\\1/\"")
                .exec();
        assertThat(slimVersion).isNotEmpty().matches("1\\.\\d+\\.\\d+");
    }

    @Test
    void testKubectl() {
        String kubectlPath = getJDVMExecutor().executablePath("kubectl").exec();
        assertThat(kubectlPath).isEqualTo("/usr/local/bin/kubectl");

        String bashCompletionDir = getJDVMExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("kubectl");

        String kubectlVersion = getJDVMExecutor().run("kubectl version --client | grep \"Client Version:\" | sed \"s/.*v//\"")
                .exec();
        assertThat(kubectlVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("kubectl.version"));
    }

    @Test
    void testKubectlKrew() {
        CharSequence[] dirs = new CharSequence[]{
                "bin", "index", "receipts", "store"
        };
        String krewDir = getJDVMExecutor().ls("/opt/krew").exec();
        assertThat(krewDir).contains(dirs).hasLineCount(dirs.length);

        String homeKrewDir = getJDVMExecutor().ls(USER_HOME + "/.krew").exec();
        assertThat(homeKrewDir).contains(dirs).hasLineCount(dirs.length);

        for (CharSequence dir : dirs) {
            String homeKrewDirSymlink = "%s/.krew/%s".formatted(USER_HOME, dir);
            if ("index".contentEquals(dir)) {
                String homeKrewDirPath = getJDVMExecutor().symlinkPath(homeKrewDirSymlink).exec();
                assertThat(homeKrewDirPath).isEqualTo("/opt/krew/%s".formatted(dir));
            } else {
                getJDVMExecutor().symlinkPath(homeKrewDirSymlink).execShouldFail();
                String homeKrewDirPath = getJDVMExecutor().ls(homeKrewDirSymlink).exec();
                assertThat(homeKrewDirPath).as(homeKrewDirSymlink).isEmpty();
            }
        }

        String krewPath = getJDVMExecutor().executablePath("kubectl-krew").exec();
        assertThat(krewPath).isEqualTo("/opt/krew/bin/kubectl-krew");

        String kubectlKrewPath = getJDVMExecutor().symlinkPath("/opt/krew/bin/kubectl-krew").exec();
        assertThat(kubectlKrewPath).isEqualTo(
                "/opt/krew/store/krew/v%s/krew".formatted(MAVEN.getProperty("kubectl-krew.version")));

        String indexDefaultDir = getJDVMExecutor().ls("/opt/krew/index/default").exec();
        assertThat(indexDefaultDir).contains("plugins", "plugins.md");

        String krewYaml = getJDVMExecutor().cat("/opt/krew/receipts/krew.yaml").exec();
        assertThat(krewYaml).contains("krew is now installed");

        String krewStoreDir = getJDVMExecutor().ls(
                "/opt/krew/store/krew/v%s".formatted(MAVEN.getProperty("kubectl-krew.version"))).exec();
        assertThat(krewStoreDir).contains("LICENSE", "krew");

        String krewVersion = getJDVMExecutor().run("kubectl krew version | grep \"GitTag\" | sed \"s/.*v//\"").exec();
        assertThat(krewVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("kubectl-krew.version"));
    }

    @Test
    void testK3d() {
        String k3dPath = getJDVMExecutor().executablePath("k3d").exec();
        assertThat(k3dPath).isEqualTo("/usr/local/bin/k3d");

        String bashCompletionDir = getJDVMExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("k3d");

        String k3dVersion = getJDVMExecutor().run("k3d version | grep \"k3d\" | sed \"s/.*v//\"").exec();
        assertThat(k3dVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("k3d.version"));

        String k3sVersion = getJDVMExecutor().run("k3d version | grep \"k3s\" | sed \"s/.*v//;s/ (.*//\"").exec();
        assertThat(k3sVersion).isNotEmpty().matches("1\\.\\d+\\.\\d+-k3s1");
    }

    @Test
    void testHelm() {
        String helmPath = getJDVMExecutor().executablePath("helm").exec();
        assertThat(helmPath).isEqualTo("/usr/local/bin/helm");

        String bashCompletionDir = getJDVMExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("helm");

        String helmVersion = getJDVMExecutor().run("helm version --template=\"Version: {{.Version}}\" | sed \"s/.*v//\"").exec();
        assertThat(helmVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("helm.version"));
    }

    @Test
    void testTerraform() {
        String terraformPath = getJDVMExecutor().executablePath("terraform").exec();
        assertThat(terraformPath).isEqualTo("/usr/local/bin/terraform");

        String bashCompletionDir = getJDVMExecutor().ls("/etc/bash_completion.d").exec();
        assertThat(bashCompletionDir).contains("terraform");

        String terraformVersion = getJDVMExecutor().run("terraform -version | grep -i \"terraform\" | sed \"s/.* v//\"").exec();
        assertThat(terraformVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("terraform.version"));
    }

    @Test
    void testAnsible() {
        String ansiblePath = getJDVMExecutor().executablePath("ansible").exec();
        assertThat(ansiblePath).isEqualTo("/usr/bin/ansible");

        String ansibleCommunityVersion = getJDVMExecutor().run("ansible-community --version | awk '{print $4}'").exec();
        assertThat(ansibleCommunityVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("ansible.version"));

        String ansibleVersion = getJDVMExecutor().run("ansible --version | grep -m 1 ansible | awk '{print $3}' | tr -d ']'")
                .exec();
        assertThat(ansibleVersion).isNotEmpty().matches("2\\.\\d+\\.\\d+");
    }

    @Test
    void testWasmtime() {
        String wasmtimePath = getJDVMExecutor().executablePath("wasmtime").exec();
        assertThat(wasmtimePath).isEqualTo("/usr/local/bin/wasmtime");

        String wasmtimeMinPath = getJDVMExecutor().executablePath("wasmtime-min").exec();
        assertThat(wasmtimeMinPath).isEqualTo("/usr/local/bin/wasmtime-min");

        String wasmtimeVersion = getJDVMExecutor().run("wasmtime --version | awk '{print $2}'").exec();
        assertThat(wasmtimeVersion).isNotEmpty().isEqualTo(MAVEN.getProperty("wasmtime.version"));
    }

    @Test
    void versionsOutput() {
        String jdvmVersions = getJDVMExecutor().run("jdvm-versions -o").exec();
        assertThat(jdvmVersions).contains("Versions saved to: /tmp/versions.md");

        String versions = getJDVMExecutor().cat("/tmp/versions.md").exec();
        assertThat(versions).isNotEmpty().doesNotContain("****");

        String homeDir = getJDVMExecutor().ls(USER_HOME).exec();
        assertThat(homeDir).doesNotContain("jmeter.log");

        getJDVM().copyFileFromContainer("/tmp/versions.md", "target/versions.md");
    }

}
