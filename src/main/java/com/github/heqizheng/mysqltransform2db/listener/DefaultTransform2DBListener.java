package com.github.heqizheng.mysqltransform2db.listener;

import com.github.heqizheng.mysqltransform2db.entity.BinLogItem;

/**
 * BinLogListener监听器
 * @author heqizheng
 * @version 1.0
 * @date 2022/3/25
 */
@FunctionalInterface
public interface DefaultTransform2DBListener {

    void transform2DB(BinLogItem item);
}
