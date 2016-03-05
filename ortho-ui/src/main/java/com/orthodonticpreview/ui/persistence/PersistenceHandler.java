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
package com.orthodonticpreview.ui.persistence;

import com.orthodonticpreview.datamodel.MediaSeriesGroupNodeSerial;
import com.orthodonticpreview.datamodel.OrthodonticModel;
import com.orthodonticpreview.datamodel.TagO;
import com.orthodonticpreview.ui.OrthodonticWin;
import com.orthodonticpreview.ui.explorer.OrthoExplorerView;
import com.orthodonticpreview.ui.internal.Messages;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.model.TreeModel;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.graphic.Graphic;

/**
 * Saves and loads a patient and its associated data.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 11 Nov
 */
public class PersistenceHandler {

    public static final FileNameExtensionFilter ORP_FILTER
            = new FileNameExtensionFilter("Orthodontic Preview Files", "orp");

    /**
     * Class logger.
     */
    private static transient final Logger LOGGER
            = LoggerFactory.getLogger(PersistenceHandler.class);

    private final OrthodonticModel dataModel;

    private static final String[] SAVE_CMDS = {ActionW.ZOOM.cmd(),
        "pixel_padding", "zoomInterpolation", ActionW.ROTATION.cmd(),
        ActionW.IMAGE_SHUTTER.cmd(), ActionW.INVERT_LUT.cmd(),
        ActionW.FLIP.cmd(), ActionW.DRAW.cmd()
    };

    private File lastPath;

    /**
     * Creaters a PersistenceHandler object.
     *
     * @param model DataModel to read from or write to.
     * @param lastPa Last path choosen by the user.
     */
    public PersistenceHandler(final OrthodonticModel model,
            final File lastPa) {
        dataModel = model;
        lastPath = lastPa;
    }

    /**
     * Save a patient (ask user for location if true).
     *
     * @param patient Patient to be saved.
     * @param choseFile True if the user has to chose a file.
     * @return last path chosen by user.
     */
    public File savePatient(final MediaSeriesGroupNodeSerial patient,
            final boolean choseFile) {

        final File file = getFileForPatient(patient, choseFile);
        LOGGER.info("Saving patient " + patient + " to " + file);

        if (file == null) {
            LOGGER.info("Saving action canceled by user.");
            return lastPath;
        }

        PortablePreview portable = new PortablePreview();
        portable.setPatient(patient);

        final Collection<MediaSeriesGroup> studies
                = dataModel.getChildren(patient);
        if (studies != null && !studies.isEmpty()) {

            //criate dir for images:
            String name = file.getPath();

            if (name.contains(".")) {
                name = name.substring(0, name.indexOf("."));
            }
            name = name + "_imgs";
            File imgsDirectory = new File(name);
            imgsDirectory.mkdirs();

            final Iterator<MediaSeriesGroup> iterator = studies.iterator();
            while (iterator.hasNext()) {
                final MediaSeriesGroup next = iterator.next();
                if (next instanceof MediaSeriesGroupNodeSerial) {
                    final MediaSeriesGroupNodeSerial stSerial
                            = (MediaSeriesGroupNodeSerial) next;
                    portable.addStudy(stSerial);

                    //este estudo tem series?
                    saveSeries(portable, stSerial, imgsDirectory);
                }
            }
        }

        saveObject(file, portable);
        return lastPath;
    }

    public File openPatient() {

        final File file = promptForOpenFile();

        if (file == null) {
            return null;
        }

        final Object obj = loadFile(file);
        if (obj instanceof PortablePreview) {
            PortablePreview prev = (PortablePreview) obj;

            final MediaSeriesGroupNodeSerial patient = prev.getPatient();
            if (patient != null) {
                patient.setTag(TagW.FilePath, file);
                dataModel.addHierarchyNode(TreeModel.rootNode, patient);

                List<MediaSeriesGroupNodeSerial> studies = prev.getStudies();
                if (studies != null && !studies.isEmpty()) {
                    for (MediaSeriesGroupNodeSerial study : studies) {
                        dataModel.addHierarchyNode(patient, study);
                    }
                }

                Map<SeriesBuilder, MediaSeriesGroupNodeSerial> series
                        = prev.getSeries();
                if (series != null && !series.isEmpty()) {
                    Iterator<Entry<SeriesBuilder, MediaSeriesGroupNodeSerial>> iterator = series.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Entry<SeriesBuilder, MediaSeriesGroupNodeSerial> next
                                = iterator.next();
                        Series buildedSerie = next.getKey().createSeries();
                        dataModel.addHierarchyNode(
                                next.getValue(), buildedSerie);
                    }
                }
            }
        }

        return lastPath;
    }

    /**
     * Ask user for file path. Saves last path user choosed, to reopen next
     * time.
     *
     * @param patientName Name of patiente (can be null).
     * @return File choosen by user.
     */
    private File promptForOpenFile() {

        final JFileChooser fileChooser = new JFileChooser(lastPath);

        fileChooser.setDialogTitle(Messages.getString(
                "PersistenceHandler.fileChooserTitle"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(ORP_FILTER);

        int userSelection
                = fileChooser.showOpenDialog(OrthodonticWin.getInstance().getFrame());

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            final File fileToSave = fileChooser.getSelectedFile();
            LOGGER.debug(
                    "File chosen by user: " + fileToSave
                    + " file? " + fileToSave.isFile());
            lastPath = fileToSave.getParentFile();
            return fileToSave;
        }

        return null;
    }

    /**
     * Save one Object (uses java persistence).
     *
     * @param file File to save.
     * @param object The object to save.
     */
    private void saveObject(final File file, final Serializable object) {
        String errorMess = null;
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            // Write object with ObjectOutputStream
            ObjectOutputStream objOut;
            try {
                objOut = new ObjectOutputStream(fOut);
                // Write object out to disk
                objOut.writeObject(object);
            } catch (IOException ex) {
                LOGGER.info("Error trying to save object: " + ex.getMessage());
                errorMess = ex.getMessage();
            }
        } catch (FileNotFoundException ex) {
            LOGGER.info("Error trying to save file: " + ex.getMessage());
            errorMess = errorMess + "; " + ex.getMessage();
        }

        if (errorMess != null) {
            JOptionPane.showMessageDialog(OrthodonticWin.getInstance().getFrame(),
                    "Error: " + errorMess,
                    Messages.getString("PersistenceHandler.messTitle"),
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * Load the object from a file.
     *
     * @param file File to load.
     * @return Objecto loaded from file.
     */
    private Object loadFile(final File file) {
        FileInputStream fIn;
        String errorMess = null;
        try {
            fIn = new FileInputStream(file);

            // Read object using ObjectInputStream
            ObjectInputStream objIn;
            try {
                objIn = new ObjectInputStream(fIn);
                Object obj = null;
                try {
                    obj = objIn.readObject();
                } catch (ClassNotFoundException ex) {
                    LOGGER.info("Error trying to load object: " + ex.getMessage());
                    errorMess = ex.getMessage();
                }
                return obj;

            } catch (IOException ex) {
                LOGGER.info("Error trying to load object: " + ex.getMessage());
                errorMess = errorMess + "; " + ex.getMessage();
            }

        } catch (FileNotFoundException ex) {
            LOGGER.info("Error trying to load object: " + ex.getMessage());
            errorMess = errorMess + "; " + ex.getMessage();
        }

        if (errorMess != null) {
            JOptionPane.showMessageDialog(OrthodonticWin.getInstance().getFrame(),
                    "Error: " + errorMess,
                    Messages.getString("PersistenceHandler.messTitle"),
                    JOptionPane.ERROR_MESSAGE);

        }

        return null;
    }

    private void buildGraphicsLayerTag(Series serie) {
        ViewerPlugin viewer
                = OrthoExplorerView.getService().getViewer(serie);
        if (viewer instanceof ImageViewerPlugin) {
            DefaultView2d selectedImagePane
                    = ((ImageViewerPlugin) viewer).getSelectedImagePane();
            LOGGER.debug("found one viewer jfor: " + selectedImagePane);

            //graphics layer
            List<Graphic> allGraphics
                    = selectedImagePane.getLayerModel().getAllGraphics();
            LOGGER.debug("allGraphics = " + allGraphics.size());
            List<GraphicPack> packList = new ArrayList<GraphicPack>();
            for (Graphic graphic : allGraphics) {

                if (graphic instanceof PortableGraphic) {
                    PortableGraphic portGraph = (PortableGraphic) graphic;
                    GraphicPack pack = new GraphicPack(portGraph);
                    packList.add(pack);
                }
            }
            LOGGER.debug("packList = " + packList.size());
            if (!packList.isEmpty()) {
                serie.setTag(TagO.GRAPHIC_PACKS, packList);
            }

            //get tags from viewer!
            HashMap newMap = new HashMap<String, Object>();
            for (String cmd : SAVE_CMDS) {
                newMap.put(cmd, selectedImagePane.getActionValue(cmd));
            }
            serie.setTag(TagO.ACTIONS_SAVE, newMap);

        } else {
            //se a serie estiver fechada, mas teve um viewer:
            //clean action_tags
            Object obj = serie.getTagValue(TagO.ACTIONS_TAG);
            if (obj instanceof HashMap) {
                HashMap newMap = copyAndCleanMap((HashMap) obj);
                serie.setTag(TagO.ACTIONS_SAVE, newMap);
            }
        }
    }

    private HashMap copyAndCleanMap(HashMap map) {
        HashMap newMap = new HashMap<String, Object>();
        Set keySet = map.keySet();
        for (Object object : keySet) {
            if (object instanceof String) {
                Object get = map.get((String) object);

                //to be Serializable is not safe, error if cant found Class
                if (get instanceof Boolean || get instanceof Integer
                        || get instanceof Double) {
                    LOGGER.debug("including on actions map tag: "
                            + object + " - " + get.getClass());
                    newMap.put((String) object, get);
                }
            }
        }
        return newMap;
    }

    /**
     * Finds the actual path for the patient, or asks the user for a new one.
     *
     * @param patient Patient to use path for.
     * @param choseFile True if is expected that the user will choose.
     * @return The path to save the patient to.
     */
    private File getFileForPatient(final MediaSeriesGroupNodeSerial patient,
            boolean choseFile) {
        File file = null;
        if (!choseFile) {
            final Object tagValue = patient.getTagValue(TagW.FilePath);

            if (tagValue instanceof File) {
                File tagFile = (File) tagValue;
                if (tagFile.isFile() && tagFile.canWrite()) {
                    file = tagFile;
                } else {
                    choseFile = true;
                }
            } else {
                choseFile = true;
            }
        }

        if (choseFile) {
            final String patientName
                    = (String) patient.getTagValue(TagW.PatientName);

            JFileChooser chooser = new JFileChooser(lastPath);
            if (patientName != null && !patientName.isEmpty()) {
                String paName = patientName.replace(" ", "_") + ".orp";
                chooser.setSelectedFile(new File(paName));
            }

            //File file;
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(ORP_FILTER);

            int showSaveDialog = chooser.showSaveDialog(null);
            if (showSaveDialog == JFileChooser.APPROVE_OPTION) {
                file = addExtIfNeeded(chooser.getSelectedFile());
                lastPath = chooser.getCurrentDirectory();
            }

            //stores filepath on patient tags:
            patient.setTag(TagW.FilePath, file);
        }

        return file;
    }

    private File addExtIfNeeded(File selectedFile) {
        String name = selectedFile.getName();
        if (name.endsWith(".orp") || name.endsWith(".ORP")) {
            return selectedFile;
        } else {
            File newFile = new File(selectedFile.getAbsolutePath() + ".orp");
            return newFile;
        }
    }

    private void saveSeries(final PortablePreview portable,
            final MediaSeriesGroupNodeSerial stSerial, File imgsDir) {
        final Collection<MediaSeriesGroup> series
                = dataModel.getChildren(stSerial);
        if (series != null && !series.isEmpty()) {
            final Iterator<MediaSeriesGroup> serIt = series.iterator();
            while (serIt.hasNext()) {
                final MediaSeriesGroup serie = serIt.next();
                final Object role = serie.getTagValue(TagO.SERIE_ROLE);
                if (OrthoExplorerView.CALC_IMAGE.equals(role)
                        && serie instanceof Series) {
                    final Series savebleSerie = (Series) serie;
                    //include all graphics if they exist
                    buildGraphicsLayerTag(savebleSerie);

                    //save images at the same directory
                    Object tagValue = savebleSerie.getTagValue(TagW.FilePath);
                    if (tagValue instanceof File) {
                        File imgFile = (File) tagValue;
                        File targetFile = new File(imgsDir, imgFile.getName());
                        try {
                            Files.copy(imgFile.toPath(), targetFile.toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                            savebleSerie.setTag(TagW.FilePath, targetFile);
                        } catch (IOException ex) {
                            LOGGER.error(ex.getMessage());
                            JOptionPane.showMessageDialog(
                                    OrthodonticWin.getInstance().getFrame(),
                                    "Error: " + ex.getMessage(),
                                    Messages.getString(
                                            "PersistenceHandler.messTitle"),
                                    JOptionPane.ERROR_MESSAGE);
                        }

                    }

                    final SeriesBuilder builder
                            = SeriesBuilder.createSeriesBuilder(
                                    savebleSerie);

                    portable.addSeries(stSerial, builder);
                }
            }
        }
    }

}
