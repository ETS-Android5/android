package mega.privacy.android.app.data.facade

import androidx.lifecycle.Observer
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


/**
 * Event bus facade
 *
 * A facade class to decouple calls to the event bus observer. Global pub/sub implementations are
 * generally considered to be a bad design as it can make debugging hard and obscure sources of bugs.
 * It is always better to observe an explicit source of events. Converting these events to a flow
 * via this facade enables us to later refactor the code to be more explicit without having to
 * change the implementation in the consumer.
 *
 */
abstract class EventBusFacade<T> constructor(
    private val event: String,
    private val type: Class<T>
) {

    /**
     * Get events
     *
     * @return A flow of the requested events
     */
    fun getEvents(): Flow<T> {
        return getEventFlow(event, type)
    }

    private fun <T> getEventFlow(
        event: String,
        type: Class<T>
    ): Flow<T> {
        val eventObservable =
            LiveEventBus.get(event, type)

        return callbackFlow {
            val flowObserver = Observer(this::trySend)
            eventObservable.observeForever(flowObserver)
            awaitClose { eventObservable.removeObserver(flowObserver) }
        }
    }
}