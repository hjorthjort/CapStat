package capstat.tests;

import capstat.infrastructure.taskqueue.ITaskQueue;
import capstat.infrastructure.taskqueue.TextFileTaskQueue;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class TextFileTaskQueueTest {

    private static ITaskQueue tq;
    private static File taskFile = new File("taskfile.txt");


    @BeforeClass
    public static void setup() throws IOException {
        tq = new TextFileTaskQueue(taskFile);
    }

    @AfterClass
    public static void removeFile() {
        //Return value ignored, since it does not matter for test purposes.
        taskFile.delete();
    }

    @Before
    public void reset() throws IOException {
        tq.clear();
    }

    //Atomic tests

    @Test(expected = NoSuchElementException.class)
    public void peekTest() throws IOException {
        tq.add("Hello World");
        assertTrue(tq.peek().equals("Hello World"));
        tq.clear();
        //Thow NoSuchElementException
        tq.peek();
    }

    @Test(expected = NoSuchElementException.class)
    public void popTest() throws IOException {
        tq.add("Foobar");
        assertTrue(tq.pop().equals("Foobar"));
        //Throw the exception
        tq.pop();
    }


    //Large tests

    @Test
    public void addTen() throws IOException {

        String[] tasks = new String[]{
                "task one",
                "task two",
                "task three",
                "task four",
                "task five",
                "task six",
                "task seven",
                "task eight",
                "task nine",
                "task ten",
        };

        for (String task : tasks) {
            tq.add(task);
            String desc1 = String.format("\"task one\" should be peeked after" +
                    " adding %s", task);
            assertEquals(desc1, "task one", tq.peek());
        }

        assertEquals("Size should be ten", 10, tq.size());
    }

    @Test
    public void addAndPopTen() throws IOException {
        String[] tasks = new String[] {
                "task one",
                "task two",
                "task three",
                "task four",
                "task five",
                "task six",
                "task seven",
                "task eight",
                "task nine",
                "task ten",
        };

        for (String task : tasks) {
            tq.add(task);
            String desc1 = "Size should be one after add";
            assertEquals(desc1, 1, tq.size());


            String popped = tq.pop();
            String desc2 = String.format("Popped task should be %s", task);
            assertEquals(desc2, task, popped);

            assertFalse("Queue should be empty after pop", tq.hasElements());
        }
    }

    @Test
    public void addSameItemMultipleTimes() throws IOException {
        String task = "task yay";
        for (int i = 1; i <= 10; i++) {
            tq.add(task);
            String desc1 = String.format("Size should be %d", i);
            assertEquals(desc1, i, tq.size());
        }
    }

    @Test
    public void addAndPopSameItemMultipleTimes() throws IOException {
        String task = "task woohoo";
        for (int i = 1; i <= 10; i++) {
            tq.add(task);
            String desc1 = String.format("Size should be %d", i);
            assertEquals(desc1, i, tq.size());
        }

        for (int i = 1; i <= 10; i++) {
            String desc1 = String.format("Popped task should be \"%s\"", task);
            assertEquals(desc1, task, tq.pop());

            int size = 10 - i;
            String desc2 = String.format("Size should be %d", size);
            assertEquals(desc2, size, tq.size());
        }
    }

    @Test
    public void addAndContainsTen() throws IOException {
        String[] tasks = new String[] {
                "task one",
                "task two",
                "task three",
                "task four",
                "task five",
                "task six",
                "task seven",
                "task eight",
                "task nine",
                "task ten",
        };

        for (String task : tasks) {
            tq.add(task);
            String desc1 = String.format("Queue should contain \"%s\"", task);
            assertTrue(desc1, tq.contains(task));
        }

        for (String task : tasks) {
            tq.pop();
            String desc1 = String.format("Queue should no longer contain \"%s\"", task);
            assertFalse(desc1, tq.contains(task));
        }
    }
}
