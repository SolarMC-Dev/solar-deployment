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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectoryCleanTest {

    @TempDir
    public Path targetDir;

    @Test
    public void deleteFilesInDirectory() throws IOException {
        Path doNotDelete = targetDir.resolve("someFile");
        Path doDelete = targetDir.resolve("anotherFile");
        Files.createFile(doNotDelete);
        Files.createFile(doDelete);

        new DirectoryClean(Set.of(doNotDelete)).deleteFilesInDirectory(targetDir);

        assertTrue(Files.exists(doNotDelete));
        assertTrue(Files.notExists(doDelete));
    }

}
