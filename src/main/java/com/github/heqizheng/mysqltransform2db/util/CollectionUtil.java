package com.github.heqizheng.mysqltransform2db.util;

import java.util.Collection;

/**
 * @author heqizheng
 * @version 1.0
 * @date 2022/3/25
 */
public class CollectionUtil {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
