package nz.ac.wgtn.shadedetector.jcompile.oracles.comparators;

/**
 * Sem ver utilities.
 * @author jens dietrich
 */
public class SemVer {

    static int[] parseSemVer(String version) {
        String[] tokens = version.split("\\.");
        int[] parts = new int[tokens.length];
        for (int i=0;i<tokens.length;i++) {
            parts[i] = Integer.parseInt(tokens[i]);
        }
        return parts;
    }

    static int compareSemVer(int[] ver1,int[] ver2) {
        for (int i=0;i<Math.min(ver1.length,ver2.length);i++) {
            int diff = ver1[i] - ver2[i];
            if (diff!=0) {
                return diff;
            }
        }
        return 0;
    }
}
