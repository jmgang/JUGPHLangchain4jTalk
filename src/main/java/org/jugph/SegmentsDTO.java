package org.jugph;

import java.util.List;

public class SegmentsDTO {
    private List<Segment> segments;

    public List<Segment> segments() {
        return segments;
    }

    public static class Segment {
        private String objectID;
        private String groupId;
        private String groupLabel;
        private String version;
        private String title;
        private String section;
        private String url;
        private String link;
        private String content;

        public String objectID() {
            return objectID;
        }

        public String groupId() {
            return groupId;
        }

        public String groupLabel() {
            return groupLabel;
        }

        public String version() {
            return version;
        }

        public String title() {
            return title;
        }

        public String section() {
            return section;
        }

        public String url() {
            return url;
        }

        public String link() {
            return link;
        }

        public String content() {
            return content;
        }
    }
}