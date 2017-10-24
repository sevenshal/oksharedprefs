package cn.framework.mpsharedpreferences.annotations.processor;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by David on 25.01.2015.
 */
class Modifier {
    final static Set<javax.lang.model.element.Modifier> PUBLIC_STATIC = new LinkedHashSet<>();
    final static Set<javax.lang.model.element.Modifier> PUBLIC = new LinkedHashSet<>();
    final static Set<javax.lang.model.element.Modifier> PRIVATE = new LinkedHashSet<>();
    final static Set<javax.lang.model.element.Modifier> PRIVATE_FINAL = new LinkedHashSet<>();
    final static Set<javax.lang.model.element.Modifier> PUBLIC_FINAL_STATIC = new LinkedHashSet<>();

    static {
        PUBLIC_STATIC.add(javax.lang.model.element.Modifier.PUBLIC);
        PUBLIC_STATIC.add(javax.lang.model.element.Modifier.STATIC);
        PUBLIC.add(javax.lang.model.element.Modifier.PUBLIC);
        PRIVATE_FINAL.add(javax.lang.model.element.Modifier.PRIVATE);
        PRIVATE_FINAL.add(javax.lang.model.element.Modifier.FINAL);
        PUBLIC_FINAL_STATIC.add(javax.lang.model.element.Modifier.PUBLIC);
        PUBLIC_FINAL_STATIC.add(javax.lang.model.element.Modifier.FINAL);
        PUBLIC_FINAL_STATIC.add(javax.lang.model.element.Modifier.STATIC);
        PRIVATE.add(javax.lang.model.element.Modifier.PRIVATE);
    }
}
