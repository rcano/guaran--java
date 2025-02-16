package guarana.java.core;

import java.util.Iterator;
import java.util.LinkedList;
import org.agrona.collections.Long2ObjectHashMap;

public interface EmittersStation {

    <T, Container> boolean hasEmitter(EmitterDescr<T, Container> descr, Container container);

    <T, Container> boolean hasListeners(EmitterDescr<T, Container> descr, Container container);

    <T, Container> void emit(EmitterDescr<T, Container> descr, Container container, T evt);

    <T, Container> void listen(EmitterDescr<T, Container> descr, Container container, EventIterator<? super T> iterator);

    <T, Container> void removeListener(EmitterDescr<T, Container> descr, Container container, EventIterator<? super T> iterator);
}

class EmittersStationImpl implements EmittersStation {

    Long2ObjectHashMap<EmitterData<?>> emittersData = new Long2ObjectHashMap<>(32, 0.67f);

    @Override
    public <T, Container> boolean hasEmitter(EmitterDescr<T, Container> descr, Container container) {
        final long keyed = Internals.keyed(descr, container);
        return emittersData.containsKey(keyed);
    }

    @Override
    public <T, Container> boolean hasListeners(EmitterDescr<T, Container> descr, Container container) {
        final long keyed = Internals.keyed(descr, container);
        var data = emittersData.get(keyed);
        return data != null && !data.listeners.isEmpty();
    }

    @Override
    public <T, Container> void emit(EmitterDescr<T, Container> descr, Container container, T evt) {
        final long keyed = Internals.keyed(descr, container);
        EmitterData<T> data = (EmitterData) emittersData.get(keyed);
        if (data != null) data.emit(evt);
    }

    @Override
    public <T, Container> void listen(EmitterDescr<T, Container> descr, Container container, EventIterator<? super T> iterator) {
        final long keyed = Internals.keyed(descr, container);
        EmitterData<T> data = (EmitterData) emittersData.computeIfAbsent(keyed, _ -> new EmitterData<T>());
        data.listeners.add(iterator);
    }

    @Override
    public <T, Container> void removeListener(EmitterDescr<T, Container> descr, Container container, EventIterator<? super T> iterator) {
        final long keyed = Internals.keyed(descr, container);
        EmitterData<T> data = (EmitterData) emittersData.get(keyed);
        if (data != null) data.listeners.remove(iterator);
    }

    private class EmitterData<T> {

        public LinkedList<EventIterator<? super T>> listeners = new LinkedList<>();

        public void emit(T event) {
            for (Iterator<EventIterator<? super T>> iterator = listeners.iterator(); iterator.hasNext();) {
                EventIterator<? super T> it = iterator.next();
                if (it.step(event) == EventIterator.STOP) iterator.remove();
            }
        }
    }
}
