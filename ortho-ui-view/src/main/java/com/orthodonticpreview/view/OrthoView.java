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

import com.orthodonticpreview.datamodel.TagO;
import com.orthodonticpreview.ui.explorer.OrthoExplorerView;
import com.orthodonticpreview.ui.persistence.GraphicPack;
import com.orthodonticpreview.view.graphics.ArcGraphic;
import com.orthodonticpreview.view.graphics.DotGraphic;
import com.orthodonticpreview.view.graphics.VectorGraphic;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ToolTipManager;
import org.weasis.base.viewer2d.View2d;
import org.weasis.core.api.gui.model.ViewModel;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.Filter;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.api.image.ImageOpEvent;
import org.weasis.core.api.image.RotationOp;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.SynchEvent;
import org.weasis.core.ui.graphic.model.AbstractLayer;
import org.weasis.core.ui.graphic.model.DefaultViewModel;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 12 Oct.
 */
public class OrthoView extends View2d {

    private static final String defaultFamily = "Dialog";
    private String contentRole;
    private double middleX;

    /**
     * Creates the viewer and changes infoLayer.
     *
     * @param eventManager The event manager.
     */
    public OrthoView(
            final ImageViewerEventManager<ImageElement> eventManager) {
        super(eventManager);
        infoLayer = new OrthoInfoLayer(this);
    }

    /**
     * Init action state map. Overriden to recover actions stored as tag.
     */
    @Override
    protected void initActionWState() {
        actionsInView.clear();
        actionsInView.put(zoomTypeCmd, ZoomType.BEST_FIT);
        
        if (series != null) {
            Object tagValue = series.getTagValue(TagO.ACTIONS_TAG);

            if (tagValue instanceof HashMap) {
                HashMap map = (HashMap) tagValue;
                actionsInView.putAll(map);
                // Rotation is not on tags anymore...
                Object get = map.get(ActionW.ROTATION.cmd());

                if (get instanceof Integer) {
                    getDisplayOpManager().setParamValue(RotationOp.OP_NAME,
                            RotationOp.P_ROTATE, (Integer) get);
                } else {
                    getDisplayOpManager().setParamValue(RotationOp.OP_NAME,
                            RotationOp.P_ROTATE, 0);
                }

                return;
            }
        }

        //if no tag or no series
        super.initActionWState();
    }

    @Override
    public Object getActionValue(String action) {
        if (ActionW.ROTATION.cmd().equals(action)) {
            return getDisplayOpManager().getParamValue(RotationOp.OP_NAME, RotationOp.P_ROTATE);
        }
        return super.getActionValue(action);
    }

    /**
     * Sets a serie to show on viewer.
     *
     * Overriden to: - store operations to reaply when showing same series
     * again. - use information loaded from file to rebuild measures layer.
     *
     * @param series new serie to show.
     * @param selectedImage selected image.
     */
    @Override
    public void setSeries(MediaSeries<ImageElement> series,
            ImageElement selectedImage) {
        //super.setSeries(series, selectedImage);
        MediaSeries<ImageElement> oldsequence = this.series;
        this.series = series;
        if (oldsequence != null && oldsequence != series) {
            closingSeries(oldsequence);
            // All the action values are initialized again with the series
            //changing
            initActionWState();
        } else if (oldsequence == null && series != null) {
            initActionWState();
        }
        if (series == null) {
            imageLayer.setImage(null, null);
            if (getLayerModel() != null) { //prevents NullPointerEx
                getLayerModel().deleteAllGraphics();
            }

        } else {
            ImageElement media = selectedImage;
            if (selectedImage == null) {
                media
                        = series.getMedia(tileOffset < 0 ? 0 : tileOffset,
                                (Filter<ImageElement>) actionsInView.get(
                                        ActionW.FILTERED_SERIES.cmd()),
                                getCurrentSortComparator());
            }

            imageLayer.fireOpEvent(new ImageOpEvent(ImageOpEvent.OpEvent.SeriesChange, series, media, null));

            setImage(media);

            Object tagValue = series.getTagValue(TagO.MID_X);
            if (tagValue instanceof Double) {
                middleX = (Double) tagValue;
            } else {
                middleX = getViewModel().getModelArea().getWidth() / 2;
            }

            contentRole = (String) series.getTagValue(TagO.SERIE_ROLE);
            if (OrthoExplorerView.REPORT_IMAGE.equalsIgnoreCase(contentRole)) {
                infoLayer = new OrthoReportLayer(this);
                actionsInView.put(zoomTypeCmd, ZoomType.BEST_FIT);
                zoom(0.0);
                align();
            } else {
                includeGraphics(series);
                moveToMiddleX();
            }

        }

        eventManager.updateComponentsListener(this);

        // Set the sequence to the state OPEN
        if (series != null && oldsequence != series) {
            series.setOpen(true);
        }

    }

    @Override
    public void registerDefaultListeners() {
        //from DefauleView2d
        addFocusListener(this);
        ToolTipManager.sharedInstance().registerComponent(this);
        imageLayer.addLayerChangeListener(this);
        ////
        ///from here:
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                Double currentZoom = (Double) actionsInView.get(ActionW.ZOOM.cmd());
                if (currentZoom <= 0.0) {
                    zoom(0.0);
                }
                if (OrthoExplorerView.REPORT_IMAGE.equalsIgnoreCase(
                        contentRole)) {
                    align();
                } else {
                    moveToMiddleX();
                }
            }
        });
    }

    /**
     * Puts middleX in the middle of viewer.
     */
    private void moveToMiddleX() {
        final Rectangle2D bound = getViewModel().getModelArea();
        setCenter(middleX, bound.getHeight() / 2.0);
    }

    @Override
    public double getBestFitViewScale() {
        if (OrthoExplorerView.REPORT_IMAGE.equalsIgnoreCase(contentRole)) {
            final double viewportWidth = getWidth() / (double) 2;
            final double viewportHeight = getHeight() * (double) 0.4;
            final Rectangle2D modelArea = getViewModel().getModelArea();
            double bestScale = cropViewScale(Math.min(
                    viewportWidth / modelArea.getWidth(),
                    viewportHeight / modelArea.getHeight()));
            ActionState zoom = eventManager.getAction(ActionW.ZOOM);
            if (zoom instanceof SliderChangeListener
                    && eventManager.getSelectedViewPane() == this) {
                ((SliderChangeListener) zoom).setValueWithoutTriggerAction(
                        eventManager.viewScaleToSliderValue(bestScale));
            }
            return bestScale;
        }
        return super.getBestFitViewScale();
    }

    private double cropViewScale(double viewScale) {
        return DefaultViewModel.cropViewScale(
                viewScale, getViewModel().getViewScaleMin(),
                getViewModel().getViewScaleMax());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);

        if (evt.getPropertyName().equals(ActionW.SYNCH.cmd())) {
            SynchEvent synch = (SynchEvent) evt.getNewValue();
            for (Map.Entry<String, Object> entry : synch.getEvents().entrySet()) {
                String command = entry.getKey();
                //update middle line!
                if (ActionW.ZOOM.cmd().equals(command)
                        || ActionW.PAN.cmd().equals(command)) {
                    updateMiddleLine();
                }
            }
        } else if (evt.getPropertyName().equals("midX")) {
            updateMiddleLine();
        }
    }

    private void updateMiddleLine() {
        ViewModel viewModel = getViewModel();
        if (getWidth() > 0.0) {
            middleX = ((getWidth() / (double) 2) / viewModel.getViewScale())
                    + viewModel.getModelOffsetX();
            firePropertyChange("midX", null, middleX);
            series.setTag(TagO.MID_X, middleX);
        }
    }

    /**
     * Overriden to change rotation and flip operation behavior.
     */
    @Override
    protected void updateAffineTransform() {
        double viewScale = getViewModel().getViewScale();
        affineTransform.setToScale(viewScale, viewScale);

        try {
            inverseTransform.setTransform(affineTransform.createInverse());
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    /**
     * Alinha para a posição ideal no relatorio. Alinhada a direita e iniciando
     * a 1/8 de top.
     */
    private void align() {
        //only align left for now.
        final double scale = getViewModel().getViewScale();
        final double x = -getInfoLayer().getBorder() / scale;
        final double y = -getBounds().height / ((double) 8 * scale);

        setOrigin(x, y);
    }

    /**
     * Recovers graphics from tag (cames from persistence file).
     *
     * @param series
     */
    private void includeGraphics(MediaSeries<ImageElement> series) {
        if (series.containTagKey(TagO.GRAPHIC_PACKS)) {
            //tem graficos loaded
            Object tagValue = series.getTagValue(TagO.GRAPHIC_PACKS);
            if (tagValue instanceof List) {
                for (Object object : (List) tagValue) {
                    if (object instanceof GraphicPack) {
                        //de que classe eh o grafico?
                        GraphicPack pack = (GraphicPack) object;
                        String clazz = pack.getClazz();
                        if (ArcGraphic.class.getName().equals(clazz)) {
                            ArcGraphic arc = new ArcGraphic(pack);
                            layerModel.getLayer(AbstractLayer.MEASURE).addGraphic(arc);
                        } else if (VectorGraphic.class.getName().equals(clazz)) {
                            VectorGraphic vec = new VectorGraphic(pack);
                            String linkedOwner = vec.getLinkedOwner();
                            if (linkedOwner == null
                                    || !linkedOwner.startsWith("force")) {
                                layerModel.getLayer(
                                        AbstractLayer.MEASURE).addGraphic(vec);
                            }
                        } else if (DotGraphic.class.getName().equals(clazz)) {
                            DotGraphic dot = new DotGraphic(pack);
                            layerModel.getLayer(AbstractLayer.MEASURE).addGraphic(dot);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Font getFont() {
        int size = (int) Math.floor(getBounds().width / (double) 55);
        if (size > 12
                && OrthoExplorerView.CALC_IMAGE.equalsIgnoreCase(contentRole)) {
            size = 12;
        }
        return new Font(defaultFamily, 0, size);
    }

    /**
     * Routine to be done when some serie is closing.
     *
     * Overriden to store actual state on a tag.
     *
     * @param series Serie that is closing.
     */
    @Override
    protected void closingSeries(MediaSeries series) {
        super.closingSeries(series);
        //set rotation:
        actionsInView.put(ActionW.ROTATION.cmd(),
                getDisplayOpManager().getParamValue(RotationOp.OP_NAME,
                        RotationOp.P_ROTATE));
        series.setTag(TagO.ACTIONS_TAG, actionsInView.clone());
        //set middle:
        series.setTag(TagO.MID_X, middleX);
    }

}
