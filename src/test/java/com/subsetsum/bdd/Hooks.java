package com.subsetsum.bdd;

import io.cucumber.java.After;

public class Hooks {

    @After
    public void resetContext() {
        TestContext.reset();
    }
}
