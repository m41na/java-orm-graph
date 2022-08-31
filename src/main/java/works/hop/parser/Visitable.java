package works.hop.parser;

public interface Visitable {

    void accept(Visitor visitor);
}
