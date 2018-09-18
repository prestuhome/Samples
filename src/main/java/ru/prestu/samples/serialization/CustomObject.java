package ru.prestu.samples.serialization;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

/*
Собственный протокол сериализации реализуется с помощью интерфейса java.io.Externalizable, разница в строгой реализации методов:
public void writeExternal(ObjectOutput out) throws IOException
public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
*/
public class CustomObject implements Externalizable {

    private transient boolean b;

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(b);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        b = in.readBoolean();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CustomObject other = (CustomObject) obj;
        return Objects.equals(b, other.b);
    }



}
