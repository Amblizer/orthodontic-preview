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
package com.orthodonticpreview.ui.explorer;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import com.orthodonticpreview.datamodel.MediaSeriesGroupNodeSerial;
import com.orthodonticpreview.datamodel.OrthodonticModel;
import com.orthodonticpreview.datamodel.OrthoReport;
import com.orthodonticpreview.datamodel.TagO;
import com.orthodonticpreview.ui.OrthodonticWin;
import com.orthodonticpreview.ui.internal.Messages;
import com.orthodonticpreview.ui.persistence.PersistenceHandler;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.ui.docking.PluginTool;
import org.weasis.core.ui.docking.UIManager;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ViewerPlugin;

/**
 * Controls the view and manages open studies.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version
 */
public class OrthoExplorerView extends PluginTool implements DataExplorerView {

    /**
     * Class logger.
     */
    private static final Logger LOGGER
            = LoggerFactory.getLogger(OrthoExplorerView.class);

    /**
     * Name used by UIManager.
     */
    public static final String UI_NAME = "Preview";

    public static final String CALC_IMAGE = "Calculation Image";
    public static final String REPORT_IMAGE = "Report Image";

    /**
     * The data model.
     */
    private final OrthodonticModel dataModel;

    /**
     * Last used directory.
     */
    private File lastPersistencePath;

    /**
     * Combo with groups (study, patient) to select.
     */
    private GroupComboBox groupComboBox;
    /**
     * Shows steps and other information about selected group.
     */
    private WizzardPanel wizzPanel;

    public OrthoExplorerView() {
        super(UI_NAME, UI_NAME, POSITION.WEST, ExtendedMode.NORMALIZED,
                PluginTool.Type.EXPLORER, 10);
        setDockableWidth(250);
        dataModel = new OrthodonticModel();
        dockable.setMaximizable(true);

        iniGUI();
    }

    private void iniGUI() {
        loadPreferences();

        setLayout(new BorderLayout());
        add(getTopPanel(), BorderLayout.NORTH);

        wizzPanel = new WizzardPanel();
        add(new JScrollPane(wizzPanel));

        validate();
    }

    /* ** DataExplorerView implementation **************/
    @Override
    public void dispose() {
        savePreferences();
    }

    @Override
    public DataExplorerModel getDataExplorerModel() {
        return dataModel;
    }

    @Override
    public List<Action> getOpenImportDialogAction() {
        return null;
    }

    @Override
    public List<Action> getOpenExportDialogAction() {
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof ObservableEvent) {
            ObservableEvent obs = (ObservableEvent) evt;
            Object newValue = obs.getNewValue();
            if (ObservableEvent.BasicAction.Register.equals(
                    obs.getActionCommand())
                    && newValue instanceof PropertyChangeListener) {
                //Registra o viewer
                dataModel.addPropertyChangeListener(
                        (PropertyChangeListener) newValue);

                //update patient selection...
                wizzPanel.changePatient(
                        (MediaSeriesGroup) groupComboBox.getSelectedItem());
            } else if (ObservableEvent.BasicAction.Add.equals(
                    obs.getActionCommand())) {

                if (newValue instanceof MediaSeriesGroup) {
                    MediaSeriesGroup group = (MediaSeriesGroup) newValue;
                    if (group.getTagID().equals(TagW.PatientPseudoUID)) {
                        groupComboBox.addGroup(group);
                        wizzPanel.changePatient(group);
                    } else if (group.getTagID().equals(TagW.StudyInstanceUID)) {
                        groupComboBox.addGroup(group);
                        groupComboBox.removeGroup(dataModel.getParent(
                                group, OrthodonticModel.patient));
                        wizzPanel.changePatient(group);
                    } else {
                        wizzPanel.changePatient(dataModel.getParent(
                                group, OrthodonticModel.study));
                    }
                    if (group instanceof Series && group.containTagKey(TagW.SeriesOpen)) {
                        Object tagValue = group.getTagValue(TagW.SeriesOpen);
                        if (tagValue instanceof Boolean && (Boolean) tagValue) {
                            openInDefaultPlugin((Series) group);
                        }
                    }
                }
            } else if (ObservableEvent.BasicAction.Select.equals(
                    obs.getActionCommand())
                    && obs.getSource() instanceof ViewerPlugin
                    && newValue instanceof MediaSeriesGroup) {
                MediaSeriesGroup group = (MediaSeriesGroup) newValue;
                selectGroup(dataModel.getParent(
                        group, OrthodonticModel.study), false);

            } else if (ObservableEvent.BasicAction.Update.equals(
                    obs.getActionCommand())) {
                if (newValue instanceof MediaSeriesGroup) {
                    wizzPanel.changePatient((MediaSeriesGroup) groupComboBox.getSelectedItem());
                } else if (newValue instanceof Exception) {
                    JOptionPane.showMessageDialog(OrthodonticWin.getInstance().getFrame(),
                            Messages.getString("OrthoExplorerView.calcError")
                            + ((Exception) newValue).getMessage(),
                            Messages.getString("OrthoExplorerView.calcErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getUIName() {
        return UI_NAME;
    }

    @Override
    public String getDescription() {
        return "Orthodontic Preview Data Explorer";
    }

    /**
     * Finds the instance of this that is registered as Explorer Plugin service.
     *
     * @return the registered instance of this.
     */
    public static OrthoExplorerView getService() {
        final DataExplorerView explorerplugin = UIManager.getExplorerplugin(UI_NAME);
        if (explorerplugin instanceof OrthoExplorerView) {
            return (OrthoExplorerView) explorerplugin;
        }
        return null;
    }

    /**
     * Finds the viewer where the given series is open.
     *
     * @param serie Serie to look for.
     * @return The viewer where the given series is open, or null.
     */
    public ViewerPlugin getViewer(final MediaSeries serie) {
        synchronized (UIManager.VIEWER_PLUGINS) {
            for (int i = UIManager.VIEWER_PLUGINS.size() - 1; i >= 0; i--) {
                ViewerPlugin plugin = UIManager.VIEWER_PLUGINS.get(i);
                if (serie.equals(plugin.getGroupID())) {
                    return plugin;
                }
            }
        }
        return null;
    }

    /**
     * Create a new Ortho-preview (study) from patient id and name.
     *
     * @param patient 
     * @param role 
     * @return the created study.
     */
    public MediaSeriesGroup createOP(
            final MediaSeriesGroup patient, final String role) {

        //study (um preview)
        MediaSeriesGroup study = dataModel.createStudy(patient, role);
        LOGGER.debug("Registered Study: {}",
                study.getTagValue(TagW.StudyDate));

        return study;
    }

    /**
     * Builds a MediaSeries from a reference file (must be readable), and sets
     * basic tags.
     *
     * Tags setted: TagO.SERIE_ROLE SeriesDescription (same as SERIE_ROLE)
     * PatientName and PatientName (copy from Study). SeriesDate (hoje)
     *
     * @param fileName file address.
     * @param study study that will own the serie
     * @param seriesRole series role (see TagO.SERIE_ROLE).
     * @return
     */
    public MediaSeries addImage(String fileName,
            MediaSeriesGroup study, String seriesRole) {

        File file = new File(fileName);
        MediaSeries serie;
        LOGGER.debug("Criating series from " + fileName);
        if (!file.isDirectory() && file.canRead()) {
            MediaReader media = ViewerPluginBuilder.getMedia(file);
            if (media != null) {
                serie = media.getMediaSeries();
                serie.setTag(TagO.SERIE_ROLE, seriesRole);

                serie.setTag(TagW.SeriesDescription, seriesRole);
                serie.setTag(TagW.PatientName,
                        study.getTagValue(TagW.PatientName));
                serie.setTag(TagW.PatientID, study.getTagValue(TagW.PatientID));
                serie.setTag(TagW.StudyDescription,
                        study.getTagValue(TagW.StudyDescription));
                serie.setTag(TagW.StudyInstanceUID,
                        study.getTagValue(TagW.StudyInstanceUID));
                serie.setTag(TagO.STUDY_ROLE,
                        study.getTagValue(TagO.STUDY_ROLE));
                serie.setTag(TagW.SeriesDate, Calendar.getInstance().getTime());

                //para o builder:
                serie.setTag(TagW.FilePath, file);

                dataModel.addHierarchyNode(study, serie);

                return serie;

            }
        }

        return null;

    }

    /**
     * Builds a report for the results of calculation on given series.
     *
     * @param serie Serie (must have calculation done).
     * @return a series that represents the report.
     * @throws java.io.IOException
     */
    public MediaSeries buildReport(Series serie) throws IOException {

        Integer rotation = 0;
        Boolean flip = false;

        if (serie != null) {
            OrthoReport orthoReport = new OrthoReport(serie);
            ViewerPlugin viewer = getViewer(serie);

            if (viewer instanceof ImageViewerPlugin) {
                ImageViewerPlugin plugin = (ImageViewerPlugin) viewer;
                Object get = plugin.getImagePanels().get(0);
                if (get instanceof DefaultView2d) {
                    Object actionValue = ((DefaultView2d) get).getActionValue(
                            ActionW.ROTATION.cmd());

                    if (actionValue instanceof Integer) {
                        rotation = (Integer) actionValue;
                    }
                    Object flipValue = ((DefaultView2d) get).getActionValue(
                            ActionW.FLIP.cmd());
                    if (flipValue instanceof Boolean) {
                        flip = (Boolean) flipValue;
                    }
                }
            }

            File file = orthoReport.createImageReport(rotation, flip);
            MediaSeriesGroup parent = dataModel.getParent(
                    serie, OrthodonticModel.study);
            try {
                MediaSeries reportSerie = addImage(file.getCanonicalPath(),
                        parent, REPORT_IMAGE);
                orthoReport.setReportTags(reportSerie);

                return reportSerie;
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }

        }
        return null;
    }

    /**
     * Open media on a viewer.
     *
     * @param factory viewer factory to use.
     * @param series media to open.
     */
    public void openMedia(SeriesViewerFactory factory, MediaSeries series) {
        ViewerPlugin view = (ViewerPlugin) factory.createSeriesViewer(null);
        if (view != null) {
            view.setGroupID(series);

            dataModel.firePropertyChange(new ObservableEvent(
                    ObservableEvent.BasicAction.Register,
                    dataModel, null, view));

            //important to be after the register event fire.
            view.setSelectedAndGetFocus();
            view.addSeries(series);
        }
    }

    public MediaSeries getRoleSerie(MediaSeriesGroup group, String role) {
        //paciente?
        if (group.getTagID().equals(TagW.PatientPseudoUID)) {
            for (MediaSeriesGroup study : dataModel.getChildren(group)) {
                for (MediaSeriesGroup serie : dataModel.getChildren(study)) {
                    if (role.equalsIgnoreCase((String) serie.getTagValue(TagW.SeriesDescription))) {
                        return (MediaSeries) serie;
                    }
                }
            }
        } else if (group.getTagID().equals(TagW.StudyInstanceUID)) {
            for (MediaSeriesGroup serie : dataModel.getChildren(group)) {
                if (role.equalsIgnoreCase((String) serie.getTagValue(TagW.SeriesDescription))) {
                    return (MediaSeries) serie;
                }
            }
        }
        return null;
    }

    public MediaSeries getImageSerie(MediaSeriesGroup group) {
        return getRoleSerie(group, CALC_IMAGE);
    }

    public MediaSeries getReportSerie(MediaSeriesGroup group) {
        return getRoleSerie(group, REPORT_IMAGE);
    }

    public void selectPatient(MediaSeriesGroup patient) {
        selectGroup(patient, true);
    }

    public void selectGroup(MediaSeriesGroup patient, boolean fireChange) {
        MediaSeriesGroup selected
                = (MediaSeriesGroup) groupComboBox.getSelectedItem();

        if (patient != null && selected != null && !selected.equals(patient)) {
            groupComboBox.setSelectedItem(patient);
            if (fireChange) {
                dataModel.firePropertyChange(new ObservableEvent(
                        ObservableEvent.BasicAction.Select,
                        dataModel, null, patient));
            }
        } else {
            wizzPanel.changePatient(patient);
            if (fireChange) {
                dataModel.firePropertyChange(new ObservableEvent(
                        ObservableEvent.BasicAction.Select,
                        dataModel, null, patient));
            }
        }
    }

    private JPanel getTopPanel() {
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        final JLabel pLabel = new JLabel(
                Messages.getString("OrhtoExplorerView.view"));
        pLabel.setFont(FontTools.getFont12());
        topPanel.add(pLabel);

        groupComboBox = new GroupComboBox(this);
        groupComboBox.setMaximumRowCount(15);
        groupComboBox.setFont(FontTools.getFont11());
        topPanel.add(groupComboBox);

        return topPanel;
    }

    public void computeOP(MediaSeriesGroup groupID) {
        dataModel.computeOP(groupID);
    }

    /**
     * Open a patient (prompt user for directory).
     */
    public void openPatient() {
        lastPersistencePath = new PersistenceHandler(
                dataModel, lastPersistencePath).openPatient();
    }

    /**
     * Save the selected patient to a directory.
     *
     * If choseFile = false, tryes to use the directory held on TagW.FilePath to
     * save. If its not possible, prompts the user for a new one.
     *
     * @param choseFile true to prompt user for file.
     */
    public void saveSelectedPatient(boolean choseFile) {
        MediaSeriesGroup selectedItem
                = (MediaSeriesGroup) groupComboBox.getSelectedItem();
        if (selectedItem != null) {
            MediaSeriesGroup parent
                    = dataModel.getParent(selectedItem, OrthodonticModel.patient);

            lastPersistencePath = new PersistenceHandler(
                    dataModel, lastPersistencePath).savePatient(
                            (MediaSeriesGroupNodeSerial) parent,
                            choseFile);
        }
    }

    public void closeSelectedPatient() {
        MediaSeriesGroup selectedItem
                = (MediaSeriesGroup) groupComboBox.getSelectedItem();
        if (selectedItem != null) {
            MediaSeriesGroup parent
                    = dataModel.getParent(selectedItem, OrthodonticModel.patient);

            //remove from ExplorerView
            groupComboBox.removeGroup(parent);
            Collection<MediaSeriesGroup> children = dataModel.getChildren(parent);
            for (MediaSeriesGroup mediaSeriesGroup : children) {
                groupComboBox.removeGroup(mediaSeriesGroup);
            }
            if (groupComboBox.getItemCount() == 0) {
                wizzPanel.changePatient(null);
            } else {
                groupComboBox.setSelectedIndex(0);
            }

            //remove from model
            dataModel.deletePatient(parent);
        }

    }

    public void openInDefaultPlugin(MediaSeries serie) {
        if (serie != null) {
            synchronized (UIManager.SERIES_VIEWER_FACTORIES) {
                final List<SeriesViewerFactory> viewerPlugins
                        = UIManager.SERIES_VIEWER_FACTORIES;
                for (final SeriesViewerFactory factory : viewerPlugins) {
                    if (factory.canReadMimeType(serie.getMimeType())
                            && factory.getLevel() == 4) {
                        openMedia(factory, serie);
                    }
                }
            }
        }
    }

    /**
     * Save prefs to preferences xml file.
     */
    private void savePreferences() {
        final Preferences prefs = BundlePreferences.getDefaultPreferences(FrameworkUtil.getBundle(this.getClass()).getBundleContext());
        if (prefs != null && lastPersistencePath != null) {
            prefs.put("last.patdir", lastPersistencePath.getAbsolutePath());
        }
    }

    /**
     * Loads prefs from xml file.
     */
    private void loadPreferences() {
        final Preferences prefs = BundlePreferences.getDefaultPreferences(FrameworkUtil.getBundle(this.getClass()).getBundleContext());
        if (prefs != null) {
            final String get = prefs.get("last.patdir", null);
            if (get != null) {
                lastPersistencePath = new File(get);
            }
        }
    }

    public MediaSeriesGroup createPatient(String id, String patientName,
            String age, String sex) {
        MediaSeriesGroup patient = dataModel.createPatient(id, patientName);
        patient.setTagNoNull(TagO.PATIENT_AGE, age);
        patient.setTagNoNull(TagW.PatientSex, sex);

        LOGGER.debug("Registered patient: {}",
                patient.getTagValue(TagW.PatientName));

        return patient;
    }

    /**
     * Creates a new series to replace first, and then deletes the first.
     *
     * @param imageSerie Series to replace.
     * @param newPath Path of new image.
     * @return A new series to replace the given one.
     */
    public MediaSeries changeImage(MediaSeries imageSerie, String newPath) {

        MediaSeriesGroup parent = dataModel.getParent(
                imageSerie, OrthodonticModel.study);
        String tagValue = (String) imageSerie.getTagValue(TagO.SERIE_ROLE);
        ViewerPlugin viewer = getViewer(imageSerie);

        dataModel.removeSeries(imageSerie);

        //Se precisar fechar viewers:
        //ver WeasisWin: 291.
        MediaSeries newSerie = addImage(newPath, parent, tagValue);
        if (viewer != null) {
            viewer.addSeries(newSerie);
            viewer.setGroupID(newSerie);
            viewer.setSelectedAndGetFocus();
        } else {
            openInDefaultPlugin(newSerie);
        }

        imageSerie.dispose();
        return newSerie;
    }

    @Override
    protected void changeToolWindowAnchor(CLocation cl) {
    }

    @Override
    public void importFiles(File[] files, boolean bln) {
    }

    @Override
    public boolean canImportFiles() {
        return false;
    }

}
