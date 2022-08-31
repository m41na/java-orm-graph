package works.hop.parser;

public interface Visitor {

    void visitEntity(EntityNode node);

    void visitField(FieldNode node);
}
