package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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

    protected Set<Path> getClasses(Path p) {
        try {
            Set<Path> classes = Utils.collectClasses(p);
            return classes.stream()
                    .filter(f -> includeClass(f))
                    .collect(Collectors.toSet());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Hit exception, see cause", e);
        }
    }

    protected List<Pair<ZipPath, ZipPath>> buildFromJarPairs(List<Pair<Path, Path>> jarOracle) throws IOException, URISyntaxException {
        List<Pair<ZipPath, ZipPath>> classOracle = new ArrayList<>();

        for (Pair<Path,Path> jarPair: jarOracle) {
            System.err.println("analysing: " + jarPair.getLeft().toString() + " vs " + jarPair.getRight().toString());

            // These lists all have 2 elements
            List<Path> jars = List.of(jarPair.getLeft(), jarPair.getRight());
            List<Set<Path>> classes = jars.stream().map(this::getClasses).toList();
            List<JarMetadata> jarMetadatas = jars.stream().map(JarMetadata::new).toList();
            List<ParsedJarPath> parsedJarPaths = jars.stream().map(ParsedJarPath::parse).toList();

            Set<Path> commonClasses = findCommonPaths(classes.get(0), classes.get(1));
            String scope = isTestJar(jars.get(0)) ? "test" : "main";
            for (Path commonClass : sorted(commonClasses)) {
                List<ZipPath> zPaths = Stream.of(0, 1)
                        .map(i -> new ZipPath(jars.get(i), commonClass, parsedJarPaths.get(i).compiler(), jarMetadatas.get(i), scope))
                        .toList();
                if (includeClassPair(zPaths.get(0), zPaths.get(1))) {
                    classOracle.add(Pair.of(zPaths.get(0), zPaths.get(1)));
                }
            }
        }

        return classOracle;
    }

    // whether to include this class reference in the oracle
    private boolean includeClassPair(ZipPath zpath1, ZipPath zpath2) throws IOException, URISyntaxException {
        assert ! zpath1.outerPath().toString().equals(zpath2.outerPath().toString()) ;
        byte[] content1 = read(zpath1);
        byte[] content2 = read(zpath2);
        return !Arrays.equals(content1,content2);
    }

}
