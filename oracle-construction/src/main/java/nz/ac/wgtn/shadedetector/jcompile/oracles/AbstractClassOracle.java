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

    protected Set<Path> getClasses(Path p) throws IOException, URISyntaxException {
        Set<Path> classes = Utils.collectClasses(p);
        return classes.stream()
            .filter(f -> includeClass(f))
            .collect(Collectors.toSet());
    }

    protected List<Pair<ZipPath, ZipPath>> buildFromJarPairs(List<Pair<Path, Path>> jarOracle) throws IOException, URISyntaxException {
        List<Pair<ZipPath, ZipPath>> classOracle = new ArrayList<>();

        for (Pair<Path,Path> jars: jarOracle) {
            System.err.println("analysing: " + jars.getLeft().toString() + " vs " + jars.getRight().toString());
            Set<Path> classes1 = getClasses(jars.getLeft());
            Set<Path> classes2 = getClasses(jars.getRight());
            Set<Path> commonClasses = findCommonPaths(classes1,classes2);
            JarMetadata jarMetadata1 = new JarMetadata(jars.getLeft());
            JarMetadata jarMetadata2 = new JarMetadata(jars.getRight());
            String scope = isTestJar(jars.getLeft()) ? "test" : "main";
            for (Path commonClass : sorted(commonClasses)) {
                ZipPath zpath1 = new ZipPath(jars.getLeft(), commonClass, jarMetadata1.getSourceFileOrigin(commonClass), jarMetadata1.getBytecodeFeatures(commonClass), scope);
                ZipPath zpath2 = new ZipPath(jars.getRight(), commonClass, jarMetadata2.getSourceFileOrigin(commonClass), jarMetadata2.getBytecodeFeatures(commonClass), scope);
                if (includeClassPair(zpath1, zpath2)) {
                    classOracle.add(Pair.of(zpath1, zpath2));
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
