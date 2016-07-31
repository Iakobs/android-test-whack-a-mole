package com.agpfd.whackamole;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

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
    private int activeMole = 0;
    private boolean moleRising = true;
    private boolean moleSinking = false;
    private int moleRate = 5;
    private boolean moleJustHit = false;
    private Bitmap whack;
    private boolean whacking = false;
    private int molesWhacked;
    private int molesMissed;
    private int fingerX, fingerY;
    private Paint blackPaint;
    private static SoundPool sounds;
    private static int whackSound;
    private static int missSound;
    public boolean soundOn = true;

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
            sounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            whackSound = sounds.load(myContext, R.raw.whack, 1);
            missSound = sounds.load(myContext, R.raw.miss, 1);
        }

        @Override
        public void run() {
            while (running) {
                Canvas c = null;
                try {
                    c = mySurfaceHolder.lockCanvas(null);
                    synchronized (mySurfaceHolder) {
                        animateMoles();
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
                    canvas.drawText(res.getString(R.string.whacked, molesWhacked), 10,
                            blackPaint.getTextSize() + 10, blackPaint);
                    canvas.drawText(res.getString(R.string.missed, molesMissed),
                            screenW - (int) (200 * drawScaleW),
                            blackPaint.getTextSize() + 10, blackPaint);
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
                    if (whacking) {
                        canvas.drawBitmap(whack, fingerX - (whack.getWidth() / 2),
                                fingerY - (whack.getHeight() / 2), null);
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
                        fingerX = X;
                        fingerY = Y;
                        if (!onTitle && detectMoleContact()) {
                            whacking = true;
                            if (soundOn) {
                                AudioManager audioManager = (AudioManager)
                                        myContext.getSystemService(Context.AUDIO_SERVICE);
                                float volume = (float)
                                        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                sounds.play(whackSound, volume, volume, 1, 0, 1);
                            }
                            molesWhacked++;
                        }
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
                            whack = BitmapFactory.decodeResource(res, R.drawable.whack);
                            scaleW = (float) screenW / (float) backgroundOrigW;
                            scaleH = (float) screenH / (float) backgroundOrigH;
                            mask = Bitmap.createScaledBitmap(mask, (int) (mask.getWidth() *
                                    scaleW), (int) (mask.getHeight() * scaleH), true);
                            mole = Bitmap.createScaledBitmap(mole, (int) (mole.getWidth() *
                                    scaleW), (int) (mole.getHeight() * scaleH), true);
                            whack = Bitmap.createScaledBitmap(whack, (int) (whack.getWidth() *
                                    scaleW), (int) (whack.getHeight() * scaleH), true);
                            onTitle = false;
                            pickActiveMole();
                        }
                        whacking = false;
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
                blackPaint = new Paint();
                blackPaint.setAntiAlias(true);
                blackPaint.setColor(Color.BLACK);
                blackPaint.setStyle(Paint.Style.STROKE);
                blackPaint.setTextAlign(Paint.Align.LEFT);
                blackPaint.setTextSize(drawScaleW * 30);
            }
        }

        public void setRunning(boolean b) {
            running = b;
        }

        private void animateMoles() {
            for (int i = 0; i < moleY.length; i++) {
                if (activeMole == i + 1) {
                    if (moleRising) {
                        moleY[i] -= moleRate;
                    } else if (moleSinking) {
                        moleY[i] += moleRate;
                    }
                    int factorX = 475;
                    int factorY = 300;
                    if (i % 2 != 0) {
                        factorX = 425;
                        factorY = 250;
                    }
                    if (moleY[i] >= (int) (factorX * drawScaleH) || moleJustHit) {
                        moleY[i] = (int) (factorX * drawScaleH);
                        pickActiveMole();
                    }
                    if (moleY[i] <= (int) (factorY * drawScaleH)) {
                        moleY[i] = (int) (factorY * drawScaleH);
                        moleRising = false;
                        moleSinking = true;
                    }
                }
            }
        }

        private void pickActiveMole() {
            if (!moleJustHit && activeMole > 0) {
                if (soundOn) {
                    AudioManager audioManager = (AudioManager)
                            myContext.getSystemService(Context.AUDIO_SERVICE);
                    float volume = (float)
                            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    sounds.play(missSound, volume, volume, 1, 0, 1);
                }
                molesMissed++;
            }
            activeMole = new Random().nextInt(7) + 1;
            moleRising = true;
            moleSinking = false;
            moleJustHit = false;
            moleRate = 5 + (molesWhacked / 10);
        }

        private boolean detectMoleContact() {
            boolean contact = false;
            for (int i = 0; i < moleX.length; i++) {
                int factor = 450;
                if (i % 2 != 0) {
                    factor = 400;
                }
                if (activeMole == i + 1 &&
                        fingerX >= moleX[i] &&
                        fingerX < moleX[i] + (int) (88 * drawScaleW) &&
                        fingerY > moleY[i] &&
                        fingerY < factor * drawScaleH) {
                    contact = true;
                    moleJustHit = true;
                }
            }
            return contact;
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