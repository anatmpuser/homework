package homework;

import java.io.File;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;

/** This class takes lines from the queue prepared by RepoDataReader,
 * and writes the lines into separate files. **/
public class RepoDataWriter implements Runnable{

    protected BlockingQueue queue = null;
    private final static String RESULT_FILE_PREFIX = "SecurityResultGitHub";

    public RepoDataWriter(BlockingQueue queue) {
        this.queue = queue;
    }

    public void run() {
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault());
        String dateTimeStr = DATE_TIME_FORMATTER.format(Instant.now());

        int i = 1;
        try {
            while (true) {
                String buffer = (String) queue.take();
                // Check whether end of file has been reached
                if (buffer.equals("EOF")) {
                    break;
                }
                // Write the buffer line to file
                PrintWriter writer = new PrintWriter(new File(RESULT_FILE_PREFIX + i++ + dateTimeStr));
                writer.println(buffer);
                writer.close();
            }
            System.out.println(String.format("Wrote %d lines to files: %s*", i-1, RESULT_FILE_PREFIX));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}