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
            // Tricky: Allow "." and "-" in the patch version, to handle paths like "ecj-3.11.1.v20150902-1521_openjdk-11.0.19".
            // The fact that the major version is mandatory should make hyphens still "stick to" the name whenever possible, as desired.
            Matcher m = Pattern.compile("([^._]+)-([^-._]+)(?:\\.([^-._]+))?(?:\\.([^_]+))?(?:_(.+))?").matcher(pathToDirContainingJar.getFileName().toString());
            if (m.matches()) {
                return new Compiler(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5));
            }

            return null;
        }

        public @NonNull String format() {
            return
                    name + "-" + majorVersion +
                    (minorVersion == null ? "" : "." + minorVersion) +
                    (patchVersion == null ? "" : "." + patchVersion) +
                    (extraConfiguration == null ? "" : "_" + extraConfiguration);
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
            Matcher m = Pattern.compile("(([^.]+)-([^-.]+)(?:\\.([^-.]+))?(?:\\.([^-.]+))?)(|-tests)\\.jar").matcher(jarPath.getFileName().toString());
            if (m.matches()) {
                // Actually does a full lookup of the project name from dataset.json, since in general it can't be
                // reliably inferred just from the filename.
                System.err.println("parse(): first field = '" + m.group(1) + "', result = '" + DatasetJson.getProjectNameForJarName(m.group(1)) + "'.");      //DEBUG
//                return new Project(DatasetJson.getProjectNameForJarName(m.group(1)), m.group(2), m.group(3), m.group(4), m.group(5));
                return new Project(DatasetJson.getProjectNameForJarName(m.group(1) + ".jar"), m.group(3), m.group(4), m.group(5), m.group(6));
            }

            return null;
        }

        public @NonNull String format() {
            return
                    name + "-" + majorVersion +
                    (minorVersion == null ? "" : "." + minorVersion) +
                    (patchVersion == null ? "" : "." + patchVersion) +
                    jarType;
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

    public @NonNull String format() {
        return compiler.format() + "/" + project.format();
    }
}
