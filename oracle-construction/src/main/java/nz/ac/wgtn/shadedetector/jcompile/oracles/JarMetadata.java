package nz.ac.wgtn.shadedetector.jcompile.oracles;

import com.google.common.collect.Sets;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instances of this class provide additional metadata about the contents of a binary jar, such as which tool (if any)
 * was used to generate the source for a given contained class.
 */
public class JarMetadata {

    private final Path jar;

    // Why use Strings as keys instead of Paths? Because Paths can contain "hidden" FileSystems that don't appear when
    // .toString() is called but break .equals() and .hashCode(). Strings are WYSIWYG.
    private Map<String, String> sourceFileOrigins;    // Maps path below target/generated-sources/<tool> to <tool>
    private Map<String, Set<String>> bytecodeFeatures;

    public JarMetadata(Path jar) {
        this.jar = jar;
    }

    /**
     * Determine what tool, if any, generated the source for a given .class file.
     * @param innerPath path of a .class file inside the jar
     * @return "-" if the class was originally present in the repo, otherwise the name of the tool (subdir of {@code target/generated-sources})
     */
    public String getSourceFileOrigin(Path innerPath) {
        return getSourceFileOrigins().getOrDefault(Utils.getSourceFileNameForClass(innerPath).toString(), "-");
    }

    public Set<String> getBytecodeFeatures(Path innerPath) {
        return getBytecodeFeatures().getOrDefault(innerPath.toString(), Sets.newHashSet("MISSING"));
    }

    private synchronized Map<String, String> getSourceFileOrigins() {
        if (sourceFileOrigins == null) {
            sourceFileOrigins = loadSourceFileOrigins(jar);
        }

        return sourceFileOrigins;
    }

    private synchronized Map<String, Set<String>> getBytecodeFeatures() {
        if (bytecodeFeatures == null) {
            bytecodeFeatures = loadBytecodeFeatures(jar);
        }

        return bytecodeFeatures;
    }

    /**
     * Read metadata about source files created under target/generated-sources directory from a.jar.generated-sources file
     * @param jarPath the path of a binary jar file. The file {@code jarPath}.generated-sources must exist.
     * @return a map from source filenames to the name of the tool (subdirectory) that generated them.
     */
    private static Map<String, String> loadSourceFileOrigins(Path jarPath) {
        Path generatedSourcesPath = Path.of(jarPath.toString() + ".generated-sources");
        Map<String, String> map = new HashMap<>();
        try {
            for (String line : Files.readAllLines(generatedSourcesPath)) {
                Matcher matcher = Pattern.compile("^target/generated-sources/([^/]+)(/.+\\.java)$").matcher(line);  // Include leading slash
                if (matcher.matches()) {
                    map.put(matcher.group(2), matcher.group(1));
                }
            }

            return map;
        } catch (IOException e) {
            throw new RuntimeException("IO failure opening or reading from " + jarPath + ".generated-sources file", e);
        }
    }

    private static Map<String, Set<String>> loadBytecodeFeatures(Path jarPath) {
        Path generatedSourcesPath = Path.of(jarPath.toString() + ".bytecode-features");
        Map<String, Set<String>> map = new HashMap<>();
        try {
            for (String line : Files.readAllLines(generatedSourcesPath)) {
                String[] fields = line.split("\t");
                Matcher matcher = Pattern.compile("^target/[^/\t]*classes(/.+\\.class)$").matcher(fields[0]);  // Include leading slash
                if (matcher.matches()) {
                    List<String> rest = new ArrayList<>(Arrays.asList(fields));
                    rest.remove(0);
                    System.err.println("Bytecode features for " + matcher.group(1) + ": " + rest); //DEBUG
                    map.put(matcher.group(1), new HashSet<>(rest));
                }
            }

            return map;
        } catch (IOException e) {
            throw new RuntimeException("IO failure opening or reading from " + jarPath + ".bytecode-features file", e);
        }
    }
}
