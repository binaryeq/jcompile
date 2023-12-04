package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.file.Path;
import java.util.Set;

/**
 * A zip path consists of a path
 */
public record ZipPath (
        @NonNull Path outerPath,
        @NonNull Path innerPath,
        @NonNull String compilerName,
        @NonNull String compilerMajorVersion,
        @Nullable String compilerMinorVersion,
        @Nullable String compilerPatchVersion,
        @Nullable String compilerExtraConfiguration,  // Everything after first "_"; null for OpenJDK builds
        @NonNull String generatedBy,
        @NonNull Set<String> bytecodeFeatures,
        @NonNull String scope)
{
    ZipPath(Path outerPath, Path innerPath, ParsedJarPath.Compiler compiler, JarMetadata jarMetadata, String scope) {
        this(outerPath, innerPath, compiler.name(), compiler.majorVersion(), compiler.minorVersion(), compiler.patchVersion(), compiler.extraConfiguration(), jarMetadata.getSourceFileOrigin(innerPath), jarMetadata.getBytecodeFeatures(innerPath), scope);
    }
}
