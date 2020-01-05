package com.github.mschroeder.github.neat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class MapUtility {

    public static enum Sort {
        None, Descending, Ascending
    }

    public static <T> Map<T, T> map(T... ts) {
        Map<T, T> m = new HashMap<>();
        for (int i = 0; i < ts.length; i += 2) {
            m.put(ts[i], ts[i + 1]);
        }
        return m;
    }

    public static <K, V> void fillKey2ListMap(K k, V v, Map<K, List<V>> map) {
        if (map.containsKey(k)) {
            map.get(k).add(v);
        } else {
            map.put(k, new ArrayList<>(Arrays.asList(v)));
        }
    }

    public static <K, V> void fillKey2ListUniqueMap(K k, V v, Map<K, List<V>> map) {
        if (map.containsKey(k)) {
            if (!map.get(k).contains(v)) {
                map.get(k).add(v);
            }
        } else {
            map.put(k, new ArrayList<>(Arrays.asList(v)));
        }
    }

    public static <K, V> void fillKey2SetMap(K k, V v, Map<K, Set<V>> map) {
        if (map.containsKey(k)) {
            map.get(k).add(v);
        } else {
            map.put(k, new HashSet<>(Arrays.asList(v)));
        }
    }

    public static <K> int fillKey2CountMap(K k, Map<K, Integer> map) {
        int c;
        if (map.containsKey(k)) {
            c = map.get(k) + 1;
        } else {
            c = 1;
        }
        map.put(k, c);
        return c;
    }

    public static <K> double fillKey2MinMap(K k, double v, Map<K, Double> map) {
        double f;
        if (map.containsKey(k)) {
            f = Math.min(v, map.get(k));
        } else {
            f = v;
        }
        map.put(k, f);
        return f;
    }

    public static <K> double fillKey2MaxMap(K k, double v, Map<K, Double> map) {
        double f;
        if (map.containsKey(k)) {
            f = Math.max(v, map.get(k));
        } else {
            f = v;
        }
        map.put(k, f);
        return f;
    }

    public static <K, V> List<Entry<K, List<V>>> toEntries(Map<K, List<V>> map, boolean removeTrival, Sort sort) {
        Stream<Entry<K, List<V>>> s = map.entrySet().stream();

        if (removeTrival) {
            s = s.filter(e -> e.getValue().size() > 1);
        }

        if (sort != Sort.None) {
            s = s.sorted((o1, o2) -> {
                return (sort == Sort.Descending ? -1 : 1) * Integer.compare(o1.getValue().size(), o2.getValue().size());
            });
        }

        return s.collect(toList());
    }

    public static <K> List<Entry<K, Integer>> toCountEntries(Map<K, Integer> map, boolean removeTrival, Sort sort) {
        Stream<Entry<K, Integer>> s = map.entrySet().stream();

        if (removeTrival) {
            s = s.filter(e -> e.getValue() > 1);
        }

        if (sort != Sort.None) {
            s = s.sorted((o1, o2) -> {
                return (sort == Sort.Descending ? -1 : 1) * Integer.compare(o1.getValue(), o2.getValue());
            });
        }

        return s.collect(toList());
    }

    public static <K, V> List<Entry<K, V>> toComparedEntries(Map<K, V> map, Comparator<V> comp, Predicate<V> isNotTrivial) {
        Stream<Entry<K, V>> s = map.entrySet().stream();

        if (isNotTrivial != null) {
            s = s.filter(e -> isNotTrivial.test(e.getValue()));
        }

        if (comp != null) {
            s = s.sorted((o1, o2) -> {
                return comp.compare(o1.getValue(), o2.getValue());
            });
        }

        return s.collect(toList());
    }

}
