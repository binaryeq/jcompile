package nz.ac.wgtn.shadedetector.jcompile.oracles.comparators;

import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.List;

/**
 * Sem ver utilities.
 * @author jens dietrich
 */
public class SemVer {

    static int[] parseSemVer(String version) {
        String[] tokens = version.split("\\.");
        List<Integer> parts = new ArrayList<>();
        for (String token : tokens) {
            Integer maybeInt = Ints.tryParse(token);
            if (maybeInt == null) {
                break;
            } else {
                parts.add(Integer.parseInt(token));
            }
        }
        return Ints.toArray(parts);
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
