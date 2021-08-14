package gal.boris.compluacoge.extras;

public class Box<T> {

    private T t;

    public Box() {}

    public Box(T t) {
        this.t = t;
    }

    public void set(T t) { this.t = t; }

    public T get() { return t; }
}