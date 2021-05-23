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
