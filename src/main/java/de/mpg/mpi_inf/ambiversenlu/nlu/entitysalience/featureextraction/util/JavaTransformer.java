package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.featureextraction.util;

import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.param.Param;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.param.ParamPair;
import scala.collection.JavaConversions;

import java.util.List;

/**
 * Base class for a generic Java Spark ML transformer.
 *
 * @author Tiziano Fagni (tiziano.fagni@isti.cnr.it)
 */
abstract public class JavaTransformer extends Transformer {

    private String uid;

    public JavaTransformer() {
        this.uid = UID.generateUID(getClass());
    }

    @Override
    public String uid() {
        return uid;
    }


    /**
     * Set the uid of the transformer.
     *
     * @param uid The uid of the transformer.
     */
    private void setUID(String uid) {
        this.uid = uid;
    }


    @Override
    public Transformer copy(ParamMap extra) {
        try {
            JavaTransformer t = getClass().newInstance();
            t.setUID(uid());
            Param[] parms = params();
            for (int i = 0; i < parms.length; i++) {
                Param p = parms[i];
                Object val = getOrDefault(p);
                t.set(p, val);
            }
            List<ParamPair<?>> extraParams = JavaConversions.asJavaList(extra.toSeq());
            for (ParamPair pp : extraParams) {
                Param p = pp.param();
                Object val = getOrDefault(p);
                t.set(p, val);
            }
            return t;
        } catch (Exception e) {
            throw new RuntimeException("Can not instantiate new instance of " + getClass().getName(), e);
        }
    }
}