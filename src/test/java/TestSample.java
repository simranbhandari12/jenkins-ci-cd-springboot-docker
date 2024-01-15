import static org.junit.Assert.*;
import org.junit.Test;

public class TestSample {

    @Test 
    public void testMethod() {
        System.out.println("Inside testMethod()");
        int check = 1 + 1;
	assertEquals(check,2);
    }
}