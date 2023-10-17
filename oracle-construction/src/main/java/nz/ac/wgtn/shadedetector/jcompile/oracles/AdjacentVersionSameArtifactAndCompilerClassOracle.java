package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import static nz.ac.wgtn.shadedetector.jcompile.oracles.Utils.findCommonPaths;
import static nz.ac.wgtn.shadedetector.jcompile.oracles.Utils.read;

/**
 * Construct a negative oracle for classes, i.e. sets of classes that originate from different but similar source code (adjacent versions),
 * generated by the same compiler.
 * @author jens dietrich
 */
public class AdjacentVersionSameArtifactAndCompilerClassOracle extends AbstractClassOracle {

    public AdjacentVersionSameArtifactAndCompilerClassOracle(boolean ignorePackageInfo, boolean ignoreAnonymousInnerClasses) {
        super(ignorePackageInfo, ignoreAnonymousInnerClasses);
    }

    public AdjacentVersionSameArtifactAndCompilerClassOracle() {
        super(true,true);
    }

    @Override
    public List<Pair<ZipPath, ZipPath>> build(Path jarFolder) throws IOException, URISyntaxException {

        List<Pair<Path,Path>> jarOracle = new AdjacentVersionSameArtifactAndCompilerJarOracle().build(jarFolder);
        List<Pair<ZipPath, ZipPath>> classOracle = new ArrayList<>();

        for (Pair<Path,Path> jars:jarOracle) {

            System.out.println("analysing: " + jars.getLeft().toString());
            Set<Path> classes1 = getClasses(jars.getLeft());
            Set<Path> classes2 = getClasses(jars.getRight());

            Set<Path> commonClasses = findCommonPaths(classes1,classes2);
            for (Path commonClass:commonClasses) {
                ZipPath zpath1 = new ZipPath(jars.getLeft(),commonClass);
                ZipPath zpath2 = new ZipPath(jars.getRight(),commonClass);
                if (include(zpath1,zpath2)) {
                    classOracle.add(Pair.of(zpath1, zpath2));
                }
            }
        }

        return classOracle;
    }

    // whether to include this class reference in the oracle
    private boolean include(ZipPath zpath1, ZipPath zpath2) throws IOException, URISyntaxException {
        assert ! zpath1.outerPath().toString().equals(zpath2.outerPath().toString()) ;
        byte[] content1 = read(zpath1);
        byte[] content2 = read(zpath2);
        return !Arrays.equals(content1,content2);
    }


    //    // for testing TODO: remove
    public static void main (String[] args) throws IOException, URISyntaxException {
        Path jarFolder = Path.of(args[0]);
        List<Pair<ZipPath, ZipPath>> oracle = new AdjacentVersionSameArtifactAndCompilerClassOracle().build(jarFolder);
        System.out.println("oracle size: " + oracle.size());
    }

}