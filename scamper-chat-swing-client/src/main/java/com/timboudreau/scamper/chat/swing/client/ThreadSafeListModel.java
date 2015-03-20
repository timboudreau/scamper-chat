package com.timboudreau.scamper.chat.swing.client;

import java.awt.EventQueue;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.DefaultListModel;

/**
 * ListModel implementation which is self-sorting and can be mutated
 * asynchronously from background threads.
 *
 * @author Tim Boudreau
 */
class ThreadSafeListModel<T extends Comparable<T>> extends DefaultListModel<T> implements Iterable<T>, List<T> {

    private void run(Runnable r) {
        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
    }

    @Override
    public boolean contains(Object o) {
        // Ensure we're doing an object equality check
        if (o == null) {
            return false;
        }
        if (this.stream().anyMatch((obj) -> (o.equals(obj)))) {
            return true;
        }
        return false;
    }

    @Override
    public void removeRange(final int i, final int i1) {
        run(() -> {
            ThreadSafeListModel.super.removeRange(i, i1);
        });
    }

    @Override
    public void clear() {
        run(() -> {
            ThreadSafeListModel.super.clear();
        });
    }

    @Override
    public T remove(final int i) {
        run(() -> {
            ThreadSafeListModel.super.remove(i);
        });
        return null;
    }

    @Override
    public void add(final int i, final T e) {
        if (contains(e)) {
            return;
        }
        run(() -> {
            ThreadSafeListModel.super.add(i, e);
            Collections.sort(ThreadSafeListModel.this);
        });
    }

    @Override
    public T set(final int i, final T e) {
        run(() -> {
            ThreadSafeListModel.super.set(i, e);
            Collections.sort(ThreadSafeListModel.this);
        });
        return null;
    }

    @Override
    public void removeAllElements() {
        run(() -> {
            ThreadSafeListModel.super.removeAllElements();
        });
    }

    @Override
    public boolean removeElement(final Object o) {
        boolean result = contains(o);
        run(() -> {
            ThreadSafeListModel.super.removeElement(o);
        });
        return result;
    }

    @Override
    public void addElement(final T e) {
        if (contains(e)) {
            return;
        }
        run(() -> {
            if (!contains(e)) {
                ThreadSafeListModel.super.addElement(e);
                Collections.sort(ThreadSafeListModel.this);
            }
        });
    }

    @Override
    public void removeElementAt(final int i) {
        run(() -> {
            ThreadSafeListModel.super.removeElementAt(i);
        });
    }

    @Override
    public void setElementAt(final T e, final int i) {
        run(() -> {
            ThreadSafeListModel.super.setElementAt(e, i);
            Collections.sort(ThreadSafeListModel.this);
        });
    }

    @Override
    public void setSize(final int i) {
        run(() -> {
            ThreadSafeListModel.super.setSize(i);
        });
    }

    @Override
    public Iterator<T> iterator() {
        return new Iter();
    }

    class Iter implements Iterator<T>, ListIterator<T> {

        private int ix = 0;

        @Override
        public boolean hasNext() {
            return ix < getSize();
        }

        @Override
        public T next() {
            return getElementAt(ix++);
        }

        @Override
        public boolean hasPrevious() {
            return ix > 0;
        }

        @Override
        public T previous() {
            return getElementAt(--ix);
        }

        @Override
        public int nextIndex() {
            return ix;
        }

        @Override
        public int previousIndex() {
            return ix - 1;
        }

        @Override
        public void remove() {
            ThreadSafeListModel.this.removeElementAt(ix - 1);
        }

        @Override
        public void set(T e) {
            ThreadSafeListModel.super.setElementAt(e, ix - 1);
        }

        @Override
        public void add(T e) {
            ThreadSafeListModel.this.addElement(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] ts) {
        int size = getSize();
        if (ts.length != size) {
            ts = (T[]) Array.newInstance(ts.getClass().getComponentType(), size);
        }
        for (int i = 0; i < size; i++) {
            ts[i] = (T) getElementAt(i);
        }
        return ts;
    }

    @Override
    public boolean add(T e) {
        boolean willAdd = contains(e);
        addElement(e);
        return willAdd;
    }

    @Override
    public boolean remove(Object o) {
        boolean willRemove = contains(o);
        removeElement(o);
        return willRemove;
    }

    @Override
    public boolean containsAll(Collection<?> clctn) {
        boolean result = true;
        for (Object o : clctn) {
            result &= contains(o);
            if (!result) {
                break;
            }
        }
        return result;
    }

    @Override
    public boolean addAll(Collection<? extends T> clctn) {
        for (T obj : clctn) {
            addElement(obj);
        }
        return true;
    }

    @Override
    public boolean addAll(int i, Collection<? extends T> clctn) {
        for (T obj : clctn) {
            add(i, obj);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> clctn) {
        boolean result = false;
        for (Object o : clctn) {
            result |= removeElement(o);
        }
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> clctn) {
        boolean result = false;
        for (T obj : this) {
            if (!clctn.contains(obj)) {
                result |= removeElement(obj);
            }
        }
        return result;
    }

    @Override
    public ListIterator<T> listIterator() {
        return new Iter();
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        Iter result = new Iter();
        result.ix = i - 1;
        return result;
    }

    @Override
    public List<T> subList(int i, int i1) {
        return toList().subList(i, i1);
    }

    private List<T> toList() {
        List<T> result = new LinkedList<>();
        int size = getSize();
        for (int i = 0; i < size; i++) {
            result.add(getElementAt(i));
        }
        return result;
    }
}
