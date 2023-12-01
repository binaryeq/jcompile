package nz.ac.wgtn.shadedetector.jcompile.oracles;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.regex.Matcher;
import java.nio.file.Path;
import java.util.regex.Pattern;

public record ParsedJarPath (
        @NonNull Compiler compiler,
        @NonNull Project project
) {

    public record Compiler (
            @NonNull String name,
            @NonNull String majorVersion,
            @Nullable String minorVersion,
            @Nullable String patchVersion,
            @Nullable String extraConfiguration
    ) {
        public static @Nullable Compiler parse(Path pathToDirContainingJar) {
            Matcher m = Pattern.compile("([^._]+)-([^-._]+)(?:\\.([^-._]+))?(?:\\.([^-._]+))?(?:_(.+))?").matcher(pathToDirContainingJar.getFileName().toString());
            if (m.matches()) {
                return new Compiler(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5));
            }

            return null;
        }

        public static String format(Compiler c) {
            return c.name() + "-" + c.majorVersion() + (c.minorVersion() == null ? "" : "." + c.minorVersion()) + (c.patchVersion() == null ? "" : "." + c.patchVersion()) + (c.extraConfiguration() == null ? "" : "_" + c.extraConfiguration());
        }
    }

    public record Project (
            @NonNull String name,
            @NonNull String majorVersion,
            @Nullable String minorVersion,
            @Nullable String patchVersion,
            @NonNull String jarType     // Either the empty string or "-tests", never null
    ) {
        public static @Nullable Project parse(Path jarPath) {
            Matcher m = Pattern.compile("([^.]+)-([^-.]+)(?:\\.([^-.]+))?(?:\\.([^-.]+))?(-tests)?\\.jar").matcher(jarPath.getFileName().toString());
            if (m.matches()) {
                return new Project(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5));
            }

            return null;
        }
    }

    public static @Nullable ParsedJarPath parse(Path jarPath) {
        Project project = Project.parse(jarPath);
        Compiler compiler = Compiler.parse(jarPath.getParent());
        if (project != null && compiler != null) {
            return new ParsedJarPath(compiler, project);
        }

        return null;
    }
}
