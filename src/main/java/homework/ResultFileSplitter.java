package homework;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/** This class reads a multi-line file and writes each line into separate file
 * Performs multi threaded read and write via blocking queue **/
public class ResultFileSplitter {
    static void split(String fileName){
        BlockingQueue queue = new ArrayBlockingQueue(1024);

        RepoDataReader reader = new RepoDataReader(queue, fileName);
        RepoDataWriter writer = new RepoDataWriter(queue);

        new Thread(reader).start();
        new Thread(writer).start();

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
