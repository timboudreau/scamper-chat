package com.mastfrog.scamper.chat.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mastfrog.scamper.chat.api.ListRoomsReply.RoomInfo;
import com.mastfrog.util.Checks;
import com.mastfrog.util.collections.CollectionUtils;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Tim Boudreau
 */
public class ListRoomsReply implements Iterable<RoomInfo> {

    public final RoomInfo[] rooms;
    public final boolean hiddenRoomsExist;

    @JsonCreator
    public ListRoomsReply(@JsonProperty("rooms") RoomInfo[] rooms, @JsonProperty("hiddenRoomsExist") boolean hiddenRoomsExist) {
        this.rooms = rooms;
        this.hiddenRoomsExist = hiddenRoomsExist;
    }

    @Override
    public Iterator<RoomInfo> iterator() {
        return CollectionUtils.toIterator(rooms);
    }

    public static final class Builder {

        private List<RoomInfo> rooms = new LinkedList<>();
        private boolean hasHiddenRooms;

        public Builder addRoom(RoomInfo info) {
            rooms.add(info);
            return this;
        }

        public RoomInfo.Builder addRoom(String room, boolean hasPassword) {
            return new RoomInfo.Builder(this, room, hasPassword);
        }

        public Builder setHasHiddenRooms(boolean hidden) {
            this.hasHiddenRooms = hidden;
            return this;
        }

        public ListRoomsReply build() {
            RoomInfo[] rms = rooms.toArray(new RoomInfo[0]);
            return new ListRoomsReply(rms, hasHiddenRooms);
        }

        static class RoomInfoComparator implements Comparator<RoomInfo> {

            @Override
            public int compare(RoomInfo t, RoomInfo t1) {
                return t.name.compareTo(t1.name);
            }
        }
    }

    public static class RoomInfo implements Iterable<String>, Comparable<RoomInfo> {

        public final String name;
        public final boolean hasPassword;
        public final String[] participants;

        @JsonCreator
        public RoomInfo(@JsonProperty("name") String name, @JsonProperty("hasPassword") boolean hasPassword, @JsonProperty("participants") String[] participants) {
            Checks.notNull("name", name);
            this.name = name;
            this.hasPassword = hasPassword;
            this.participants = participants;
        }

        @Override
        public Iterator<String> iterator() {
            return CollectionUtils.toIterator(participants);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof RoomInfo && ((RoomInfo) o).name.equals(name);
        }

        @Override
        public int hashCode() {
            return 23 * name.hashCode();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(name);
            if (hasPassword) {
                sb.append("(pw)");
            }
            if (participants != null) {
                sb.append(": ");
                for (Iterator<String> iter=CollectionUtils.toIterator(participants); iter.hasNext();) {
                    sb.append(iter.next());
                    if (iter.hasNext()) {
                        sb.append(", ");
                    }
                }
            }
            return sb.toString();
        }

        @Override
        public int compareTo(RoomInfo t) {
            return name.compareToIgnoreCase(t.name);
        }

        public static final class Builder {

            private final ListRoomsReply.Builder parent;
            private final String name;
            private boolean hasPassword;
            private final Set<String> participants = new HashSet<>();

            Builder(ListRoomsReply.Builder parent, String name, boolean hasPassword) {
                this.parent = parent;
                this.name = name;
                this.hasPassword = hasPassword;
            }

            public Builder setHasPassword(boolean hasPassword) {
                this.hasPassword = hasPassword;
                return this;
            }

            public Builder addParticipant(String participant) {
                participants.add(participant);
                return this;
            }

            private RoomInfo build() {
                String[] users = participants.toArray(new String[participants.size()]);
                Arrays.sort(users);
                return new RoomInfo(name, hasPassword, users);
            }

            public ListRoomsReply.Builder close() {
                return parent.addRoom(build());
            }
        }
    }
}
