package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util;

/**
 * @author Tiziano Fagni (tiziano.fagni@isti.cnr.it)
 */
public class Cond {
    /**
     * Check if the specified condition is true otherwise it raises an exception
     * specifying the given error message.
     *
     * @param condition
     * @param errorMsg
     */
    public static void require(boolean condition, String errorMsg) {
        if (!condition)
            throw new IllegalArgumentException(errorMsg);
    }

    public static void requireNotNull(Object obj, String paramName) {
        if (obj == null)
            throw new NullPointerException("The object specified by '" + paramName + "' is 'null'");
    }
}