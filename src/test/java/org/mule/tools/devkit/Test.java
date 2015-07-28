package org.mule.tools.devkit;

/**
 * Created by estebanwasinger on 7/28/15.
 */
public class Test {

    @org.junit.Test
    public void test(){
        System.out.println(MavenUtil.resolveMavenHomeDirectory("hola"));
    }
}
