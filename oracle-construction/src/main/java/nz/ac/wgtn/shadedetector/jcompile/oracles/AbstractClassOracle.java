package nz.ac.wgtn.shadedetector.jcompile.oracles;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nz.ac.wgtn.shadedetector.jcompile.oracles.Utils.*;

/**
 * Abstract super class.
 * @author jens dietrich
 */
public abstract class AbstractClassOracle implements ClassOracle {

    private boolean includePackageInfo = false;  // older Java compilers (8) do not generate those
    private boolean includeAnonymousInnerClasses = false;  // numbering might not be deterministic

    public AbstractClassOracle(boolean includePackageInfo, boolean includeAnonymousInnerClasses) {
        this.includePackageInfo = includePackageInfo;
        this.includeAnonymousInnerClasses = includeAnonymousInnerClasses;
    }

    public AbstractClassOracle() {
        this(false,false);
    }

    public boolean includePackageInfo() {
        return includePackageInfo;
    }

    public boolean includeAnonymousInnerClasses() {
        return includeAnonymousInnerClasses;
    }

    protected boolean includeClass(Path p) {
        if (!includePackageInfo && isPackageInfo(p)) {
            return false;
        }
        if (!includeAnonymousInnerClasses && isAnonymousInnerClass(p)) {
            return false;
        }
        return true;
    }

    /**
     * Group all paths by the path of the deepest <i>named</i> class.
     * @return a map in which keys are the paths of named classes and each value is the top-level path plus any corresponding anonymous classes
     */
    protected Map<Path, Set<Path>> getClasses(Path p) {
        try {
            Set<Path> classes = Utils.collectClasses(p);
            return classes.stream()
                    .map(pathWithZipCruft -> Path.of(pathWithZipCruft.toString()))      // Important to strip out hidden FileSystem part of Path, ugh
                    .filter(f -> includeClass(f))
                    .collect(Collectors.groupingBy(Utils::getDeepestNamedClass, Collectors.toSet()));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Hit exception, see cause", e);
        }
    }

    protected <R extends ClassOracleRow> List<R> buildFromJarPairs(List<Pair<Path, Path>> jarOracle, Function<Pair<ZipPath, ZipPath>, R> makeRow) throws IOException, URISyntaxException {
        List<R> classOracle = new ArrayList<>();

        for (Pair<Path,Path> jarPair: jarOracle) {
            System.err.println("analysing: " + jarPair.getLeft().toString() + " vs " + jarPair.getRight().toString());
            processJarPair(jarPair);        // Subclasses can do arbitrary per-jar-pair processing here

            // These lists all have 2 elements
            List<Path> jars = List.of(jarPair.getLeft(), jarPair.getRight());
            List<Map<Path, Set<Path>>> classes = jars.stream().map(this::getClasses).toList();
            List<JarMetadata> jarMetadatas = jars.stream().map(JarMetadata::new).toList();
            List<ParsedJarPath> parsedJarPaths = jars.stream().map(ParsedJarPath::parse).toList();

            Set<Path> commonNamedClasses = findCommonPaths(classes.get(0).keySet(), classes.get(1).keySet());
            String scope = isTestJar(jars.get(0)) ? "test" : "main";
            for (Path commonNamedClass : sorted(commonNamedClasses)) {
                List<ZipPath> zPaths = Stream.of(0, 1)
                        .map(i -> groupAnonInnerClasses(classes.get(i).get(commonNamedClass), jars.get(i), parsedJarPaths.get(i).compiler(), parsedJarPaths.get(i).project(), jarMetadatas.get(i), scope))
                        .toList();
                if (includeClassPair(zPaths.get(0), zPaths.get(1))) {
                    classOracle.add(makeRow.apply(Pair.of(zPaths.get(0), zPaths.get(1))));
                }
            }
        }

        return classOracle;
    }

    /**
     * A slot for subclasses to do per-jar-pair processing during {@link #buildFromJarPairs(List, Function)}.
     */
    protected void processJarPair(Pair<Path, Path> jarPair) {
        // Does nothing by default
    }

    /**
     * The top-level named class itself should appear in {@code classes} for {@code innerPath} to be set correctly.
     */

    private static ZipPath groupAnonInnerClasses(Set<Path> classes, Path jarPath, ParsedJarPath.Compiler compiler, ParsedJarPath.Project project, JarMetadata jarMetadata, String scope) {
        return classes.stream()
                .map(c -> new ZipPath(jarPath, c, compiler, project, jarMetadata, scope))
                .reduce(AbstractClassOracle::combineNamedClassZipPaths).get();
    }

    /**
     * Combine metadata for a named class and all of its anonymous inner classes, preferring a named {@code innerClass}
     * to any anonymous inner class {@code innerClass}.
     *
     * The two arguments must have identical {@code outerPath}, {@code compilerName}, {@code compilerMajorVersion},
     * {@code compilerMinorVersion}, {@code compilerPatchVersion}, {@code compilerExtraConfiguration}.
     */
    private static ZipPath combineNamedClassZipPaths(ZipPath a, ZipPath b) {
        Preconditions.checkArgument(Objects.equals(a.outerPath(), b.outerPath()));
        Preconditions.checkArgument(Objects.equals(a.compilerName(), b.compilerName()));
        Preconditions.checkArgument(Objects.equals(a.compilerMajorVersion(), b.compilerMajorVersion()));
        Preconditions.checkArgument(Objects.equals(a.compilerMinorVersion(), b.compilerMinorVersion()));
        Preconditions.checkArgument(Objects.equals(a.compilerPatchVersion(), b.compilerPatchVersion()));
        Preconditions.checkArgument(Objects.equals(a.projectName(), b.projectName()));
        Preconditions.checkArgument(Objects.equals(a.projectMajorVersion(), b.projectMajorVersion()));
        Preconditions.checkArgument(Objects.equals(a.projectMinorVersion(), b.projectMinorVersion()));
        Preconditions.checkArgument(Objects.equals(a.projectPatchVersion(), b.projectPatchVersion()));
        Preconditions.checkArgument(Objects.equals(a.projectJarType(), b.projectJarType()));
        Preconditions.checkArgument(Objects.equals(a.compilerExtraConfiguration(), b.compilerExtraConfiguration()));

        return new ZipPath(
                a.outerPath(),                       // Identical in a and b
                Stream.of(a, b).min(Comparator.comparing(p -> p.innerPath().toString().replaceFirst("\\.class$", ""))).get().innerPath(),   // Choose the named class over all anonymous classes
                a.compilerName(),                    // Identical in a and b
                a.compilerMajorVersion(),            // Identical in a and b
                a.compilerMinorVersion(),            // Identical in a and b
                a.compilerPatchVersion(),            // Identical in a and b
                a.compilerExtraConfiguration(),      // Identical in a and b
                a.projectName(),                     // Identical in a and b
                a.projectMajorVersion(),             // Identical in a and b
                a.projectMinorVersion(),             // Identical in a and b
                a.projectPatchVersion(),             // Identical in a and b
                a.projectJarType(),                  // Identical in a and b
                Stream.of(a, b).max(Comparator.comparing(ZipPath::generatedBy)).get().generatedBy(),      // Any generated beats non-generated, arbitrary within that
                Sets.union(a.bytecodeFeatures(), b.bytecodeFeatures()),
                Stream.of(a, b).min(Comparator.comparing(ZipPath::scope)).get().scope(),    // Arbitrary
                Sets.union(a.allInnerPaths(), b.allInnerPaths())
        );
    }

    // whether to include this (named) class reference in the oracle
    private boolean includeClassPair(ZipPath zpath1, ZipPath zpath2) throws IOException, URISyntaxException {
        assert ! zpath1.outerPath().toString().equals(zpath2.outerPath().toString()) ;
        if (!zpath1.allInnerPaths().equals(zpath2.allInnerPaths())) {
            return true;
        }

        // The same sets of anonymous inner classes went into each top-level named class.
        for (Path innerPath : zpath1.allInnerPaths()) {
            byte[] content1 = read(zpath1.outerPath(), innerPath);
            byte[] content2 = read(zpath2.outerPath(), innerPath);
            if (!Arrays.equals(content1,content2)) {
                return true;
            }
        }

        return false;
    }

}
