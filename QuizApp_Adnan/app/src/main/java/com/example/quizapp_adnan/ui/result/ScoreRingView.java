package com.example.quizapp_adnan.ui.result;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom View that draws a full circular progress ring with a blue→purple gradient.
 * The unfilled track is drawn in light grey. Inside the ring, the score percentage
 * and the label "Score" are rendered in bold text.
 *
 * Animate with {@link #setProgress(float)} from a ValueAnimator.
 */
public class ScoreRingView extends View {

    // Ring geometry
    private static final float STROKE_DP   = 20f;
    private static final float START_ANGLE = -90f;   // start at 12 o'clock

    // Colors
    private static final int COLOR_TRACK   = 0xFFE8E8F0;
    private static final int COLOR_GRAD_START = 0xFF7B8FFF;  // blue
    private static final int COLOR_GRAD_END   = 0xFFB57BFF;  // purple
    private static final int COLOR_SCORE_TEXT = 0xFF1A1D3A;
    private static final int COLOR_LABEL_TEXT = 0xFF9EA3C0;

    private final Paint trackPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scorePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF oval = new RectF();

    /** 0–100 float, driven by ValueAnimator */
    private float progress = 0f;

    // ────────────────────────────────────────────────────────────
    //  Constructors
    // ────────────────────────────────────────────────────────────

    public ScoreRingView(Context context) {
        super(context);
        init();
    }

    public ScoreRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScoreRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // ────────────────────────────────────────────────────────────
    //  Init
    // ────────────────────────────────────────────────────────────

    private void init() {
        float strokePx = dpToPx(STROKE_DP);

        // Grey track (full 360°)
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokePx);
        trackPaint.setColor(COLOR_TRACK);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        // Gradient arc — shader assigned in onSizeChanged
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(strokePx);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Score text (large, bold)
        scorePaint.setTextAlign(Paint.Align.CENTER);
        scorePaint.setColor(COLOR_SCORE_TEXT);
        scorePaint.setTextSize(spToPx(36f));
        scorePaint.setFakeBoldText(true);

        // "Score" label (small, grey)
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(COLOR_LABEL_TEXT);
        labelPaint.setTextSize(spToPx(13f));
    }

    // ────────────────────────────────────────────────────────────
    //  Size change — build gradient shader
    // ────────────────────────────────────────────────────────────

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        float strokePx = dpToPx(STROKE_DP);
        float inset = strokePx / 2f;

        oval.set(inset, inset, w - inset, h - inset);

        // Horizontal linear gradient across the full view width
        arcPaint.setShader(new LinearGradient(
                0, 0, w, 0,
                COLOR_GRAD_START, COLOR_GRAD_END,
                Shader.TileMode.CLAMP
        ));
    }

    // ────────────────────────────────────────────────────────────
    //  Draw
    // ────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. Grey full-circle track
        canvas.drawArc(oval, 0f, 360f, false, trackPaint);

        // 2. Gradient arc (progress 0–360°)
        float sweep = 360f * (progress / 100f);
        if (sweep > 0f) {
            canvas.drawArc(oval, START_ANGLE, sweep, false, arcPaint);
        }

        // 3. Percentage text
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        String scoreStr = Math.round(progress) + "%";
        // Center vertically: offset by half the text height
        float textOffset = (scorePaint.descent() + scorePaint.ascent()) / 2f;
        canvas.drawText(scoreStr, cx, cy - textOffset - spToPx(8f), scorePaint);

        // 4. "Score" label below percentage
        canvas.drawText("Score", cx, cy - textOffset + spToPx(16f), labelPaint);
    }

    // ────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────

    /**
     * Set the current progress (0–100). Call from a ValueAnimator on each update.
     */
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(100f, progress));
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    // ────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
