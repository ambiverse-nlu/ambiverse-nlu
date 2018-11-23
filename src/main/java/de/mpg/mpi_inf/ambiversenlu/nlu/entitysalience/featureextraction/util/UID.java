package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util;

import java.util.Random;

/**
 * @author Tiziano Fagni (tiziano.fagni@isti.cnr.it)
 */
public class UID {
    public static <T> String generateUID(Class<T> cl) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append(cl.getSimpleName() + "-");
        for (int i = 0; i < 10; i++)
            sb.append("" + r.nextInt(10));
        return sb.toString();
    }
}