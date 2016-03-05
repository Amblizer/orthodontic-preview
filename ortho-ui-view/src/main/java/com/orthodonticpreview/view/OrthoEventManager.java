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
import com.orthodonticpreview.view.graphics.ArcGraphic;
import com.orthodonticpreview.view.graphics.DotGraphic;
import com.orthodonticpreview.view.graphics.VectorGraphic;
import com.orthodonticpreview.view.tool.TeethTool;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSizeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.api.image.OpManager;
import org.weasis.core.api.image.RotationOp;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.ui.docking.DockableTool;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MeasureToolBar;
import org.weasis.core.ui.editor.image.MouseActions;
import org.weasis.core.ui.editor.image.SynchView;
import org.weasis.core.ui.graphic.Graphic;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version
 */
public final class OrthoEventManager extends ImageViewerEventManager<ImageElement>
        implements ActionListener {

    /**
     * The single instance of this singleton class.
     */
    private static OrthoEventManager instance;

    private static final Logger LOGGER
            = LoggerFactory.getLogger(OrthoEventManager.class);

    private final SliderChangeListener zoomAction;
    private final SliderChangeListener rotateAction;
    private final ToggleButtonListener onlyOnceAction;
    private final ToggleButtonListener flipAction;

    public static final ArcGraphic arcGraphic
            = new ArcGraphic(2, Color.RED, true);
    public static final VectorGraphic vectorGraphic
            = new VectorGraphic(1, Color.BLUE, true);
    public static final DotGraphic dotGraphic
            = new DotGraphic(3, Color.red, true);

    public static final List<Graphic> graphicList = new ArrayList<Graphic>();

    static {
        graphicList.add(MeasureToolBar.selectionGraphic);
        graphicList.add(arcGraphic);
        graphicList.add(vectorGraphic);
        graphicList.add(dotGraphic);
    }
    private static final List<String> graphicCmd = new ArrayList<String>();

    private PrintRequestAttributeSet printAset;
    private PrintService lastService;

    private OrthoEventManager() {
        iniAction(zoomAction = newZoomAction());
        zoomAction.setMouseSensivity(0.1);
        iniAction(rotateAction = newRotateAction());
        rotateAction.setMouseSensivity(0.25);
        iniAction(onlyOnceAction = newDrawOnlyOnceAction());
        onlyOnceAction.setSelected(true);
        iniAction(flipAction = newFlipAction());

        iniAction(newPanAction());

        //Mouse Actions for Orthoview:
        mouseActions.setAction(MouseActions.LEFT, ActionW.PAN.cmd());
        mouseActions.setAction(MouseActions.MIDDLE, ActionW.ZOOM.cmd());
        mouseActions.setAction(MouseActions.RIGHT, ActionW.PAN.cmd());
        mouseActions.setAction(MouseActions.WHEEL, ActionW.ROTATION.cmd());

        for (Graphic graphic : graphicList) {
            graphicCmd.add(graphic.toString());
        }

        ComboItemListener measureAction
                = newMeasurementAction(graphicList.toArray(
                                new Graphic[graphicList.size()]));
        iniAction(measureAction);

        printAset = new HashPrintRequestAttributeSet();
        printAset.add(MediaSizeName.ISO_A4);
        printAset.add(new MediaPrintableArea(12, 12, 186, 273,
                MediaPrintableArea.MM));

    }

    public static synchronized OrthoEventManager getInstance() {
        if (instance == null) {
            instance = new OrthoEventManager();
        }
        return instance;
    }

    private void iniAction(ActionState action) {
        actions.put(action.getActionW(), action);
    }

    @Override
    public void setSelectedView2dContainer(
            final ImageViewerPlugin<ImageElement> selectedView) {

        if (selectedView2dContainer != null) {
            if (selectedView2dContainer == selectedView) {
                return;
            }
            selectedView2dContainer.setMouseActions(null);
            selectedView2dContainer.setDrawActions(null);
        }
        selectedView2dContainer = selectedView;
        if (selectedView != null) {

            updateComponentsListener(
                    selectedView.getSelectedImagePane());

            //importante, enable /disable actions by role
            final MediaSeries<ImageElement> series
                    = selectedView.getSelectedImagePane().getSeries();
            if (series != null) {
                setActions(selectedView,
                        (String) series.getTagValue(TagO.SERIE_ROLE));
            }
        }
    }

    /**
     * Enable / Disable actions and set mouse according to the series role of
     * selected container.
     *
     * @param selectedView Selected container.
     * @param contentRole Role of series on selected container.
     */
    public void setActions(
            final ImageViewerPlugin<ImageElement> selectedView,
            final String contentRole) {
        final ActionState measures = getAction(ActionW.DRAW_MEASURE);
        if (OrthoExplorerView.CALC_IMAGE.equals(
                contentRole)) {

            measures.enableAction(true);
            zoomAction.enableAction(true);
            flipAction.enableAction(true);
            rotateAction.enableAction(true);
            mouseActions.setAction(
                    MouseActions.WHEEL, ActionW.ROTATION.cmd());
            selectedView.setMouseActions(mouseActions);

            for (DockableTool dockableTool : selectedView.getToolPanel()) {
                if (dockableTool instanceof TeethTool) {
                    dockableTool.showDockable();
                }
            }

        } else {
            zoomAction.enableAction(true);

            measures.enableAction(false);
            flipAction.enableAction(false);
            rotateAction.enableAction(false);
            mouseActions.setAction(
                    MouseActions.WHEEL, ActionW.ZOOM.cmd());
            selectedView.setMouseActions(mouseActions);
        }
    }

    public void setMouseLeftAction(String action) {
        mouseActions.setAction(MouseActions.LEFT, action);
        getSelectedView2dContainer().setMouseActions(mouseActions);
    }

    @Override
    public boolean updateComponentsListener(
            DefaultView2d<ImageElement> view) {

        LOGGER.debug("OrthoEventManager.updateComponentsListener");
        if (view == null) {
            LOGGER.debug("Return false becouse view == null");
            return false;
        }

        if (selectedView2dContainer == null
                || view != selectedView2dContainer.getSelectedImagePane()) {
            return false;
        }

        clearAllPropertyChangeListeners();
        if (view.getSourceImage() == null) {
            enableActions(false);
            return false;
        }
        if (!enabledAction) {
            enableActions(true);
        }

        ImageElement image = view.getImage();
        LOGGER.debug("Image = " + image);

        OpManager dispOp = view.getDisplayOpManager();
        rotateAction.setValueWithoutTriggerAction(
                (Integer) dispOp.getParamValue(RotationOp.OP_NAME, RotationOp.P_ROTATE));
        zoomAction.setValueWithoutTriggerAction(viewScaleToSliderValue(
                Math.abs((Double) view.getActionValue(ActionW.ZOOM.cmd()))));
        updateAllListeners(selectedView2dContainer, SynchView.NONE);

        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int indexOf = graphicCmd.indexOf(e.getActionCommand());
        if (indexOf >= 0) {
            ImageViewerPlugin<ImageElement> view = getSelectedView2dContainer();
            if (view instanceof ViewContainer) {
                setMouseLeftAction(ActionW.MEASURE.cmd());
                ActionState measure = getAction(ActionW.DRAW_MEASURE);
                if (measure instanceof ComboItemListener) {
                    ((ComboItemListener) measure).setSelectedItem(
                            graphicList.get(indexOf));
                }
            }
        }
    }

    public PrintRequestAttributeSet getPrintAset() {
        return printAset;
    }

    public PageFormat getPageFormat() {
        PrinterJob job = PrinterJob.getPrinterJob();
        return job.getPageFormat(printAset);
    }

    /**
     * @return the lastService
     */
    public PrintService getLastService() {
        if (lastService == null) {
            lastService = PrintServiceLookup.lookupDefaultPrintService();
        }
        return lastService;
    }

    /**
     * @param lastService the lastService to set
     */
    public void setLastService(PrintService lastService) {
        this.lastService = lastService;
    }

    @Override
    public void resetDisplay() {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}
