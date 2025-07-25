/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.minecraft.block.BlockStairs
import net.minecraft.util.BlockPos

object FastStairs : Module("FastStairs", Category.MOVEMENT) {

    private val mode by choices("Mode", arrayOf("Step", "NCP", "AAC3.1.0", "AAC3.3.6", "AAC3.3.13"), "NCP")
    private val longJump by boolean("LongJump", false) { mode.startsWith("AAC") }

    private var canJump = false

    private var walkingDown = false

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        if (!thePlayer.isMoving || Speed.handleEvents())
            return@handler

        when {
            thePlayer.fallDistance > 0 && !walkingDown -> walkingDown = true
            thePlayer.posY > thePlayer.prevChasingPosY -> walkingDown = false
        }

        val mode = mode

        if (!thePlayer.onGround)
            return@handler

        val blockPos = BlockPos(thePlayer)

        if (blockPos.block is BlockStairs && !walkingDown) {
            thePlayer.setPosition(thePlayer.posX, thePlayer.posY + 0.5, thePlayer.posZ)

            val motion = when (mode) {
                "NCP" -> 1.4
                "AAC3.1.0" -> 1.5
                "AAC3.3.13" -> 1.2
                else -> 1.0
            }

            thePlayer.motionX *= motion
            thePlayer.motionZ *= motion
        }

        if (blockPos.down().block is BlockStairs) {
            if (walkingDown) {
                when (mode) {
                    "NCP" -> thePlayer.motionY = -1.0
                    "AAC3.3.13" -> thePlayer.motionY -= 0.014
                }

                return@handler
            }

            val motion = when (mode) {
                "AAC3.3.6" -> 1.48
                "AAC3.3.13" -> 1.52
                else -> 1.3
            }

            thePlayer.motionX *= motion
            thePlayer.motionZ *= motion
            canJump = true
        } else if (mode.startsWith("AAC") && canJump) {
            if (longJump) {
                thePlayer.tryJump()
                thePlayer.motionX *= 1.35
                thePlayer.motionZ *= 1.35
            }

            canJump = false
        }
    }

    override val tag
        get() = mode
}