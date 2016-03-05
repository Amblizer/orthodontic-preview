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

import com.orthodonticpreview.datamodel.TagO;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.ui.editor.ViewerPluginBuilder;

/**
 * Holds data to save and load a serie.
 *
 * Always check for backward compatibility when making changes on this
 * class.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 10 Nov.
 */
public class SeriesBuilder implements Serializable {

    /** Serial as required by <code>Serializable</code>. */
    public static final long serialVersionUID = 7167607419904496883L;

    /** Class logger. */
    private static transient final Logger LOGGER =
            LoggerFactory.getLogger(SeriesBuilder.class);

    private TagW tagID;
    private TagW displayTag;
    private File imgRefference;
    private HashMap<TagW, Object> tags;
    private String identifier;

    /** Serializable must have a void constructor. */
    public SeriesBuilder() {
        tags = new HashMap<TagW, Object>();
    }

    /**
     * @return the tagID
     */
    public TagW getTagID() {
        return tagID;
    }

    /**
     * @param tagID the tagID to set
     */
    public void setTagID(TagW tagID) {
        this.tagID = tagID;
    }

    /**
     * @return the displayTag
     */
    public TagW getDisplayTag() {
        return displayTag;
    }

    /**
     * @param displayTag the displayTag to set
     */
    public void setDisplayTag(TagW displayTag) {
        this.displayTag = displayTag;
    }

    /**
     * @return the imgRefference
     */
    public File getImgRefference() {
        return imgRefference;
    }

    /**
     * @param imgRefference the imgRefference to set
     */
    public void setImgRefference(File imgRefference) {
        this.imgRefference = imgRefference;
    }

    /**
     * @return the tags
     */
    public HashMap<TagW, Object> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(HashMap<TagW, Object> tags) {
        this.tags = tags;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public void setTag(TagW tag, Object value) {
        if (tag != null) {
            tags.put(tag, value);
        }
    }

    public Object getTagValue(TagW tag) {
        return tags.get(tag);
    }

    public static SeriesBuilder createSeriesBuilder(Series serie) {
        SeriesBuilder builder = new SeriesBuilder();

        builder.setIdentifier((String) serie.getTagValue(serie.getTagID()));
        Iterator<Entry<TagW, Object>> tagSetIt = serie.getTagEntrySetIterator();
        LOGGER.debug("Includin tags of " + serie);
        while (tagSetIt.hasNext()) {
            Entry<TagW, Object> next = tagSetIt.next();

            if (next.getValue() instanceof Serializable
                    && !(next.getValue() instanceof AbstractTableModel)
                    && !(next.getKey().equals(TagO.ACTIONS_TAG))) {
                LOGGER.debug(next.getKey() + " - " + next.getValue());
                builder.setTag(next.getKey(), next.getValue());

            } else {
                LOGGER.debug("not including " + next.getValue()
                        + " - not Serializable");
            }
        }
        return builder;
    }

    public Series createSeries() {
        LOGGER.debug("Creating serie...");
        Object tagValue = getTagValue(TagW.FilePath);
        if (tagValue instanceof File) {
            File imgFile = (File) tagValue;
            LOGGER.debug("Creating serie from imgFile = " + imgFile);
            if (imgFile.canRead()) {
                MediaReader media = ViewerPluginBuilder.getMedia(imgFile);
                if (media != null) {
                    MediaSeries serie = media.getMediaSeries();
                    //set tags:
                    Set<TagW> keySet = tags.keySet();
                    for (TagW tagW : keySet) {
                        serie.setTag(tagW, tags.get(tagW));
                    }
                    //copy actions in view
                    if (serie.containTagKey(TagO.ACTIONS_SAVE)) {
                        serie.setTag(TagO.ACTIONS_TAG,
                                serie.getTagValue(TagO.ACTIONS_SAVE));
                    }

                    return (Series) serie;
                }
            }
        }
        return null;
    }
}
