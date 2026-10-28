package dev.sebastian.companion;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.View;

public final class PetView extends View {
    private final PetRenderer renderer = new PetRenderer();
    private CompanionState state = CompanionState.IDLE;
    private boolean reduced, active;
    public PetView(Context context) { super(context); setContentDescription("Sebastian character. Tap to open ChatGPT."); }
    public void setState(CompanionState next) { state = next; invalidate(); }
    public void setReducedMotion(boolean value) { reduced = value; invalidate(); }
    public void setActive(boolean value) { active = value; if(value) invalidate(); }
    @Override protected void onDraw(Canvas c) {
        renderer.draw(c, getWidth(), getHeight(), state, reduced ? 0 : SystemClock.uptimeMillis()/1000f);
        if(active && !reduced && android.animation.ValueAnimator.areAnimatorsEnabled() && isShown()) postInvalidateDelayed(33);
    }
}
