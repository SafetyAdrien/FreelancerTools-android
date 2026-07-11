package com.freelancertools.app.ui.tools.blobmaker

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Generates a smooth, organic blob shape from randomized points around a circle. */
object BlobGenerator {

    fun points(complexity: Int, irregularityPercent: Int, radius: Float, seed: Long = Random.nextLong()): List<Offset> {
        val random = Random(seed)
        val irregularity = irregularityPercent / 100f
        return (0 until complexity).map { i ->
            val angle = (2 * Math.PI * i / complexity).toFloat()
            val variance = 1f - irregularity / 2f + random.nextFloat() * irregularity
            val r = radius * variance
            Offset(cos(angle) * r, sin(angle) * r)
        }
    }

    /** Midpoints used as smooth curve endpoints between successive control points. */
    fun midpoints(points: List<Offset>): List<Offset> {
        val n = points.size
        return (0 until n).map { i ->
            val a = points[i]
            val b = points[(i + 1) % n]
            Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f)
        }
    }

    /** SVG path `d` attribute for the same blob, centered at (cx, cy). */
    fun svgPathData(points: List<Offset>, cx: Float, cy: Float): String {
        val mids = midpoints(points)
        val start = mids.last()
        val sb = StringBuilder("M ${cx + start.x},${cy + start.y} ")
        for (i in points.indices) {
            val control = points[i]
            val end = mids[i]
            sb.append("Q ${cx + control.x},${cy + control.y} ${cx + end.x},${cy + end.y} ")
        }
        sb.append("Z")
        return sb.toString()
    }
}
