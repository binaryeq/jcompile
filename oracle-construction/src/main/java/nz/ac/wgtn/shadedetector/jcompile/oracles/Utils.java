package nz.ac.wgtn.shadedetector.jcompile.oracles;

import com.google.common.collect.Sets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Reusable utilities.
 * @author jens dietrich
 */
public class Utils {

    public static final Function<Path,String> COMPILER_USED = p -> p.getParent().getFileName().toString();
    public static final Function<Path,String> COMPONENT_NAME = p -> {
        String fileName = p.getFileName().toString();
        return fileName.replaceFirst("-[^-]+(?:-tests)?\\.jar$", "");       // Ignore "-tests"
    };
    public static final Function<Path,String> ARTIFACT = p -> p.getFileName().toString();
    public static final Function<Path,String> JAR_TYPE = p -> p.getFileName().toString().matches(".*-tests\\.jar") ? "-tests" : "";    // Strict check to minimally restrict version syntax

    private static final Pattern INNER_CLASS_PATTERN = Pattern.compile("\\$\\d+");
    public static boolean isAnonymousInnerClass (Path p) {
        return INNER_CLASS_PATTERN.matcher(p.getFileName().toString()).find();
    }

    public static boolean isPackageInfo (Path p) {
        return p.getFileName().toString().equals("package-info.class");
    }

    /**
     * Strips any inner class part (anonymous or not) from a {@code .class} or {@code .java} filename.
     */
    public static Path getTopLevelClass(Path p) {
        return p.getParent().resolve(p.getFileName().toString().replaceFirst("\\$[^.]*", ""));
    }

    /**
     * Converts a {@code .class} filename to its corresponding {@code .java} source filename.
     * Strips any inner class part (anonymous or not).
     * @param p the path to a {@code .class} file
     * @return the path to the corresponding {@code .java} file
     */
    public static Path getSourceFileNameForClass(Path p) {
        assert p.toString().endsWith(".class");
        Path topLevelClass = getTopLevelClass(p);
        return topLevelClass.getParent().resolve(topLevelClass.getFileName().toString().replaceFirst("\\.class$", ".java"));    // Preserves the FileSystem
    }

    /**
     * Safe to pass a regular (non-test) jar, in which case the return value will be the same as the input.
     */
    public static Path getBaseJarForTestJar(Path jarPath) {
        return jarPath.getParent().resolve(jarPath.getFileName().toString().replaceFirst("-tests.jar$", ".jar"));    // Preserves the FileSystem
    }

    public static boolean isTestJar(Path jarPath) {
        return !jarPath.equals(getBaseJarForTestJar(jarPath));
    }

    public static Set<Path> collectJars(Path jarFolder) throws IOException {
        return Files.walk(jarFolder)
            .filter(Files::exists)
            .filter(f -> f.getFileName().toString().endsWith(".jar"))  // this excludes .jar.error !
            .collect(Collectors.toSet());
    }

    public static Set<Path> collectClasses(Path jar) throws IOException, URISyntaxException {
        Set<Path> classFiles = new HashSet<>();
        try (FileSystem zipfs = getJarFileSystem(jar)) {
            for (Path root:zipfs.getRootDirectories()) {
                Files.walk(root)
                    .filter(Files::exists)
                    .filter(f -> !Files.isDirectory(f))
                    .filter(f -> f.getFileName()!=null)
                    .filter(f -> f.getFileName().toString().endsWith(".class"))
                    //.map(f -> jar.resolve(f))
                    .forEach(f -> classFiles.add(f));
            }
        }
        return classFiles;
    }

    public static byte[] read(ZipPath zipPath) throws IOException, URISyntaxException {
        try (FileSystem zipfs = getJarFileSystem(zipPath.outerPath())) {
            Path entry = zipfs.getPath(zipPath.innerPath().toString());
            return Files.readAllBytes(entry);
        }
    }

    // find commons paths, note that we can not rely on Path::equal here
    // note that the file system is not checked, this is mainly used to compare the content of zip files or folders
    public static Set<Path> findCommonPaths(Set<Path> paths1, Set<Path> paths2) {
        Map<String,Path> index1 = paths1.stream().collect(Collectors.toMap(Path::toString,p->p));
        Map<String,Path> index2 = paths2.stream().collect(Collectors.toMap(Path::toString,p->p));
        Set<String> commonPathNames = Sets.intersection(index1.keySet(),index2.keySet());
        return  commonPathNames.stream().map(n -> index1.get(n)).collect(Collectors.toSet());
    }

    // for testing TODO: remove
    public static void main (String[] args) throws IOException, URISyntaxException {
        Path jarFolder = Path.of(args[0]);
        Set<Path> jars = collectJars(jarFolder);
        System.out.println("jars collected: " + jars.size());
        Path jar = jars.iterator().next();
        System.out.println("first jar: " + jar);
        Set<Path> classes = collectClasses(jar);
        System.out.println("classes collected: " + classes.size());

        for (Path classFile:classes) {
            System.out.println("\t"+classFile);
        }

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
     * Organise / index jars in a map, the keys correspond to the GA of the respective component, the build (compiler) and the jar type (regular ("") or "-test"), represented by file name ignoring the version part.
     * @param jarFolder
     * @return
     * @throws IOException
     */
    public static Map<String,Set<Path>> collectJarsByComponentAndBuildAndJarType(Path jarFolder) throws IOException {
        return index(jarFolder, f -> COMPILER_USED.apply(f) + "#" + COMPONENT_NAME.apply(f) + "#" + JAR_TYPE.apply(f));

    }

    /**
     * Convenience method to make a sorted list from any collection by using the default comparator.
     * Useful for increasing determinism when using {@link HashSet} or {@link HashMap}.
     */
    public static <E> List<E> sorted(Collection<E> c) {
        return c.stream().sorted().toList();
    }

    public static FileSystem getJarFileSystem(Path jarPath) throws IOException, URISyntaxException {
        return FileSystems.newFileSystem(new URI("jar:" + jarPath.toUri()), new HashMap<>());
    }

}
