package nz.ac.wgtn.shadedetector.jcompile.oracles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static nz.ac.wgtn.shadedetector.jcompile.oracles.Utils.isAnonymousInnerClass;
import static nz.ac.wgtn.shadedetector.jcompile.oracles.Utils.isPackageInfo;

/**
 * Abstract super class.
 * @author jens dietrich
 */
public abstract class AbstractClassOracle implements ClassOracle {

    private boolean includePackageInfo = false;  // older Java compilers (8) do not generate those
    private boolean includeAnonymousInnerClasses = false;  // numbering might not be deterministic

    public AbstractClassOracle(boolean includePackageInfo, boolean includeAnonymousInnerClasses) {
        this.includePackageInfo = includePackageInfo;
        this.includeAnonymousInnerClasses = includeAnonymousInnerClasses;
    }

    public AbstractClassOracle() {
        this(false,false);
    }

    public boolean includePackageInfo() {
        return includePackageInfo;
    }

    public boolean includeAnonymousInnerClasses() {
        return includeAnonymousInnerClasses;
    }

    protected boolean include(Path p) {
        if (!includePackageInfo && isPackageInfo(p)) {
            return false;
        }
        if (!includeAnonymousInnerClasses() && isAnonymousInnerClass(p)) {
            return false;
        }
        return true;
    }

    protected Set<Path> getClasses(Path p) throws IOException, URISyntaxException {
        Set<Path> classes = Utils.collectClasses(p);
        return classes.stream()
            .filter(f -> include(f))
            .collect(Collectors.toSet());
    }

}
