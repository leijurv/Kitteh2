/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.util;
import compiler.Compiler;
import compiler.tac.TACFunctionCall;
import compiler.tac.TACStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class Prune {
    public static List<Pair<String, List<TACStatement>>> prune(List<Pair<String, List<TACStatement>>> wew) {
        HashMap<String, List<TACStatement>> mapping = new HashMap<>();
        for (Pair<String, List<TACStatement>> x : wew) {
            mapping.put(x.getA(), x.getB());
        }
        List<Pair<String, List<TACStatement>>> result = new ArrayList<>();
        LinkedList<String> toExplore = new LinkedList<>();
        HashSet<String> explored = new HashSet<>();
        toExplore.push("main");
        while (!toExplore.isEmpty()) {
            String s = toExplore.pop();
            List<TACStatement> body = mapping.get(s);
            if (body == null) {
                continue;
            }
            if (explored.contains(s)) {
                continue;
            }
            explored.add(s);
            toExplore.addAll(body.stream().filter(TACFunctionCall.class::isInstance).map(TACFunctionCall.class::cast).map(TACFunctionCall::calling).collect(Collectors.toList()));
            result.add(new Pair<>(s, body));
        }
        if (Compiler.verbose()) {
            System.out.println("Pruned " + (wew.size() - result.size()) + " functions inaccessible from entrypoint: " + wew.stream().map(Pair::getA).filter((String name) -> !explored.contains(name)).collect(Collectors.toList()));
        }
        return result;
    }
}
