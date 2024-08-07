/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

public interface BldBuildListener {
    int FINISHED_SUCCESSFULLY = 0;
    int ABORTED = 1;
    int FAILED_TO_RUN = 2;

    BldBuildListener DUMMY = state -> { };

    void buildFinished(int state);
}