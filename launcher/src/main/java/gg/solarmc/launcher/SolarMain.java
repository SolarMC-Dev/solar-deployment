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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SolarMain {

    private SolarMain() {}

    public static void main(String[] args) throws Exception {
        String modulesDirArg = args[0];
        Path modulesDir = Path.of(modulesDirArg);
        String arguments = new SolarMain().modulePathArguments(modulesDirArg, modulesDir);
        //noinspection UseOfSystemOutOrSystemErr
        System.out.println(arguments);
    }

    private String modulePathArguments(String modulesDirArg, Path modulesDir) {
        Set<Path> copiedJars = new JarExtraction(getJar(), "jars").copyJarsToDir(modulesDir);
        new DirectoryClean(copiedJars).deleteFilesInDirectory(modulesDir);

        ModuleGroup modules = readModules();
        return "--add-modules " + String.join(",", modules.extraRoots())
                + " --module-path " + modulesDirArg
                + " --module " + modules.mainModule();
    }

    record ModuleGroup(String mainModule, Iterable<String> extraRoots) {}

    private ModuleGroup readModules() {
        URL url = SolarMain.class.getResource("/solar-launcher/root-modules");
        if (url == null) {
            throw new IllegalStateException("'/solar-launcher/root-modules' resource not found");
        }
        List<String> modules = new ArrayList<>();
        try (InputStream inputStream = url.openStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null && !line.isBlank() && !line.startsWith("#")) {
                modules.add(line);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        Iterable<String> extraRoots = () -> {
            var it = modules.iterator();
            it.next();
            return it;
        };
        return new ModuleGroup(modules.get(0), extraRoots);
    }

    private Path getJar() {
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            throw new IllegalStateException("No CodeSource");
        }
        URL location = codeSource.getLocation();
        if (location == null) {
            throw new IllegalStateException("No CodeSource#getLocation");
        }
        try {
            return Path.of(location.toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

}
