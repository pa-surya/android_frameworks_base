package com.android.systemui.util.kotlin

import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Application
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.dagger.qualifiers.Main
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newFixedThreadPoolContext

private const val LIMIT_BACKGROUND_DISPATCHER_THREADS = true

/** Providers for various coroutines-related constructs. */
@Module
object CoroutinesModule {
    @Provides
    @SysUISingleton
    @Application
    fun applicationScope(
        @Main dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(dispatcher)

    @Provides
    @SysUISingleton
    @Main
    fun mainDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate

    /**
     * Default Coroutine dispatcher for background operations.
     *
     * Note that this is explicitly limiting the number of threads. In the past, we used
     * [Dispatchers.IO]. This caused >40 threads to be spawned, and a lot of thread list lock
     * contention between then, eventually causing jank.
     */
    @OptIn(DelicateCoroutinesApi::class)
    @Provides
    @SysUISingleton
    @Background
    fun bgDispatcher(): CoroutineDispatcher {
        return if (LIMIT_BACKGROUND_DISPATCHER_THREADS) {
            // Why a new ThreadPool instead of just using Dispatchers.IO with
            // CoroutineDispatcher.limitedParallelism? Because, if we were to use Dispatchers.IO, we
            // would share those threads with other dependencies using Dispatchers.IO.
            // Using a dedicated thread pool we have guarantees only SystemUI is able to schedule
            // code on those.
            newFixedThreadPoolContext(
                nThreads = Runtime.getRuntime().availableProcessors(),
                name = "SystemUIBg"
            )
        } else {
            Dispatchers.IO
        }
    }
}
