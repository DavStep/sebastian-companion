package dev.sebastian.companion;

import android.graphics.*;

/** Original procedural artwork; no bundled product assets or copied sprite frames. */
public final class PetRenderer {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path body = new Path();
    private final Path eye = new Path();
    public void draw(Canvas c, float width, float height, CompanionState state, float time) {
        c.save();
        float scale = Math.min(width / 240f, height / 240f);
        c.translate((width - 240 * scale) / 2f, (height - 240 * scale) / 2f);
        c.scale(scale, scale);
        float bob = (float)Math.sin(time * 1.8) * 3;
        p.setShader(null); p.setColor(0x40100A18);
        c.drawOval(54, 203, 190, 220, p);
        c.translate(0, bob);
        c.save();
        c.scale(1f + (float)Math.sin(time * 1.8) * .012f, 1, 120, 150);
        body.reset();
        body.moveTo(65, 67); body.cubicTo(64, 40, 88, 34, 106, 46);
        body.cubicTo(123, 37, 147, 45, 157, 57);
        body.cubicTo(182, 49, 198, 71, 187, 91);
        body.cubicTo(208, 111, 204, 129, 190, 138);
        body.cubicTo(206, 158, 191, 184, 171, 182);
        body.cubicTo(162, 182, 169, 202, 157, 204);
        body.lineTo(137, 193); body.cubicTo(122, 204, 107, 195, 94, 194);
        body.cubicTo(87, 219, 70, 208, 71, 193);
        body.cubicTo(45, 201, 44, 176, 49, 160);
        body.cubicTo(34, 144, 40, 128, 46, 119);
        body.cubicTo(18, 95, 41, 75, 65, 67); body.close();
        p.setShader(new LinearGradient(67, 52, 166, 208, new int[]{0xFFC05BFF,0xFFA72CF1,0xFF7921CC}, null, Shader.TileMode.CLAMP));
        c.drawPath(body, p); p.setShader(null);
        p.setColor(0x23EDD9FF); c.drawOval(72, 53, 113, 67, p);
        float blink = state == CompanionState.RESTING || (time % 5.8f > 5.62f) ? .12f : 1f;
        float glance = state == CompanionState.THINKING ? (float)Math.sin(time*.8f)*2 : 0;
        p.setColor(0xFF2C123B);
        for(int i=0;i<2;i++) {
            float x=81+i*56+glance;
            if(state == CompanionState.HAPPY) {
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(6); p.setStrokeCap(Paint.Cap.ROUND);
                c.drawArc(x-8, 101, x+12, 123, 200, 140, false, p); p.setStyle(Paint.Style.FILL);
            } else {
                c.drawRoundRect(x-11, 99, x+18, 107, 4, 4, p);
                c.drawOval(x-3, 103, x+11, 103+21*blink, p);
                if(blink>.5f){ p.setColor(0xFFD993FF); c.drawCircle(x+6, 112, 2.3f, p); p.setColor(0xFF2C123B); }
            }
        }
        if(state==CompanionState.SPEAKING){
            float opening = 5 + (float)(Math.sin(time * 13)+1)*4;
            c.drawOval(111, 140-opening/2, 130, 140+opening, p);
        } else if(state==CompanionState.HAPPY) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(4); c.drawArc(107, 127, 136, 149, 0, 180, false, p); p.setStyle(Paint.Style.FILL);
        }
        c.restore();
        if(state==CompanionState.THINKING) {
            p.setShader(new RadialGradient(198, 42, 25, new int[]{0xAAFFD980,0x00FFD980}, null, Shader.TileMode.CLAMP));
            c.drawCircle(198, 42, 25, p); p.setShader(null);
            p.setColor(0xFFFFD27B); c.drawCircle(198, 39, 10, p);
            p.setColor(0xFFF8E6BC); c.drawRoundRect(193, 46, 203, 55, 2, 2, p);
            p.setColor(0xFF927558); c.drawRect(193, 54, 203, 58, p);
        }
        if(state==CompanionState.RESTING) {
            p.setColor(0xFFC8ACEA); p.setTextSize(22); p.setTypeface(Typeface.DEFAULT_BOLD); c.drawText("z", 184, 63, p);
        }
        c.restore();
    }
}
