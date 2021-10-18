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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

public final class DirectoryClean {

    private final Set<Path> doNotDelete;

    /**
     * Creates from files which should not be deleted
     *
     * @param doNotDelete that which should not be deleted
     */
    public DirectoryClean(Set<Path> doNotDelete) {
        this.doNotDelete = Set.copyOf(doNotDelete);
    }

    /**
     * Deletes all files in the target directory except the ones previously specified
     *
     * @param targetDirectory the target directory
     */
    public void deleteFilesInDirectory(Path targetDirectory) {
        try (Stream<Path> fileStream = Files.list(targetDirectory)) {
            fileStream.filter((file) -> !doNotDelete.contains(file)).forEach((toDelete) -> {
                try {
                    Files.delete(toDelete);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
