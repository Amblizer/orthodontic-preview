/**
 * *****************************************************************************
 * Copyright (c) 2012 Cesar Moreira.
 *
 * This file is part of Orthodontic Preview.
 *
 * Orthodontic Preview is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Orthodontic Preview is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Orthodontic Preview. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package com.orthodonticpreview.view;

import com.orthodonticpreview.view.internal.Messages;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.HashMap;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.DecFormater;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.api.util.StringUtil;
import org.weasis.core.ui.editor.image.AnnotationsLayer;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.PixelInfo;
import org.weasis.core.ui.graphic.model.AbstractLayer;

/**
 * Info layer for calculation images.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 12 Oct.
 */
class OrthoInfoLayer implements AnnotationsLayer {

    /**
     * Display prefferences (what to show).
     */
    private final HashMap<String, Boolean> displayPrefs
            = new HashMap<String, Boolean>();

    /**
     * Controls visibility of this layer.
     */
    private boolean visible = true;
    /**
     * Owner viewer.
     */
    private final DefaultView2d view2DPane;
    /**
     * Area of pixel information.
     */
    private final Rectangle pixelInfoBound;
    /**
     * Pixel information.
     */
    private PixelInfo pixelInfo = null;
    /**
     * Info layer margin.
     */
    private int border = 10;

    /**
     * Mid Line name.
     */
    public static final String MID_LINE = "Mid Line";
    /**
     * Orientation of image (sides) name.
     */
    public static final String SIDES = "Mid Line";
    /**
     * Help text.
     */
    public static final String HELP = "Help";

    /**
     * Creates a new info-layer for given view.
     *
     * @param view view to own the layer.
     */
    public OrthoInfoLayer(final DefaultView2d view) {
        this.view2DPane = view;
        displayPrefs.put(MID_LINE, true);
        displayPrefs.put(SIDES, true);
        displayPrefs.put(HELP, false);
        displayPrefs.put(PIXEL, true);
        displayPrefs.put(ZOOM, true);
        displayPrefs.put(ROTATION, true);

        pixelInfoBound = new Rectangle();
    }

    @Override
    public boolean getDisplayPreferences(final String item) {
        final Boolean val = displayPrefs.get(item);
        if (val == null) {
            return false;
        }
        return val;
    }

    @Override
    public boolean setDisplayPreferencesValue(
            final String displayItem, final boolean selected) {
        final boolean selected2 = getDisplayPreferences(displayItem);
        displayPrefs.put(displayItem, selected);
        return selected != selected2;
    }

    @Override
    public Rectangle getPreloadingProgressBound() {
        return new Rectangle();
    }

    @Override
    public Rectangle getPixelInfoBound() {
        return pixelInfoBound;
    }

    @Override
    public void setPixelInfo(PixelInfo info) {
        pixelInfo = info;
    }

    @Override
    public int getBorder() {
        return border;
    }

    @Override
    public void setBorder(final int margin) {
        border = margin;
    }

    @Override
    public void paint(final Graphics2D g2d) {
        final ImageElement image = view2DPane.getImage();
        if (!visible || image == null) {
            return;
        }

        final Rectangle bound = view2DPane.getBounds();
        float midx = bound.width / 2f;
        float midy = bound.height / 2f;

        g2d.setPaint(Color.white);

        final float fontHeight = FontTools.getAccurateFontHeight(g2d);

        float drawY = bound.height - border;

        if (!image.isReadable()) {
            paintError(g2d, image, midx, midy, fontHeight);
        }

        if (getDisplayPreferences(MID_LINE)) {
            final Line2D midLine = new Line2D.Double(
                    midx, bound.y, midx, bound.height);
            g2d.setPaint(Color.red);
            g2d.draw(midLine);
        }

        if (getDisplayPreferences(SIDES)) {
            String str = Messages.getString("OrthoInfoLayer.LeftSide");
            paintFontOutline(g2d, str,
                    midx - 20 - g2d.getFontMetrics().stringWidth(str),
                    bound.y + border + fontHeight);
            paintFontOutline(g2d,
                    Messages.getString("OrthoInfoLayer.RightSide"), midx + 20,
                    bound.y + border + fontHeight);
        }

        if (getDisplayPreferences(HELP)) {
            int biggerWidth = g2d.getFontMetrics().stringWidth(
                    Messages.getString("OrthoInfoLayer.helpLine2"));
            int initX = bound.width - border - biggerWidth;
            int titleX = initX - 10;
            for (int i = 1; i <= 7; i++) {
                String str = Messages.getString("OrthoInfoLayer.helpLine" + i);
                if (i == 1 || i == 6) {
                    paintFontOutline(g2d, str, titleX, border + fontHeight * i);
                } else {
                    paintFontOutline(g2d, str, initX, border + fontHeight * i);
                }
            }
        }

        if (getDisplayPreferences(PIXEL)) {
            StringBuilder str = new StringBuilder(Messages.getString("OrthoInfoLayer.Pixel"));
            str.append(StringUtil.COLON_AND_SPACE);
            if (pixelInfo != null) {
                str.append(pixelInfo.getPixelValueText());
                str.append(" - ");
                str.append(pixelInfo.getPixelPositionText());
            }
            paintFontOutline(g2d, str.toString(), border, drawY - 1);
            drawY -= fontHeight + 2;
            pixelInfoBound.setBounds(border - 2, (int) drawY + 3,
                    g2d.getFontMetrics().stringWidth(str.toString()) + 4,
                    (int) fontHeight + 2);
        }

        if (getDisplayPreferences(ZOOM)) {
            paintFontOutline(g2d, Messages.getString("OrthoInfoLayer.Zoom")
                    + DecFormater.twoDecimal(
                            view2DPane.getViewModel().getViewScale() * 100)
                    + " %", border, drawY);
            drawY -= fontHeight;
        }
        if (getDisplayPreferences(ROTATION)) {
            paintFontOutline(g2d, Messages.getString("OrthoInfoLayer.Angle")
                    + view2DPane.getActionValue(
                            ActionW.ROTATION.cmd()) + " °", border, drawY);
            drawY -= fontHeight;
        }
    }

    /**
     * Painted when viewer cant open the image.
     *
     * @param g2d graphics.
     * @param image image.
     * @param midx middle x.
     * @param midy middle y.
     * @param fontHeight fint height.
     */
    private void paintError(final Graphics2D g2d, final ImageElement image,
            final float midx, final float midy, final float fontHeight) {
        String message = "Cannot read this media!";
        float line = midy;
        paintFontOutline(g2d, message, (int) (midx
                - g2d.getFontMetrics().stringWidth(message)
                / (double) 2), line);
        final String[] desc = image.getMediaReader().getReaderDescription();
        if (desc != null) {
            for (String str : desc) {
                if (str != null) {
                    line += fontHeight;
                    paintFontOutline(g2d, str, (int) (midx
                            - g2d.getFontMetrics().stringWidth(str)
                            / (double) 2), line);
                }
            }
        }
    }

    public void paintFontOutline(Graphics2D g2, String str, float x, float y) {
        g2.setPaint(Color.BLACK);
        g2.drawString(str, x - 1f, y - 1f);
        g2.drawString(str, x - 1f, y);
        g2.drawString(str, x - 1f, y + 1f);
        g2.drawString(str, x, y - 1f);
        g2.drawString(str, x, y + 1f);
        g2.drawString(str, x + 1f, y - 1f);
        g2.drawString(str, x + 1f, y);
        g2.drawString(str, x + 1f, y + 1f);
        g2.setPaint(Color.WHITE);
        g2.drawString(str, x, y);
    }

    @Override
    public AnnotationsLayer getLayerCopy(DefaultView2d view2DPane) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setVisible(boolean flag) {
        visible = flag;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setLevel(int i) {
        //do nothing;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public boolean isShowBottomScale() {
        return false;
    }

    @Override
    public void setShowBottomScale(boolean showBottomScale) {
        //empty
    }

    @Override
    public PixelInfo getPixelInfo() {
        return pixelInfo;
    }

    @Override
    public AbstractLayer.Identifier getIdentifier() {
        return AbstractLayer.ANNOTATION;
    }

}
