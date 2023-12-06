package nz.ac.wgtn.shadedetector.jcompile.oracles;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Construct a negative oracle for classes, i.e. sets of classes that originate from different but similar source code (adjacent versions),
 * generated by the same compiler.
 * @author jens dietrich
 */
public class AdjacentVersionSameArtifactAndCompilerClassOracle extends AbstractClassOracle {
    private RevApiJarComparer currentJarPairRevApiJarComparer;

    public AdjacentVersionSameArtifactAndCompilerClassOracle(boolean ignorePackageInfo, boolean ignoreAnonymousInnerClasses) {
        super(ignorePackageInfo, ignoreAnonymousInnerClasses);
    }

    public AdjacentVersionSameArtifactAndCompilerClassOracle() {
        super(true,true);
    }

    @Override
    public List<ClassOracleRow> build(Path jarFolder) throws IOException, URISyntaxException {

        List<Pair<Path,Path>> jarOracle = new AdjacentVersionSameArtifactAndCompilerJarOracle().build(jarFolder);
        return buildFromJarPairs(jarOracle, this::makeRow);
    }

    private AdjacentVersionSameArtifactAndCompilerClassOracleRow makeRow(Pair<ZipPath, ZipPath> zPaths) {
        Preconditions.checkArgument(zPaths.getLeft().innerPath().equals(zPaths.getRight().innerPath()));
        return new AdjacentVersionSameArtifactAndCompilerClassOracleRow(zPaths, currentJarPairRevApiJarComparer.compareClassVersions(zPaths.getLeft().innerPath()));
    }

    /**
     * Called from inside {@link #buildFromJarPairs(List, Function)} whenever a new jar pair is being considered.
     * We need to run a revapi analysis on these two jars.
     */
    protected void processJarPair(Pair<Path, Path> jarPair) {
        currentJarPairRevApiJarComparer = new PreprocessedJsonRevApiJarComparer(jarPair.getLeft(), jarPair.getRight());
    }

    //    // for testing TODO: remove
    public static void main (String[] args) throws IOException, URISyntaxException {
        Path jarFolder = Path.of(args[0]);
        List<ClassOracleRow> oracle = new AdjacentVersionSameArtifactAndCompilerClassOracle().build(jarFolder);     //TODO: Use more specific type
        System.out.println(String.join("\t", Arrays.asList(
                "container_1",
                "container_2",
                "class_1",
                "class_2",
                "compiler_name",
                "compiler_major_version",
                "compiler_minor_version",
                "compiler_patch_version",
                "compiler_extra_config",
                "project_name",
                "project_major_version_1",
                "project_major_version_2",
                "project_minor_version_1",
                "project_minor_version_2",
                "project_patch_version_1",
                "project_patch_version_2",
                "generated_by_1",
                "generated_by_2",
                "bytecode_jep181_1",
                "bytecode_jep181_2",
                "bytecode_jep280_1",
                "bytecode_jep280_2",
                "scope_1",
                "scope_2",
                "n_anon_inner_classes_1",
                "n_anon_inner_classes_2",
                "source_compatible",
                "binary_compatible",
                "semantic_compatible"
        )));
        for (ClassOracleRow paths : oracle) {
            paths.printRow(System.out);
        }
    }

}
