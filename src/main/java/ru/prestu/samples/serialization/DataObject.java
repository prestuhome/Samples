package ru.prestu.samples.serialization;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

/*
Сериализация
private void writeObject(ObjectOutputStream out) throws IOException
private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
*/
public class DataObject extends NonSerializable implements Serializable {

    private int i;
    private String s;
    private transient String[] def;
    private CustomObject obj;

    public CustomObject getObj() {
        return obj;
    }

    public void setObj(CustomObject obj) {
        this.obj = obj;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String[] getDef() {
        return def;
    }

    public void setDef(String[] def) {
        this.def = def;
    }

    @Override
    public int hashCode() {
        return Objects.hash(i, s, obj);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DataObject other = (DataObject) obj;

        return Objects.equals(i, other.i) &&
               Objects.equals(s, other.s) &&
               //Objects.equals(def, other.def) &&
               Objects.equals(this.obj, other.obj);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(getMyData());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setMyData((String) in.readObject());
    }

}
