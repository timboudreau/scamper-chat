package com.mastfrog.scamper.hub.impl;

import com.google.common.collect.Lists;
import com.mastfrog.scamper.chat.api.RoomDescriptor;
import com.mastfrog.util.collections.CollectionUtils;
import com.mastfrog.util.collections.Converter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Tim Boudreau
 */
public class ServerRoom implements Iterable<ChannelHandlerContext> {

    final List<ChannelHandlerContext> contexts = Lists.newCopyOnWriteArrayList();
    final RoomDescriptor descriptor;
    private final char[] passwordHashBase64;

    ServerRoom(RoomDescriptor desc, char[] passwordHashBase64) {
        // password is actually a sha-512 hash of the password, which oly
        // clients know
        this.descriptor = desc;
        this.passwordHashBase64 = passwordHashBase64;
    }

    public boolean hasPassword() {
        return passwordHashBase64 != null;
    }

    public boolean isHidden() {
        return name().startsWith("_");
    }

    public boolean checkPassword(char[] passwordHashBase64) {
        if (this.passwordHashBase64 == null) {
            return true;
        }
        if (passwordHashBase64 == null) {
            return false;
        }
        return Arrays.equals(passwordHashBase64, this.passwordHashBase64);
    }

    public String name() {
        return descriptor.room;
    }

    public int size() {
        return contexts.size();
    }

    public void add(ChannelHandlerContext ctx) {
        contexts.add(ctx);
    }

    public List<String> users() {
        List<String> result = new ArrayList<>(contexts.size());
        for (ChannelHandlerContext ctx : contexts) {
            String un = ctx.attr(Rooms.USER_NAME_KEY).get();
            if (un != null) {
                result.add(un);;
            }
        }
        return result;
    }

    public boolean contains(ChannelHandlerContext ctx) {
        return contexts.contains(ctx);
    }

    public Iterable<InetSocketAddress> addresses() {
        return CollectionUtils.toIterable(CollectionUtils.convertedIterator(new Cvt(), contexts.iterator()));
    }

    @Override
    public Iterator<ChannelHandlerContext> iterator() {
        return contexts.iterator();
    }
    
    private static class Cvt implements Converter<InetSocketAddress, ChannelHandlerContext> {

        @Override
        public InetSocketAddress convert(ChannelHandlerContext r) {
            return (InetSocketAddress) r.channel().remoteAddress();
        }

        @Override
        public ChannelHandlerContext unconvert(InetSocketAddress t) {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
