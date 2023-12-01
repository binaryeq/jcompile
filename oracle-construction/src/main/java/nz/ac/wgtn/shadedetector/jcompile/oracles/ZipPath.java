package nz.ac.wgtn.shadedetector.jcompile.oracles;

import java.nio.file.Path;
import java.util.Set;

/**
 * A zip path consists of a path
 */
public record ZipPath (
        Path outerPath,
        Path innerPath,
        String compilerName,
        String compilerMajorVersion,
        String compilerMinorVersion,
        String compilerPatchVersion,        // Includes underlying OpenJDK version for ECJ builds
        String generatedBy,
        Set<String> bytecodeFeatures,
        String scope)
{}
