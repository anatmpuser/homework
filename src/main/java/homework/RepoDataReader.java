package homework;

import java.io.*;
import java.util.concurrent.BlockingQueue;

/** This class reads lines from a file and puts them into a queue.
 * The queue will be consumed by RepoDataWriter class.
 * **/
public class RepoDataReader implements Runnable{

    protected BlockingQueue queue = null;
    protected String fileName;

    public RepoDataReader(BlockingQueue queue, String fileName) {
        this.queue = queue;
        this.fileName = fileName;
    }

    public void run() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(fileName)))){

            String buffer;
            int i=0;
            while((buffer = bufferedReader.readLine())!=null){
                i++;
                queue.put(buffer);
                Thread.sleep(100);
            }
            queue.put("EOF");  //When end of file has been reached
            System.out.println(String.format("Read %d lines from file: %s", i, fileName));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}