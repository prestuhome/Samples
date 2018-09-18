package ru.prestu.samples.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StreamTokenizer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FilesTest {

    @BeforeEach
    @AfterEach
    public void deleteExistedFile() {
        File file = new File("tmp");
        if (file.exists()) file.delete();
    }

    @Test
    public void testFiles() throws IOException {
        File file = new File("tmp");

        //Проверка существования файла
        assertTrue(!file.exists());
        //Создание файла
        file.createNewFile();
        assertTrue(file.exists());
        //Проверка того, что данный объект - файл
        assertTrue(file.isFile());
        //Удаление файла
        file.delete();
        assertTrue(!file.exists());
        //Создание директории
        file.mkdir();
        assertTrue(file.exists());
        //Проверка того, что данный объект - директория
        assertTrue(file.isDirectory());
        file.delete();
        assertTrue(!file.exists());
    }

    @Test
    public void testIO() throws IOException {
        File file = new File("tmp");

        //BufferedWriter (переписывание файла)
        String string = "Hello";
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        bufferedWriter.write(string);
        bufferedWriter.close();

        //BufferedReader
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String currentLine = bufferedReader.readLine();
        bufferedReader.close();
        assertEquals(string, currentLine);

        //BufferedWriter (добавление в файл)
        String str = "world";
        bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        bufferedWriter.append(' ');
        bufferedWriter.append(str);
        bufferedWriter.append(' ');
        bufferedWriter.append("1");
        bufferedWriter.close();

        //Scanner, чтение по слову
        Scanner scanner = new Scanner(file);
        scanner.useDelimiter(" ");
        assertTrue(scanner.hasNext());
        assertEquals("Hello", scanner.next());
        assertEquals("world", scanner.next());
        assertEquals(1, scanner.nextInt());
        scanner.close();

        //PrintWriter, форматированный ввод
        PrintWriter printWriter = new PrintWriter(new FileWriter(file));
        printWriter.print("Some ");
        printWriter.printf("%s %d", "notebook", 5);
        printWriter.close();

        //StreamTokenizer, чтение по токенам
        FileReader fileReader = new FileReader(file);
        StreamTokenizer tokenizer = new StreamTokenizer(fileReader);
        tokenizer.nextToken();
        assertEquals(StreamTokenizer.TT_WORD, tokenizer.ttype);
        assertEquals("Some", tokenizer.sval);
        tokenizer.nextToken();
        assertEquals(StreamTokenizer.TT_WORD, tokenizer.ttype);
        assertEquals("notebook", tokenizer.sval);
        tokenizer.nextToken();
        assertEquals(StreamTokenizer.TT_NUMBER, tokenizer.ttype);
        assertEquals(5, tokenizer.nval, 0.0000001);
        tokenizer.nextToken();
        assertEquals(StreamTokenizer.TT_EOF, tokenizer.ttype);
        fileReader.close();

        //FileOutputStream
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] stringToBytes = string.getBytes();
        fileOutputStream.write(stringToBytes);
        fileOutputStream.close();

        //FileInputStream
        byte[] bytes = new byte[stringToBytes.length];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(bytes);
        String result = new String(bytes);
        fileInputStream.close();
        assertEquals(string, result);

        //DataOutputStream, запись примитивов и строк
        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));
        dataOutputStream.writeUTF(string);
        dataOutputStream.close();

        //DataInputStream, чтение примитивов и строк
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
        result = dataInputStream.readUTF();
        dataInputStream.close();
        assertEquals(string, result);

        //RandomAccessFile
        int data = 2000;
        int integerResult;
        RandomAccessFile randomAccessFileWriter = new RandomAccessFile(file, "rw");
        randomAccessFileWriter.seek(4);
        randomAccessFileWriter.writeInt(data);
        randomAccessFileWriter.close();

        RandomAccessFile randomAccessFileReader = new RandomAccessFile(file, "r");
        randomAccessFileReader.seek(4);
        integerResult = randomAccessFileReader.readInt();
        randomAccessFileReader.close();
        assertEquals(data, integerResult);

        //FileChannel, обработка больших файлов происходит быстрее
        RandomAccessFile streamWriter = new RandomAccessFile(file, "rw");
        FileChannel channelWriter = streamWriter.getChannel();
        stringToBytes = string.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(stringToBytes.length);
        byteBuffer.put(stringToBytes);
        byteBuffer.flip();
        channelWriter.write(byteBuffer);
        streamWriter.close();
        channelWriter.close();

        RandomAccessFile streamReader = new RandomAccessFile(file, "r");
        FileChannel channelReader = streamReader.getChannel();
        //Обычно берут "int bufferSize = (int) channelReader.size();", в данном случае возьмем точное число для теста.
        int bufferSize = stringToBytes.length;
        byteBuffer = ByteBuffer.allocate(bufferSize);
        channelReader.read(byteBuffer);
        byteBuffer.flip();
        assertEquals(string, new String(byteBuffer.array()));
        channelReader.close();
        streamReader.close();

        //Java 7, запись
        Path path = Paths.get(file.getAbsolutePath());
        stringToBytes = string.getBytes();
        Files.write(path, stringToBytes);

        //Java 7, чтение маленького файла
        result = Files.readAllLines(path).get(0);
        assertEquals(string, result);

        //Java 7, чтение большого файла
        bufferedReader = Files.newBufferedReader(path);
        result = bufferedReader.readLine();
        assertEquals(string, result);

        file.delete();
        assertTrue(!file.exists());
    }

    /*
    Блокирование файлов для других пользователей
    */
    @Test
    public void testLock() throws IOException {
        File file = new File("tmp");

        RandomAccessFile stream = new RandomAccessFile(file, "rw");
        FileChannel channel = stream.getChannel();

        try (FileLock lock = channel.tryLock()) {
            stream.writeChars("test lock");
            lock.release();
        } catch (OverlappingFileLockException e) {
            //something to do
        } finally {
            stream.close();
            channel.close();
        }
        file.delete();
        assertTrue(!file.exists());
    }

}
