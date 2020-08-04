/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package io.github.spleefx.dep;

import io.github.spleefx.dep.relocation.Relocation;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * The dependencies used by SpleefX.
 */
public enum Dependency {

    /* Important dependencies */
    ASM(
            "org.ow2.asm",
            "asm",
            "7.1",
            "SrL6K20sycyx6qBeoynEB7R7E+0pFfYvjEuMyWJY1N4="
    ),
    ASM_COMMONS(
            "org.ow2.asm",
            "asm-commons",
            "7.1",
            "5VkEidjxmE2Fv+q9Oxc3TFnCiuCdSOxKDrvQGVns01g="
    ),
    JAR_RELOCATOR(
            "me.lucko",
            "jar-relocator",
            "1.4",
            "1RsiF3BiVztjlfTA+svDCuoDSGFuSpTZYHvUK8yBx8I="
    ),

    CAFFEINE(
            "com.github.ben-manes.caffeine",
            "caffeine",
            "2.8.5",
            "gUsVqb9Zjg+oVN1wup9uA6QTqXl53gw/STFyleQ1K8g=",
            Relocation.of("caffeine", "com{}github{}benmanes{}caffeine")
    ),

    /* okhttp + OKIO */
    OKIO(
            "com{}squareup{}" + DependencyManager.OKIO_STRING,
            DependencyManager.OKIO_STRING,
            "1.17.5",
            "Gaf/SNhtPPRJf38lD78pX0MME6Uo3Vt7ID+CGAK4hq0=",
            Relocation.of(DependencyManager.OKIO_STRING, DependencyManager.OKIO_STRING)
    ),

    OKHTTP(
            "com{}squareup{}" + DependencyManager.OKHTTP3_STRING,
            "okhttp",
            "3.14.7",
            "Yg1PpDxcal72JXYCBKiHmeHkpl4ceh2NoC4GHEy7gAA=",
            Relocation.of(DependencyManager.OKHTTP3_STRING, DependencyManager.OKHTTP3_STRING),
            Relocation.of(DependencyManager.OKIO_STRING, DependencyManager.OKIO_STRING)
    ),
    COMMODORE(
            "me{}lucko",
            "commodore",
            "1.7",
            "ncwmvNFfvyZf1Pa0v4fWyMR0Jxe1v1ZgXOiI255IX5Q=",
            Relocation.of("commodore", "me{}lucko{}commodore")
    ),

    GSON(
            "com.google.code.gson",
            "gson",
            "2.8.5",
            "IzoBSfw2XJ9u29aDz+JmsZvcdzvpjqva9rPJJLSOfYE="
    ),

    /* Storage dependencies */
    MYSQL_DRIVER(
            "mysql",
            "mysql-connector-java",
            "8.0.21",
            "L2LYhicKdevI6P2JEn1KMMzHEfAiVq3iz7cJCBcTIAM=",
            Relocation.of("mysql", "com{}mysql")
    ),

    POSTGRESQL_DRIVER(
            "org{}postgresql",
            "postgresql",
            "9.4.1212",
            "DLKhWL4xrPIY4KThjI89usaKO8NIBkaHc/xECUsMNl0=",
            Relocation.of("postgresql", "org{}postgresql")
    ),

    H2_DRIVER(
            "com.h2database",
            "h2",
            "1.4.199",
            "MSWhZ0O8a0z7thq7p4MgPx+2gjCqD9yXiY95b5ml1C4="
    ),

    SQLITE_DRIVER(
            "org.xerial",
            "sqlite-jdbc",
            "3.28.0",
            "k3hOVtv1RiXgbJks+D9w6cG93Vxq0dPwEwjIex2WG2A="
    ),

    HIKARI(
            "com{}zaxxer",
            "HikariCP",
            "3.4.5",
            "i3MvlHBXDUqEHcHvbIJrWGl4sluoMHEv8fpZ3idd+mE=",
            Relocation.of("hikari", "com{}zaxxer{}hikari")
    ),

    MONGODB_DRIVER(
            "org.mongodb",
            "mongo-java-driver",
            "3.12.2",
            "eMxHcEtasb/ubFCv99kE5rVZMPGmBei674ZTdjYe58w=",
            Relocation.of("mongodb", "com{}mongodb"),
            Relocation.of("bson", "org{}bson")
    ),

    TOML4J(
            "com{}moandjiezana{}toml",
            "toml4j",
            "0.7.2",
            "9UdeY+fonl22IiNImux6Vr0wNUN3IHehfCy1TBnKOiA=",
            Relocation.of("toml4j", "com{}moandjiezana{}toml")
    ),

    /* Logging frameworks */

    SLF4J_API(
            "org.slf4j",
            "slf4j-api",
            "1.7.30",
            "zboHlk0btAoHYUhcax6ML4/Z6x0ZxTkorA1/lRAQXFc="
    ),

    SLF4J_SIMPLE(
            "org.slf4j",
            "slf4j-simple",
            "1.7.30",
            "i5J5y/9rn4hZTvrjzwIDm2mVAw7sAj7UOSh0jEFnD+4="
    );

    private final String mavenRepoPath;
    private final String version;
    private final byte[] checksum;
    private final List<Relocation> relocations;

    private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";

    Dependency(String groupId, String artifactId, String version, String checksum) {
        this(groupId, artifactId, version, checksum, new Relocation[0]);
    }

    Dependency(String groupId, String artifactId, String version, String checksum, Relocation... relocations) {
        mavenRepoPath = String.format(MAVEN_FORMAT,
                rewriteEscaping(groupId).replace(".", "/"),
                rewriteEscaping(artifactId),
                version,
                rewriteEscaping(artifactId),
                version
        );
        this.version = version;
        this.checksum = Base64.getDecoder().decode(checksum);
        this.relocations = Arrays.asList(relocations);
    }

    private static String rewriteEscaping(String s) {
        return s.replace("{}", ".");
    }

    public String getFileName() {
        return name().toLowerCase().replace('_', '-') + "-" + version;
    }

    String getMavenRepoPath() {
        return mavenRepoPath;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public boolean checksumMatches(byte[] hash) {
        return Arrays.equals(checksum, hash);
    }

    public List<Relocation> getRelocations() {
        return relocations;
    }

    /**
     * Creates a {@link MessageDigest} suitable for computing the checksums
     * of dependencies.
     *
     * @return the digest
     */
    public static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Dependency[] dependencies = values();
        DependencyRepository[] repos = DependencyRepository.values();

        for (Dependency dependency : dependencies) {
            System.out.println(dependency);
            for (DependencyRepository repo : repos) {
                try {
                    byte[] hash = createDigest().digest(repo.downloadRaw(dependency));
                    if (!dependency.checksumMatches(hash)) {
                        System.out.println("NO MATCH - " + repo.name() + " - " + dependency.name() + ": " + Base64.getEncoder().encodeToString(hash));
                    } else {
                        System.out.println("OK - " + repo.name() + " - " + dependency.name());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
