package jet.isur.nsi.common.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

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
        if(graph.addVertex(dict)) {
            // рекурсивно добавляем все справочники для которых dict является владельцем
            addAllOwnedDicts(dict);
            // рекурсивно добавляем все справочники на которые dict ссылается
            addAllRefDicts(dict);
        }
    }

    private void addAllRefDicts(NsiConfigDict dict) {
        for (NsiConfigAttr attr : dict.getAttrs()) {
            if(attr.getType() == MetaAttrType.REF) {
                NsiConfigDict refDict = attr.getRefDict();
                // игнорируем parent атрибуты
                if(attr == dict.getParentAttr()) continue;
                // выключаем прямые циклы
                add(refDict);
                DefaultEdge e = graph.addEdge(dict, refDict);
                if(hasCycles()) {
                    graph.removeEdge(e);
                }
            }
        }
    }

    private void addAllOwnedDicts(NsiConfigDict ownerDict) {
        for ( NsiConfigDict dict : config.getDicts()) {
            NsiConfigAttr ownerAttr = dict.getOwnerAttr();
            if(ownerAttr != null && ownerAttr.getRefDict() == ownerDict ) {
                add(dict);
                graph.addEdge(dict, ownerDict);
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
}
