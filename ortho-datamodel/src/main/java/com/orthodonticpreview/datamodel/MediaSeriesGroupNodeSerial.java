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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.weasis.core.api.Messages;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagW;

/**
 * A Serielizable version to MediaSeriesGroupNode, so it can be saved.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 9 Nov.
 */
public class MediaSeriesGroupNodeSerial
        implements MediaSeriesGroup, Serializable {

    /**
     * Serial as required by <code>Serializable</code>.
     */
    private static final long serialVersionUID = 5782529268251227496L;

    /**
     * Tag that represents ID of instance.
     */
    private TagW tagID;
    /**
     * Tag to be displayed.
     */
    private TagW displayTag;
    /**
     * Map of tags.
     */
    private final HashMap<TagW, Object> tags;

    /**
     * Unique identifyer.
     */
    private String identifier;

    /**
     * Comparator.
     */
    private Comparator<TagW> comparator;

    /**
     * Constructor with no parameters (required by Serializable).
     */
    public MediaSeriesGroupNodeSerial() {
        tags = new HashMap<TagW, Object>();
    }

    /**
     * Constructor.
     *
     * @param tagId Tag to represent the ID.
     * @param ident The unique identifyer (to be included in tagID).
     * @param display Tag to be displayed.
     */
    public MediaSeriesGroupNodeSerial(
            final TagW tagId, final String ident, final TagW display) {
        tagID = tagId;
        displayTag = display;
        identifier = ident;
        tags = new HashMap<TagW, Object>();
        tags.put(tagID, identifier);
    }

    @Override
    public TagW getTagID() {
        return tagID;
    }

    @Override
    public void setTag(final TagW tag, final Object value) {
        if (tag != null) {
            tags.put(tag, value);
        }
    }

    @Override
    public boolean containTagKey(final TagW tag) {
        return tags.containsKey(tag);
    }

    @Override
    public Object getTagValue(final TagW tag) {
        return tags.get(tag);
    }

    @Override
    public TagW getTagElement(final int id) {
        final Iterator<TagW> enumVal = tags.keySet().iterator();
        while (enumVal.hasNext()) {
            TagW tag = enumVal.next();
            if (tag.getId() == id) {
                return tag;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        //empty
    }

    @Override
    public void setComparator(final Comparator<TagW> comparator) {
        this.comparator = comparator;
    }

    @Override
    public Comparator<TagW> getComparator() {
        return comparator;
    }

    @Override
    public void setTagNoNull(final TagW tag, final Object value) {
        if (tag != null && value != null) {
            tags.put(tag, value);
        }
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param ident The identifier to set.
     */
    public void setIdentifier(final String ident) {
        identifier = ident;
        tags.put(tagID, identifier);
    }

    @Override
    public String toString() {
        Object val = tags.get(getDisplayTag());
        if (val instanceof Date) {
            val = TagW.DATETIME_FORMATTER.format(val);
        }
        return val == null
                ? Messages.getString("MediaSeriesGroupNode.no_val") + " "
                + getDisplayTag().getName() : val.toString();
    }

    /**
     * @param tagID the tagID to set
     */
    public void setTagID(final TagW tagID) {
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
    public void setDisplayTag(final TagW displayTag) {
        this.displayTag = displayTag;
    }

    @Override
    public Iterator<Map.Entry<TagW, Object>> getTagEntrySetIterator() {
        return tags.entrySet().iterator();
    }

}
