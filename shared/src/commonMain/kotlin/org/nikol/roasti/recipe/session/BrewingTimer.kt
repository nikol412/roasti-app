package org.nikol.roasti.recipe.session

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface BrewingTimer {
    fun countdown(durationSeconds: Int): Flow<Int>
}

class BrewingTimerImpl : BrewingTimer {
    override fun countdown(durationSeconds: Int): Flow<Int> = flow {
        var remaining = durationSeconds
        emit(remaining)
        while (remaining > 0) {
            delay(1000L)
            remaining--
            emit(remaining)
        }
    }
}
