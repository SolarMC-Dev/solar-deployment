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
