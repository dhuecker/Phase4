package Mips_code_gen;

import java.io.*;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class VM2MTest {
    private static final InputStream DEFAULT_STDIN = System.in;
    private static final ByteArrayOutputStream outputContent = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream errorContent = new ByteArrayOutputStream();
    private static final PrintStream ogOut = System.out;
    private static final PrintStream ogError = System.err;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outputContent));
        System.setErr(new PrintStream(errorContent));
    }

    @After
    public void rollbackChangesToStdin() {
        try {
            outputContent.reset();
            errorContent.reset();
        } catch (Exception e) {

        }

        System.setIn(DEFAULT_STDIN);
        System.setOut(ogOut);
        System.setErr(ogError);
        System.out.println(errorContent.toString());
    }

    @Test
    public void factorialTest() {
        try {
            File inputFile = new File("./tester/Phase4Tester/SelfTestCases/Factorial.vaporm");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        VM2M.instructionSelection();
        assertEquals("", outputContent.toString());
    }

    @Test
    public void binaryTreeTest() {
        try {
            File inputFile = new File("./tester/Phase4Tester/SelfTestCases/BinaryTree.vaporm");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        VM2M.instructionSelection();
        assertEquals("", outputContent.toString());
    }

    @Test
    public void binaryTreeOptTest() {
        try {
            File inputFile = new File("./tester/Phase4Tester/SelfTestCases/BinaryTree.opt.vaporm");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        VM2M.instructionSelection();
        assertEquals("", outputContent.toString());
    }

    @Test
    public void bubbleSortTest() {
        try {
            File inputFile = new File("./tester/Phase4Tester/SelfTestCases/BubbleSort.vaporm");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        VM2M.instructionSelection();
        assertEquals("", outputContent.toString());
    }

    @Test
    public void linearSearchTest() {
        try {
            File inputFile = new File("./tester/Phase4Tester/SelfTestCases/LinearSearch.vaporm");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        VM2M.instructionSelection();
        assertEquals("", outputContent.toString());
    }

    @Test
    public void moreThan4Test() {
        try {
            File inputFile = new File("./tester/Phase4Tester/SelfTestCases/MoreThan4.vaporm");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        VM2M.instructionSelection();
        assertEquals("", outputContent.toString());
    }

    @Test
    public void linkedListTest() {
        try {
            File inputFile = new File("./tester/Phase4Tester/SelfTestCases/LinkedList.vaporm");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }

        VM2M.instructionSelection();
        assertEquals("", outputContent.toString());
    }

}
