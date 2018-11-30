package one.mixin.android.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.text.TextPaint
import android.util.ArrayMap
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import one.mixin.android.R
import one.mixin.android.api.LocalJobException
import one.mixin.android.db.ConversationDao
import one.mixin.android.db.ParticipantDao
import one.mixin.android.di.type.DatabaseCategory
import one.mixin.android.di.type.DatabaseCategoryEnum
import one.mixin.android.di.worker.AndroidWorkerInjector
import one.mixin.android.extension.getGroupAvatarPath
import one.mixin.android.extension.md5
import one.mixin.android.extension.saveGroupAvatar
import one.mixin.android.vo.User
import one.mixin.android.widget.AvatarView
import org.jetbrains.anko.dip
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GenerateAvatarWorker(context: Context, parameters: WorkerParameters) : BaseWork(context, parameters) {
    companion object {
        const val GROUP_ID = "group_id"
    }

    private lateinit var texts: ArrayMap<Int, String>

    @Inject
    lateinit var participantDao: ParticipantDao
    @Inject
    @field:[DatabaseCategory(DatabaseCategoryEnum.BASE)]
    lateinit var conversationDao: ConversationDao

    private val size = 256

    override fun onRun(): Result {
        val groupId = inputData.getString(GROUP_ID) ?: return Result.FAILURE
        val users = mutableListOf<User>()
        texts = ArrayMap()
        users.addAll(participantDao.getParticipantsAvatar(groupId))
        val sb = StringBuilder()
        for (u in users) {
            sb.append(u.avatarUrl).append("-")
        }
        sb.append(groupId)
        val name = sb.toString().md5()
        val f = applicationContext.getGroupAvatarPath(name, false)
        val icon = conversationDao.getGroupIconUrl(groupId)
        if (f.exists()) {
            if (f.absolutePath != name) {
                conversationDao.updateGroupIconUrl(groupId, f.absolutePath)
            }
            return Result.SUCCESS
        }

        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(result)
        val bitmaps = mutableListOf<Bitmap>()
        try {
            getBitmaps(bitmaps, users)
            drawInternal(c, bitmaps)
        } catch (e: Exception) {
            throw LocalJobException()
        }
        result.saveGroupAvatar(applicationContext, name)
        if (icon != null && icon != f.absolutePath) {
            try {
                File(icon).delete()
            } catch (e: Exception) {
            }
        }
        conversationDao.updateGroupIconUrl(groupId, f.absolutePath)
        return Result.SUCCESS
    }

    private fun drawInternal(canvas: Canvas, bitmaps: List<Bitmap>) {
        val textSizeLarge = applicationContext.resources.getDimension(R.dimen.group_avatar_text_size)
        val textSizeMedium = applicationContext.resources.getDimension(R.dimen.group_avatar_text_medium)
        val textSizeSmall = applicationContext.resources.getDimension(R.dimen.group_avatar_text_small)
        val textOffset = applicationContext.dip(5f).toFloat()
        val dividerOffset = applicationContext.dip(.5f).toFloat()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        val c0 = Color.parseColor("#33FFFFFF")  // 20%
        val c1 = Color.parseColor("#E6FFFFFF")  // 90%
        val verticalDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, 0f, size / 2f, c0, c1, Shader.TileMode.MIRROR)
        }
        val horizontalDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, size / 2f, 0f, c0, c1, Shader.TileMode.MIRROR)
        }
        val halfHorizontalDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(size / 2f, 0f, size.toFloat(), 0f, c1, c0, Shader.TileMode.CLAMP)
        }
        val verticalDividerRectF = RectF(size / 2f - dividerOffset, 0f, size / 2f + dividerOffset, size.toFloat())
        val horizontalDividerRectF = RectF(0f, size / 2f - dividerOffset, size.toFloat(), size / 2f + dividerOffset)
        val halfHorizontalDividerRectF = RectF(size / 2f, size / 2f - dividerOffset, size.toFloat(),
            size / 2f + dividerOffset)
        val rectF = RectF()

        when (bitmaps.size) {
            1 -> {
                val b = bitmaps[0]
                val m = Matrix()
                val src = RectF(0f, 0f, b.width.toFloat(), b.height.toFloat())
                val dst = RectF(0f, 0f, size.toFloat(), size.toFloat())
                setPaintAndRect(b, m, src, dst, paint, rectF)
                canvas.drawArc(rectF, 0f, 360f, true, paint)

                if (texts[0] != null) {
                    textPaint.textSize = textSizeLarge
                    val t = texts[0].toString()
                    val bounds = Rect()
                    textPaint.getTextBounds(t, 0, t.length, bounds)
                    val x = (size - bounds.width()) / 2f
                    val y = size / 2f + bounds.height() / 2f
                    canvas.drawText(t, x, y, textPaint)
                }
            }
            2 -> {
                for (i in 0 until 2) {
                    val b = bitmaps[i]
                    if (texts[i] != null) {
                        val offset = b.width * .2f
                        val src = Rect(offset.toInt(), offset.toInt(), b.width - offset.toInt(),
                            b.height - offset.toInt())
                        val dst = if (i == 0) {
                            RectF(0f, 0f, size / 2f, size.toFloat())
                        } else {
                            RectF(size / 2f, 0f, size.toFloat(), size.toFloat())
                        }
                        canvas.drawBitmap(b, src, dst, null)
                    } else {
                        val m = Matrix()
                        val offset = size / 4f
                        val src = when (i) {
                            0 -> RectF(offset, 0f, b.width.toFloat() + offset, b.height.toFloat())
                            else -> RectF(-offset, 0f, b.width.toFloat() - offset, b.height.toFloat())
                        }
                        val dst = when (i) {
                            0 -> RectF(0f, 0f, size.toFloat(), size.toFloat())
                            else -> RectF(0f, 0f, size.toFloat(), size.toFloat())
                        }

                        setPaintAndRect(b, m, src, dst, paint, rectF)

                        val angle = if (i == 0) -180f else 180f
                        canvas.drawArc(rectF, -90f, angle, true, paint)
                    }

                    if (texts[i] != null) {
                        textPaint.textSize = textSizeMedium
                        val t = texts[i].toString()
                        val bounds = Rect()
                        textPaint.getTextBounds(t, 0, t.length, bounds)
                        val x = if (i == 0) {
                            (size / 2f - bounds.width()) / 2
                        } else {
                            size / 2f + (size / 4f - bounds.width() / 2)
                        }
                        val y = size / 2f + bounds.height() / 2f
                        canvas.drawText(t, x, y, textPaint)
                    }

                    canvas.drawRect(verticalDividerRectF, verticalDividerPaint)
                }
            }
            3 -> {
                for (i in 0 until 3) {
                    val b = bitmaps[i]
                    if (i == 0) {
                        if (texts[i] != null) {
                            val offset = b.width * .2f
                            val src = Rect(offset.toInt(), offset.toInt(), b.width - offset.toInt(),
                                b.height - offset.toInt())
                            val dst = RectF(0f, 0f, size / 2f, size.toFloat())
                            canvas.drawBitmap(b, src, dst, null)
                        } else {
                            val m = Matrix()
                            val offset = size / 4f
                            val src = RectF(offset, 0f, b.width.toFloat() + offset, b.height.toFloat())
                            val dst = RectF(0f, 0f, size.toFloat(), size.toFloat())
                            setPaintAndRect(b, m, src, dst, paint, rectF)
                            canvas.drawArc(rectF, -90f, -180f, true, paint)
                        }
                    } else {
                        val offset = if (texts[i] != null) {
                            b.width * .2f
                        } else {
                            b.width * .1f
                        }
                        val src = Rect(offset.toInt(), offset.toInt(), b.width - offset.toInt(),
                            b.height - offset.toInt())
                        val dst = when (i) {
                            1 -> {
                                RectF(size / 2f, 0f, size.toFloat(), size / 2f)
                            }
                            else -> {
                                RectF(size / 2f, size / 2f, size.toFloat(), size.toFloat())
                            }
                        }
                        canvas.drawBitmap(b, src, dst, null)
                    }

                    if (texts[i] != null) {
                        textPaint.textSize = if (i == 0) {
                            textSizeMedium
                        } else {
                            textSizeSmall
                        }
                        val t = texts[i].toString()
                        val bounds = Rect()
                        textPaint.getTextBounds(t, 0, t.length, bounds)
                        val x = if (i == 0) {
                            (size / 2f - bounds.width()) / 2
                        } else {
                            size / 2f + size / 4f - bounds.width() / 2 - textOffset
                        }
                        val y = when (i) {
                            0 -> size / 2f + bounds.height() / 2f
                            1 -> size / 4f + bounds.height() / 2f + textOffset
                            else -> size / 2f + size / 4f + bounds.height() / 2f - textOffset
                        }
                        canvas.drawText(t, x, y, textPaint)
                    }

                    canvas.drawRect(verticalDividerRectF, verticalDividerPaint)
                    canvas.drawRect(halfHorizontalDividerRectF, halfHorizontalDividerPaint)
                }
            }
            4 -> {
                for (i in 0 until 4) {
                    val item = bitmaps[i]
                    val offset = if (texts[i] != null) {
                        item.width * .2f
                    } else {
                        item.width * .1f
                    }
                    val src = Rect(offset.toInt(), offset.toInt(), item.width - offset.toInt(),
                        item.height - offset.toInt())
                    val dst = when (i) {
                        0 -> {
                            RectF(0f, 0f, size / 2f, size / 2f)
                        }
                        1 -> {
                            RectF(size / 2f, 0f, size.toFloat(), size / 2f)
                        }
                        2 -> {
                            RectF(0f, size / 2f, size / 2f, size.toFloat())
                        }
                        else -> {
                            RectF(size / 2f, size / 2f, size.toFloat(), size.toFloat())
                        }
                    }
                    canvas.drawBitmap(item, src, dst, null)

                    if (texts[i] != null) {
                        textPaint.textSize = textSizeSmall
                        val t = texts[i].toString()
                        val bounds = Rect()
                        textPaint.getTextBounds(t, 0, t.length, bounds)
                        val x = if (i % 2 == 0) {
                            size / 4f - bounds.width() / 2 + textOffset
                        } else {
                            size / 2f + size / 4f - bounds.width() / 2 - textOffset
                        }
                        val y = if (i < 2) {
                            size / 4f + bounds.width() / 2 + textOffset
                        } else {
                            size / 2f + size / 4f + bounds.height() / 2f - textOffset
                        }
                        canvas.drawText(t, x, y, textPaint)
                    }

                    canvas.drawRect(verticalDividerRectF, verticalDividerPaint)
                    canvas.drawRect(horizontalDividerRectF, horizontalDividerPaint)
                }
            }
            else -> {
            }
        }
    }

    private fun getBitmaps(bitmaps: MutableList<Bitmap>, users: MutableList<User>) {
        for (i in 0 until users.size) {
            val item = users[i].avatarUrl

            if (item.isNullOrEmpty()) {
                val user = users[i]
                texts[i] = AvatarView.checkEmoji(user.fullName)
                bitmaps.add(getBitmapByPlaceHolder(getAvatarPlaceHolderById(user.identityNumber.toLong())))
            } else {
                bitmaps.add(Glide.with(applicationContext)
                    .asBitmap()
                    .load(item)
                    .submit()
                    .get(10, TimeUnit.SECONDS))
            }
        }
    }

    private fun setPaintAndRect(b: Bitmap, m: Matrix, src: RectF, dst: RectF, paint: Paint, rectF: RectF) {
        m.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER)
        val shader = BitmapShader(b, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        shader.setLocalMatrix(m)
        paint.shader = shader
        m.mapRect(rectF, src)
    }

    private fun getAvatarPlaceHolderById(id: Long): Int {
        try {
            val num = id.rem(24) + 1
            return applicationContext.resources.getIdentifier("bg_avatar_$num",
                "drawable", applicationContext.packageName)
        } catch (e: Exception) {
        }
        return R.drawable.default_avatar
    }

    private fun getBitmapByPlaceHolder(placeHolder: Int): Bitmap {
        val d = applicationContext.resources.getDrawable(placeHolder, null)
        if (d is BitmapDrawable) {
            return d.bitmap
        }
        val b = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        d.setBounds(0, 0, c.width, c.height)
        d.draw(c)
        return b
    }
}
