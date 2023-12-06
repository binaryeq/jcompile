package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintStream;
import java.util.stream.Stream;

public class SameArtifactDifferentCompilerClassOracleRow extends ClassOracleRow {
    public SameArtifactDifferentCompilerClassOracleRow(Pair<ZipPath, ZipPath> zPaths) {
        super(zPaths);
    }

    @Override
    public void printRow(PrintStream out) {
        out.println(String.join("\t", Stream.of(
                        getLeft().outerPath(),
                        getRight().outerPath(),
                        getLeft().innerPath(),
                        getRight().innerPath(),
                        getLeft().compilerName(),
                        getRight().compilerName(),
                        getLeft().compilerMajorVersion(),
                        getRight().compilerMajorVersion(),
                        getLeft().compilerMinorVersion(),
                        getRight().compilerMinorVersion(),
                        getLeft().compilerPatchVersion(),
                        getRight().compilerPatchVersion(),
                        getLeft().compilerExtraConfiguration(),
                        getRight().compilerExtraConfiguration(),
                        getLeft().projectName(),
                        getLeft().projectMajorVersion(),
                        getLeft().projectMinorVersion(),
                        getLeft().projectPatchVersion(),
                        getLeft().generatedBy(),
                        getRight().generatedBy(),
                        getLeft().bytecodeFeatures().contains("JEP181"),
                        getRight().bytecodeFeatures().contains("JEP181"),
                        getLeft().bytecodeFeatures().contains("JEP280"),
                        getRight().bytecodeFeatures().contains("JEP280"),
                        getLeft().scope(),
                        getRight().scope(),
                        getLeft().allInnerPaths().size() - 1,
                        getRight().allInnerPaths().size() - 1)
                .map(Utils::hyphenateEmpty).toList()));
    }
}
