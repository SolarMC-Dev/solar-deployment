/*
 * solar-deployment
 * Copyright © 2021 SolarMC Developers
 *
 * solar-deployment is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * solar-deployment is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with solar-deployment. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Affero General Public License.
 */

package gg.solarmc.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;

public final class JarExtraction {

    private final Path sourceJar;
    private final String jarDirectory;

    public JarExtraction(Path sourceJar, String jarDirectory) {
        this.sourceJar = Objects.requireNonNull(sourceJar, "sourceJar");
        this.jarDirectory = Objects.requireNonNull(jarDirectory, "jarDirectory");
    }

    /**
     * Copies all jars from the source jar to the target directory. <br>
     * <br>
     * Will overwrite jars whose name includes "snapshot". Will not overwrite
     * other jars.
     *
     * @param targetDirectory the target directory
     * @return all the jars which were copied
     */
    public Set<Path> copyJarsToDir(Path targetDirectory) {
        try {
            Files.createDirectories(targetDirectory);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        Set<Path> outputPaths = new HashSet<>();

        try (JarFile jar = new JarFile(sourceJar.toFile())) {
            jar.stream().filter((entry) -> {
                return entry.getName().startsWith(jarDirectory);
            }).forEach((entry) -> {
                String targetJarName = entry.getName().substring(jarDirectory.length());
                if (targetJarName.isBlank() || targetJarName.equals("/")) {
                    return;
                }
                if (targetJarName.startsWith("/")) {
                    targetJarName = targetJarName.substring(1);
                }
                Path destination = targetDirectory.resolve(targetJarName);
                outputPaths.add(destination);

                boolean replaceExisting = targetJarName.contains("SNAPSHOT");
                if (replaceExisting || !Files.exists(destination)) {
                    try (InputStream inputStream = jar.getInputStream(entry)) {
                        Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return Set.copyOf(outputPaths);
    }

}
