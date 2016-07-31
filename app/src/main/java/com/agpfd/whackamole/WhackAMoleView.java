package com.agpfd.whackamole;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author <a mailto="jacobibanez@jacobibanez.com">Jacob Ibáñez Sánchez</a>
 * @since 29/07/2016
 */
public class WhackAMoleView extends SurfaceView implements SurfaceHolder.Callback {

    private Resources res;
    private Context myContext;
    private SurfaceHolder mySurfaceHolder;
    private Bitmap backgroundImg;
    private int screenW = 1;
    private int screenH = 1;
    private boolean running = false;
    private boolean onTitle = true;
    private WhackAMoleThread thread;
    private int backgroundOrigW;
    private int backgroundOrigH;
    private float scaleW;
    private float scaleH;
    private float drawScaleW;
    private float drawScaleH;
    private Bitmap mole;
    private Bitmap mask;
    private int[] moleX = new int[7];
    private int[] moleY = new int[7];

    public WhackAMoleView(Context context, AttributeSet attrs) {
        super(context, attrs);


        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        thread = new WhackAMoleThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message msg) {

            }
        });
        setFocusable(true);
    }

    public WhackAMoleThread getThread() {
        return thread;
    }

    class WhackAMoleThread extends Thread {
        public WhackAMoleThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
            mySurfaceHolder = surfaceHolder;
            myContext = context;
            res = myContext.getResources();
            backgroundImg = BitmapFactory.decodeResource(res, R.drawable.title);
            backgroundOrigW = backgroundImg.getWidth();
            backgroundOrigH = backgroundImg.getHeight();
        }

        @Override
        public void run() {
            while (running) {
                Canvas c = null;
                try {
                    c = mySurfaceHolder.lockCanvas(null);
                    synchronized (mySurfaceHolder) {
                        draw(c);
                    }
                } finally {
                    if (c != null) {
                        mySurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        private void draw(Canvas canvas) {
            try {
                canvas.drawBitmap(backgroundImg, 0, 0, null);
                if (!onTitle) {
                    for (int i = 0; i < moleX.length; i++) {
                        canvas.drawBitmap(mole, moleX[i], moleY[i], null);
                    }
                    int loop = 0;
                    for (int i = 50; i <= 650; i += 100) {
                        int factor = 450;
                        if (loop % 2 != 0) {
                            factor = 400;
                        }
                        canvas.drawBitmap(mask, i * drawScaleW, factor * drawScaleH, null);
                        loop++;
                    }
                }
            } catch (Exception e) {

            }
        }

        boolean doTouchEvent(MotionEvent event) {
            synchronized (mySurfaceHolder) {
                int eventaction = event.getAction();
                int X = (int) event.getX();
                int Y = (int) event.getY();

                switch (eventaction) {
                    case MotionEvent.ACTION_DOWN:

                        break;
                    case MotionEvent.ACTION_MOVE:

                        break;
                    case MotionEvent.ACTION_UP:
                        if (onTitle) {
                            backgroundImg = BitmapFactory.decodeResource(res, R.drawable.background);
                            backgroundImg = Bitmap.createScaledBitmap(backgroundImg, screenW,
                                    screenH, true);
                            mask = BitmapFactory.decodeResource(res, R.drawable.mask);
                            mole = BitmapFactory.decodeResource(res, R.drawable.mole);
                            scaleW = (float) screenW / (float) backgroundOrigW;
                            scaleH = (float) screenH / (float) backgroundOrigH;
                            mask = Bitmap.createScaledBitmap(mask, (int) (mask.getWidth() *
                                    scaleW), (int) (mask.getHeight() * scaleH), true);
                            mole = Bitmap.createScaledBitmap(mole, (int) (mole.getWidth() *
                                    scaleW), (int) (mole.getHeight() * scaleH), true);
                            onTitle = false;
                        }
                        break;
                }
            }
            return true;
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (mySurfaceHolder) {
                screenH = height;
                screenW = width;
                backgroundImg = Bitmap.createScaledBitmap(backgroundImg, width, height, true);
                drawScaleW = (float) screenW / 800;
                drawScaleH = (float) screenH / 600;
                int index = 0;
                for (int i = 55; i <= 655; i += 100) {
                    moleX[index] = (int) (i * drawScaleW);
                    index++;
                }
                for (int i = 0; i < moleY.length; i++) {
                    int factor = 475;
                    if (i % 2 != 0) {
                        factor = 425;
                    }
                    moleY[i] = (int) (factor * drawScaleH);
                }
            }
        }

        public void setRunning(boolean b) {
            running = b;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return thread.doTouchEvent(event);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRunning(false);
    }
}