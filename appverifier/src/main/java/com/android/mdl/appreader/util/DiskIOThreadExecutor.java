package com.android.mdl.appreader.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

/* Global executor pools for the whole application. Grouping tasks like this avoids the effects of
 * task starvation (e.g. disk reads don't wait behind webservice requests).
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class DiskIOThreadExecutor
        implements Executor {

    private final Executor mDiskIO;

    public DiskIOThreadExecutor() {

        mDiskIO = Executors.newSingleThreadExecutor();
    }

    @Override
    public void
    execute(@NonNull Runnable command) {

        mDiskIO.execute(command);
    }
}
