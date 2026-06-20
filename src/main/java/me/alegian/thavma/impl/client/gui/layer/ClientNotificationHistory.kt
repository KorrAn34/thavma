package me.alegian.thavma.impl.client.gui.layer

import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

// ClientNotificationHistory.kt
@OnlyIn(Dist.CLIENT)
object ClientNotificationHistory {

  // Null = response not yet received (distinct from empty = no entries recorded)
  var entries: List<NotificationHistory.Entry>? = null
    private set

  internal fun update(incoming: List<NotificationHistory.Entry>) {
    entries = incoming
  }

  fun clear() {
    entries = null
  }
}