package com.example.snowflakesk

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

data class Snowflake(
    var x: Float, var y: Float,
    val velocity: Float, val radius: Float, val color: Int,
    val baseX: Float,
    val amp: Float,
    val phase: Float
)

lateinit var snow: Array<Snowflake>
val paint = Paint()
var h = 1000; var w = 1000

open class Snowflakes(ctx: Context) : View(ctx) {
    var moveTask: MoveTask? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLUE)
        for (s in snow) {
            paint.color = s.color
            canvas.drawCircle(s.x, s.y, s.radius, paint)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        h = bottom - top; w = right - left
        snow = Array(40) {
            val x0 = Random.nextFloat() * w
            Snowflake(
                x = x0,
                y = Random.nextFloat() * h,
                velocity = 15 + 10 * Random.nextFloat(),
                radius = 8 + 16 * Random.nextFloat(),
                color = Color.rgb(200 + Random.nextInt(56), 200 + Random.nextInt(56), 255),
                baseX = x0,
                amp = 20 + 40 * Random.nextFloat(),
                phase = Random.nextFloat() * 6.28f
            )
        }
        Log.d("mytag", "snowflakes: ${snow.size}, view $w x $h px")
    }

    fun moveSnowflakes() {
        for (s in snow) {
            val k = 1f - s.y / h
            s.y += s.velocity * k
            s.x = s.baseX + s.amp * sin(s.y / 80f + s.phase)
            if (s.y > h - s.radius) {
                s.y = -s.radius
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (moveTask?.status != AsyncTask.Status.RUNNING) {
            moveTask = MoveTask(this)
            moveTask?.execute(30)
        }
        return false
    }

    override fun onDetachedFromWindow() {
        moveTask?.cancel(true)
        super.onDetachedFromWindow()
    }

    @Suppress("DEPRECATION")
    class MoveTask(val s: Snowflakes) : AsyncTask<Int, Int, Int>() {
        override fun doInBackground(vararg params: Int?): Int {
            val delay = (params[0] ?: 30).toLong()
            while (!isCancelled) {
                try {
                    Thread.sleep(delay)
                } catch (e: InterruptedException) {
                    break
                }
                s.moveSnowflakes()
                publishProgress()
            }
            return 0
        }

        override fun onProgressUpdate(vararg values: Int?) {
            s.invalidate()
        }
    }
}