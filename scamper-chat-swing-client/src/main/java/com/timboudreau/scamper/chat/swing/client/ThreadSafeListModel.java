/*
 * The MIT License
 *
 * Copyright 2022 Mastfrog Technologies.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.timboudreau.scamper.chat.swing.client;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Tim Boudreau
 */
class ThreadSafeListModel<T extends Comparable<T>> implements ListModel<T>, Iterable<T> {

    private final List<T> items = new ArrayList<>();
    private final List<ListDataListener> listeners = new ArrayList<>();

    private void fire(ListDataEvent evt) {
        for (ListDataListener l : listeners) {
            switch (evt.getType()) {
                case ListDataEvent.INTERVAL_ADDED:
                    l.intervalAdded(evt);
                    break;
                case ListDataEvent.INTERVAL_REMOVED:
                    l.intervalRemoved(evt);
                    break;
                default:
                case ListDataEvent.CONTENTS_CHANGED:
                    l.contentsChanged(evt);
                    break;
            }
        }
    }

    int indexOf(T obj) {
        return items.indexOf(obj);
    }

    int size() {
        return items.size();
    }

    void set(int index, T obj) {
        run(() -> {
            T old = items.get(index);
            if (Objects.equals(old, obj)) {
                return;
            }
            items.set(index, obj);
            fire(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index));
        });
    }

    void removeElement(T obj) {
        run(() -> {
            int ix = items.indexOf(obj);
            items.remove(ix);
            fire(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, ix, ix));
        });
    }

    public void clear() {
        run(() -> {
            int sz = items.size();
            items.clear();
            if (sz > 0) {
                fire(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, sz - 1));
            }
        });
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public T getElementAt(int index) {
        return items.get(index);
    }

    @Override
    public synchronized void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    @Override
    public synchronized void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    public boolean contains(Object o) {
        return items.contains(o);
    }

    public void addElement(T obj) {
        run(() -> {
            items.add(obj);
            Collections.sort(items);
            int ix = items.indexOf(obj);
            fire(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, ix, ix));
        });
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(items).iterator();
    }

    private void run(Runnable r) {
        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
    }

}
