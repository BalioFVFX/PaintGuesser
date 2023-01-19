package com.paintguesser.view.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.paintguesser.network.socket.DrawData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

public class DrawingView extends View {

    public interface DrawingListener {
        void onDraw(DrawData drawData);
    }

    private static final String TAG = "DrawingView";
    private static final float PAINT_BREAK = Float.MIN_VALUE;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private DrawData drawData;
    private boolean playable = false;
    private DrawingListener drawingListener;
    private float width;
    private float height;
    private float strokeWidth = 15f;
    private int paintColor = Color.RED;

    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Point point : drawData.points) {
            if (point.x != PAINT_BREAK) {
                float drawX = point.x;
                float drawY = point.y;

                if (width != drawData.screenWidth) {
                    drawX = drawX / drawData.screenWidth;
                    drawX = drawX * width;
                }
                if (height != drawData.screenHeight) {
                    drawY = drawY / drawData.screenHeight;
                    drawY = drawY * height;
                }

                paint.setColor(point.color);
                canvas.drawPoint(drawX, drawY, paint);
            }
        }

        if (drawingListener != null) {
            drawingListener.onDraw(new DrawData(drawData));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!playable) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //Log.d(TAG, "Moving");
            drawData.points.add(new Point(event.getX(), event.getY(), paintColor));
            invalidate();
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d(TAG, "UP");
            drawData.points.add(new Point(PAINT_BREAK, PAINT_BREAK, paintColor));
        }

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawData = new DrawData(drawData.points, w, h);
        width = w;
        height = h;
    }

    public void undo() {
        if (!playable) {
            return;
        }

        ArrayDeque<Point> points = new ArrayDeque<>(drawData.points);

        Iterator<Point> pointIterator = points.descendingIterator();

        if (!pointIterator.hasNext()) {
            return;
        }

        pointIterator.next();

        pointIterator.remove();

        if (!pointIterator.hasNext()) {
            return;
        }

        while (pointIterator.hasNext()) {
            Point point = pointIterator.next();

            if (point.x == PAINT_BREAK) {
                break;
            }

            pointIterator.remove();
        }

        drawData = new DrawData(new ArrayList<>(points), drawData.screenWidth, drawData.screenHeight);
        invalidate();
    }

    public void setDrawingUpdate(DrawingListener listener) {
        this.drawingListener = listener;
    }

    public void update(DrawData drawData) {
        this.drawData = drawData;
        invalidate();
    }

    public void setPaintColor(int color) {
        this.paintColor = color;
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
    }

    public DrawData getDrawData() {
        return drawData;
    }

    private void init() {
        drawData = new DrawData(new ArrayList<>(), 0, 0);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(strokeWidth);
    }
}
