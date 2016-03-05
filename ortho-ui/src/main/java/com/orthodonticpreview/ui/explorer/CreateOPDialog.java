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

import com.orthodonticpreview.datamodel.OrthodonticModel;
import com.orthodonticpreview.datamodel.TagO;
import com.orthodonticpreview.ui.OrthodonticWin;
import com.orthodonticpreview.ui.internal.Messages;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.Preferences;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.FileFormatFilter;
import org.weasis.core.api.gui.util.JMVUtils;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.service.BundlePreferences;

/**
 * Dialog to create a new patient (& study, & serie ...).
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version
 */
public class CreateOPDialog extends JDialog {

    private static final Dimension DIALOG_SIZE = new Dimension(500, 300);
    private static final int TX_FIELD_SIXE = 300;

    private static final String MAXILLA = "Maxilla";
    private static final String MANDIBLE = "Mandible";

    /**
     * Field for Maxilla calc serie image.
     */
    private JTextField maxImgField;
    /**
     * Field for Mandible calc serie image.
     */
    private JTextField mandImgField;
    /**
     * Field for patient id.
     */
    private JTextField idTextArea;
    /**
     * Field for patient name.
     */
    private JTextField nameTextArea;
    /**
     * Field for patient age.
     */
    private JTextField ageTextArea;
    /**
     * Field for patient sex.
     */
    private JTextField sexTextArea;

    /**
     * Last directory used to find an image.
     */
    private String lastDir;

    /**
     * Next id (increments each time is used).
     */
    private String nextId;

    /**
     * Group to edit (null if creating new).
     */
    private MediaSeriesGroup editingGroup;

    /**
     * Creates this dialog to create a new patient.
     *
     * @param wParent Parent window.
     */
    public CreateOPDialog(final Window wParent) {
        super(wParent);

        setTitle(Messages.getString("CreateOPDialog.createTitle"));
        loadCreatePrefs();
        initGUI();
    }

    /**
     * Creates this dialog to edit a patient.
     *
     * @param wParent Parent window.
     * @param editGroup Group to edit.
     */
    public CreateOPDialog(final OrthodonticWin wParent,
            final MediaSeriesGroup editGroup) {
        this(wParent.getFrame());

        setTitle(Messages.getString("CreateOPDialog.editTitle"));
        editingGroup = editGroup;
        populate(editGroup);
    }

    /**
     * Inits user interface.
     */
    private void initGUI() {
        setLayout(new GridBagLayout());

        int line = 0;
        final GridBagConstraints lblCon = new GridBagConstraints();
        lblCon.anchor = GridBagConstraints.WEST;
        lblCon.insets = new Insets(15, 15, 5, 5);
        lblCon.gridx = 0;
        lblCon.gridy = line;
        add(new JLabel(Messages.getString("CreateOPDialog.ID")), lblCon);

        final GridBagConstraints inpCon = new GridBagConstraints();
        inpCon.anchor = GridBagConstraints.WEST;
        inpCon.insets = new Insets(15, 5, 5, 5);
        inpCon.gridx = 1;
        inpCon.gridy = line;
        inpCon.gridwidth = 3;
        idTextArea = new JTextField();
        idTextArea.setText(nextId);
        JMVUtils.setPreferredWidth(idTextArea, TX_FIELD_SIXE, TX_FIELD_SIXE);
        add(idTextArea, inpCon);

        lblCon.gridy = inpCon.gridy = ++line;
        lblCon.insets = new Insets(5, 15, 5, 5);
        inpCon.insets = new Insets(5, 5, 5, 5);
        inpCon.gridwidth = 3;
        add(new JLabel(Messages.getString("CreateOPDialog.Name")), lblCon);
        nameTextArea = new JTextField();
        JMVUtils.setPreferredWidth(nameTextArea, TX_FIELD_SIXE, TX_FIELD_SIXE);
        add(nameTextArea, inpCon);

        lblCon.gridy = inpCon.gridy = ++line;
        inpCon.gridwidth = 1;
        add(new JLabel(Messages.getString("CreateOPDialog.Age")), lblCon);
        ageTextArea = new JTextField();
        JMVUtils.setPreferredWidth(ageTextArea, 80, 80);
        add(ageTextArea, inpCon);

        lblCon.gridx = 2;
        add(new JLabel(Messages.getString("CreateOPDialog.Sex")), lblCon);
        inpCon.gridx = 3;
        inpCon.gridwidth = 1;
        sexTextArea = new JTextField();
        JMVUtils.setPreferredWidth(sexTextArea, 100, 100);
        add(sexTextArea, inpCon);

        lblCon.gridy = ++line;
        lblCon.gridx = 0;
        lblCon.gridwidth = 5;
        maxImgField = new JTextField();
        JMVUtils.setPreferredWidth(maxImgField, TX_FIELD_SIXE, TX_FIELD_SIXE);
        add(new ImagePanel(
                maxImgField, Messages.getString("CreateOPDialog.MaxImage")),
                lblCon);

        lblCon.gridy = ++line;
        mandImgField = new JTextField();
        JMVUtils.setPreferredWidth(mandImgField, TX_FIELD_SIXE, TX_FIELD_SIXE);
        add(new ImagePanel(
                mandImgField, Messages.getString("CreateOPDialog.ManImage")),
                lblCon);

        inpCon.gridx = 1;
        lblCon.gridy = inpCon.gridy = ++line;
        inpCon.insets = new Insets(5, 5, 15, 5);
        JButton okButton = new JButton(Messages.getString("CreateOPDialog.OK"));
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!"".equals(idTextArea.getText())
                        && !"".equals(nameTextArea.getText())) {
                    if (editingGroup == null) {
                        createPreview();
                    } else {
                        editGroup(editingGroup);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(CreateOPDialog.this,
                            Messages.getString("CreateOPDialog.errorNoID"),
                            getTitle(), JOptionPane.ERROR_MESSAGE);
                }
            }

        });
        add(okButton, inpCon);

        inpCon.gridx = 3;
        final JButton cancelButton = new JButton(
                Messages.getString("CreateOPDialog.Cancel"));
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        add(cancelButton, inpCon);

        pack();
    }

    /**
     * Get actual path, or last used directory.
     *
     * @return path.
     */
    private String getImportPath() {
        String path = maxImgField.getText().trim();
        if (path != null && !path.equals("")) {
            return path;
        }
        return lastDir;
    }

    /**
     * Save last used directory and ID.
     *
     * @param dir last directory.
     * @param nextIntId next ID to use.
     */
    private void saveCreatePrefs(final String dir, final int nextIntId) {
        final Preferences prefs = BundlePreferences.getDefaultPreferences(FrameworkUtil.getBundle(this.getClass()).getBundleContext());
        if (prefs != null) {
            final Preferences node = prefs.node("create");
            if (dir != null) {
                node.put("lastDir", dir);
            }
            node.putInt("nextId", nextIntId);
        }
    }

    /**
     * Load directory and next ID from preference file.
     */
    private void loadCreatePrefs() {
        final Preferences prefs = BundlePreferences.getDefaultPreferences(FrameworkUtil.getBundle(this.getClass()).getBundleContext());
        if (prefs != null) {
            final Preferences node = prefs.node("create");
            lastDir = node.get("lastDir", null);
            final int aInt = node.getInt("nextId", 1);
            nextId = String.format("%05d", aInt);
        }
    }

    /**
     * Creates a new patient, studies and series with dialog information.
     *
     * Studys and series are created only ir there is paths for its images.
     */
    private void createPreview() {
        String id = idTextArea.getText();
        final MediaSeriesGroup patient = OrthoExplorerView.getService()
                .createPatient(
                        id, nameTextArea.getText(), ageTextArea.getText(),
                        sexTextArea.getText());

        createAndOpenStudy(patient, MAXILLA, maxImgField.getText());
        createAndOpenStudy(patient, MANDIBLE, mandImgField.getText());

        int nextIntId;
        try {
            nextIntId = Integer.parseInt(id) + 1;
        } catch (NumberFormatException ex) {
            nextIntId = 1;
        }
        saveCreatePrefs(lastDir, nextIntId);

    }

    /**
     * Edit the patient with dialog information.
     *
     * @param editingGroup group to change.
     */
    private void editGroup(final MediaSeriesGroup editingGroup) {
        editPatientData(editingGroup, TagW.PatientID, idTextArea.getText());
        editPatientData(editingGroup, TagW.PatientName, nameTextArea.getText());
        editPatientData(editingGroup, TagW.PatientSex, sexTextArea.getText());
        editPatientData(editingGroup, TagO.PATIENT_AGE, ageTextArea.getText());

        //update combo after change name
        OrthoExplorerView.getService().getDataExplorerModel()
                .firePropertyChange(new ObservableEvent(
                                ObservableEvent.BasicAction.Update, this, null, editingGroup));

        //// imagens:
        editOrCreateImage(MAXILLA, maxImgField);
        editOrCreateImage(MANDIBLE, mandImgField);
    }

    /**
     * Populate fiels with data from patient to edit.
     *
     * @param editGroup patient to get data from.
     */
    private void populate(final MediaSeriesGroup editGroup) {
        idTextArea.setText((String) editGroup.getTagValue(TagW.PatientID));
        nameTextArea.setText((String) editGroup.getTagValue(TagW.PatientName));
        sexTextArea.setText((String) editGroup.getTagValue(TagW.PatientSex));
        ageTextArea.setText((String) editGroup.getTagValue(TagO.PATIENT_AGE));

        final OrthodonticModel model = (OrthodonticModel) OrthoExplorerView.getService().getDataExplorerModel();
        Collection<MediaSeriesGroup> children = model.getChildren(editGroup);
        if (!children.isEmpty()) {
            for (Iterator<MediaSeriesGroup> it = children.iterator();
                    it.hasNext();) {
                final MediaSeriesGroup study = it.next();
                final String stRole
                        = (String) study.getTagValue(TagO.STUDY_ROLE);
                if (MAXILLA.equals(stRole)) {
                    maxImgField.setText(getImageStudyPath(study));
                } else if (MANDIBLE.equals(stRole)) {
                    mandImgField.setText(getImageStudyPath(study));
                }
            }
        }
    }

    /**
     * Get path for the CALC_IMAGE on given study.
     *
     * @param study given study.
     * @return path for CALC_IMAGE.
     */
    private String getImageStudyPath(final MediaSeriesGroup study) {
        final MediaSeries imageSerie
                = OrthoExplorerView.getService().getImageSerie(study);
        if (imageSerie != null) {
            return imageSerie.getTagValue(TagW.FilePath).toString();
        }
        return null;
    }

    /**
     * Edits or creates study and series if the user has changed or added image
     * path.
     *
     * @param role Study role.
     * @param imgField Image field.
     */
    private void editOrCreateImage(
            final String role, final JTextField imgField) {
        OrthoExplorerView explorer = OrthoExplorerView.getService();
        //o paciente já tinha este grupo?
        final OrthodonticModel model = (OrthodonticModel) explorer.getDataExplorerModel();
        final Collection<MediaSeriesGroup> children
                = model.getChildren(editingGroup);

        MediaSeriesGroup roleStudy = null;
        if (!children.isEmpty()) {
            for (Iterator<MediaSeriesGroup> it = children.iterator();
                    it.hasNext();) {
                final MediaSeriesGroup study = it.next();
                final String stRole
                        = (String) study.getTagValue(TagO.STUDY_ROLE);
                if (role.equals(stRole)) {
                    roleStudy = study;
                }
            }
        }

        if (roleStudy == null) { //nao tem, criar
            createAndOpenStudy(editingGroup, role, imgField.getText());
        } else { //já tem:
            //é igual? (está sendo editada?)
            final String newPath = imgField.getText();
            final MediaSeries imageSerie = explorer.getImageSerie(roleStudy);
            if (imageSerie == null) {
                //tem estudo mas nao tem a serie
                //(acontece se houve um erro na tentativa anterior)
                final MediaSeries serie = explorer.addImage(
                        newPath, roleStudy, OrthoExplorerView.CALC_IMAGE);
                explorer.openInDefaultPlugin(serie);
            } else {
                maybeReplaceSerie(imageSerie, newPath);
            }
        }
    }

    /**
     * Replaces series with one created after new Path.
     *
     * If series is replaced, all graphics are going to be lost, so asks the
     * user first.
     *
     * @param imageSerie Series to replace.
     * @param newPath New path for image.
     */
    private void maybeReplaceSerie(MediaSeries imageSerie, String newPath) {
        final Object tagValue = imageSerie.getTagValue(TagW.FilePath);
        if (tagValue instanceof File
                && !AbstractWizzardStep.isBlankOrNull(newPath)) {
            if (!newPath.equals(tagValue.toString())) {
                int ans = JOptionPane.showConfirmDialog(this,
                        Messages.getString("CreateOPDialog.changeImageConf"),
                        getTitle(), JOptionPane.OK_CANCEL_OPTION);
                if (ans == JOptionPane.OK_OPTION) {
                    OrthoExplorerView.getService().changeImage(
                            imageSerie, newPath);
                }
            }
        }
    }

    /**
     * Creates and opens a new study and series.
     *
     * @param patient Patient.
     * @param stRole Study Role.
     * @param path Path for image.
     */
    private void createAndOpenStudy(final MediaSeriesGroup patient,
            final String stRole, final String path) {
        OrthoExplorerView explorer = OrthoExplorerView.getService();
        if (path != null && !"".equals(path)) {
            MediaSeriesGroup study = explorer.createOP(
                    patient, stRole);
            String description = Messages.getString("CreateOPDialog." + stRole);
            study.setTag(TagW.StudyDescription, description + " - "
                    + patient.getTagValue(TagW.PatientName));
            MediaSeries serie = explorer.addImage(
                    path, study,
                    OrthoExplorerView.CALC_IMAGE);

            explorer.openInDefaultPlugin(serie);
        }
    }

    /**
     * Edit a tag on all related instances.
     *
     * @param editGroup Group being edited
     * @param tag Tag to be edited.
     * @param text Text to put in tag.
     */
    private void editPatientData(final MediaSeriesGroup editGroup,
            final TagW tag, final String text) {
        final OrthodonticModel model = (OrthodonticModel) OrthoExplorerView
                .getService().getDataExplorerModel();
        editGroup.setTag(tag, text);
        String description = "";
        //two levels only
        for (MediaSeriesGroup group1 : model.getChildren(editGroup)) {
            //study
            group1.setTag(tag, text);
            if (tag.equals(TagW.PatientName)) {
                description = Messages.getString("CreateOPDialog."
                        + group1.getTagValue(TagO.STUDY_ROLE)) + " - "
                        + text;
                group1.setTag(TagW.StudyDescription, description);
            }
            for (MediaSeriesGroup group2 : model.getChildren(group1)) {
                //serie
                group2.setTag(tag, text);
                if (tag.equals(TagW.PatientName)) {
                    group2.setTag(TagW.StudyDescription, description);
                }
            }
        }
    }

    /**
     * Class to encapsulate image panels.
     */
    private class ImagePanel extends JPanel {

        /**
         * Fiels for image path.
         */
        private final JTextField imgField;

        /**
         * Builds this panel.
         *
         * @param field Fild to image path.
         * @param name Name for Label.
         */
        public ImagePanel(JTextField field, String name) {
            imgField = field;

            add(new JLabel(name));
            add(imgField);

            final JButton imgButton = new JButton("...");
            JMVUtils.setPreferredWidth(imgButton, 40, 40);
            imgButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    browseImgFile();
                }
            });
            add(imgButton);
        }

        /**
         * Shows browser.
         */
        private void browseImgFile() {
            final String directory = getImportPath();

            final JFileChooser fileChooser = new JFileChooser(directory);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            FileFormatFilter.setImageDecodeFilters(fileChooser);

            if (fileChooser.showOpenDialog(this)
                    == JFileChooser.APPROVE_OPTION) {
                final File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    lastDir = selectedFile.getPath();
                    imgField.setText(lastDir);
                }
            }
        }
    }

}
