package com.example.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.R;
import com.google.android.material.button.MaterialButton;

/**
 * The opening screen.
 *
 * Shows the logo with six ingredients drifting around it. Tapping "Enter My Pantry"
 * throws the ingredients outwards and fades into the pantry list.
 *
 * The animations use ViewPropertyAnimator, which is the simplest way to animate a
 * view in Android — you describe the end state and how long it should take, and
 * Android works out every frame in between.
 */
public class SplashActivity extends AppCompatActivity {

    /** How far from the centre the ingredients sit, in dp. */
    private static final float RING_RADIUS_DP = 130f;

    /** How far they fly when the button is tapped. */
    private static final float BURST_RADIUS_DP = 700f;

    private ImageView[] ingredients;
    private View branding;
    private MaterialButton buttonEnter;

    // Stops a fast double tap starting the animation twice.
    private boolean isLeaving = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        branding = findViewById(R.id.layoutBranding);
        buttonEnter = findViewById(R.id.buttonEnter);

        ingredients = new ImageView[]{
                findViewById(R.id.foodTomato),
                findViewById(R.id.foodEgg),
                findViewById(R.id.foodCarrot),
                findViewById(R.id.foodBread),
                findViewById(R.id.foodCheese),
                findViewById(R.id.foodMilk)
        };

        playEntrance();

        buttonEnter.setOnClickListener(v -> playExit());
    }

    /**
     * The opening animation.
     *
     * Everything starts invisible and small. The logo pops in first, then each
     * ingredient flies out from the centre to its place in the ring, one shortly
     * after the other so they arrive in sequence rather than all at once.
     */
    private void playEntrance() {
        // Logo starts small and see-through, then springs to full size.
        branding.setAlpha(0f);
        branding.setScaleX(0.6f);
        branding.setScaleY(0.6f);

        branding.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                // Overshoot makes it grow slightly past full size and settle back,
                // which reads as livelier than stopping dead.
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        for (int i = 0; i < ingredients.length; i++) {
            ImageView food = ingredients[i];

            food.setAlpha(0f);
            food.setScaleX(0.3f);
            food.setScaleY(0.3f);

            // Work out where this one belongs in the ring. Six ingredients means
            // one every 60 degrees around a circle.
            double angle = Math.toRadians((360.0 / ingredients.length) * i - 90);
            float x = (float) (Math.cos(angle) * dp(RING_RADIUS_DP));
            float y = (float) (Math.sin(angle) * dp(RING_RADIUS_DP));

            food.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationX(x)
                    .translationY(y)
                    .rotation(360f)
                    .setStartDelay(300L + (i * 90L))
                    .setDuration(700)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> drift(food))
                    .start();
        }
    }

    /**
     * A slow up and down float, so the ingredients are not frozen while the user
     * decides whether to tap the button.
     *
     * It calls itself when it finishes, which keeps it looping until the screen
     * closes.
     */
    private void drift(ImageView food) {
        if (isLeaving) {
            return;
        }

        float start = food.getTranslationY();

        food.animate()
                .translationY(start - dp(8))
                .setDuration(1200)
                .withEndAction(() -> {
                    if (isLeaving) {
                        return;
                    }
                    food.animate()
                            .translationY(start)
                            .setDuration(1200)
                            .withEndAction(() -> drift(food))
                            .start();
                })
                .start();
    }

    /**
     * The exit animation: the ingredients burst outwards while the logo swells and
     * fades, then the pantry list opens.
     */
    private void playExit() {
        if (isLeaving) {
            return;
        }
        isLeaving = true;

        buttonEnter.setEnabled(false);
        buttonEnter.animate().alpha(0f).setDuration(200).start();

        for (int i = 0; i < ingredients.length; i++) {
            ImageView food = ingredients[i];

            // Same angle as before, but a much larger radius so each one flies
            // straight off its own edge of the screen.
            double angle = Math.toRadians((360.0 / ingredients.length) * i - 90);
            float x = (float) (Math.cos(angle) * dp(BURST_RADIUS_DP));
            float y = (float) (Math.sin(angle) * dp(BURST_RADIUS_DP));

            food.animate()
                    .translationX(x)
                    .translationY(y)
                    .scaleX(2.2f)
                    .scaleY(2.2f)
                    .rotation(720f)
                    .alpha(0f)
                    .setDuration(550)
                    // Accelerate makes them start slowly and speed up, which feels
                    // like being thrown rather than gliding.
                    .setInterpolator(new AccelerateInterpolator(1.4f))
                    .start();
        }

        // The logo swells and fades at the same time, so the whole screen feels
        // like it is opening up rather than the pieces leaving one by one.
        branding.animate()
                .scaleX(1.6f)
                .scaleY(1.6f)
                .alpha(0f)
                .setStartDelay(150)
                .setDuration(450)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(this::openPantry)
                .start();
    }

    /** Opens the pantry list and closes this screen. */
    private void openPantry() {
        startActivity(new Intent(this, PantryListActivity.class));

        // A cross fade rather than the default slide, so it continues the burst.
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // finish() means pressing back from the pantry exits the app instead of
        // returning to this screen, which is what a user expects from a splash.
        finish();
    }

    /** Converts dp into pixels, since animations work in pixels. */
    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
