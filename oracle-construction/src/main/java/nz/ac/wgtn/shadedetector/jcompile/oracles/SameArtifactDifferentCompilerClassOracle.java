package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;

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
        super(true,false);
    }

    @Override
    public List<Pair<ZipPath, ZipPath>> build(Path jarFolder) throws IOException, URISyntaxException {

        List<Pair<Path,Path>> jarOracle = new SameArtifactDifferentCompilerJarOracle().build(jarFolder);
        return buildFromJarPairs(jarOracle);
    }

    //    // for testing TODO: remove
    public static void main (String[] args) throws IOException, URISyntaxException {
        Path jarFolder = Path.of(args[0]);
        List<Pair<ZipPath, ZipPath>> oracle = new SameArtifactDifferentCompilerClassOracle().build(jarFolder);
        System.out.println("container1\tcontainer2\tclass1\tclass2\tgenerated_by_1\tgenerated_by_2\tbytecode_jep181_1\tbytecode_jep181_2\tbytecode_jep280_1\tbytecode_jep280_2\tscope_1\tscope_2");
        for (Pair<ZipPath, ZipPath> paths : oracle) {
            System.out.println(paths.getLeft().outerPath() +
                    "\t" + paths.getRight().outerPath() +
                    "\t" + paths.getLeft().innerPath() +
                    "\t" + paths.getRight().innerPath() +
                    "\t" + paths.getLeft().compilerName() +
                    "\t" + paths.getRight().compilerName() +
                    "\t" + paths.getLeft().compilerMajorVersion() +
                    "\t" + paths.getRight().compilerMajorVersion() +
                    "\t" + paths.getLeft().compilerMinorVersion() +
                    "\t" + paths.getRight().compilerMinorVersion() +
                    "\t" + paths.getLeft().compilerExtraConfiguration() +
                    "\t" + paths.getRight().compilerExtraConfiguration() +
                    "\t" + paths.getLeft().generatedBy() +
                    "\t" + paths.getRight().generatedBy() +
                    "\t" + paths.getLeft().bytecodeFeatures().contains("JEP181") +
                    "\t" + paths.getRight().bytecodeFeatures().contains("JEP181") +
                    "\t" + paths.getLeft().bytecodeFeatures().contains("JEP280") +
                    "\t" + paths.getRight().bytecodeFeatures().contains("JEP280") +
                    "\t" + paths.getLeft().scope() +
                    "\t" + paths.getRight().scope());
        }
    }

}
