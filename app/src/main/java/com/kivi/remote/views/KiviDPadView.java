package com.kivi.remote.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.kivi.remote.App;
import com.kivi.remote.R;

import java.util.HashSet;
import java.util.Set;

import static com.kivi.remote.common.Utils.dpToPx;
import static com.kivi.remote.common.Utils.getColorCompat;

/**
 * Created by andre on 30.05.2017.
 */

public class KiviDPadView extends AppCompatImageView {

    private Path arrowsPath;
    private int ownPadding;

    private RectF mainRect = new RectF();

    private Paint paintMain;
    private Paint paintShadow;
    private Paint paintArrows;
    private Paint paintArrowClicked;

    private SelectableArc bottomSelector;
    private SelectableArc topSelector;
    private SelectableArc rightSelector;
    private SelectableArc leftSelector;

    private float arrowSize = 0f;

    private float arrowWidth = 0f;

    private float arrowShift = 0f;

    private float circleShift = 0f;

    Context context;

    private Set<SectorLocation> pressedArrowsList = new HashSet<>(SectorLocation.values().length);

    public KiviDPadView(Context context) {
        super(context);
        init(context);
    }

    public KiviDPadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initAttrs(attrs);
    }

    public KiviDPadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initAttrs(attrs);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.KiviDPadView,
                0, 0
        );

        try {
            arrowSize = a.getDimensionPixelSize(R.styleable.KiviDPadView_arrowSize, 0);
        } finally {
            a.recycle();
        }
    }

    private void init(Context context) {
        this.context = context;

        arrowsPath = new Path();
        ownPadding = dpToPx(context, 8);

        int grayLight = getColorCompat(context, R.color.colorSecondaryText);
        int shadowColor = getColorCompat(context, App.isDarkMode()? R.color.kiviDark : R.color.shadow_outline);
        int mainPaintColor = getColorCompat(context, App.isDarkMode()? R.color.btnDark : R.color.colorWhite);

        arrowWidth = dpToPx(context, 2);
        circleShift = dpToPx(getContext(), 4);
        arrowShift = dpToPx(getContext(), 4);

        paintMain = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMain.setStyle(Paint.Style.FILL);
        paintMain.setColor(mainPaintColor);
        paintMain.setShader(null);

        paintArrows = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintArrows.setStrokeWidth(arrowWidth);
        paintArrows.setStyle(Paint.Style.STROKE);
        paintArrows.setColor(grayLight);

        paintArrowClicked = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintArrowClicked.setStrokeWidth(arrowWidth);
        paintArrowClicked.setStyle(Paint.Style.STROKE);
        paintArrowClicked.setColor(getColorCompat(context, R.color.colorAccent));


        paintShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintShadow.setStrokeWidth(arrowWidth);
        paintShadow.setStyle(Paint.Style.STROKE);
        paintShadow.setColor(shadowColor);

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int finalSize = widthSize > heightSize ? heightSize : widthSize;

        createFiguresObjects();
        setMeasuredDimension(finalSize, finalSize);
    }

    private void createFiguresObjects() {
        topSelector = new SelectableArc(mainRect, SectorLocation.TOP);
        bottomSelector = new SelectableArc(mainRect, SectorLocation.BOTTOM);
        rightSelector = new SelectableArc(mainRect, SectorLocation.RIGHT);
        leftSelector = new SelectableArc(mainRect, SectorLocation.LEFT);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = canvas.getWidth() > canvas.getHeight() ? canvas.getHeight() : canvas.getWidth();
        mainRect.set(0F, 0F, size, size);

        topSelector.draw(canvas);
        bottomSelector.draw(canvas);
        rightSelector.draw(canvas);
        leftSelector.draw(canvas);

        drawMainCircle(canvas);
        drawShadow(canvas);
        drawImages(canvas);
    }

    public float getRadius() {
        float size = mainRect.width() > mainRect.height() ? mainRect.height() : mainRect.width();
        return (size) / 2 - ownPadding + arrowWidth;
    }

    private void drawMainCircle(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int size = width > height ? height : width;

        float radius = size / 2 - ownPadding + circleShift;

        canvas.drawCircle(width / 2, height / 2, radius, paintMain);
    }

    private void drawShadow(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int size = width > height ? height : width;

        float radius = size / 2 - ownPadding + circleShift;

        canvas.drawCircle(width / 2, height / 2, radius, paintShadow);
    }



    public void onSectorSelected(SectorLocation sector, boolean show) {
        selectorPressedEvent(sector, show);
        invalidate();
    }

    private void drawImages(Canvas canvas) {
        arrowsPath.reset();

        float width = canvas.getWidth();
        float height = canvas.getHeight();

        float heightSixth = height / 6F;
        float widthSixth = width / 6F;

        Pair<Float, Float> topCenter = new Pair<>(width / 2, heightSixth - arrowSize - arrowShift);
        Pair<Float, Float> rightCenter = new Pair<>(width - widthSixth + arrowSize + arrowShift, height / 2);
        Pair<Float, Float> bottomCenter = new Pair<>(width / 2, height - heightSixth + arrowSize + arrowShift);
        Pair<Float, Float> leftCenter = new Pair<>(widthSixth - arrowSize - arrowShift, height / 2);

        arrowsPath.moveTo(topCenter.first, topCenter.second);
        arrowsPath.lineTo(topCenter.first - arrowSize, topCenter.second + arrowSize);
        arrowsPath.lineTo(topCenter.first, topCenter.second);
        arrowsPath.lineTo(topCenter.first + arrowSize, topCenter.second + arrowSize);
        canvas.drawPath(arrowsPath, getSelectorPaint(SectorLocation.TOP));

        arrowsPath.reset();
        arrowsPath.moveTo(rightCenter.first - arrowSize, rightCenter.second - arrowSize);
        arrowsPath.lineTo(rightCenter.first, rightCenter.second);
        arrowsPath.lineTo(rightCenter.first - arrowSize, rightCenter.second + arrowSize);

        canvas.drawPath(arrowsPath, getSelectorPaint(SectorLocation.RIGHT));

        arrowsPath.reset();
        arrowsPath.moveTo(bottomCenter.first - arrowSize, bottomCenter.second - arrowSize);
        arrowsPath.lineTo(bottomCenter.first, bottomCenter.second);
        arrowsPath.lineTo(bottomCenter.first + arrowSize, bottomCenter.second - arrowSize);
        canvas.drawPath(arrowsPath, getSelectorPaint(SectorLocation.BOTTOM));


        arrowsPath.reset();
        arrowsPath.moveTo(leftCenter.first + arrowSize, leftCenter.second - arrowSize);
        arrowsPath.lineTo(leftCenter.first, leftCenter.second);
        arrowsPath.lineTo(leftCenter.first + arrowSize, leftCenter.second + arrowSize);

        canvas.drawPath(arrowsPath, getSelectorPaint(SectorLocation.LEFT));

        arrowsPath.close();
    }

    public void onArrowPressed(SectorLocation what) {
        pressedArrowsList.add(what);
        selectorPressedEvent(what, true);

        invalidate();
    }

    public void onArrowReleased(SectorLocation what) {
        pressedArrowsList.remove(what);
        selectorPressedEvent(what, false);

        invalidate();
    }

    public void selectorPressedEvent(SectorLocation location, Boolean show) {
        if (location == SectorLocation.TOP) {
            topSelector.changeState(show);
        }
        if (location == SectorLocation.BOTTOM) {
            bottomSelector.changeState(show);
        }
        if (location == SectorLocation.LEFT) {
            leftSelector.changeState(show);
        }
        if (location == SectorLocation.RIGHT) {
            rightSelector.changeState(show);
        }
    }

    private Paint getSelectorPaint(SectorLocation buttonL) {
        if (pressedArrowsList.contains(buttonL)) {
            return paintArrowClicked;
        } else {
            return paintArrows;
        }
    }

    private class SelectableArc {
        RectF rectToDrawOn;
        SectorLocation location;

        SelectableArc(RectF rectToDrawOn, SectorLocation location) {
            this.location = location;
            this.rectToDrawOn = rectToDrawOn;
            setupPaint();
        }

        private Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private int colorTransparent;
        private int greenColour;

        void setupPaint() {
            arcPaint.setColor(colorTransparent);
            arcPaint.setStyle(Paint.Style.FILL);

            if (VERSION.SDK_INT > VERSION_CODES.M) {
                colorTransparent = getContext().getResources().getColor(android.R.color.transparent, null);
                greenColour = getContext().getResources().getColor(R.color.colorPrimary, null);
            } else {
                colorTransparent = getContext().getResources().getColor(android.R.color.transparent);
                greenColour = getContext().getResources().getColor(R.color.colorPrimary);
            }
        }

        private float getCx() {
            if (location == SectorLocation.TOP) {
                return rectToDrawOn.centerX();
            }
            if (location == SectorLocation.BOTTOM) {
                return rectToDrawOn.centerX();
            }
            if (location == SectorLocation.RIGHT) {
                return rectToDrawOn.centerX() + arrowShift;
            }
            if (location == SectorLocation.LEFT) {
                return rectToDrawOn.centerX() - arrowShift;
            }

            return 0f;
        }

        private float getCy() {

            if (location == SectorLocation.TOP) {
                return rectToDrawOn.centerY() - arrowShift;
            }
            if (location == SectorLocation.BOTTOM) {
                return rectToDrawOn.centerY() + arrowShift;
            }
            if (location == SectorLocation.RIGHT) {
                return rectToDrawOn.centerY();
            }
            if (location == SectorLocation.LEFT) {
                return rectToDrawOn.centerY();
            }
            return 0f;
        }

        void draw(Canvas canvas) {
            canvas.drawCircle(getCx(), getCy(), getRadius(), arcPaint);
        }

        void changeState(Boolean show) {
            if (!show)
                arcPaint.setColor(colorTransparent);
            else
                arcPaint.setColor(greenColour);
        }
    }

    public enum SectorLocation {
        TOP, BOTTOM, LEFT, RIGHT
    }
}
