package nz.ac.wgtn.shadedetector.jcompile.oracles.comparators;

import com.google.common.base.Preconditions;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nz.ac.wgtn.shadedetector.jcompile.oracles.comparators.SemVer.compareSemVer;
import static nz.ac.wgtn.shadedetector.jcompile.oracles.comparators.SemVer.parseSemVer;

public class OpenJDKVersionsComparator implements Comparator<String> {

    public static final String OPENJDK_SEMVER = "^([^\\.]+)-(\\d+\\.\\d+\\.\\d+)";
    public static final Pattern OPENJDK_SEMVER_REGEX = Pattern.compile(OPENJDK_SEMVER);

    @Override
    public int compare(String o1, String o2) {
        Matcher m1 = OPENJDK_SEMVER_REGEX.matcher(o1);
        Matcher m2 = OPENJDK_SEMVER_REGEX.matcher(o2);
        if (m1.find() && m2.find()) {
            int lineageDiff = m1.group(0).compareTo(m2.group(0));
            if (lineageDiff != 0) {
                return lineageDiff;      // Compiler lineages differ
            }
        } else {
            throw new IllegalArgumentException("One of the operands did not match " + OPENJDK_SEMVER);
        }

        String version1 = m1.group(1);
        String version2 = m2.group(1);

        int[] semver1 = parseSemVer(version1);
        int[] semver2 = parseSemVer(version2);

        return compareSemVer(semver1,semver2);
    }



}
