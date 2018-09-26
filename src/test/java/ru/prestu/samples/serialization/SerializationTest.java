package ru.prestu.samples.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SerializationTest {

    /*
    Интерфейс java.io.Serializable
    * Интерфейс-маркер
    * Сохранение объекта в последовательность байт
    * Запись/чтение через ObjectOutputStream/ObjectInputStream
    * Записываются:
      1) Метаданные класса;
      2) Метаданные всех родителей;
      3) Значение полей всех родителей;
      4) Значение полей класса
    * serialVersionUID (версия класса, желательно указывать)
    * transient поля пропускаются
    * все потомки Serializable-класса тоже Serializable
    * если супер-класс не Serializable, то его поля инициализируются конструктором по умолчанию (no-args constructor)
    */
    @Test
    public void testSerialization() throws FileNotFoundException, IOException, ClassNotFoundException{
        DataObject obj = new DataObject();
        obj.setI(10);
        obj.setS("string");
        obj.setDef(new String[] {"1", "2", "3"});
        obj.setMyData("long string, only string, nothing, but long string");
        obj.setObj(new CustomObject());

        File file = new File("object.bin");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
        objectOutputStream.close();
        fileOutputStream.close();

        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        DataObject clone = (DataObject) objectInputStream.readObject();
        objectInputStream.close();
        fileInputStream.close();
        Assertions.assertEquals(clone.getMyData(), obj.getMyData());
        Assertions.assertEquals(obj, clone);
        file.delete();
        Assertions.assertTrue(!file.exists());
    }

}
