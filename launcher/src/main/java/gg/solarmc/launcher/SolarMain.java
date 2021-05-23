package gg.solarmc.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class SolarMain {

    private SolarMain() {}

    public static void main(String[] args) throws IOException {
        URL url = SolarMain.class.getResource("/solar-launcher/root-modules");
        if (url == null) {
            throw new IllegalStateException("'/solar-launcher/root-modules' resource not found");
        }
        List<String> modules = new ArrayList<>();
        try (InputStream inputStream = url.openStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null && !line.isBlank()) {
                modules.add(line);
            }
        }
        SolarLauncher.create(modules.get(0), modules, args).launch();
    }
}
