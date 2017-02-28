package com.tarnag.hearts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by viktor on 2017. 02. 28..
 */

public class GamePanel extends SurfaceView implements Runnable{

    //VARIABLES USED FROM OUTSIDE
    //own cards
    ArrayList<Card> cards = new ArrayList<>();
    //players names
    Player[] players = new Player[4];
    int ownPosition;
    //num of cards for each player
    int[] numOfCards = new int[4];


    public static final int WIDTH = 1600;
    public static final int HEIGHT = 1200;
    public static final int CARD_WIDTH = 500;
    public static final int CARD_HEIGHT = 726;
    public static final float HALF_CARD_SCALE = 0.4f;
    public static final float OTHER_CARDS_SCALE = 0.8f;
    public int desired_card_width;
    public int desired_card_height;
    public int desired_half_card_width;
    public int screnWidth;
    public int screenHeight;
    public int cardsTop;
    public int cardsLeft;
    public int cardsRight;
    public int cardsBottom;
    private String TAG = GameActivity.class.getSimpleName();
    float initialX, initialY;
    int leftNum;
    int rightNum;
    int topNum;

    Thread thread = null;
    boolean canDraw = false;

    Bitmap background;
    Canvas canvas;
    SurfaceHolder surfaceHolder;
    Context context;

    public boolean canPress = true;


    public GamePanel(Context context) {
        super(context);
        this.context = context;
        background = BitmapFactory.decodeResource(getResources(), R.drawable.table_background);

    }

    public void draw() {
        canDraw = true;
        for (int i = 0; i < 4; i++) {
            int cur = (ownPosition + i) % 4;
            switch (cur) {
                case 1: leftNum = numOfCards[i]; break;
                case 2: topNum = numOfCards[i]; break;
                case 3: rightNum = numOfCards[i]; break;
            }
        }
        Thread thread = new Thread(this);
        thread.run();
    }

    @Override
    public void run() {
        while (canDraw) {

            //determine screen width
            screnWidth = getWidth();
            screenHeight = getHeight();
            surfaceHolder = getHolder();

            //check whether the surface is valid
            if(!surfaceHolder.getSurface().isValid()) {
                continue;
            }

            desired_card_height = (int) ((double) screenHeight / 3.8);
            desired_card_width = screnWidth / 9;
            desired_half_card_width = (int) (desired_card_width * HALF_CARD_SCALE);

            canDraw = false;
            canvas = surfaceHolder.lockCanvas();
            Bitmap scaled = Bitmap.createScaledBitmap(background, getWidth(), getHeight(), false);
            canvas.drawBitmap(scaled, 0, 0, null);

            drawCards(canvas);
            drawLeftCards(leftNum, canvas);
            drawTopCards(topNum, canvas);
            drawRightCards(rightNum, canvas);

            surfaceHolder.unlockCanvasAndPost(canvas);
            Log.d("run", "finished");
        }
    }

    protected void drawCards(Canvas canvas) {

        int size = cards.size();

        //if there are no cards
        if (size == 0) return;

        cardsTop = (int) (screenHeight - 1.2 * desired_card_height);
        cardsBottom = cardsTop + desired_card_height;
        //computing the left and the right side of cards
        int cardRowWidth = desired_card_width + (size - 1) * desired_half_card_width;
        cardsLeft = screnWidth / 2 - cardRowWidth / 2;
        cardsRight = cardsLeft + cardRowWidth;
        int x = cardsLeft;
        int y = cardsTop;
        for (int i = 0; i < size; i++) {
            //get bitmap
            Log.d("Card number " + i, cards.get(i).bmName);
            int id = getResources().getIdentifier(cards.get(i).bmName, "drawable", context.getPackageName());
            Bitmap bm = BitmapFactory.decodeResource(getResources(), id);
            Log.d("des_card_width", String.valueOf(desired_card_width));
            Log.d("des_card_height", String.valueOf(desired_card_height));
            Bitmap scaled = Bitmap.createScaledBitmap(bm, desired_card_width, desired_card_height, false);
            canvas.drawBitmap(scaled, x, y, null);
            x += desired_half_card_width;
        }
    }

    protected void drawLeftCards(int num, Canvas canvas) {
        if (num == 0) return;
        int desiredRotatedWidth = (int) (desired_card_height * OTHER_CARDS_SCALE);
        int desiredRotatedHeight = (int) (desired_card_width * OTHER_CARDS_SCALE);
        int desiredHalfRotatedHeight = (int) (desired_half_card_width * 0.45f);
        int x = (int) (- desiredRotatedWidth * 0.5f);
        int leftCentre = (int) (cardsTop * 0.5f);
        int sumHeight =  desiredRotatedHeight + (num - 1) * desiredHalfRotatedHeight;
        int y = (int) (leftCentre - sumHeight/2f);

        for (int i = 0; i < num; i++) {
            //get bitmap
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.card_back_rotated);
            Bitmap scaled = Bitmap.createScaledBitmap(bm, desiredRotatedWidth, desiredRotatedHeight, false);
            canvas.drawBitmap(scaled, x, y, null);
            y += desiredHalfRotatedHeight;
        }
    }

    protected void drawTopCards(int num, Canvas canvas) {
        if (num == 0) return;
        int desiredSmallWidth = (int) (desired_card_width * OTHER_CARDS_SCALE);
        int desiredSmalldHeight = (int) (desired_card_height * OTHER_CARDS_SCALE);
        int desiredHalfSmallWidth = (int) (desired_half_card_width * 0.45f);
        int y = (int) (- desiredSmalldHeight * 0.5f);
        int topCentre = (int) (screnWidth * 0.5f);
        int sumWidth =  desiredSmallWidth + (num - 1) * desiredHalfSmallWidth;
        int x = (int) (topCentre - sumWidth/2f);

        for (int i = 0; i < num; i++) {
            //get bitmap
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.card_back_rotated);
            Bitmap scaled = Bitmap.createScaledBitmap(bm, desiredSmallWidth, desiredSmalldHeight, false);
            canvas.drawBitmap(scaled, x, y, null);
            x += desiredHalfSmallWidth;
        }
    }

    protected void drawRightCards(int num, Canvas canvas) {
        if (num == 0) return;
        int desiredRotatedWidth = (int) (desired_card_height * OTHER_CARDS_SCALE);
        int desiredRotatedHeight = (int) (desired_card_width * OTHER_CARDS_SCALE);
        int desiredHalfRotatedHeight = (int) (desired_half_card_width * 0.45f);
        int x = (int) (screnWidth - desiredRotatedWidth * 0.5f);
        int rightCentre = (int) (cardsTop * 0.5f);
        int sumHeight =  desiredRotatedHeight + (num - 1) * desiredHalfRotatedHeight;
        int y = (int) (rightCentre - sumHeight/2f);

        for (int i = 0; i < num; i++) {
            //get bitmap
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.card_back_rotated);
            Bitmap scaled = Bitmap.createScaledBitmap(bm, desiredRotatedWidth, desiredRotatedHeight, false);
            canvas.drawBitmap(scaled, x, y, null);
            y += desiredHalfRotatedHeight;
        }
    }

    //handles user input
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialY = event.getY();

                Log.d(TAG, "Action was DOWN");
                Log.d("X coordinate", String.valueOf(initialX));
                Log.d("Y coordinate", String.valueOf(initialY));

                screenPressed(initialX, initialY);

                break;

            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "Action was MOVE");
                break;

            case MotionEvent.ACTION_UP:
                float finalX = event.getX();
                float finalY = event.getY();

                Log.d(TAG, "Action was UP");

                if (initialX < finalX) {
                    Log.d(TAG, "Left to Right swipe performed");
                }

                if (initialX > finalX) {
                    Log.d(TAG, "Right to Left swipe performed");
                }

                if (initialY < finalY) {
                    Log.d(TAG, "Up to Down swipe performed");
                }

                if (initialY > finalY) {
                    Log.d(TAG, "Down to Up swipe performed");
                }

                break;

            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG,"Action was CANCEL");
                break;

            case MotionEvent.ACTION_OUTSIDE:
                Log.d(TAG, "Movement occurred outside bounds of current screen element");
                break;
        }

        return super.onTouchEvent(event);

    }

    protected void selectedCard(int index) {
        Log.d("Card was selected", cards.get(index).name);
        cards.get(index).selected = !(cards.get(index).selected);
        draw();
    }

    private void screenPressed(float x, float y) {
        if (canPress) {
            if ((cardsTop < y) && (y < cardsBottom)) {
                if ((cardsLeft < x) && (x < cardsRight)) {
                    int index = -1;
                    int size = cards.size();
                    int curLeft = cardsLeft;
                    int curRight = curLeft + desired_half_card_width;
                    for (int i = 0; i < size - 1; i++) {
                        if ((curLeft < x) && (x < curRight)) {
                            index = i;
                            break;
                        }
                        curLeft = curRight;
                        curRight += desired_half_card_width;
                    }
                    if (index == -1) {
                        index = size - 1;
                    }
                    selectedCard(index);
                }
            }
        }
    }

    public void pause() {
        canDraw = false;
        while(true) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        thread = null;
    }

    public void resume() {
        canDraw = true;
        thread = new Thread(this);
        thread.start();
    }

}
