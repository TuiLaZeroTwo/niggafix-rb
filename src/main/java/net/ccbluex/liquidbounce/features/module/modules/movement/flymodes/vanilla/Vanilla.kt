/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.vanilla

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly.handleVanillaKickBypass
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly.vanillaSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

object Vanilla : FlyMode("Vanilla") {
    override fun onMove(event: MoveEvent) {
        val thePlayer = mc.thePlayer ?: return

        strafe(vanillaSpeed, true, event)

        thePlayer.onGround = false
        thePlayer.isInWeb = false

        thePlayer.capabilities.isFlying = false

        var ySpeed = 0.0

        if (mc.gameSettings.keyBindJump.isKeyDown)
            ySpeed += vanillaSpeed

        if (mc.gameSettings.keyBindSneak.isKeyDown)
            ySpeed -= vanillaSpeed

        thePlayer.motionY = ySpeed
        event.y = ySpeed

        handleVanillaKickBypass()
    }
}
