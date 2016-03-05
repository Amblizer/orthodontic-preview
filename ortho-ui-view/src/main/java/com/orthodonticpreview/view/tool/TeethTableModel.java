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
package com.orthodonticpreview.view.tool;

import com.orthodonticpreview.datamodel.TagO;
import com.orthodonticpreview.datamodel.VectorMath;
import com.orthodonticpreview.ui.persistence.PortableGraphic;
import com.orthodonticpreview.ui.persistence.TeethPlace;
import com.orthodonticpreview.view.OrthoEventManager;
import com.orthodonticpreview.view.OrthoView;
import com.orthodonticpreview.view.graphics.DotGraphic;
import com.orthodonticpreview.view.graphics.VectorGraphic;
import com.orthodonticpreview.view.internal.Messages;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.graphic.Graphic;
import org.weasis.core.ui.graphic.model.AbstractLayer;
import org.weasis.core.ui.graphic.model.AbstractLayerModel;

/**
 * Controls the information and operations envolved on TeethTool.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 16 Oct
 */
public class TeethTableModel extends AbstractTableModel {

    /**
     * Class Logger.
     */
    private static final Logger LOGGER
            = LoggerFactory.getLogger(TeethTableModel.class);

    /**
     * List of row objects.
     */
    private final List<TeethPlace> list;

    /**
     * Center of arc place.
     */
    private TeethPlace midPlace;

    /**
     * Link to arc Graphic.
     */
    private Graphic arcLink;

    /**
     * Format for decimal numbers on table.
     */
    private final DecimalFormat dFormat = new DecimalFormat("#.#");

    /**
     * Column names.
     */
    private static final String[] COL_NAMES = new String[]{
        Messages.getString("TeethTableModel.N"),
        Messages.getString("TeethTableModel.LenMm"),
        Messages.getString("TeethTableModel.Center"),
        Messages.getString("TeethTableModel.ForceVec"),
        Messages.getString("TeethTableModel.LenPix")
    };

    public static final int N_COL = 0;
    public static final int MM_COL = 1;
    public static final int CENTER_COL = 2;
    public static final int FORCEVEC_COL = 3;
    public static final int LENPIX_COL = 4;

    private double scalePixelValue;
    private double scaleMmValue;

    /**
     * Constructor.
     */
    public TeethTableModel() {
        super();
        list = new ArrayList<TeethPlace>();
    }

    /**
     * Populates model for the first time.
     *
     * @param names Names for the row Objects.
     * @param view Corresponding view.
     */
    public void populateModel(
            final String[] names, final DefaultView2d view) {
        list.clear();
        scaleMmValue = scalePixelValue = 0;

        for (int i = 0; i < names.length; i++) {
            if ("mid".equalsIgnoreCase(names[i])) {
                midPlace = new TeethPlace("mid");
                //view.addPropertyChangeListener("midX", midPlace);
                view.addPropertyChangeListener("midX", graphicsChangeListener);

                //truque para receber primeiro valor
                view.propertyChange(
                        new PropertyChangeEvent(this, "midX", 0, 0));

                list.add(midPlace);
            } else {
                list.add(new TeethPlace(names[i]));
            }
        }
    }

    /**
     * Populates model with a previous list (normally stored at
     * TagO.TEETH_PLACE_LIST.
     *
     * @param plList List with the previous data.
     * @param view Corresponding view.
     */
    public void populateModel(final List plList, final DefaultView2d view) {
        List<Graphic> allGraphics = view.getLayerModel().getAllGraphics();
        List<PortableGraphic> withLinks = lookForLinks(allGraphics);

        list.clear();
        scaleMmValue = scalePixelValue = 0;

        Iterator iterator = plList.iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();

            if (object instanceof TeethPlace) {
                TeethPlace place = (TeethPlace) object;
                if ("mid".equalsIgnoreCase(place.getPlace())) {
                    midPlace = new TeethPlace("mid");
                    view.addPropertyChangeListener(
                            "midX", graphicsChangeListener);

                    //truque para receber primeiro valor
                    view.propertyChange(
                            new PropertyChangeEvent(this, "midX", 0, 0));

                    list.add(midPlace);
                } else {
                    list.add(place);
                    //links?
                    addLinkedGraphic(place, withLinks);
                }
            }
        }
        //alimentar escala
        findScale();
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return COL_NAMES.length;
    }

    @Override
    public String getColumnName(final int column) {
        return COL_NAMES[column];
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final TeethPlace place = list.get(rowIndex);
        String result = "";
        switch (columnIndex) {
            case N_COL:
                result = place.getPlace();
                break;
            case LENPIX_COL:
                if (place.getPlaceLength() > 0) {
                    result = dFormat.format(place.getPlaceLength());
                }
                break;
            case CENTER_COL:
                Point2D center = place.getCenter();
                if (center != null) {
                    result = "(" + dFormat.format(center.getX()) + ", "
                            + dFormat.format(center.getY()) + ")";
                }
                break;
            case FORCEVEC_COL:
                if (place.getForceVectorLen() > 0) {
                    result = dFormat.format(place.getForceVectorLen());
                }
                break;
            case MM_COL:
                if (place.getMmLength() > 0) {
                    result = dFormat.format(place.getMmLength());
                }
                break;
            default:
                result = "error";
        }
        return result;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        //System.out.println("testing cell editable: " + columnIndex);
        TeethPlace place = list.get(rowIndex);
        if (columnIndex == LENPIX_COL
                && !"mid".equalsIgnoreCase(place.getPlace())) {
            return true;
        } else if (columnIndex == CENTER_COL
                && !"mid".equalsIgnoreCase(place.getPlace())) {
            return true;
        } else if (columnIndex == MM_COL
                && !"mid".equalsIgnoreCase(place.getPlace())) {
            return true;
        }
        return false;
    }

    @Override
    public void setValueAt(final Object value, final int rowIndex,
            final int columnIndex) {

        final TeethPlace row = list.get(rowIndex);
        if (columnIndex == LENPIX_COL) {
            if (value instanceof Double) { //TODO used?
                row.setPlaceLength((Double) value);
            }

            if (value instanceof VectorGraphic) {
                final VectorGraphic vector = (VectorGraphic) value;
                row.setLinkedVector(vector, getScale());
                vector.setLinkedOwner(row.getPlace(), OrthoEventManager
                        .getInstance().getSelectedViewPane().getLayerModel());
                vector.addPropertyChangeListener(graphicsChangeListener);
                row.setPlaceLength(vector.getVectorLenght());
                //updateScale();
                maybeSetForce(row);
            }
        } else if (columnIndex == CENTER_COL) {
            if (value instanceof DotGraphic) {
                DotGraphic dot = (DotGraphic) value;
                row.setCenter(dot.getCenter());
                dot.setLinkedOwner(row.getPlace());
                dot.addPropertyChangeListener(graphicsChangeListener);
            }
        } else if (columnIndex == MM_COL) {
            if (value instanceof String) {
                try {
                    Double valueOf = Double.valueOf((String) value);
                    row.setMmLength(valueOf, getScale());
                    //updateScale();
                } catch (NumberFormatException ignore) {
                    //ignore
                }
            }
        }

        //avisa que os dados mudaram
        fireTableDataChanged();
    }

    /**
     * Find the arc on viewer. It has to be only one.
     *
     * Returns null if there is no arc, or if there is more than one.
     *
     * @param selView View to look for the arc on.
     * @return Only arc on view, or null
     */
    public static Graphic findOnlyArc(final OrthoView selView) {
        final AbstractLayer layer
                = selView.getLayerModel().getLayer(AbstractLayer.MEASURE);

        final List<Graphic> graphics = layer.getGraphics();
        int arcCount = 0;
        Graphic arc = null;
        for (Graphic graphic : graphics) {
            String uIName = graphic.getUIName();
            if (uIName.startsWith("Arc")) {
                if (arcCount <= 1) {
                    arc = graphic;
                    arcCount++;
                } else {
                    arc = null;
                }
            }
        }
        return arc;
    }

    /**
     * Get a TeethPlace by name.
     *
     * @param string name.
     * @return The TeethPlace with given name.
     */
    public TeethPlace get(final String string) {
        for (TeethPlace teethPlace : list) {
            if (teethPlace.getPlace().equalsIgnoreCase(string)) {
                return teethPlace;
            }
        }
        return null;
    }

    //****************************************
    // Scale
    /**
     * Sets lenght in pixels of the reference scale object.
     *
     * @param value Lenght to set
     */
    public void setScalePixelValue(final double value) {
        scalePixelValue = value;
    }

    /**
     * Sets lenght in milimiters of the reference scale object.
     *
     * @param value Lenght to set
     */
    public void setScaleMmValue(final double value) {
        scaleMmValue = value;
    }

    /**
     * @return Scale in use by this model,
     */
    public double getScale() {
        if (scaleMmValue > 0 && scalePixelValue > 0) {
            return scaleMmValue / scalePixelValue;
        }
        return 0;
    }

    /**
     * Finds scale based on teeth lenghts.
     */
    private void findScale() {
        for (TeethPlace place : list) {
            if (place.getPlaceLength() > 0 && place.getMmLength() > 0) {
                scaleMmValue += place.getMmLength();
                scalePixelValue += place.getPlaceLength();
            }
        }
    }

    //*******************************************
    public Point2D getMiddlePointOfLinePlace(String name) {
        TeethPlace get = get(name);
        if (get != null) {
            return get.getMiddleOfLinePoint();
        }
        return null;
    }

    protected void maybeSetForce(TeethPlace row) {
        //verifica se existem comprimento e centro para este place.

        if (row.getCenter() instanceof Point2D
                && row.getMiddleOfLinePoint() instanceof Point2D) {

            Line2D line = new Line2D.Double((Point2D) row.getCenter(),
                    (Point2D) row.getMiddleOfLinePoint());

            if (row.getForceLinkedVec() instanceof VectorGraphic) { //já tem um vetor
                //update
                ((VectorGraphic) row.getForceLinkedVec()).changeShape(line);
            } else {
                //na tabela:
                VectorGraphic force = new VectorGraphic(1, Color.black, true);
                force.createShape(line);
                row.setForceLinkedVec(force);
                //no viewer
                AbstractLayerModel layerModel = OrthoEventManager.getInstance()
                        .getSelectedViewPane().getLayerModel();
                AbstractLayer layer = layerModel.getLayer(
                        AbstractLayer.MEASURE);
                force.setLinkedOwner("force." + row.getPlace(), layerModel);
                force.addPropertyChangeListener(graphicsChangeListener);
                layer.addGraphic(force);
            }
        } else if (row.getForceLinkedVec() != null) {
            //tem um force mas center ou line foram removidos
            //precisa remover o force também:
            row.getForceLinkedVec().fireRemoveAction();
            row.setForceLinkedVec(null);
        }
    }

    /**
     * @return Force vectors list for main calculations.
     */
    public List<Line2D> getForceVectorList() {
        List<Line2D> forceList = new ArrayList<Line2D>();
        for (TeethPlace place : list) {
            Graphic linkedVec = place.getForceLinkedVec();
            if (linkedVec != null && linkedVec.getShape() instanceof Line2D) {
                forceList.add((Line2D) linkedVec.getShape());
            }
        }
        return forceList;
    }

    public GeneralPath getArc() {
        if (arcLink != null && arcLink.getShape() instanceof GeneralPath) {
            return (GeneralPath) arcLink.getShape();
        }
        return null;
    }

    public double getMidX() {
        return midPlace.getCenter().getX();
    }

    /**
     * Constroi vetor para sobrepor ao arco, se nao encontrar um mas tiver
     * comprimento e escala.
     *
     * @param place
     * @param initPoint
     * @return
     */
    private VectorGraphic getOrBuildLinkedVector(TeethPlace place,
            Point2D initPoint) {

        // try get...
        VectorGraphic linkedVector = null;
        Graphic linkedGraph = place.getLinkedVector();

        if (linkedGraph instanceof VectorGraphic) {
            linkedVector = (VectorGraphic) linkedGraph;
        } else {
            // try build
            double placeLength = place.getPlaceLength();
            if (placeLength > 0) { //build
                Line2D line = new Line2D.Double(initPoint,
                        new Point2D.Double(initPoint.getX(),
                                initPoint.getY() + placeLength));
                linkedVector = new VectorGraphic(1, Color.blue, true);
                linkedVector.createShape(line);
                //adiciona ao viewer:
                AbstractLayerModel layerModel = OrthoEventManager.getInstance()
                        .getSelectedViewPane().getLayerModel();
                AbstractLayer layer = layerModel.getLayer(
                        AbstractLayer.MEASURE);
                layer.addGraphic(linkedVector);
                //link to place:
                place.setLinkedVector(linkedVector, getScale());
                linkedVector.setLinkedOwner(place.getPlace(), layerModel);
                linkedVector.addPropertyChangeListener(graphicsChangeListener);
            }
        }
        return linkedVector;
    }

    public List getPlaceList() {
        return list;
    }

    /**
     * Look for links to add graphics to the model.
     *
     * Dont add ForceVectorLinks - they will be added when LinkedVector and
     * Point are present on each place.
     *
     * Also adds the propertie change listener to the graphics to be added.
     *
     * @param allGraphics A list of Graphics.
     * @return The list of graphics to be added to the model.
     */
    private List<PortableGraphic> lookForLinks(
            final List<Graphic> allGraphics) {
        final List<PortableGraphic> withLink = new ArrayList<PortableGraphic>();
        for (Graphic graphic : allGraphics) {
            if (graphic instanceof PortableGraphic) {
                final String linkedOwner
                        = ((PortableGraphic) graphic).getLinkedOwner();
                if (linkedOwner != null && !linkedOwner.startsWith("force")) {
                    withLink.add((PortableGraphic) graphic);
                    graphic.addPropertyChangeListener(graphicsChangeListener);
                }
            }
        }
        return withLink;
    }

    private void addLinkedGraphic(TeethPlace place, List<PortableGraphic> withLinks) {
        for (PortableGraphic graph : withLinks) {
            if (place.getPlace().equals(graph.getLinkedOwner())) {
                if (graph instanceof VectorGraphic) {
                    place.setLinkedVector(
                            (VectorGraphic) graph, getScale());
                } else if (graph instanceof DotGraphic) {
                    place.setCenter(
                            ((DotGraphic) graph).getCenter());
                }
                maybeSetForce(place);
            }
        }
    }

    /**
     * Remove the force link vector of a place. Deletes reference from place and
     * removes graphic from layer.
     *
     * @param place Place of reference for force vector to be removed.
     */
    private void removeForceVector(final TeethPlace place) {
        final Graphic forceLinkedVec = place.getForceLinkedVec();
        //deletar referencia no place
        place.setForceLinkedVec(null);
        //tirar o grafico do layer
        if (forceLinkedVec != null) {
            forceLinkedVec.fireRemoveAction();
        }
    }

    /**
     * Change listener for graphics and view changes. *
     */
    private PropertyChangeListener graphicsChangeListener
            = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("bounds".equalsIgnoreCase(evt.getPropertyName())) {
                        //System.out.println("source = " + evt.getSource().getClass());

                        if (evt.getSource() instanceof VectorGraphic
                        && evt.getNewValue() instanceof Line2D) {
                            Line2D line = (Line2D) evt.getNewValue();
                            String linkedOwner
                            = ((VectorGraphic) evt.getSource()).getLinkedOwner();

                            TeethPlace place = get(linkedOwner);
                            if (place != null) {
                                place.setPlaceLength(line.getP1().distance(line.getP2()));
                                fireTableDataChanged();
                                maybeSetForce(place);
                            }

                        } else if (evt.getSource() instanceof DotGraphic) {
                            TeethPlace place = get(((DotGraphic) evt.getSource())
                                    .getLinkedOwner());
                            if (place != null) {
                                place.setCenter(((DotGraphic) evt.getSource()).getCenter());
                                fireTableDataChanged();
                                maybeSetForce(place);
                            }
                        }
                    } else if ("remove".equalsIgnoreCase(evt.getPropertyName())) {
                        //remove graphic
                        final Object newValue = evt.getNewValue();
                        if (newValue instanceof VectorGraphic) {
                            final String linkedOwner = ((VectorGraphic) newValue)
                            .getLinkedOwner();
                            if (linkedOwner.startsWith("force")) {
                                final String name = linkedOwner.substring(
                                        linkedOwner.indexOf('.') + 1);
                                get(name).setForceLinkedVec(null);
                            } else {
                                final TeethPlace place = get(linkedOwner);
                                if (place != null) {
                                    place.setLinkedVector(null, getScale());
                                    removeForceVector(place);
                                }
                            }
                        } else if (newValue instanceof DotGraphic) {
                            final TeethPlace place = get(((DotGraphic) newValue)
                                    .getLinkedOwner());
                            if (place != null) {
                                place.setCenter(null);
                                removeForceVector(place);
                            }
                        }
                        fireTableDataChanged();
                    } else if ("midX".equals(evt.getPropertyName())
                    && evt.getNewValue() instanceof Double) {
                        midPlace.setCenter(
                                new Point2D.Double((Double) evt.getNewValue(), 0));
                        fireTableDataChanged();
                    }
                }
            };

    /* -----------------------------------------------------------
     * ----------  Transfer places routine -----------------------
     * ----------------------------------------------------------- */
    public void transferPlaces(OrthoView selView) throws TransferError {
        //confere se todos os comprimentos estao medidos e se o ponto médio
        //é conhecido

        //ponto médio:
        //passar para o ponto correspondente no arco:
        //arco:
        arcLink = findOnlyArc(selView);

        if (arcLink == null) {
            LOGGER.error(
                    "Arc must be on viewer, and needs to be only one.");
            throw new TransferError(
                    Messages.getString("TeethTableModel.ErrorArc"));
        } else {
            //inclui arco na tag para o caso de o usuário salvar e querer
            //recuperar depois.
            selView.getSeries().setTag(TagO.ARC, arcLink.getShape());
        }

        //Find center or arc:
        //para o arco e o arco mover para cima, o centro vai mover um pto para
        //baixo...
        Point2D center = midPlace.getCenter();
        Shape shape = arcLink.getShape();
        GeneralPath path = null;
        if (shape instanceof GeneralPath) {
            path = (GeneralPath) shape;
            if (path.contains(center)) {
                do {
                    center.setLocation(center.getX(), center.getY() - 1);
                } while (path.contains(center));

            } else {
                do {
                    center.setLocation(center.getX(), center.getY() + 1);
                } while (!path.contains(center));
            }
        }
        fireTableDataChanged();
        //esquerda
        //transfere(ou copia?) a linha de 11 para o ponto direito
        //ficar sobre o arco

        Point2D refPoint = center;
        boolean valid = true;
        String[] rigthList = getLefttList();
        for (String string : rigthList) {
            TeethPlace place = get(string);
            if (place != null) {
                //requisito do transfer:
                //do centro para fora, tem que começar no x1 e parar onde for,
                //mas sem pular nenhum.
                VectorGraphic link = getOrBuildLinkedVector(place, refPoint);
                if (link != null && valid) {
                    try {
                        refPoint = transferLeftPlaceLenght(place, refPoint, path);
                    } catch (ArithmeticException ex) {
                        LOGGER.error(ex.getMessage());
                        throw new TransferError(Messages.getString(
                                "TeethTableModel.ErrorNaN"));
                    }
                } else {
                    if (valid) {
                        valid = false;
                        //saber se foi uma falha..
                    } else if (link != null) {
                        throw new TransferError(Messages.getString(
                                "TeethTableModel.ErrorLines"));
                    }
                }
            }
        }

        //direita
        refPoint = center;
        valid = true;
        String[] leftList = getRightList();
        for (String string : leftList) {
            TeethPlace place = get(string);
            if (place != null) {

                //requisito do transfer:
                //do centro para fora, tem que começar no x1 e parar onde for,
                //mas sem pular nenhum.
                VectorGraphic link = getOrBuildLinkedVector(place, refPoint);
                if (link != null && valid) {
                    try {
                        refPoint = transferRightPlaceLenght(place, refPoint, path);
                    } catch (ArithmeticException ex) {
                        LOGGER.error(ex.getMessage());
                        throw new TransferError(Messages.getString(
                                "TeethTableModel.ErrorNaN"));
                    }
                } else {
                    if (valid) {
                        valid = false;
                        //saber se foi uma falha..
                    } else if (link != null) {
                        throw new TransferError(Messages.getString(
                                "TeethTableModel.ErrorLines"));
                    }
                }
            }
        }

        //repaint all viewer!
        selView.repaint();
    }

    private Point2D transferLeftPlaceLenght(TeethPlace place, Point2D refPoint,
            GeneralPath path) throws ArithmeticException {
        Shape shape1 = place.getLinkedVector().getShape();
        if (shape1 instanceof Line2D) {
            Line2D line11 = (Line2D) shape1;

            Point2D rgPoint = VectorMath.rightPoint(line11);

            //ponto direito:
            VectorMath.move(line11, refPoint.getX() - rgPoint.getX(),
                    refPoint.getY() - rgPoint.getY());

            //ponto esquerdo:
            if (place.getPlace().endsWith("1") || place.getPlace().endsWith("2")) {
                refPoint = placeLeftPointByY(line11, refPoint, path);
            } else {
                refPoint = placeLeftPointByX(line11, refPoint, path);
            }

            //mover graph, nao soh o shape
            ((VectorGraphic) place.getLinkedVector()).changeShape(line11);
            fireTableDataChanged();
        }
        return refPoint;
    }

    private Point2D transferRightPlaceLenght(
            TeethPlace place, Point2D refPoint, GeneralPath path)
            throws ArithmeticException {

        Shape shape1 = place.getLinkedVector().getShape();
        if (shape1 instanceof Line2D) {
            Line2D line11 = (Line2D) shape1;
            Point2D lfPoint = VectorMath.leftPoint(line11);

            //ponto esquerdo:
            VectorMath.move(line11, refPoint.getX() - lfPoint.getX(),
                    refPoint.getY() - lfPoint.getY());

            //ponto direito:
            if (place.getPlace().endsWith("1") || place.getPlace().endsWith("2")) {
                refPoint = placeRightPointByY(line11, refPoint, path);
            } else {
                refPoint = placeRightPointByX(line11, refPoint, path);
            }

            //quando ocorre NaN nos placeRight...
            if (refPoint == null) {
                fireTableDataChanged();
                return null;
            }

            ((VectorGraphic) place.getLinkedVector()).changeShape(line11);
            fireTableDataChanged();
        }
        return refPoint;
    }

    /**
     * Cover case 11, 12.
     *
     * @param line11
     * @param rgPoint
     * @param path
     */
    private Point2D placeLeftPointByY(Line2D line11, Point2D rgPoint,
            GeneralPath path) throws ArithmeticException {
        Point2D lfPoint = VectorMath.leftPoint(line11);
        //distance before
        double distance
                = line11.getP1().distance(line11.getP2());

        if (path.contains(lfPoint)) {
            do {
                //mudando leftPt
                //move left up to go out
                updatePointX(rgPoint, lfPoint, distance, -1);
            } while (path.contains(lfPoint));
        } else {
            do {
                //muda lfPoint
                //move left down to go inn
                updatePointX(rgPoint, lfPoint, distance, 1);
            } while (!path.contains(lfPoint));
        }
        line11.setLine(VectorMath.rightPoint(line11),
                lfPoint);
        return lfPoint;
    }

    //falta nan
    private Point2D placeRightPointByY(
            Line2D line11, Point2D refPoint, GeneralPath path)
            throws ArithmeticException {

        Point2D rgPoint = VectorMath.rightPoint(line11);
        //distance before
        double distance
                = line11.getP1().distance(line11.getP2());

        if (path.contains(rgPoint)) {
            do {
                //mudando rightPt
                //move left up to go out
                //updatePointX(refPoint, rgPoint, distance, 1);
                //mudando riPt
                double distY = (rgPoint.getY() - refPoint.getY()) - 1;

                double newXdist = VectorMath.circumferenceInverseCoorden(
                        distance, distY);
                //para a esquerda:
                double newLeftPtx = refPoint.getX() + newXdist;
                if (Double.isNaN(newLeftPtx)) {
                    throw new ArithmeticException(
                            "Nan error on placing right point by Y (going out).");
                }

                rgPoint.setLocation(newLeftPtx, refPoint.getY() + distY);

            } while (path.contains(rgPoint));
        } else {
            do {
                //muda lfPoint
                //move left down to go inn
                //updatePointX(refPoint, rgPoint, distance, -1);
                double distY = (rgPoint.getY() - refPoint.getY()) + 1;

                double newXdist = VectorMath.circumferenceInverseCoorden(
                        distance, distY);
                //para a esquerda:
                double newLeftPtx = refPoint.getX() + newXdist;
                if (Double.isNaN(newLeftPtx)) {
                    throw new ArithmeticException(
                            "Nan error on placing right point by Y (going in).");
                }

                rgPoint.setLocation(newLeftPtx, refPoint.getY() + distY);
            } while (!path.contains(rgPoint));
        }
        line11.setLine(VectorMath.leftPoint(line11),
                rgPoint);
        return rgPoint;
    }

    /**
     * Covers cases 13 +.
     *
     * @param line11
     * @param rgPoint
     * @param path
     * @return
     */
    private Point2D placeLeftPointByX(
            Line2D line11, Point2D rgPoint, GeneralPath path)
            throws ArithmeticException {

        //para 13+ o ponto esquerdo é o ponto mais baixo.
        Point2D lfPoint = VectorMath.lowerPoint(line11);
        //distance before
        double distance
                = line11.getP1().distance(line11.getP2());
        if (path.contains(lfPoint)) {
            do {
                //mudando leftPt
                //move left x left to go out
                uptadePointY(rgPoint, lfPoint, distance, -1);
            } while (path.contains(lfPoint));
        } else {
            do {
                //muda lfPoint
                //move left x right to go inn
                uptadePointY(rgPoint, lfPoint, distance, 1);
            } while (!path.contains(lfPoint));
        }
        line11.setLine(rgPoint, lfPoint);
        return lfPoint;
    }

    private Point2D placeRightPointByX(
            Line2D line11, Point2D refPoint, GeneralPath path)
            throws ArithmeticException {
        //para 13+ o ponto esquerdo é o ponto mais baixo.
        Point2D rgPoint = VectorMath.lowerPoint(line11);
        //distance before
        double distance
                = line11.getP1().distance(line11.getP2());
        if (path.contains(rgPoint)) {
            do {
                //mudando rightPt
                //move right x right to go out
                double distX = (rgPoint.getX() - refPoint.getX()) + 1;
                double newYdist = VectorMath.circumferenceInverseCoorden(
                        distance, distX);
                //abaixo do centro.
                double newLeftPty = refPoint.getY() + newYdist;
                if (Double.isNaN(newLeftPty)) {
                    throw new ArithmeticException(
                            "Nan error on placing right point by X (going out).");
                }

                rgPoint.setLocation(refPoint.getX() + distX, newLeftPty);

            } while (path.contains(rgPoint));
        } else {
            do {
                //muda rgPoint
                //move right x right to go inn
                double distX = (rgPoint.getX() - refPoint.getX()) - 1;
                double newYdist = VectorMath.circumferenceInverseCoorden(
                        distance, distX);
                //abaixo do centro.
                double newLeftPty = refPoint.getY() + newYdist;
                if (Double.isNaN(newLeftPty)) {
                    throw new ArithmeticException(
                            "Nan error on placing right point by X (going in).");
                }
                rgPoint.setLocation(refPoint.getX() + distX, newLeftPty);

            } while (!path.contains(rgPoint));
        }
        line11.setLine(refPoint, rgPoint);
        return rgPoint;
    }

    private void updatePointX(Point2D rgPoint, Point2D lfPoint,
            double distance, double move) throws ArithmeticException {

        //mudando leftPt
        double distY = (lfPoint.getY() - rgPoint.getY()) + move;

        double newXdist = VectorMath.circumferenceInverseCoorden(
                distance, distY);
        //para a esquerda:
        double newLeftPtx = rgPoint.getX() - newXdist;
        if (Double.isNaN(newLeftPtx)) {
            throw new ArithmeticException(
                    "Nan error on update point X.");
        }

        lfPoint.setLocation(newLeftPtx, rgPoint.getY() + distY);
    }

    private void uptadePointY(Point2D rgPoint, Point2D lfPoint,
            double distance, int move) throws ArithmeticException {
        double distX = (lfPoint.getX() - rgPoint.getX()) + move;

        double newYdist = VectorMath.circumferenceInverseCoorden(
                distance, distX);

        //abaixo do centro.
        double newLeftPty = rgPoint.getY() + newYdist;
        if (Double.isNaN(newLeftPty)) {
            throw new ArithmeticException(
                    "Nan error on update point Y.");
        }

        lfPoint.setLocation(rgPoint.getX() + distX, newLeftPty);
    }

    private String[] getLefttList() {
        String place = list.get(0).getPlace();
        if (place.startsWith("2")) {
            return TeethPlace.SUP_LEFT;
        } else {
            return TeethPlace.INF_LEFT;
        }
    }

    private String[] getRightList() {
        String place = list.get(0).getPlace();
        if (place.startsWith("2")) {
            return TeethPlace.SUP_RIGHT;
        } else {
            return TeethPlace.INF_RIGHT;
        }
    }

}
