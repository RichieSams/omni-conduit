package richiesams.omniconduit.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import richiesams.omniconduit.events.MouseEvents.MouseScroll


object MouseEvents {
    @JvmField
    val MOUSE_WHEEL_SCROLLED: Event<MouseScroll> = EventFactory.createArrayBacked(
        MouseScroll::class.java
    ) { listeners: Array<MouseScroll> ->
        MouseScroll { dx: Double, dy: Double ->
            var cancel = false
            for (listener in listeners) {
                cancel = cancel || listener.onMouseScrolled(dx, dy)
            }
            return@MouseScroll cancel
        }
    }

    fun interface MouseScroll {
        fun onMouseScrolled(dx: Double, dy: Double): Boolean
    }
}
