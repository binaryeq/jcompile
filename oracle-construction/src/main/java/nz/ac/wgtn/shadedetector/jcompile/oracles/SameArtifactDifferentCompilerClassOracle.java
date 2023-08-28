package nz.ac.wgtn.shadedetector.jcompile.oracles;

import nz.ac.wgtn.shadedetector.jcompile.oracles.comparators.OpenJDKVersionsComparator;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static nz.ac.wgtn.shadedetector.jcompile.oracles.Utils.*;

/**
 * Construct a positive oracle for classes, i.e. sets of classes that originate from the same source code,
 * but are generated by different compilers.
 * @author jens dietrich
 */
public class SameArtifactDifferentCompilerClassOracle extends AbstractClassOracle {


    public SameArtifactDifferentCompilerClassOracle(boolean includePackageInfo, boolean includeAnonymousInnerClasses) {
        super(includePackageInfo, includeAnonymousInnerClasses);
    }

    public SameArtifactDifferentCompilerClassOracle() {
        super(false,false);
    }

    @Override
    public List<Pair<ZipPath, ZipPath>> build(Path jarFolder) throws IOException, URISyntaxException {

        List<Pair<Path,Path>> jarOracle = new SameArtifactDifferentCompilerJarOracle().build(jarFolder);
        List<Pair<ZipPath, ZipPath>> classOracle = new ArrayList<>();

        for (Pair<Path,Path> jars:jarOracle) {
            System.out.println("analysing: " + jars.getLeft().toString());
            Set<Path> classes1 = getClasses(jars.getLeft());
            Set<Path> classes2 = getClasses(jars.getRight());
            Set<Path> commonClasses = findCommonPaths(classes1,classes2);
            for (Path commonClass:commonClasses) {
                ZipPath zpath1 = new ZipPath(jars.getLeft(),commonClass);
                ZipPath zpath2 = new ZipPath(jars.getRight(),commonClass);
                classOracle.add(Pair.of(zpath1, zpath2));
            }
        }

        return classOracle;
    }


    //    // for testing TODO: remove
    public static void main (String[] args) throws IOException, URISyntaxException {
        Path jarFolder = Path.of(args[0]);
        List<Pair<ZipPath, ZipPath>> oracle = new SameArtifactDifferentCompilerClassOracle(true,true).build(jarFolder);
        System.out.println("oracle size: " + oracle.size());
    }

}
