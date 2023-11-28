package nz.ac.wgtn.shadedetector.jcompile.oracles;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instances of this class provide additional metadata about the contents of a binary jar, such as which tool (if any)
 * was used to generate the source for a given contained class.
 */
public class JarMetadata {

    private final Path jar;
    private Map<Path, String> sourceFileOrigins;    // Maps path below target/generated-sources/<tool> to <tool>

    public JarMetadata(Path jar) {
        this.jar = jar;
    }

    /**
     * Determine what tool, if any, generated the source for a given .class file.
     * @param innerPath path of a .class file inside the jar
     * @return "-" if the class was originally present in the repo, otherwise the name of the tool (subdir of {@code target/generated-sources})
     */
    public String getSourceFileOrigin(Path innerPath) {
        return getSourceFileOrigins().getOrDefault(Utils.getSourceFileNameForClass(innerPath), "-");
    }

    private synchronized Map<Path, String> getSourceFileOrigins() {
        if (sourceFileOrigins == null) {
            sourceFileOrigins = loadSourceFileOrigins(jar);
        }

        return sourceFileOrigins;
    }

    /**
     * Read metadata about source files created under target/generated-sources directory from a.jar.generated-sources file
     * @param jarPath the path of a binary jar file. The file {@code jarPath}.generated-sources must exist.
     * @return a map from source filenames to the name of the tool (subdirectory) that generated them.
     */
    private static Map<Path, String> loadSourceFileOrigins(Path jarPath) {
        Path generatedSourcesPath = Path.of(jarPath.toString() + ".generated-sources");
        Map<Path, String> map = new HashMap<>();
        try (Reader reader = new FileReader(generatedSourcesPath.toFile());
             BufferedReader br = new BufferedReader(reader)) {
            for (String line : br.lines().toList()) {
                Matcher matcher = Pattern.compile("^target/generated-sources/([^/]+)(/.+\\.java)$").matcher(line);  // Include leading slash
                if (matcher.matches()) {
                    System.err.println("Generated file: " + matcher.group(1) + " generated " + matcher.group(2));       //DEBUG
                    map.put(Path.of(matcher.group(2)), matcher.group(1));
                }
            }

            return map;
        } catch (IOException e) {
            throw new RuntimeException("IO failure opening or reading from ECJ JDK version resource file", e);
        }
    }
}
