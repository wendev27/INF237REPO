package com.tutorial.androidgametutorial.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.entities.Character;
import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.gamestates.Playing;
import com.tutorial.androidgametutorial.main.Game;

public class PlayingUI {

    //For UI
    private final PointF joystickCenterPos = new PointF(250, 800);
    private final PointF attackBtnCenterPos = new PointF(1700, 800);
    private final float radius = 150;
    private final Paint circlePaint;

    //For Multitouch
    private int joystickPointerId = -1;
    private int attackBtnPointerId = -1;
    private boolean touchDown;

    private CustomButton btnMenu;

    private final Playing playing;

    // Health
    private final int healthIconX = 150, healthIconY = 25;
//    private int maxPlayerHealth = 600;
//    private int currentPlayerHealth = 275;

    public PlayingUI(Playing playing) {
        this.playing = playing;

        circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setStrokeWidth(5);
        circlePaint.setStyle(Paint.Style.STROKE);

        btnMenu = new CustomButton(5, 5, ButtonImages.PLAYING_MENU.getWidth(), ButtonImages.PLAYING_MENU.getHeight());

    }

    public void draw(Canvas c) {
        drawUI(c);
    }

    private void drawUI(Canvas c) {
        c.drawCircle(joystickCenterPos.x, joystickCenterPos.y, radius, circlePaint);
        c.drawCircle(attackBtnCenterPos.x, attackBtnCenterPos.y, radius, circlePaint);

        c.drawBitmap(
                ButtonImages.PLAYING_MENU.getBtnImg(btnMenu.isPushed(btnMenu.getPointerId())),
                btnMenu.getHitbox().left,
                btnMenu.getHitbox().top,
                null);

        drawHealth(c);

    }

    private void drawHealth(Canvas c) {
        Player player = playing.getPlayer();
        for (int i = 0; i < player.getMaxHealth() / 100; i++) {
            int x = healthIconX + 100 * i;
            int heartValue = player.getCurrentHealth() - 100 * i;

            if (heartValue < 100) {
                if (heartValue <= 0)
                    c.drawBitmap(HealthIcons.HEART_EMPTY.getIcon(), x, healthIconY, null);
                else if (heartValue == 25)
                    c.drawBitmap(HealthIcons.HEART_1Q.getIcon(), x, healthIconY, null);
                else if (heartValue == 50)
                    c.drawBitmap(HealthIcons.HEART_HALF.getIcon(), x, healthIconY, null);
                else
                    c.drawBitmap(HealthIcons.HEART_3Q.getIcon(), x, healthIconY, null);
            } else
                c.drawBitmap(HealthIcons.HEART_FULL.getIcon(), x, healthIconY, null);
        }


    }



    private boolean isInsideRadius(PointF eventPos, PointF circle) {
        float a = Math.abs(eventPos.x - circle.x);
        float b = Math.abs(eventPos.y - circle.y);
        float c = (float) Math.hypot(a, b);

        return c <= radius;
    }

    private boolean checkInsideAttackBtn(PointF eventPos) {
        return isInsideRadius(eventPos, attackBtnCenterPos);
    }

    private boolean checkInsideJoyStick(PointF eventPos, int pointerId) {
        if (isInsideRadius(eventPos, joystickCenterPos)) {
            touchDown = true;
            joystickPointerId = pointerId;
            return true;
        }
        return false;
    }


    public void touchEvents(MotionEvent event) {
        final int action = event.getActionMasked();
        final int actionIndex = event.getActionIndex();
        final int pointerId = event.getPointerId(actionIndex);

        final PointF eventPos = new PointF(event.getX(actionIndex), event.getY(actionIndex));

        switch (action) {
            case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (checkInsideJoyStick(eventPos, pointerId))
                    touchDown = true;
                else if (checkInsideAttackBtn(eventPos)) {
                    if (attackBtnPointerId < 0) {
                        playing.getPlayer().setAttacking(true);
                        attackBtnPointerId = pointerId;
                    }
                } else {
                    if (isIn(eventPos, btnMenu))
                        btnMenu.setPushed(true, pointerId);
                }
            }

            case MotionEvent.ACTION_MOVE -> {
                if (touchDown)
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        if (event.getPointerId(i) == joystickPointerId) {
                            float xDiff = event.getX(i) - joystickCenterPos.x;
                            float yDiff = event.getY(i) - joystickCenterPos.y;
                            playing.setPlayerMoveTrue(new PointF(xDiff, yDiff));
                        }
                    }
            }
            case MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (pointerId == joystickPointerId) {
                    resetJoystickButton();
                } else {
                    if (isIn(eventPos, btnMenu))
                        if (btnMenu.isPushed(pointerId)) {
                            resetJoystickButton();
                            playing.setGameStateToMenu();
                        }
                    btnMenu.unPush(pointerId);
                    if (pointerId == attackBtnPointerId) {
                        playing.getPlayer().setAttacking(false);
                        attackBtnPointerId = -1;
                    }
                }
            }
        }
    }

    private void resetJoystickButton() {
        touchDown = false;
        playing.setPlayerMoveFalse();
        joystickPointerId = -1;
    }

    private boolean isIn(PointF eventPos, CustomButton b) {
        return b.getHitbox().contains(eventPos.x, eventPos.y);
    }

//    public void resetPlayerHealth() {
//        this.currentPlayerHealth = maxPlayerHealth;
//    }
}
