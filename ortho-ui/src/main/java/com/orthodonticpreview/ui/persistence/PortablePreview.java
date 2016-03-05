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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2013, 3 Jan.
 */
public class PortablePreview implements Serializable {

    /**
     * Serial as required by <code>Serializable</code>.
     */
    private static final long serialVersionUID = 2721621553565026611L;

    /**
     * Patient.
     */
    private MediaSeriesGroupNodeSerial patient;
    /**
     * List of studies of this preview.
     */
    private List<MediaSeriesGroupNodeSerial> studies
            = new ArrayList<MediaSeriesGroupNodeSerial>();

    /**
     * Map with all SeriesBuilder, each one related to its study.
     */
    private Map<SeriesBuilder, MediaSeriesGroupNodeSerial> series
            = new HashMap<SeriesBuilder, MediaSeriesGroupNodeSerial>();

    /**
     * @return the patient
     */
    public MediaSeriesGroupNodeSerial getPatient() {
        return patient;
    }

    /**
     * @param patient the patient to set
     */
    public void setPatient(MediaSeriesGroupNodeSerial patient) {
        this.patient = patient;
    }

    /**
     * @return the studies
     */
    public List<MediaSeriesGroupNodeSerial> getStudies() {
        return studies;
    }

    /**
     * @param studies the studies to set
     */
    public void setStudies(List<MediaSeriesGroupNodeSerial> studies) {
        this.studies = studies;
    }

    public void addStudy(MediaSeriesGroupNodeSerial study) {
        studies.add(study);
    }

    /**
     * @return the series
     */
    public Map<SeriesBuilder, MediaSeriesGroupNodeSerial> getSeries() {
        return series;
    }

    /**
     * @param series the series to set
     */
    public void setSeries(Map<SeriesBuilder, MediaSeriesGroupNodeSerial> series) {
        this.series = series;
    }

    public void addSeries(
            MediaSeriesGroupNodeSerial study, SeriesBuilder serie) {
        series.put(serie, study);
    }
}
