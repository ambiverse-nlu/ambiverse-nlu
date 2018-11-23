package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima;

import org.apache.hadoop.io.Writable;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.cas.impl.Serialization;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import java.io.*;

public class SCAS implements Externalizable, Writable{
    private CAS cas;

    public SCAS() throws UIMAException {
        this(JCasFactory.createJCas().getCas());
    }

    public SCAS(CAS cas) {
        this.cas = cas;
    }

    public JCas getJCas() throws CASException {
        return this.cas.getJCas();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        this.write(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.readFields(in);
    }

    public void write(DataOutput dataOutput) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Serialization.serializeWithCompression(this.cas, baos);

        dataOutput.writeInt(baos.size());
        dataOutput.write(baos.toByteArray());
    }

    public void readFields(DataInput dataInput) throws IOException {
        int size = dataInput.readInt();
        byte[] bytes = new byte[size];
        dataInput.readFully(bytes);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Serialization.deserializeCAS(this.cas, bais);
    }

    public SCAS copy() throws UIMAException, IOException, ClassNotFoundException {
        JCas jcas = JCasFactory.createJCas();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream docOS = new ObjectOutputStream(buffer);
        CASCompleteSerializer serializerOut = Serialization.serializeCASComplete(this.getJCas().getCasImpl());
        docOS.writeObject(serializerOut);
        docOS.close();
        ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        CASCompleteSerializer serializerIn = (CASCompleteSerializer)is.readObject();
        Serialization.deserializeCASComplete(serializerIn, jcas.getCasImpl());
        return new SCAS(jcas.getCas());
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Serialization.serializeWithCompression(this.cas, baos);

        return baos.toByteArray();
    }

    public void deserialize(byte[] bytes) throws UIMAException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Serialization.deserializeCAS(this.cas, bais);
    }
}