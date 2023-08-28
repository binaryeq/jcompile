package nz.ac.wgtn.shadedetector.jcompile.oracles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reusable utilitiues.
 * @author jens dietrich
 */
public class Utils {


    public static final Function<File,String> COMPILER_USED = f -> f.getParentFile().getName();
    public static final Function<File,String> COMPONENT_NAME = f -> f.getName().substring(0,f.getName().lastIndexOf("-"));

    public static Set<File> collectJars(File jarFolder) throws IOException {
        return Files.walk(jarFolder.toPath())
            .map(p -> p.toFile())
            .filter(f -> f.exists())
            .filter(f -> f.getName().endsWith(".jar"))  // this excludes .jar.error !
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
    public static Map<String,Set<File>> index(File jarFolder, Function<File,String> indexingFunction,Comparator<String> comparator) throws IOException {
        Set<File> jars = collectJars(jarFolder);
        Map<String,Set<File>> indexedJars = comparator==null ? new TreeMap<>() : new TreeMap<>(comparator);
        for (File jar:jars) {
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
    public static Map<String,Set<File>> index(File jarFolder, Function<File,String> indexingFunction) throws IOException {
        return index(jarFolder,indexingFunction,null);
    }

    /**
     * Organise / index jars in a map, the keys are the identifiers for the builds (compiler versions) used.
     * @param jarFolder
     * @return
     * @throws IOException
     */
    public static Map<String,Set<File>> collectJarsByCompilerUsed(File jarFolder) throws IOException {
        return index(jarFolder, COMPILER_USED);
    }

    /**
     * Organise / index jars in a map, the keys are the identifiers for the builds (compiler versions) used.
     * @param jarFolder
     * @param comparator a custom comparator
     * @return
     * @throws IOException
     */
    public static Map<String,Set<File>> collectJarsByCompilerUsed(File jarFolder, Comparator<String> comparator) throws IOException {
        return index(jarFolder, f -> f.getParentFile().getName(),comparator);
    }

    /**
     * Organise / index jars in a map, the keys correspond to the GAV of the respective component, represented by file name.
     * @param jarFolder
     * @return
     * @throws IOException
     */
    public static Map<String,Set<File>> collectJarsByArtifact(File jarFolder) throws IOException {
        return index(jarFolder, f -> f.getName());
    }

    /**
     * Organise / index jars in a map, the keys correspond to the GA of the respective component, represented by file name ignoring the version part.
     * @param jarFolder
     * @return
     * @throws IOException
     */
    public static Map<String,Set<File>> collectJarsByComponent(File jarFolder) throws IOException {
        return index(jarFolder, COMPONENT_NAME);
    }

    /**
     * Organise / index jars in a map, the keys correspond to the GA of the respective component and the build (compiler), represented by file name ignoring the version part.
     * @param jarFolder
     * @return
     * @throws IOException
     */
    public static Map<String,Set<File>> collectJarsByComponentAndBuild(File jarFolder) throws IOException {
        return index(jarFolder, f -> COMPILER_USED.apply(f) + "#" + COMPONENT_NAME.apply(f));
    }

}
