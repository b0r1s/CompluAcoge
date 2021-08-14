/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gal.boris.compluacoge.extras;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Global executor pools for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
public class AppExecutors {

    private static final int THREAD_COUNT = 4;

    private final Executor singleThread;

    private final Executor cachePool;

    private final Executor mainThread;

    private static volatile AppExecutors INSTANCE;

    private AppExecutors(Executor singleThread, Executor cachePool, Executor mainThread) {
        this.singleThread = singleThread;
        this.cachePool = cachePool;
        this.mainThread = mainThread;
    }

    public static AppExecutors getInstance() {
        if(INSTANCE == null) {
            synchronized (AppExecutors.class) {
                if(INSTANCE == null) {
                    INSTANCE = new AppExecutors(Executors.newSingleThreadExecutor(), Executors.newCachedThreadPool(),
                            new MainThreadExecutor());
                }
            }
        }
        return INSTANCE;
    }

    public Executor singleThread() {
        return singleThread;
    }


    public Executor cachePool() {
        return cachePool;
    }

    public Executor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
