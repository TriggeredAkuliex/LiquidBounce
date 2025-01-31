/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2016 - 2021 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EngineRenderEvent
import net.ccbluex.liquidbounce.event.PlayerTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.renderer.engine.*
import net.ccbluex.liquidbounce.renderer.utils.rainbow
import org.lwjgl.opengl.GL11.*

object ModuleBreadcrumbs : Module("Breadcrumbs", Category.RENDER) {

    private val color by color("Color", Color4b(255, 179, 72, 255))
    private val colorRainbow by boolean("Rainbow", false)

    private val positions = mutableListOf<Double>()
    private var lastPosX = 0.0
    private var lastPosY = 0.0
    private var lastPosZ = 0.0

    override fun enable() {
        synchronized(positions) {
            positions.addAll(listOf(player.x, player.eyeY, player.z))
            positions.addAll(listOf(player.x, player.y, player.z))
        }
    }

    override fun disable() {
        synchronized(positions) {
            positions.clear()
        }
    }

    val renderHandler = handler<EngineRenderEvent> {
        val color = if (colorRainbow) rainbow() else color

        synchronized(positions) {
            val renderTask = ColoredPrimitiveRenderTask(this.positions.size, PrimitiveType.LineStrip)

            for (i in 0 until this.positions.size / 3) {
                renderTask.index(renderTask.vertex(Vec3(positions[i * 3], positions[i * 3 + 1], positions[i * 3 + 2]), color))
            }

            RenderEngine.enqueueForRendering(RenderEngine.CAMERA_VIEW_LAYER, renderTask)
        }
    }

    val updateHandler = handler<PlayerTickEvent> {
        if (player.x == lastPosX && player.y == lastPosY && player.z == lastPosZ) {
            return@handler
        }

        lastPosX = player.x
        lastPosY = player.y
        lastPosZ = player.z

        synchronized(positions) {
            positions.addAll(listOf(player.x, player.y, player.z))
        }
    }

}
