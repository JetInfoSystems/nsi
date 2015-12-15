package jet.isur.nsi.common.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import jet.isur.nsi.api.data.NsiConfig;
import jet.isur.nsi.api.data.NsiConfigAttr;
import jet.isur.nsi.api.data.NsiConfigDict;
import jet.isur.nsi.api.model.MetaAttrType;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.collect.ComparisonChain;

public class DictDependencyGraph {

    private final NsiConfig config;
    private final DirectedGraph<NsiConfigDict, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    private final Map<NsiConfigDict, Set<String>> cycleRefs = new HashMap<>();

    public DictDependencyGraph(NsiConfig config) {
        this.config = config;
    }

    public static DictDependencyGraph build(NsiConfig config, Collection<NsiConfigDict> dicts) {
        DictDependencyGraph result = new DictDependencyGraph(config);
        for (NsiConfigDict dict : dicts) {
            result.add(dict);
        }
        return result;
    }

    private void add(NsiConfigDict dict) {
        NsiConfigDict mainDict = getMainDict(dict);
        if(graph.addVertex(mainDict)) {
            // рекурсивно добавляем все справочники для которых dict является владельцем
            addAllOwnedDicts(mainDict);
            // рекурсивно добавляем все справочники на которые dict ссылается
            addAllRefDicts(mainDict);
        }
    }

    private void addAllRefDicts(NsiConfigDict dict) {
        for (NsiConfigAttr attr : dict.getAttrs()) {
            if(attr.getType() == MetaAttrType.REF) {
                // игнорируем parent атрибуты
                if(attr == dict.getParentAttr()) continue;
                NsiConfigDict refDict = attr.getRefDict();
                NsiConfigDict mainRefDict = getMainDict(refDict);
                // выключаем прямые циклы
                add(mainRefDict);
                // refDict может оказаться прокси представлением
                DefaultEdge e = graph.addEdge(dict, mainRefDict);
                if(hasCycles()) {
                    graph.removeEdge(e);
                    if(!cycleRefs.containsKey(dict)) {
                        cycleRefs.put(dict, new HashSet<String>());
                    }
                    cycleRefs.get(dict).add(attr.getName());
                }
            }
        }
    }

    private NsiConfigDict getMainDict(NsiConfigDict refDict) {
        return refDict.getMainDict() != null ? refDict.getMainDict() : refDict;
    }

    private void addAllOwnedDicts(NsiConfigDict ownerDict) {
        for ( NsiConfigDict dict : config.getDicts()) {
            NsiConfigAttr ownerAttr = dict.getOwnerAttr();
            if(ownerAttr != null) {
                NsiConfigDict mainRefDict = getMainDict(ownerAttr.getRefDict()); 
                if(mainRefDict == ownerDict ) {
                    NsiConfigDict mainDict = getMainDict(dict);
                    add(mainDict);
                    graph.addEdge(mainDict, ownerDict);
                }
            }
        }
    }

    public boolean hasCycles() {
        CycleDetector<NsiConfigDict, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
        return cycleDetector.detectCycles();
    }

    public List<NsiConfigDict> sort() {
        int size = graph.vertexSet().size();
        PriorityQueue<NsiConfigDict> q = new PriorityQueue<>(size, new Comparator<NsiConfigDict>() {

            @Override
            public int compare(NsiConfigDict o1, NsiConfigDict o2) {
                return ComparisonChain.start().compare(o1.getName(), o2.getName()).result();
            }
        });

        TopologicalOrderIterator<NsiConfigDict, DefaultEdge> i = new TopologicalOrderIterator<>(graph, q);
        List<NsiConfigDict> items = new ArrayList<>();
        while (i.hasNext()) {
            items.add(i.next());
        }
        Collections.reverse(items);
        return items;
    }

    public Map<NsiConfigDict, Set<String>> getCycleRefs() {
        return cycleRefs;
    }
}
