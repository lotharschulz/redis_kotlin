import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class CoroutineIdentifier(
    val identifier: Long,
) : AbstractCoroutineContextElement(CoroutineIdentifier) {
    public companion object CoroutineKey : CoroutineContext.Key<CoroutineIdentifier>
}