package gg.solarmc.launcher;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Entry point for starting everything up
 *
 */
public final class SolarLauncher {

    private final String targetModule;
    private final Set<String> rootModules;
    private final String[] args;
    private final ModuleLayer ownLayer;

    public SolarLauncher(String targetModule, Collection<String> rootModules,
                         String[] args, ModuleLayer ownLayer) {
        this.targetModule = Objects.requireNonNull(targetModule, "targetModule");
        this.rootModules = Set.copyOf(rootModules);
        this.args = args;
        this.ownLayer = Objects.requireNonNull(ownLayer, "Launcher must be started on the module path");
    }

    /**
     * Creates from a target module to launch, and arguments provided to its main method
     *
     * @param targetModule the target module whose main class to launch
     * @param rootModules modules to add to the root set of modules
     * @param args the arguments of {@code main(String[] args)}
     * @return the launcher
     */
    public static SolarLauncher create(String targetModule, Collection<String> rootModules, String[] args) {
        ModuleLayer ownLayer = SolarLauncher.class.getModule().getLayer();
        if (!rootModules.contains(targetModule)) {
            rootModules = new HashSet<>(rootModules);
            rootModules.add(targetModule);
        }
        return new SolarLauncher(targetModule, rootModules, args, ownLayer);
    }

    /**
     * Launches
     *
     */
    public void launch() {
        Path modulesDir = Path.of("modules");

        Set<Path> copiedJars = new JarExtraction(getJar(), "jars").copyJarsToDir(modulesDir);
        new DirectoryClean(copiedJars).deleteFilesInDirectory(modulesDir);

        startInLayer(createLayer(modulesDir));
    }

    private void startInLayer(ModuleLayer.Controller layerController) {
        ModuleLayer layer = layerController.layer();
        Module module = layer.findModule(targetModule)
                .orElseThrow(() -> new IllegalStateException("Module " + targetModule + " not found"));
        String mainClassName = module.getDescriptor().mainClass()
                .orElseThrow(() -> new IllegalStateException("Module " + targetModule + " has no main class"));
        Class<?> mainClass;
        try {
            mainClass = layer.findLoader(targetModule).loadClass(mainClassName);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Main class " + mainClassName + " of " + targetModule + "not found", ex);
        }
        layerController.addExports(module, mainClass.getPackageName(), getClass().getModule());
        try {
            Method method;
            method = mainClass.getMethod("main", String[].class);
            method.invoke(null, new Object[] {args});
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ModuleLayer.Controller createLayer(Path modulesDir) {
        ModuleFinder moduleFinder = ModuleFinder.of(modulesDir);
        Configuration configuration = ownLayer.configuration().resolve(
                moduleFinder,
                ModuleFinder.of(),
                rootModules);
        return ModuleLayer.defineModulesWithOneLoader(configuration, List.of(ownLayer), getClass().getClassLoader());
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
