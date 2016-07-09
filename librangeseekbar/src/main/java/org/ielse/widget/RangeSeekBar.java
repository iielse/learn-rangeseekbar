package org.ielse.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import ielse.org.widget.R;

public class RangeSeekBar extends View {
    private Paint paint = new Paint();

    private int lineTop, lineBottom, lineLeft, lineRight;
    private int lineCorners;
    private int lineWidth;
    private RectF line = new RectF();

    private int colorLineSelected;
    private int colorLineEdge;

    private SeekBar leftSB = new SeekBar();
    private SeekBar rightSB = new SeekBar();
    private SeekBar currTouch;

    private OnRangeChangedListener callback;

    private int seekBarResId;
    private float offsetValue;
    private float maxValue, minValue;
    private int cellsCount = 1;
    private float cellsPercent;
    private float reserveValue;
    private int reserveCount;
    private float reservePercent;

    private class SeekBar {
        RadialGradient shadowGradient;
        Paint defaultPaint;
        int lineWidth;
        int widthSize, heightSize;
        float currPercent;
        int left, right, top, bottom;
        Bitmap bmp;

        float material = 0;
        ValueAnimator anim;
        final TypeEvaluator<Integer> te = new TypeEvaluator<Integer>() {
            @Override
            public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                int alpha = (int) (Color.alpha(startValue) + fraction * (Color.alpha(endValue) - Color.alpha(startValue)));
                int red = (int) (Color.red(startValue) + fraction * (Color.red(endValue) - Color.red(startValue)));
                int green = (int) (Color.green(startValue) + fraction * (Color.green(endValue) - Color.green(startValue)));
                int blue = (int) (Color.blue(startValue) + fraction * (Color.blue(endValue) - Color.blue(startValue)));
                return Color.argb(alpha, red, green, blue);
            }
        };

        void onSizeChanged(int centerX, int centerY, int hSize, int parentLineWidth, boolean cellsMode, int bmpResId, Context context) {
            heightSize = hSize;
            widthSize = (int) (heightSize * 0.8f);
            left = centerX - widthSize / 2;
            right = centerX + widthSize / 2;
            top = centerY - heightSize / 2;
            bottom = centerY + heightSize / 2;

            if (cellsMode) {
                lineWidth = parentLineWidth;
            } else {
                lineWidth = parentLineWidth - widthSize;
            }

            if (bmpResId > 0) {
                Bitmap original = BitmapFactory.decodeResource(context.getResources(), bmpResId);
                Matrix matrix = new Matrix();
                float scaleWidth = ((float) widthSize) / original.getWidth();
                float scaleHeight = ((float) heightSize) / original.getHeight();
                matrix.postScale(scaleWidth, scaleHeight);
                bmp = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
            } else {
                defaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                int radius = (int) (widthSize * 0.5f);
                int barShadowRadius = (int) (radius * 0.95f);
                int mShadowCenterX = widthSize / 2;
                int mShadowCenterY = heightSize / 2;
                shadowGradient = new RadialGradient(mShadowCenterX, mShadowCenterY, barShadowRadius, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
            }
        }

        boolean collide(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            int offset = (int) (lineWidth * currPercent);
            return x > left + offset && x < right + offset && y > top && y < bottom;
        }

        void slide(float percent) {
            if (percent < 0) percent = 0;
            else if (percent > 1) percent = 1;
            currPercent = percent;
        }


        void draw(Canvas canvas) {
            int offset = (int) (lineWidth * currPercent);
            canvas.save();
            canvas.translate(offset, 0);
            if (bmp != null) {
                canvas.drawBitmap(bmp, left, top, null);
            } else {
                canvas.translate(left, 0);
                drawDefault(canvas);
            }
            canvas.restore();
        }

        private void drawDefault(Canvas canvas) {
            int centerX = widthSize / 2;
            int centerY = heightSize / 2;
            int radius = (int) (widthSize * 0.5f);
            // draw shadow
            defaultPaint.setStyle(Paint.Style.FILL);
            canvas.save();
            canvas.translate(0, radius * 0.25f);
            canvas.scale(1 + (0.1f * material), 1 + (0.1f * material), centerX, centerY);
            defaultPaint.setShader(shadowGradient);
            canvas.drawCircle(centerX, centerY, radius, defaultPaint);
            defaultPaint.setShader(null);
            canvas.restore();
            // draw body
            defaultPaint.setStyle(Paint.Style.FILL);
            defaultPaint.setColor(te.evaluate(material, 0xFFFFFFFF, 0xFFE7E7E7));
            canvas.drawCircle(centerX, centerY, radius, defaultPaint);
            // draw border
            defaultPaint.setStyle(Paint.Style.STROKE);
            defaultPaint.setColor(0xFFD7D7D7);
            canvas.drawCircle(centerX, centerY, radius, defaultPaint);
        }

        private void materialRestore() {
            if (anim != null) anim.cancel();
            anim = ValueAnimator.ofFloat(material, 0);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    material = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    material = 0;
                    invalidate();
                }
            });
            anim.start();
        }
    }

    public interface OnRangeChangedListener {
        void onRangeChanged(RangeSeekBar view, float min, float max);
    }

    public RangeSeekBar(Context context) {
        this(context, null);
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar);
        seekBarResId = t.getResourceId(R.styleable.RangeSeekBar_seekBarResId, 0);
        colorLineSelected = t.getColor(R.styleable.RangeSeekBar_lineColorSelected, 0xFF4BD962);
        colorLineEdge = t.getColor(R.styleable.RangeSeekBar_lineColorEdge, 0xFFD7D7D7);
        float min = t.getFloat(R.styleable.RangeSeekBar_min, 0);
        float max = t.getFloat(R.styleable.RangeSeekBar_max, 1);
        float reserve = t.getFloat(R.styleable.RangeSeekBar_reserve, 0);
        int cells = t.getInt(R.styleable.RangeSeekBar_cells, 1);
        setRules(min, max, reserve, cells);
        t.recycle();
    }

    public void setOnRangeChangedListener(OnRangeChangedListener listener) {
        callback = listener;
    }

    public void setValue(float min, float max) {
        min = min + offsetValue;
        max = max + offsetValue;

        if (min < minValue) {
            throw new IllegalArgumentException("setValue() min < (preset min - offsetValue) . #min:" + min + " #preset min:" + minValue + " #offsetValue:" + offsetValue);
        }
        if (max > maxValue) {
            throw new IllegalArgumentException("setValue() max > (preset max - offsetValue) . #max:" + max + " #preset max:" + maxValue + " #offsetValue:" + offsetValue);
        }

        if (reserveCount > 1) {
            if ((min - minValue) % reserveCount != 0) {
                throw new IllegalArgumentException("setValue() (min - preset min) % reserveCount != 0 . #min:" + min + " #preset min:" + minValue + "#reserveCount:" + reserveCount + "#reserve:" + reserveValue);
            }
            if ((max - minValue) % reserveCount != 0) {
                throw new IllegalArgumentException("setValue() (max - preset min) % reserveCount != 0 . #max:" + max + " #preset min:" + minValue + "#reserveCount:" + reserveCount + "#reserve:" + reserveValue);
            }
            leftSB.currPercent = (min - minValue) / reserveCount * cellsPercent;
            rightSB.currPercent = (max - minValue) / reserveCount * cellsPercent;
        } else {
            leftSB.currPercent = (min - minValue) / (maxValue - minValue);
            rightSB.currPercent = (max - minValue) / (maxValue - minValue);
        }

        invalidate();
    }

    public void setRules(float min, float max) {
        setRules(min, max, reserveCount, cellsCount);
    }

    public void setRules(float min, float max, float reserve, int cells) {
        if (max <= min) {
            throw new IllegalArgumentException("setRules() max must be greater than min ! #max:" + max + " #min:" + min);
        }
        if (min < 0) {
            offsetValue = 0 - min;
            min = min + offsetValue;
            max = max + offsetValue;
        }
        minValue = min;
        maxValue = max;

        if (reserve < 0) {
            throw new IllegalArgumentException("setRules() reserve must be greater than zero ! #reserve:" + reserve);
        }
        if (reserve >= max - min) {
            throw new IllegalArgumentException("setRules() reserve must be less than (max - min) ! #reserve:" + reserve + " #max - min:" + (max - min));
        }
        if (cells < 1) {
            throw new IllegalArgumentException("setRules() cells must be greater than 1 ! #cells:" + cells);
        }
        cellsCount = cells;
        cellsPercent = 1f / cellsCount;
        reserveValue = reserve;
        reservePercent = reserve / (max - min);
        reserveCount = (int) (reservePercent / cellsPercent + (reservePercent % cellsPercent != 0 ? 1 : 0));
        if (cellsCount > 1) {
            if (leftSB.currPercent + cellsPercent * reserveCount <= 1 && leftSB.currPercent + cellsPercent * reserveCount > rightSB.currPercent) {
                rightSB.currPercent = leftSB.currPercent + cellsPercent * reserveCount;
            } else if (rightSB.currPercent - cellsPercent * reserveCount >= 0 && rightSB.currPercent - cellsPercent * reserveCount < leftSB.currPercent) {
                leftSB.currPercent = rightSB.currPercent - cellsPercent * reserveCount;
            }
        } else {
            if (leftSB.currPercent + reservePercent <= 1 && leftSB.currPercent + reservePercent > rightSB.currPercent) {
                rightSB.currPercent = leftSB.currPercent + reservePercent;
            } else if (rightSB.currPercent - reservePercent >= 0 && rightSB.currPercent - reservePercent < leftSB.currPercent) {
                leftSB.currPercent = rightSB.currPercent - reservePercent;
            }
        }
        invalidate();
    }

    public float[] getCurrentRange() {
        float range = maxValue - minValue;
        return new float[]{-offsetValue + minValue + range * leftSB.currPercent,
                -offsetValue + minValue + range * rightSB.currPercent};
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightSize * 1.8f > widthSize) {
            setMeasuredDimension(widthSize, (int) (widthSize / 1.8f));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int seekBarRadius = h / 2;

        lineLeft = seekBarRadius;
        lineRight = w - seekBarRadius;
        lineTop = seekBarRadius - seekBarRadius / 4;
        lineBottom = seekBarRadius + seekBarRadius / 4;
        lineWidth = lineRight - lineLeft;
        line.set(lineLeft, lineTop, lineRight, lineBottom);
        lineCorners = (int) ((lineBottom - lineTop) * 0.45f);

        leftSB.onSizeChanged(seekBarRadius, seekBarRadius, h, lineWidth, cellsCount > 1, seekBarResId, getContext());
        rightSB.onSizeChanged(seekBarRadius, seekBarRadius, h, lineWidth, cellsCount > 1, seekBarResId, getContext());

        if (cellsCount == 1) {
            rightSB.left += leftSB.widthSize;
            rightSB.right += leftSB.widthSize;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colorLineEdge);
        if (cellsPercent > 0) {
            paint.setStrokeWidth(lineCorners * 0.2f);
            for (int i = 1; i < cellsCount; i++) {
                canvas.drawLine(lineLeft + i * cellsPercent * lineWidth, lineTop - lineCorners,
                        lineLeft + i * cellsPercent * lineWidth, lineBottom + lineCorners, paint);
            }
        }
        canvas.drawRoundRect(line, lineCorners, lineCorners, paint);
        paint.setColor(colorLineSelected);
        canvas.drawRect(leftSB.left + leftSB.widthSize / 2 + leftSB.lineWidth * leftSB.currPercent, lineTop,
                rightSB.left + rightSB.widthSize / 2 + rightSB.lineWidth * rightSB.currPercent, lineBottom, paint);

        leftSB.draw(canvas);
        rightSB.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                boolean touchResult = false;
                if (rightSB.currPercent >= 1 && leftSB.collide(event)) {
                    currTouch = leftSB;
                    touchResult = true;
                } else if (rightSB.collide(event)) {
                    currTouch = rightSB;
                    touchResult = true;
                } else if (leftSB.collide(event)) {
                    currTouch = leftSB;
                    touchResult = true;
                }
                return touchResult;
            case MotionEvent.ACTION_MOVE:
                float percent;
                float x = event.getX();

                currTouch.material = currTouch.material >= 1 ? 1 : currTouch.material + 0.1f;

                if (currTouch == leftSB) {
                    if (cellsCount > 1) {
                        if (x < lineLeft) {
                            percent = 0;
                        } else {
                            percent = (x - lineLeft) * 1f / (lineWidth);
                        }
                        int touchLeftCellsValue = Math.round(percent / cellsPercent);
                        int currRightCellsValue = Math.round(rightSB.currPercent / cellsPercent);
                        percent = touchLeftCellsValue * cellsPercent;

                        while (touchLeftCellsValue > currRightCellsValue - reserveCount) {
                            touchLeftCellsValue--;
                            if (touchLeftCellsValue < 0) break;
                            percent = touchLeftCellsValue * cellsPercent;
                        }
                    } else {
                        if (x < lineLeft) {
                            percent = 0;
                        } else {
                            percent = (x - lineLeft) * 1f / (lineWidth - rightSB.widthSize);
                        }

                        if (percent > rightSB.currPercent - reservePercent) {
                            percent = rightSB.currPercent - reservePercent;
                        }
                    }
                    leftSB.slide(percent);
                } else if (currTouch == rightSB) {
                    if (cellsCount > 1) {
                        if (x > lineRight) {
                            percent = 1;
                        } else {
                            percent = (x - lineLeft) * 1f / (lineWidth);
                        }
                        int touchRightCellsValue = Math.round(percent / cellsPercent);
                        int currLeftCellsValue = Math.round(leftSB.currPercent / cellsPercent);
                        percent = touchRightCellsValue * cellsPercent;

                        while (touchRightCellsValue < currLeftCellsValue + reserveCount) {
                            touchRightCellsValue++;
                            if (touchRightCellsValue > maxValue - minValue) break;
                            percent = touchRightCellsValue * cellsPercent;
                        }
                    } else {
                        if (x > lineRight) {
                            percent = 1;
                        } else {
                            percent = (x - lineLeft - leftSB.widthSize) * 1f / (lineWidth - leftSB.widthSize);
                        }
                        if (percent < leftSB.currPercent + reservePercent) {
                            percent = leftSB.currPercent + reservePercent;
                        }
                    }
                    rightSB.slide(percent);
                }

                if (callback != null) {
                    float[] result = getCurrentRange();
                    callback.onRangeChanged(this, result[0], result[1]);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                currTouch.materialRestore();

                if (callback != null) {
                    float[] result = getCurrentRange();
                    callback.onRangeChanged(this, result[0], result[1]);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.minValue = minValue - offsetValue;
        ss.maxValue = maxValue - offsetValue;
        ss.reserveValue = reserveValue;
        ss.cellsCount = cellsCount;
        float[] results = getCurrentRange();
        ss.currSelectedMin = results[0];
        ss.currSelectedMax = results[1];
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        float min = ss.minValue;
        float max = ss.maxValue;
        float reserve = ss.reserveValue;
        int cells = ss.cellsCount;
        setRules(min, max, reserve, cells);
        float currSelectedMin = ss.currSelectedMin;
        float currSelectedMax = ss.currSelectedMax;
        setValue(currSelectedMin, currSelectedMax);
    }

    private class SavedState extends BaseSavedState {
        private float minValue;
        private float maxValue;
        private float reserveValue;
        private int cellsCount;
        private float currSelectedMin;
        private float currSelectedMax;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            minValue = in.readFloat();
            maxValue = in.readFloat();
            reserveValue = in.readFloat();
            cellsCount = in.readInt();
            currSelectedMin = in.readFloat();
            currSelectedMax = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(minValue);
            out.writeFloat(maxValue);
            out.writeFloat(reserveValue);
            out.writeInt(cellsCount);
            out.writeFloat(currSelectedMin);
            out.writeFloat(currSelectedMax);
        }
    }
}
