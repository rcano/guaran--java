package guarana.java.core;

import static guarana.java.core.EventIterator.CONTINUE;
import static guarana.java.core.EventIterator.DISCARD;
import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction1;
import io.vavr.Function1;
import io.vavr.PartialFunction;
import java.util.function.Consumer;

public interface EventIterator<T> {

    static int CONTINUE = 0;
    static int DISCARD = 1;
    static int STOP = 2;
    
    static EventIterator<Object> all = (Object t) -> CONTINUE;

    int step(T t);

    default <U extends T> EventIterator<U> foreach(CheckedConsumer<U> f) {
        return new Foreach<>(this, f);
    }

    default <U extends T> EventIterator<U> forsome(PartialFunction<U, Void> pf) {
        return new Forsome<>(this, pf);
    }
    
    default <U extends T> EventIterator<U> filter(CheckedFunction1<U, Boolean> pred) {
        return new Filter<>(this, pred);
    }
    
    default <U extends T> EventIterator<U> drop(int num) {
        return new Drop<>(this, num);
    }
    
    default <U extends T> EventIterator<U> dropWhile(CheckedFunction1<U, Boolean> pred) {
        return new DropWhile<>(this, pred);
    }
    
    default <U extends T> EventIterator<U> take(int num) {
        return new Take<>(this, num);
    }
    
    default <U extends T> EventIterator<U> takeWhile(CheckedFunction1<U, Boolean> pred) {
        return new TakeWhile<>(this, pred);
    }
    
}

abstract class Chain<T> implements EventIterator<T> {

    private final EventIterator<? super T> parent;

    public Chain(EventIterator<? super T> parent) {
        this.parent = parent;
    }

    @Override
    final public int step(T t) {
        var parentRes = parent.step(t);
        return parentRes == CONTINUE ? stepImpl(t) : parentRes;
    }

    abstract int stepImpl(T t);

}

class Foreach<T> extends Chain<T> {

    private final Consumer<T> f;

    public Foreach(EventIterator<? super T> parent, CheckedConsumer<T> f) {
        super(parent);
        this.f = f.unchecked();
    }

    @Override
    public int stepImpl(T t) {
        f.accept(t);
        return CONTINUE;
    }
}

class Forsome<T> extends Chain<T> {

    private final PartialFunction<T, Void> pf;

    public Forsome(EventIterator<? super T> parent, PartialFunction<T, Void> pf) {
        super(parent);
        this.pf = pf;
    }

    @Override
    public int stepImpl(T t) {
        if (pf.isDefinedAt(t)) {
            pf.apply(t);
            return CONTINUE;
        } else return DISCARD;
    }
}

class Filter<T> extends Chain<T> {

    private final Function1<T, Boolean> f;
    public Filter(EventIterator<? super T> parent, CheckedFunction1<T, Boolean> f) {
        super(parent);
        this.f = f.unchecked();
    }

    @Override
    public int stepImpl(T t) {
        return f.apply(t) ? CONTINUE : DISCARD;
    }
}


class Drop<T> extends Chain<T> {

    private final int drop;
    private int dropped = 0;
    public Drop(EventIterator<? super T> parent, int drop) {
        super(parent);
        this.drop = drop;
    }

    @Override
    public int stepImpl(T t) {
        var res = dropped < drop ? DISCARD : CONTINUE;
        dropped = Math.max(dropped, drop); // ensure it never overflows
        return res;
    }
}

class DropWhile<T> extends Chain<T> {

    private final Function1<T, Boolean> pred;
    private boolean flipped = false;

    public DropWhile(EventIterator<? super T> parent, CheckedFunction1<T, Boolean> f) {
        super(parent);
        this.pred = f.unchecked();
    }

    @Override
    public int stepImpl(T t) {
        if (flipped) return CONTINUE;
        flipped = !pred.apply(t);
        return flipped ? CONTINUE : DISCARD;
    }
}

class Take<T> extends Chain<T> {

    private final int take;
    private int taken = 0;
    public Take(EventIterator<? super T> parent, int take) {
        super(parent);
        this.take = take;
    }

    @Override
    public int stepImpl(T t) {
        var res = taken < take ? CONTINUE : STOP;
        taken = Math.max(taken, take);
        return res;
    }
}


class TakeWhile<T> extends Chain<T> {

    private final Function1<T, Boolean> pred;
    private boolean flipped = false;

    public TakeWhile(EventIterator<? super T> parent, CheckedFunction1<T, Boolean> f) {
        super(parent);
        this.pred = f.unchecked();
    }

    @Override
    public int stepImpl(T t) {
        if (flipped) return STOP;
        flipped = !pred.apply(t);
        return flipped ? STOP : CONTINUE;
    }
}