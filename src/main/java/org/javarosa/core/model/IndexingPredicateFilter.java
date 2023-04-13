package org.javarosa.core.model;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.PredicateFilter;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathCmpExpr;
import org.javarosa.xpath.expr.XPathEqExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class IndexingPredicateFilter implements PredicateFilter {

    private final Map<String, List<TreeReference>> cachedEvaluations = new HashMap<>();

    @Nullable
    @Override
    public List<TreeReference> filter(DataInstance sourceInstance, TreeReference nodeSet, XPathExpression predicate, List<TreeReference> children, EvaluationContext evaluationContext, Supplier<List<TreeReference>> next) {
        XPathPathExpr left = null;
        XPathPathExpr right = null;
        if (predicate instanceof XPathEqExpr &&
            ((XPathEqExpr) predicate).a instanceof XPathPathExpr &&
            ((XPathEqExpr) predicate).b instanceof XPathPathExpr) {
            left = (XPathPathExpr) ((XPathEqExpr) predicate).a;
            right = (XPathPathExpr) ((XPathEqExpr) predicate).b;
        } else if (predicate instanceof XPathCmpExpr &&
            ((XPathCmpExpr) predicate).a instanceof XPathPathExpr &&
            ((XPathCmpExpr) predicate).b instanceof XPathPathExpr) {
            left = (XPathPathExpr) ((XPathCmpExpr) predicate).a;
            right = (XPathPathExpr) ((XPathCmpExpr) predicate).b;
        }

        String key = null;
        if (left != null && right != null && left.init_context == XPathPathExpr.INIT_CONTEXT_RELATIVE) {
            Object rightValue = right.eval(sourceInstance, evaluationContext).unpack();
            key = nodeSet.toString() + predicate + left + rightValue.toString();
        }

        if (key != null) {
            if (cachedEvaluations.containsKey(key)) {
                return cachedEvaluations.get(key);
            } else {
                List<TreeReference> filtered = next.get();
                cachedEvaluations.put(key, filtered);
                return filtered;
            }
        } else {
            return next.get();
        }
    }
}
