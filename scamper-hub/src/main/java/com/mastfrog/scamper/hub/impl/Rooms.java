package com.mastfrog.scamper.hub.impl;

import com.mastfrog.scamper.chat.api.RoomDescriptor;
import com.google.common.collect.Maps;
import com.mastfrog.scamper.Address;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.Sender;
import com.mastfrog.scamper.hub.JoinRoomHandler;
import com.mastfrog.scamper.hub.RoomJoinLeaveProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author Tim Boudreau
 */
@Singleton
public class Rooms implements Iterable<ServerRoom> {

    private final Map<RoomDescriptor, ServerRoom> contextsForRoom
            = Maps.newConcurrentMap();
    private final RoomJoinLeaveProcessor processor;
    public static final AttributeKey<String> USER_NAME_KEY = AttributeKey.valueOf(Rooms.class, "userName");
    private final Sender sender;

    @Inject
    Rooms(RoomJoinLeaveProcessor processor, Sender sender) {
        this.processor = processor;
        this.sender = sender;
    }

    public void broadcast(Message<?> message) {
        for (ServerRoom room : contextsForRoom.values()) {
            for (ChannelHandlerContext ctx : room) {
                sender.send(new Address((InetSocketAddress) ctx.channel().remoteAddress()), message);
            }
        }
    }

    public ServerRoom addOrCreate(RoomDescriptor descriptor, char[] password, ChannelHandlerContext ctx) {
        if (!isKnown(ctx)) {
            ctx.channel().closeFuture().addListener(new ExitOnClose());
        }
        ServerRoom roomEntered = contextsForRoom.get(descriptor);
        ServerRoom old = find(ctx);
        if (roomEntered == null) {
            roomEntered = new ServerRoom(descriptor, password);
            contextsForRoom.put(descriptor, roomEntered);
        } else {
            if (!roomEntered.checkPassword(password)) {
                return null;
            }
        }
        if (old != null) {
            old.contexts.remove(ctx);
            if (old.contexts.isEmpty()) {
                // Close the room
                contextsForRoom.remove(old.descriptor);
            }
        }
        if (!roomEntered.contains(ctx)) {
            roomEntered.add(ctx);
        }
        Attribute<String> userNameAttribute = ctx.attr(USER_NAME_KEY);
        userNameAttribute.set(descriptor.userName);

        processor.onRoomChange(descriptor.userName, old, roomEntered);
        return roomEntered;
    }

    @Override
    public Iterator<ServerRoom> iterator() {
        return contextsForRoom.values().iterator();
    }

    private class ExitOnClose implements ChannelFutureListener {

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            ServerRoom room = find(future.channel());
            if (room != null) {
                Attribute<String> userNameAttribute = future.channel().attr(USER_NAME_KEY);
                String name = userNameAttribute.get();
                processor.onRoomChange(name, room, null);
                ChannelHandlerContext toRemove = null;
                for (ChannelHandlerContext ctx : room) {
                    if (ctx.channel().remoteAddress().equals(future.channel().remoteAddress())) {
                        toRemove = ctx;
                        break;
                    }
                }
                if (toRemove != null) {
                    room.contexts.remove(toRemove);
                }
            }
        }

    }

    private boolean isKnown(ChannelHandlerContext ctx) {
        for (ServerRoom room : contextsForRoom.values()) {
            if (room.contains(ctx)) {
                return true;
            }
        }
        return false;
    }

    public ServerRoom find(Channel channel) {
        Attribute<ServerRoom> roomAttr = channel.attr(JoinRoomHandler.ROOM_KEY);
        ServerRoom result = roomAttr.get();
        return result;
    }

    public ServerRoom find(ChannelHandlerContext ctx) {
        Attribute<ServerRoom> roomAttr = ctx.attr(JoinRoomHandler.ROOM_KEY);
        ServerRoom result = roomAttr.get();
        return result;
    }

    public RoomDescriptor find(String name) {
        for (Map.Entry<RoomDescriptor, ServerRoom> e : contextsForRoom.entrySet()) {
            if (name.equals(e.getKey().room)) {
                return e.getKey();
            }
        }
        return null;
    }

    public List<ChannelHandlerContext> channels(RoomDescriptor room) {
        ServerRoom ctxts = contextsForRoom.get(room);
        return ctxts == null ? Collections.<ChannelHandlerContext>emptyList() : ctxts.contexts;
    }
}
