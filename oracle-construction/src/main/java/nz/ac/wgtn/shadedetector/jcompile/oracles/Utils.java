package nz.ac.wgtn.shadedetector.jcompile.oracles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reusable utilities.
 * @author jens dietrich
 */
public class Utils {


    public static final Function<Path,String> COMPILER_USED = p -> p.getParent().getFileName().toString();
    public static final Function<Path,String> COMPONENT_NAME = p -> {
        String fileName = p.getFileName().toString();
        return fileName.substring(0,fileName.lastIndexOf("-"));
    };
    public static final Function<Path,String> ARTIFACT = p -> p.getFileName().toString();

    public static Set<Path> collectJars(Path jarFolder) throws IOException {
        return Files.walk(jarFolder)
            .filter(Files::exists)
            .filter(f -> f.getFileName().toString().endsWith(".jar"))  // this excludes .jar.error !
            .collect(Collectors.toSet());
    }

    /**
     * Organise / index jars in a map, the keys correspond to the GAV of the respective component, represented by file name.
     * @param jarFolder
     * @param indexingFunction
     * @param comparator
     * @return
     * @throws IOException
     */
    public static Map<String,Set<Path>> index(Path jarFolder, Function<Path,String> indexingFunction,Comparator<String> comparator) throws IOException {
        Set<Path> jars = collectJars(jarFolder);
        Map<String,Set<Path>> indexedJars = comparator==null ? new TreeMap<>() : new TreeMap<>(comparator);
        for (Path jar:jars) {
            String key = indexingFunction.apply(jar);
            indexedJars.computeIfAbsent(key, k -> new TreeSet<>()).add(jar);
        }
        return indexedJars;
    }

    /**
     * Organise / index jars in a map, the keys correspond to the GAV of the respective component, represented by file name.
     * @param jarFolder
     * @param indexingFunction
     * @return
     * @throws IOException
     */
    public static Map<String,Set<Path>> index(Path jarFolder, Function<Path,String> indexingFunction) throws IOException {
        return index(jarFolder,indexingFunction,null);
    }

    /**
     * Organise / index jars in a map, the keys are the identifiers for the builds (compiler versions) used.
     * @param jarFolder
     * @return
     * @throws IOException
     */
    public static Map<String,Set<Path>> collectJarsByCompilerUsed(Path jarFolder) throws IOException {
        return index(jarFolder, COMPILER_USED);
    }

    /**
     * Organise / index jars in a map, the keys are the identifiers for the builds (compiler versions) used.
     * @param jarFolder
     * @param comparator a custom comparator
     * @return
     * @throws IOException
     */
    public static Map<String,Set<Path>> collectJarsByCompilerUsed(Path jarFolder, Comparator<String> comparator) throws IOException {
        return index(jarFolder, COMPILER_USED,comparator);
    }

    /**
     * Organise / index jars in a map, the keys correspond to the GAV of the respective component, represented by file name.
     * @param jarFolder
     * @return
     * @throws IOException
     */
    public static Map<String,Set<Path>> collectJarsByArtifact(Path jarFolder) throws IOException {
        return index(jarFolder, ARTIFACT);
    }

    /**
     * Organise / index jars in a map, the keys correspond to the GA of the respective component, represented by file name ignoring the version part.
     * @param jarFolder
     * @return
     * @throws IOException
     */
    public static Map<String,Set<Path>> collectJarsByComponent(Path jarFolder) throws IOException {
        return index(jarFolder, COMPONENT_NAME);
    }

    /**
     * Organise / index jars in a map, the keys correspond to the GA of the respective component and the build (compiler), represented by file name ignoring the version part.
     * @param jarFolder
     * @return
     * @throws IOException
     */
    public static Map<String,Set<Path>> collectJarsByComponentAndBuild(Path jarFolder) throws IOException {
        return index(jarFolder, f -> COMPILER_USED.apply(f) + "#" + COMPONENT_NAME.apply(f));
    }

}
