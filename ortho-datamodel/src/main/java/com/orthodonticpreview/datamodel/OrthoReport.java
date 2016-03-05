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
 *****************************************************************************
 */
package com.orthodonticpreview.datamodel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.TransposeDescriptor;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.Series;

/**
 * Utility to make the image and process all tags needed to the report to be
 * shown and printed.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 11 Oct.
 */
public class OrthoReport {

    /**
     * Serie where the calculation was done.
     */
    private final Series dataSerie;

    /**
     * Color for some of the drawings.
     */
    private final Color basicColor = Color.black;

    /**
     * Blue color from Visual ID (R0 G152 e B218).
     */
    private final Color blueColor = new Color(0, 152, 218);
    /**
     * Basic (line) stroke.
     */
    private final Stroke basicStroke = new BasicStroke(2);
    /**
     * A dashed stroke.
     */
    private final Stroke dashedStroke
            = new BasicStroke(2, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 10, new float[]{15, 15}, 0);

    /**
     * Tolerance for turning tencence result.
     */
    private static final double TURNING_TOLERANCE = 0.1; //(mm)

    /**
     * Builds a new OrthoReport utility.
     *
     * @param serie Serie where the calculation was done.
     */
    public OrthoReport(final Series serie) {
        dataSerie = serie;
    }

    /**
     * Creates an image to represent the exam calculation and saves it.
     *
     * @param rotation Rotation angle on viewer.
     * @param flip True if the image has a flip operation.
     * @return File with the new image (saves on temp).
     * @throws IOException if cant create a temp file.
     */
    public File createImageReport(final int rotation, final boolean flip)
            throws IOException {

        final File file = getRepImgFile();
        final Rectangle bounds
                = (Rectangle) dataSerie.getTagValue(TagO.RESULT_BOUNDS);
        if (bounds == null) { //was not calculated yet...
            throw new IllegalStateException("No bounds found.");
        }

        PlanarImage image = getImageFromMedia(dataSerie);
        if (rotation != 0) {
            image = rotateImage(image, rotation);
        }
        if (flip) {
            image = flipImage(image);
        }

        BufferedImage bufimage = null;
        if (isOutOfBounds(image.getBounds(), bounds)) {
            bufimage = new BufferedImage(bounds.width, bounds.height,
                    BufferedImage.TYPE_INT_RGB);
            final Graphics graph = bufimage.getGraphics();

            graph.setColor(Color.white);
            graph.fillRect(bufimage.getMinX(), bufimage.getMinY(),
                    bufimage.getWidth(), bufimage.getHeight());

            final Rectangle imageBounds = image.getBounds();

            graph.drawImage(image.getAsBufferedImage(
                    bounds, image.getColorModel()),
                    calcNewImageOriginCoord(imageBounds.x, bounds.x),
                    calcNewImageOriginCoord(imageBounds.y, bounds.y), null);
        } else {
            bufimage = image.getAsBufferedImage(bounds, image.getColorModel());
        }

        final Graphics2D graphics = (Graphics2D) bufimage.getGraphics();

        //delta between the original and the cuted image
        final int dx = -bounds.x;
        final int dy = -bounds.y;

        drawGraphics(graphics, dx, dy, bounds.height);

        ImageIO.write(bufimage, "png", file);

        return file;
    }

    /**
     * Extends a line until the point where Y = maxY.
     *
     * @param line Line to be extended.
     * @param maxY Point Y the line has to be extended to.
     * @return The new extended line.
     */
    private Line2D extendResultant(final Line2D line, final int maxY) {
        final Line2D lowLine = new Line2D.Double(0, maxY, 100, maxY);
        final Point2D intersect
                = VectorMath.getIntersectionPoint(line, lowLine);
        return new Line2D.Double(intersect, line.getP1());
    }

    /**
     * Sets all tags to be shown on report.
     *
     * @param reportSerie The report serie.
     */
    public void setReportTags(final MediaSeries reportSerie) {
        //needed info:
        //patient name (OK) & creation date (OK)

        //results:
        reportSerie.setTag(TagO.TURNING_TENDENCY, getTurningTen());

        try {
            reportSerie.setTag(TagO.PROJECTION_TENDENCY, getProjectionTen());
        } catch (IllegalStateException ex) {
            reportSerie.setTag(TagO.PROJECTION_TENDENCY, null);
        }
    }

    /**
     * Finds out if bounds is extended to out of image area.
     *
     * @param imageBounds bound or image.
     * @param bounds bounds of draw.
     * @return true if bounds are bigger than image.
     */
    private boolean isOutOfBounds(final Rectangle imageBounds,
            final Rectangle bounds) {
        if (bounds.x < imageBounds.x || bounds.y < imageBounds.y) {
            return true;
        }
        if (bounds.x + bounds.width > imageBounds.x + imageBounds.width
                || bounds.y + bounds.height
                > imageBounds.y + imageBounds.height) {
            return true;
        }
        return false;
    }

    /**
     * Calculates coordinates to origin of the new image (report-image).
     *
     * @param imgCoord X or Y on image.
     * @param drawCoord X or Y on old graphics layer.
     * @return coordinate to use on new image.
     */
    private static int calcNewImageOriginCoord(final int imgCoord,
            final int drawCoord) {
        int result = 0;
        if (drawCoord < imgCoord) {
            result = imgCoord - drawCoord;
        } else {
            result = 0;
        }
        return result;
    }

    /**
     * Calculates projection dendency.
     *
     * Tendencia de projecao dos anteriores para anterior [quando o ponto cair
     * fora da linha].
     *
     * @return result: true or false.
     */
    private Boolean getProjectionTen() {
        final Object frontValue = dataSerie.getTagValue(TagO.FRONT_LIMIT);
        final Object pointValue = dataSerie.getTagValue(TagO.POINT_GR);
        if (frontValue instanceof Double
                && pointValue instanceof Point2D) {
            final double front = (Double) frontValue;
            final double yPoint = ((Point2D) pointValue).getY();
            return (yPoint < front);
        }
        throw new IllegalStateException("Tags ARC or FRONT_LIMIT invalid.");
    }

    /**
     * Calculates turning dendency.
     *
     * TendÃªncia de giro na arcada para a [direita / esquerda]. Lado contrÃ¡rio
     * ao do centro de resistÃªncia.
     *
     * @return "left" or "right".
     */
    private String getTurningTen() {
        String res = null;
        final Object midTag = dataSerie.getTagValue(TagO.MID_X);
        final Object pointTag = dataSerie.getTagValue(TagO.POINT_GR);

        double scale = 1;
        final Object vectsScale = dataSerie.getTagValue(TagO.VECTORS_SCALE);
        if (vectsScale instanceof Double) {
            scale = (Double) vectsScale;
        }

        if (midTag instanceof Double && pointTag instanceof Point2D) {
            final Double mid = (Double) midTag;
            final double ptX = ((Point2D) pointTag).getX();
            if ((Math.abs(mid - ptX) * scale) < TURNING_TOLERANCE) {
                res = "none";
            } else if (ptX >= mid) { //point at right, tendency to left
                res = "left";
            } else { //point at left, tendency to right
                res = "right";
            }
        }
        return res;
    }

    /**
     * Creates the image file for report.
     *
     * @return The file.
     * @throws IOException If cant create it.
     */
    private File getRepImgFile() throws IOException {
        final File tempDir = AppProperties.APP_TEMP_DIR;
        return File.createTempFile("imrep", ".png", tempDir);
    }

    /**
     * Gets the calculation image from media.
     *
     * @param dataSerie Serie that has the calculation image.
     * @return The image over with calculation was done.
     */
    private static PlanarImage getImageFromMedia(final Series dataSerie) {
        final MediaElement media = dataSerie.getMedia(0, null, null);
        PlanarImage image = null;
        if (media instanceof ImageElement) {
            final ImageElement imageEl = (ImageElement) media;
            image = imageEl.getImage();
        }
        return image;
    }

    /**
     * Rotates image (uses JAI).
     *
     * @param source Source image.
     * @param rotationAngle Angle (degrees).
     * @return The rotated image.
     */
    private PlanarImage rotateImage(
            final PlanarImage source, final int rotationAngle) {

        final ParameterBlock pb = new ParameterBlock();
        pb.addSource(source);
        pb.add(source.getWidth() / 2.0f);
        pb.add(source.getHeight() / 2.0f);
        pb.add((float) (rotationAngle * Math.PI / 180.0));
        pb.add(new InterpolationBilinear());
        pb.add(new double[]{255, 255, 255});
        final PlanarImage result = JAI.create("rotate", pb);

        return result;
    }

    /**
     * Flips one image (uses JAI).
     *
     * @param image Source image.
     * @return new Fliped image.
     */
    private PlanarImage flipImage(final PlanarImage image) {
        final ParameterBlock param = new ParameterBlock();
        param.addSource(image);
        param.add(TransposeDescriptor.FLIP_HORIZONTAL);
        return JAI.create("transpose", param);
    }

    /**
     * Draw graphics on the report image.
     *
     * @param graphics Graphics object to draw on.
     * @param dx Diference between old and new X-coord.
     * @param dy Diference between old and new Y-coord.
     * @param boundsHeight Height of image bounds.
     */
    private void drawGraphics(final Graphics2D graphics, final int dx,
            final int dy, final int boundsHeight) {
        final AffineTransform translate = AffineTransform.getTranslateInstance(
                dx, dy);

        graphics.setStroke(basicStroke);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setPaint(blueColor);

        //ponto GR
        Object tagValue = dataSerie.getTagValue(TagO.POINT_GR);
        if (tagValue instanceof Point2D) {
            final Point2D centerOr = (Point2D) tagValue;
            final Point2D center = new Point2D.Double(
                    centerOr.getX() + dx, centerOr.getY() + dy);
            int radio = 6;
            graphics.draw(new Ellipse2D.Double(
                    center.getX() - radio, center.getY() - radio,
                    radio * 2, radio * 2));
        }

        //resultante
        tagValue = dataSerie.getTagValue(TagO.RESULTANT);
        if (tagValue instanceof Line2D) {
            final Line2D lineRes = (Line2D) tagValue;
            final Line2D line = new Line2D.Double(lineRes.getX1() + dx,
                    lineRes.getY1() + dy, lineRes.getX2() + dx,
                    lineRes.getY2() + dy);
            graphics.draw(line);

            //prolongamento da resultante
            graphics.setPaint(basicColor);
            graphics.setStroke(dashedStroke);
            graphics.draw(extendResultant(line, boundsHeight - 20));
        }

        graphics.setStroke(basicStroke);
        graphics.setPaint(basicColor);

        //arco:
        tagValue = dataSerie.getTagValue(TagO.ARC);
        if (tagValue instanceof GeneralPath) {
            final GeneralPath arc = new GeneralPath((GeneralPath) tagValue);
            arc.transform(translate);
            graphics.draw(arc);
        }
    }

}
