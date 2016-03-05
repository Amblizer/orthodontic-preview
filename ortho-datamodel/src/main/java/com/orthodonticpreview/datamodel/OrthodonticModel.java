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
package com.orthodonticpreview.datamodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.explorer.model.Tree;
import org.weasis.core.api.explorer.model.TreeModel;
import org.weasis.core.api.explorer.model.TreeModelNode;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagW;

/**
 * The data-model class.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 19 Jul.
 */
public class OrthodonticModel implements TreeModel, DataExplorerModel {

    /**
     * Class logger.
     */
    private static final Logger LOGGER
            = LoggerFactory.getLogger(OrthodonticModel.class);

    /**
     * support for propertie change.
     */
    private PropertyChangeSupport propertyChange;

    /**
     * Tree model that stores data.
     */
    private final Tree<MediaSeriesGroup> model;

    public static final TreeModelNode patient
            = new TreeModelNode(1, 0, TagW.PatientPseudoUID);
    public static final TreeModelNode study
            = new TreeModelNode(2, 0, TagW.StudyInstanceUID);
    public static final TreeModelNode series
            = new TreeModelNode(3, 0, TagW.SubseriesInstanceUID);

    /**
     * Creates a new OrthodonticModel.
     */
    public OrthodonticModel() {
        model = new Tree<MediaSeriesGroup>(rootNode);
    }

    @Override
    public List<Codec> getCodecPlugins() {
        return null;
    }

    @Override
    public void addPropertyChangeListener(
            final PropertyChangeListener listener) {
        if (propertyChange == null) {
            propertyChange = new PropertyChangeSupport(this);
        }
        propertyChange.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(
            final PropertyChangeListener listener) {
        if (propertyChange != null) {
            propertyChange.removePropertyChangeListener(listener);
        }
    }

    @Override
    public void firePropertyChange(final ObservableEvent event) {
        if (propertyChange != null) {
            if (SwingUtilities.isEventDispatchThread()) {
                propertyChange.firePropertyChange(event);
            } else {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        propertyChange.firePropertyChange(event);
                    }
                });
            }
        }
    }

    @Override
    public TreeModelNode getTreeModelNodeForNewPlugin() {
        return null;
    }

    @Override
    public boolean applySplittingRules(Series original, MediaElement media) {
        return false;
    }

    /**
     * Creates a patient, sets name and id and add it to model.
     *
     * @param id patient ID (also used as PatientPseudoUID).
     * @param name patient name.
     * @return the registered patient object.
     */
    public MediaSeriesGroup createPatient(final String id, final String name) {
        final MediaSeriesGroup patient = new MediaSeriesGroupNodeSerial(
                TagW.PatientPseudoUID, id, TagW.PatientName);
        patient.setTag(TagW.PatientID, id);
        patient.setTag(TagW.PatientName, name);
        addHierarchyNode(TreeModel.rootNode, patient);

        return patient;
    }

    /**
     * Creates a Study for given patient and registers it to model.
     *
     * Tags filled: StudyInstanceUID (created). StudyDate (today). PatientID and
     * PatientName (copied form patiente). StudyDescription ("Ortodontic
     * Preview").
     *
     * @param patient patient to own the study.
     * @return the study object registered.
     */
    public MediaSeriesGroup createStudy(final MediaSeriesGroup patient,
            final String role) {

        final String studyInstUID
                = (String) patient.getTagValue(TagW.PatientID)
                + System.currentTimeMillis();
        LOGGER.debug("Creating studyInstUID = " + studyInstUID);
        final MediaSeriesGroup study = new MediaSeriesGroupNodeSerial(
                TagW.StudyInstanceUID, studyInstUID, TagW.StudyDescription);

        //copy patient tags
        study.setTag(TagW.PatientID, (String) patient.getTagValue(
                TagW.PatientID));
        study.setTag(TagW.PatientName, (String) patient.getTagValue(
                TagW.PatientName));

        //study tags
        study.setTag(TagO.STUDY_ROLE, role);
        study.setTag(TagW.StudyDescription, "Orthodontic Preview");
        LOGGER.debug("Setting study date to "
                + TagW.formatDate(Calendar.getInstance().getTime()));
        study.setTag(TagW.StudyDate, Calendar.getInstance().getTime());
        addHierarchyNode(patient, study);

        return study;
    }

    //** TreeModel implementation */
    @Override
    public List<TreeModelNode> getModelStructure() {
        return null;
    }

    @Override
    public Collection<MediaSeriesGroup> getChildren(
            final MediaSeriesGroup node) {
        return model.getSuccessors(node);
    }

    @Override
    public MediaSeriesGroup getHierarchyNode(final MediaSeriesGroup parent,
            final Object value) {
        if (parent != null || value != null) {
            LOGGER.debug("Looking for value: " + value.toString());
            synchronized (model) {
                for (MediaSeriesGroup node : model.getSuccessors(parent)) {
                    LOGGER.debug("Found value: " + node.toString());
                    if (node.equals(value)) {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void addHierarchyNode(final MediaSeriesGroup root,
            final MediaSeriesGroup leaf) {
        synchronized (model) {
            LOGGER.debug("Adding to model: " + leaf);
            model.addLeaf(root, leaf);
            firePropertyChange(new ObservableEvent(
                    ObservableEvent.BasicAction.Add, this, null, leaf));
        }
    }

    @Override
    public void removeHierarchyNode(final MediaSeriesGroup root,
            final MediaSeriesGroup leaf) {
        synchronized (model) {
            final Tree<MediaSeriesGroup> tree = model.getTree(root);
            if (tree != null) {
                tree.removeLeaf(leaf);
            }
        }
    }

    /**
     * Removes given serie.
     *
     * @param oneSerie Serie to be removed.
     */
    public void removeSeries(final MediaSeriesGroup oneSerie) {
        if (oneSerie != null) {

            firePropertyChange(new ObservableEvent(
                    ObservableEvent.BasicAction.Remove, this, null, oneSerie));
            // remove in the data model
            final MediaSeriesGroup studyGroup = getParent(oneSerie, study);
            removeHierarchyNode(studyGroup, oneSerie);
            LOGGER.info("Remove Series: {}", oneSerie);
            oneSerie.dispose();
        }
    }

    @Override
    public MediaSeriesGroup getParent(final MediaSeriesGroup node,
            final TreeModelNode modelNode) {

        if (null != node && modelNode != null) {
            if (node.getTagID().equals(modelNode.getTagElement())) {
                return node;
            }
            synchronized (model) {
                Tree<MediaSeriesGroup> tree = model.getTree(node);
                if (tree != null) {
                    Tree<MediaSeriesGroup> parent = null;
                    while ((parent = tree.getParent()) != null) {
                        if (parent.getHead().getTagID().equals(
                                modelNode.getTagElement())) {
                            return parent.getHead();
                        }
                        tree = parent;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Calculates the preview parameters for the given serie.
     *
     * If the series does not have a TagO.FORCE_VECTOR_UNITS setted properly,
     * the calculation isn`t made.
     *
     * Tags with results will ge setted on groupID.
     *
     * @param groupID serie to use for calculation.
     */
    public void computeOP(final MediaSeriesGroup groupID) {

        try {
            final PreviewCalculation calc = new PreviewCalculation(
                    new MediaSeriesCalculationModel(groupID));
            calc.storeResults();
            firePropertyChange(new ObservableEvent(
                    ObservableEvent.BasicAction.Update, groupID, null, null));

        } catch (IllegalArgumentException ex) {
            LOGGER.info("Previw cannot be calculated: " + ex);
            firePropertyChange(new ObservableEvent(
                    ObservableEvent.BasicAction.Update, groupID, null, ex));

        }

    }

    /**
     * Finds a patient with the given ID.
     *
     * (Usado na construçao do relatorio.)
     *
     * @param patID The ID.
     * @return The patient.
     */
    public MediaSeriesGroup getPatientById(final String patID) {
        final Collection<MediaSeriesGroup> successors
                = model.getSuccessors(model.getHead());
        for (MediaSeriesGroup patient : successors) {
            String tagValue = (String) patient.getTagValue(TagW.PatientID);
            if (patID.equalsIgnoreCase(tagValue)) {
                return patient;
            }
        }
        return null;
    }

    /**
     * Deletes a given patient from model.
     *
     * @param patient Patient to be removed.
     */
    public void deletePatient(final MediaSeriesGroup patient) {
        firePropertyChange(new ObservableEvent(
                ObservableEvent.BasicAction.Remove, this, null, patient));

        removeHierarchyNode(rootNode, patient);
        patient.dispose();
    }
}
