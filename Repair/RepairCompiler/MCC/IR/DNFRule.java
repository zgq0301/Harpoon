package MCC.IR;
import java.util.*;

public class DNFRule {
   Vector ruleconjunctions;

    public DNFRule(Expr e) {
	ruleconjunctions=new Vector();
	ruleconjunctions.add(new RuleConjunction(new DNFExpr(false,e)));
    }

    public DNFRule(DNFExpr de) {
	ruleconjunctions=new Vector();
	ruleconjunctions.add(new RuleConjunction(de));
    }

    public DNFRule(RuleConjunction conj) {
	ruleconjunctions=new Vector();
	ruleconjunctions.add(conj);
    }

    public DNFRule(Vector conj) {
	ruleconjunctions=conj;
    }

    DNFRule() {
	ruleconjunctions=new Vector();
    }

    int size() {
	return ruleconjunctions.size();
    }

    RuleConjunction get(int i) {
	return (RuleConjunction)ruleconjunctions.get(i);
    }

    void add(RuleConjunction c) {
	ruleconjunctions.add(c);
    }

    public DNFRule copy() {
	Vector vector=new Vector();
	for (int i=0;i<size();i++) {
	    vector.add(get(i).copy());
	}
	return new DNFRule(vector);
    }

    public DNFRule and(DNFRule c) {
	DNFRule newdnf=new DNFRule();
	for(int i=0;i<size();i++) {
	    for(int j=0;j<c.size();j++) {
		newdnf.add(get(i).append(c.get(j))); //Cross product
	    }
	}
	return newdnf;
    }

    public DNFRule or(DNFRule c) {
	DNFRule copy=copy();
	for(int i=0;i<c.size();i++) {
	    copy.add(c.get(i).copy()); //Add in other conjunctions
	}
	return copy;
    }

    public DNFRule not() {
	DNFRule copy=copy();
        DNFRule notrule=null;
	for (int i=0;i<size();i++) {
	    RuleConjunction conj=copy.get(i);
            DNFRule newrule=null;
	    for (int j=0;j<conj.size();j++) {
		DNFExpr dp=conj.get(j);
		dp.negatePred();
                if (newrule==null)
                   newrule=new DNFRule(dp);
                else
                   newrule=newrule.or(new DNFRule(dp));
	    }
            if (notrule==null)
               notrule=newrule;
            else
               notrule=notrule.and(newrule);
	}
	return notrule;
   }
}
