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
public class SameArtifactDifferentCompilerFromDotClassOracle extends AbstractClassOracle {


    public SameArtifactDifferentCompilerFromDotClassOracle(boolean includePackageInfo, boolean includeAnonymousInnerClasses) {
        super(includePackageInfo, includeAnonymousInnerClasses);
    }

    public SameArtifactDifferentCompilerFromDotClassOracle() {
        super(true,true);
    }

    @Override
    public List<ClassOracleRow> build(Path jarFolder) throws IOException, URISyntaxException {

        List<Pair<Path,Path>> jarOracle = new SameArtifactDifferentCompilerFromDotJarOracle().build(jarFolder);
        return buildFromJarPairs(jarOracle, SameArtifactDifferentCompilerFromDotClassOracle::makeRow);
    }

    private static SameArtifactDifferentCompilerClassOracleRow makeRow(Pair<ZipPath, ZipPath> zPaths) {
        return new SameArtifactDifferentCompilerClassOracleRow(zPaths);
    }

    //    // for testing TODO: remove
    public static void main (String[] args) throws IOException, URISyntaxException {
        Path jarFolder = Path.of(args[0]);
        List<ClassOracleRow> oracle = new SameArtifactDifferentCompilerFromDotClassOracle().build(jarFolder);     //TODO: Use more specific type
        System.out.println(String.join("\t", Arrays.asList(
                "container_1",
                "container_2",
                "class_1",
                "class_2",
                "compiler_name_1",
                "compiler_name_2",
                "compiler_major_version_1",
                "compiler_major_version_2",
                "compiler_minor_version_1",
                "compiler_minor_version_2",
                "compiler_patch_version_1",
                "compiler_patch_version_2",
                "compiler_extra_config_1",
                "compiler_extra_config_2",
                "project_name",
                "project_major_version",
                "project_minor_version",
                "project_patch_version",
                "generated_by_1",
                "generated_by_2",
                "bytecode_jep181_1",
                "bytecode_jep181_2",
                "bytecode_jep280_1",
                "bytecode_jep280_2",
                "scope_1",
                "scope_2",
                "n_anon_inner_classes_1",
                "n_anon_inner_classes_2"
        )));
        for (ClassOracleRow paths : oracle) {
            paths.printRow(System.out);
        }
    }

}