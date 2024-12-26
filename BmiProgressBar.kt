package com.movevi.weight_loss_center

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt

class BmiProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val segmentWidth = 63f
    private val segmentHeight = 8f
    private val segmentSpacing = 8f
    private val segmentColors =
        arrayOf(
            "#60C8F6".toColorInt(),
            "#85D8CA".toColorInt(),
            "#F6DA7F".toColorInt(),
            "#FEA119".toColorInt(),
            "#CC5B53".toColorInt()
        )
    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint().apply {
        color = "#666666".toColorInt()
        textSize = 16f
        textAlign = Paint.Align.CENTER
    }
    private val cornerRadius = 5f

    private val bmiValues = arrayOf(18.5f, 24f, 27f, 30f)
    private val descriptions = arrayOf("偏瘦", "标准", "微胖", "肥胖", "重度")

    private var bmiValue: Float = 25.5f
    private var currentBmiDrawable: Drawable? = null

    init {
        currentBmiDrawable = ContextCompat.getDrawable(context, R.drawable.ic_cur_bmi_progress)?.let { drawable ->
            val wrappedDrawable = DrawableCompat.wrap(drawable.mutate())
            DrawableCompat.setTint(wrappedDrawable, segmentColors[0]) // 初始颜色，可以根据需要调整
            wrappedDrawable
        }
    }

    fun setBmiValue(value: Float) {
        bmiValue = value
        invalidate() // 重新绘制视图
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val totalWidth = segmentWidth * 5 + segmentSpacing * 4
        val startX = (width - totalWidth) / 2 // 居中显示

        // 绘制BMI数值在每一段的空隙处
        val bmiTextY = -textPaint.fontMetrics.top
        textPaint.setColor("#AAAAAA".toColorInt())
        for (i in 0 until bmiValues.size) {
            val gapStartX = startX + i * (segmentWidth + segmentSpacing) + segmentWidth
            val gapEndX = gapStartX + segmentSpacing
            val textX = (gapStartX + gapEndX) / 2
            canvas.drawText(bmiValues[i].toString(), textX, bmiTextY, textPaint)
        }

        // 绘制进度条
        val progressBarY = bmiTextY + 20f // 留出一些间距
        for (i in 0 until 5) {
            val segmentStartX = startX + i * (segmentWidth + segmentSpacing)
            val segmentEndX = segmentStartX + segmentWidth

            paint.color = segmentColors[i]
            if (segmentStartX < width) {
                val drawEndX = (if (segmentEndX > width) width else segmentEndX) as Float

                val path = Path()
                if (i == 0) {
                    // 第一段，左上和左下角有圆角
                    path.moveTo(segmentStartX + cornerRadius, progressBarY)
                    path.lineTo(drawEndX, progressBarY)
                    path.lineTo(drawEndX, progressBarY + segmentHeight)
                    path.lineTo(segmentStartX + cornerRadius, progressBarY + segmentHeight)
                    path.arcTo(
                        segmentStartX,
                        progressBarY + segmentHeight - cornerRadius * 2,
                        segmentStartX + cornerRadius * 2,
                        progressBarY + segmentHeight,
                        90f,
                        90f,
                        false
                    )
                    path.arcTo(
                        segmentStartX,
                        progressBarY,
                        segmentStartX + cornerRadius * 2,
                        progressBarY + cornerRadius * 2,
                        180f,
                        90f,
                        false
                    )
                } else if (i == 4) {
                    // 最后一段，右上和右下角有圆角
                    path.moveTo(segmentStartX, progressBarY)
                    path.lineTo(drawEndX - cornerRadius, progressBarY)
                    path.arcTo(
                        drawEndX - cornerRadius * 2,
                        progressBarY,
                        drawEndX,
                        progressBarY + cornerRadius * 2,
                        270f,
                        90f,
                        false
                    )
                    path.lineTo(drawEndX, progressBarY + segmentHeight)
                    path.arcTo(
                        drawEndX - cornerRadius * 2,
                        progressBarY + segmentHeight - cornerRadius * 2,
                        drawEndX,
                        progressBarY + segmentHeight,
                        0f,
                        90f,
                        false
                    )
                    path.lineTo(segmentStartX, progressBarY + segmentHeight)
                } else {
                    // 其他段，直接绘制矩形
                    path.moveTo(segmentStartX, progressBarY)
                    path.lineTo(drawEndX, progressBarY)
                    path.lineTo(drawEndX, progressBarY + segmentHeight)
                    path.lineTo(segmentStartX, progressBarY + segmentHeight)
                }
                path.close()

                canvas.drawPath(path, paint)
            }
        }

        // 绘制描述
        textPaint.setColor("#666666".toColorInt())
        val descriptionY = progressBarY + segmentHeight + textPaint.fontMetrics.top + 48f
        for (i in 0 until descriptions.size) {
            val descriptionX = startX + i * (segmentWidth + segmentSpacing) + segmentWidth / 2
            canvas.drawText(descriptions[i], descriptionX, descriptionY, textPaint)
        }

        // 绘制当前BMI指示器
        currentBmiDrawable?.let { drawable ->
            val drawableWidth = 30f
            val drawableHeight = 30f
            val drawableY = progressBarY + (segmentHeight - drawableHeight) / 2

            // 计算当前BMI值对应的X位置
            val bmiPositionX = calculateBmiPosition(bmiValue, startX)

            // 根据BMI值选择颜色
            val colorIndex = when {
                bmiValue <= 18.5f -> 0
                bmiValue <= 24f -> 1
                bmiValue <= 27f -> 2
                bmiValue <= 30f -> 3
                else -> 4
            }
            DrawableCompat.setTint(drawable, segmentColors[colorIndex])

            paint.setColor(Color.WHITE)
            canvas.drawCircle(bmiPositionX, drawableY + drawableHeight / 2, drawableWidth / 2, paint)
            // 绘制指示器
            drawable.setBounds(
                (bmiPositionX - drawableWidth / 2).toInt(),
                drawableY.toInt(),
                (bmiPositionX + drawableWidth / 2).toInt(),
                (drawableY + drawableHeight).toInt()
            )
            drawable.draw(canvas)
        }
    }

    private fun calculateBmiPosition(bmi: Float, startX: Float): Float {
        val segmentWidthWithSpacing = segmentWidth + segmentSpacing
        val segmentStartX = startX

        return when {
            bmi <= 18.5f -> {
                segmentStartX + (bmi / 18.5f) * segmentWidth
            }
            bmi <= 24f -> {
                segmentStartX + segmentWidthWithSpacing + ((bmi - 18.5f) / (24f - 18.5f)) * segmentWidth
            }
            bmi <= 27f -> {
                segmentStartX + 2 * segmentWidthWithSpacing + ((bmi - 24f) / (27f - 24f)) * segmentWidth
            }
            bmi <= 30f -> {
                segmentStartX + 3 * segmentWidthWithSpacing + ((bmi - 27f) / (30f - 27f)) * segmentWidth
            }
            bmi <= 40f -> {
                segmentStartX + 4 * segmentWidthWithSpacing + ((bmi - 30f) / (40f - 30f)) * segmentWidth
            }
            else -> {
                segmentStartX + 4 * segmentWidthWithSpacing + segmentWidth
            }
        }
    }
}
