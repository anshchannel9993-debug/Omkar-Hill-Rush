package com.example.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.Vehicle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object VehicleRenderer {

    fun drawVehicle(
        scope: DrawScope,
        vehicle: Vehicle,
        center: Offset,
        angleDegrees: Float,
        rearWheelPos: Offset,
        frontWheelPos: Offset,
        rearWheelRot: Float = 0f,
        frontWheelRot: Float = 0f,
        isThrottling: Boolean = false,
        exhaustFlame: Boolean = false
    ) {
        val rad = Math.toRadians(angleDegrees.toDouble()).toFloat()

        // 1. Draw suspension links from chassis to wheels
        drawSuspension(scope, center, rearWheelPos, vehicle)
        drawSuspension(scope, center, frontWheelPos, vehicle)

        // 2. Draw Chassis with Rotation around vehicle center
        scope.rotate(degrees = angleDegrees, pivot = center) {
            when (vehicle.id) {
                "desert_beast" -> drawDuneBuggyChassis(this, center, vehicle)
                "thunder_truck" -> drawMonsterTruckChassis(this, center, vehicle)
                "shadow_racer" -> drawShadowRacerChassis(this, center, vehicle)
                else -> drawMountainRunnerChassis(this, center, vehicle)
            }

            // Exhaust fire & smoke when accelerating hard
            if (isThrottling) {
                drawExhaustFlames(this, center, vehicle, exhaustFlame)
            }
        }

        // 3. Draw Wheels (unrotated relative to chassis angle, rotated on their axle)
        drawWheel(scope, rearWheelPos, vehicle.wheelRadius, rearWheelRot, vehicle.accentColor)
        drawWheel(scope, frontWheelPos, vehicle.wheelRadius, frontWheelRot, vehicle.accentColor)
    }

    private fun drawSuspension(scope: DrawScope, chassisCenter: Offset, wheelCenter: Offset, vehicle: Vehicle) {
        // Coilover suspension strut
        scope.drawLine(
            color = Color(0xFF475569),
            start = chassisCenter,
            end = wheelCenter,
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )

        // Coil spring rings along the strut
        val dx = wheelCenter.x - chassisCenter.x
        val dy = wheelCenter.y - chassisCenter.y
        val steps = 4
        for (i in 1..steps) {
            val t = i.toFloat() / (steps + 1)
            val sx = chassisCenter.x + dx * t
            val sy = chassisCenter.y + dy * t
            scope.drawCircle(
                color = vehicle.accentColor,
                radius = 4f,
                center = Offset(sx, sy)
            )
        }
    }

    private fun drawWheel(scope: DrawScope, center: Offset, radius: Float, rotationDegrees: Float, accentColor: Color) {
        // Outer Rubber Tire with treads
        scope.drawCircle(
            color = Color(0xFF181C24),
            radius = radius,
            center = center,
            style = Fill
        )
        scope.drawCircle(
            color = Color(0xFF0F1218),
            radius = radius,
            center = center,
            style = Stroke(width = 3.5f)
        )

        // Metallic Rim
        val rimRadius = radius * 0.62f
        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE2E8F0), Color(0xFF64748B), Color(0xFF334155)),
                center = center,
                radius = rimRadius
            ),
            radius = rimRadius,
            center = center
        )

        // Spokes with wheel rotation
        scope.rotate(degrees = rotationDegrees, pivot = center) {
            for (i in 0 until 5) {
                val spRad = (i * 72f) * (PI / 180f)
                val ex = center.x + cos(spRad).toFloat() * rimRadius * 0.9f
                val ey = center.y + sin(spRad).toFloat() * rimRadius * 0.9f
                drawLine(
                    color = accentColor,
                    start = center,
                    end = Offset(ex, ey),
                    strokeWidth = 2.5f
                )
            }
        }

        // Center hub bolt
        scope.drawCircle(
            color = Color(0xFFCBD5E1),
            radius = radius * 0.2f,
            center = center
        )
    }

    private fun drawMountainRunnerChassis(scope: DrawScope, center: Offset, vehicle: Vehicle) {
        val w = vehicle.chassisLength
        val h = vehicle.chassisHeight

        // Main body path
        val path = Path().apply {
            moveTo(center.x - w * 0.48f, center.y + h * 0.25f) // Rear bumper
            lineTo(center.x - w * 0.45f, center.y - h * 0.2f) // Rear pillar
            lineTo(center.x - w * 0.15f, center.y - h * 0.55f) // Cabin roof back
            lineTo(center.x + w * 0.15f, center.y - h * 0.55f) // Cabin roof front
            lineTo(center.x + w * 0.32f, center.y - h * 0.1f) // Windshield to hood
            lineTo(center.x + w * 0.48f, center.y - h * 0.05f) // Front nose
            lineTo(center.x + w * 0.48f, center.y + h * 0.25f) // Front bumper
            lineTo(center.x + w * 0.28f, center.y + h * 0.3f) // Front wheel well
            lineTo(center.x - w * 0.28f, center.y + h * 0.3f) // Underbody
            close()
        }

        // Body paint
        scope.drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(vehicle.accentColor, vehicle.primaryColor, Color(0xFFBF360C)),
                startY = center.y - h,
                endY = center.y + h
            )
        )
        scope.drawPath(
            path = path,
            color = Color(0x99FFFFFF),
            style = Stroke(width = 2f)
        )

        // Tinted cabin glass
        val glassPath = Path().apply {
            moveTo(center.x - w * 0.12f, center.y - h * 0.48f)
            lineTo(center.x + w * 0.13f, center.y - h * 0.48f)
            lineTo(center.x + w * 0.26f, center.y - h * 0.12f)
            lineTo(center.x - w * 0.12f, center.y - h * 0.12f)
            close()
        }
        scope.drawPath(
            path = glassPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xEE00E5FF), Color(0xAA0284C7)),
                start = Offset(center.x, center.y - h * 0.5f),
                end = Offset(center.x, center.y)
            )
        )

        // Roll bar rack on roof
        scope.drawLine(
            color = Color(0xFFF1F5F9),
            start = Offset(center.x - w * 0.2f, center.y - h * 0.58f),
            end = Offset(center.x + w * 0.18f, center.y - h * 0.58f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // Headlight glow
        scope.drawCircle(
            color = Color(0xFFFFFF77),
            radius = 5f,
            center = Offset(center.x + w * 0.47f, center.y + h * 0.05f)
        )
    }

    private fun drawDuneBuggyChassis(scope: DrawScope, center: Offset, vehicle: Vehicle) {
        val w = vehicle.chassisLength
        val h = vehicle.chassisHeight

        // Tubular exoskeleton frame
        val framePath = Path().apply {
            moveTo(center.x - w * 0.45f, center.y + h * 0.3f)
            lineTo(center.x - w * 0.3f, center.y - h * 0.5f) // Roll cage arch rear
            lineTo(center.x + w * 0.1f, center.y - h * 0.55f) // Roof top
            lineTo(center.x + w * 0.42f, center.y + h * 0.1f) // Slanted front nose
            lineTo(center.x + w * 0.45f, center.y + h * 0.3f)
            lineTo(center.x - w * 0.4f, center.y + h * 0.3f)
        }

        scope.drawPath(
            path = framePath,
            color = vehicle.primaryColor,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // Inner aero panel
        val panelPath = Path().apply {
            moveTo(center.x - w * 0.25f, center.y + h * 0.25f)
            lineTo(center.x - w * 0.05f, center.y - h * 0.2f)
            lineTo(center.x + w * 0.3f, center.y + h * 0.15f)
            close()
        }
        scope.drawPath(
            path = panelPath,
            brush = Brush.horizontalGradient(
                colors = listOf(vehicle.primaryColor, vehicle.accentColor)
            )
        )

        // Rear engine block
        scope.drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(center.x - w * 0.46f, center.y - h * 0.15f),
            size = Size(w * 0.18f, h * 0.4f),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }

    private fun drawMonsterTruckChassis(scope: DrawScope, center: Offset, vehicle: Vehicle) {
        val w = vehicle.chassisLength
        val h = vehicle.chassisHeight

        // Massive Heavy Truck Cabin & Bed
        val path = Path().apply {
            moveTo(center.x - w * 0.48f, center.y + h * 0.2f) // Truck bed end
            lineTo(center.x - w * 0.48f, center.y - h * 0.15f)
            lineTo(center.x - w * 0.15f, center.y - h * 0.15f) // Bed front
            lineTo(center.x - w * 0.12f, center.y - h * 0.52f) // Cabin roof back
            lineTo(center.x + w * 0.2f, center.y - h * 0.52f) // Cabin roof front
            lineTo(center.x + w * 0.36f, center.y - h * 0.15f) // Hood
            lineTo(center.x + w * 0.48f, center.y - h * 0.12f) // Massive grille
            lineTo(center.x + w * 0.48f, center.y + h * 0.2f)
            close()
        }

        scope.drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(vehicle.primaryColor, Color(0xFF4A148C)),
                startY = center.y - h * 0.6f,
                endY = center.y + h * 0.3f
            )
        )
        scope.drawPath(
            path = path,
            color = vehicle.accentColor,
            style = Stroke(width = 2.5f)
        )

        // Roof light bar with 4 LEDs
        for (i in 0 until 4) {
            val lx = center.x - w * 0.05f + i * (w * 0.07f)
            scope.drawCircle(
                color = Color(0xFFFFD600),
                radius = 3.5f,
                center = Offset(lx, center.y - h * 0.56f)
            )
        }

        // Heavy front bull-bar
        scope.drawLine(
            color = Color(0xFFE2E8F0),
            start = Offset(center.x + w * 0.46f, center.y - h * 0.1f),
            end = Offset(center.x + w * 0.5f, center.y + h * 0.25f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }

    private fun drawShadowRacerChassis(scope: DrawScope, center: Offset, vehicle: Vehicle) {
        val w = vehicle.chassisLength
        val h = vehicle.chassisHeight

        // Ultra sleek hypercar silhouette
        val path = Path().apply {
            moveTo(center.x - w * 0.48f, center.y + h * 0.2f)
            lineTo(center.x - w * 0.45f, center.y - h * 0.35f) // Rear high spoiler
            lineTo(center.x - w * 0.25f, center.y - h * 0.15f)
            lineTo(center.x - w * 0.08f, center.y - h * 0.45f) // Cockpit canopy peak
            lineTo(center.x + w * 0.22f, center.y - h * 0.15f) // Long sloped hood
            lineTo(center.x + w * 0.48f, center.y + h * 0.1f) // Sharp nose splitter
            lineTo(center.x + w * 0.48f, center.y + h * 0.22f)
            close()
        }

        scope.drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF020617))
            )
        )
        // Neon green racing pinstripe
        scope.drawPath(
            path = path,
            color = vehicle.accentColor,
            style = Stroke(width = 2.5f)
        )

        // Aerodynamic canopy glass
        scope.drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x9900E676), Color(0x33004D40)),
                center = Offset(center.x + w * 0.05f, center.y - h * 0.25f),
                radius = 16f
            ),
            topLeft = Offset(center.x - w * 0.12f, center.y - h * 0.42f),
            size = Size(w * 0.28f, h * 0.4f)
        )
    }

    private fun drawExhaustFlames(scope: DrawScope, center: Offset, vehicle: Vehicle, isFlicker: Boolean) {
        val exX = center.x - vehicle.chassisLength * 0.48f
        val exY = center.y + vehicle.chassisHeight * 0.15f
        val flameLen = if (isFlicker) 22f else 14f

        val flamePath = Path().apply {
            moveTo(exX, exY - 3f)
            lineTo(exX - flameLen, exY)
            lineTo(exX, exY + 3f)
            close()
        }

        scope.drawPath(
            path = flamePath,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFFFD600), Color(0xFFFF3D00), Color(0x00FF3D00)),
                startX = exX,
                endX = exX - flameLen
            )
        )
    }
}
