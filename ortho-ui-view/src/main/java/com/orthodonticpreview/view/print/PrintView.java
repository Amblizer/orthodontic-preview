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
package com.orthodonticpreview.view.print;

import com.orthodonticpreview.view.OrthoView;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.media.jai.PlanarImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.model.ViewModel;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.api.image.SimpleOpManager;
import org.weasis.core.api.image.util.ImageLayer;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.MouseActions;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2013, 7 Jan
 */
public class PrintView<E extends ImageElement> extends DefaultView2d {

    /**
     * Class logger.
     */
    private static final Logger LOGGER
            = LoggerFactory.getLogger(PrintView.class);

    public PrintView(OrthoView view) {

        super(view.getEventManager(), view.getLayerModel(), null);
        initContent(view);
    }

    private void initContent(OrthoView view) {

        // No need to have random pixel iterator
        this.imageLayer.setBuildIterator(false);

        //font and info-layer
        setFont(view.getFont());
        setInfoLayer(view);

        // Copy image operations from view2d
        SimpleOpManager operations = imageLayer.getDisplayOpManager();
        for (ImageOpNode op : view.getImageLayer().getDisplayOpManager().getOperations()) {
            try {
                operations.addImageOperationAction(op.clone());
            } catch (CloneNotSupportedException e) {
                LOGGER.error("Cannot clone image operation: {}", op); //$NON-NLS-1$
            }
        }
        // Copy the current values of image operations
        view.copyActionWState(actionsInView);

        setPreferredSize(new Dimension(1024, 1024));

        final ViewModel model = view.getViewModel();

        final Rectangle2D mArea = view.getViewModel().getModelArea();
        final Rectangle2D viewFullImg
                = new Rectangle2D.Double(0, 0,
                        view.modelToViewLength(mArea.getWidth()),
                        view.modelToViewLength(mArea.getHeight()));
        Rectangle2D canvas
                = new Rectangle2D.Double(view.modelToViewLength(
                                model.getModelOffsetX()), view.modelToViewLength(model
                                .getModelOffsetY()), view.getWidth(), view.getHeight());

        Rectangle2D.intersect(canvas, viewFullImg, viewFullImg);

        actionsInView.put("origin.image.bound", canvas);
        actionsInView.put("origin.zoom",
                view.getActionValue(ActionW.ZOOM.cmd()));

        final Point2D centerPt = new Point2D.Double(
                view.viewToModelX(viewFullImg.getX() - canvas.getX()
                        + (viewFullImg.getWidth() - 1) * 0.5),
                view.viewToModelY(viewFullImg.getY() - canvas.getY()
                        + (viewFullImg.getHeight() - 1) * 0.5));
        actionsInView.put("origin.center", centerPt);

        final Point2D origin = new Point2D.Double(
                view.getViewModel().getModelOffsetX(),
                view.getViewModel().getModelOffsetY());
        actionsInView.put("origin.origin", origin);

        setSeries(view.getSeries(), view.getImage());

    }

    @Override
    public void enableMouseAndKeyListener(MouseActions mouseActions) {
        //empty
    }

    @Override
    public void handleLayerChanged(ImageLayer layer) {
        //empty
    }

    public void draw(Graphics2D g2d, int fontSize) {
        //para recuperar depois...
        final Stroke oldStroke = g2d.getStroke();
        final Paint oldPaint = g2d.getPaint();
        final Shape oldClip = g2d.getClip();

        //o clip precisa ser = ao canvas-size!
        g2d.setClip(getBounds());

        final double viewScale = getViewModel().getViewScale();
        final double offsetX = getViewModel().getModelOffsetX() * viewScale;
        final double offsetY = getViewModel().getModelOffsetY() * viewScale;
        // Paint the visible area
        g2d.translate(-offsetX, -offsetY);

        // Set font size according to the view size and scale to pagaformat
        g2d.setFont(new Font("Dialog", 0, fontSize));

        //Dont use this for print
        //imageLayer.drawImage(g2d);
        //set zoom to 1 to make the zoom on the printer.
        zoom(1);
        //Use this one to get more resolution to the image: #1041
        final PlanarImage image = (PlanarImage) imageLayer.getDisplayImage();
        if (image != null) {
            g2d.drawImage(image.getAsBufferedImage(),
                    AffineTransform.getScaleInstance(viewScale, viewScale),
                    null);
        }

        //retorna o zoom para manter as medidas e outros na mesma escala.
        zoom(viewScale);
        drawLayers(g2d, affineTransform, inverseTransform);
        g2d.translate(offsetX, offsetY);
        if (infoLayer != null) {
            // Set font size according to the view size
            infoLayer.paint(g2d);

        }
        if (oldClip != null) {
            g2d.clip(oldClip);
        }
        g2d.setPaint(oldPaint);
        g2d.setStroke(oldStroke);
    }

    private void setInfoLayer(OrthoView view) {
        infoLayer = view.getInfoLayer().getLayerCopy(this);
    }

}
