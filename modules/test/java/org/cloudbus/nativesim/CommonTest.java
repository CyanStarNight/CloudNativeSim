package org.cloudbus.nativesim;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;

public class CommonTest {

    @Test
    public void test(){
        int i=0;
        boolean f = testAssertBool(i);

    }


    @AssertTrue
    boolean testAssertBool(int i){
        i = 0;
        if (i == 1) return true;
        return false;
    }
}