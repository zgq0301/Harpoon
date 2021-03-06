package MCC.IR;

public class DNFPredicate {
    boolean negate;
    Predicate predicate;

    public DNFPredicate(DNFPredicate dp) {
	this.negate=dp.negate;
	this.predicate=dp.predicate;
    }
    Predicate getPredicate() {
	return predicate;
    }
    public DNFPredicate(boolean negate,Predicate predicate) {
	this.negate=negate;
	this.predicate=predicate;
    }
    String name() {
	String name="";
	if (this.negate)
	    name+="!";
	name+=predicate.name();
	return name;
    }

    void negatePred() {
	negate=!negate;
    }

    boolean isNegated() {
	return negate;
    }
}
