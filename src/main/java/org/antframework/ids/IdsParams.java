/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2018-01-20 21:04 创建
 */
package org.antframework.ids;

import org.antframework.common.util.id.IdGenerator;
import org.antframework.common.util.id.PeriodType;
import org.antframework.common.util.other.PropertyUtils;
import org.antframework.common.util.zookeeper.WorkerId;
import org.antframework.idcenter.client.IdContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * ids参数
 */
public class IdsParams {
    /**
     * 机房id属性名（如果不存在多机房情况，则不用填）
     */
    public static final String ROOM_ID_PROPERTY_NAME = "ids.room-id";
    /**
     * id中心地址属性名
     */
    public static final String SERVER_URL_PROPERTY_NAME = "ids.server-url";
    /**
     * ids的home文件路径属性名
     */
    public static final String HOME_PATH_PROPERTY_NAME = "ids.home-path";
    /**
     * worker编码属性名（每个应用实例的worker编码应该唯一）
     */
    public static final String WORKER_PROPERTY_NAME = "ids.worker";
    /**
     * zookeeper地址属性名（存在多个zookeeper的话以“,”分隔（比如：192.168.0.1:2181,192.168.0.2:2181））
     */
    public static final String ZK_URLS_PROPERTY_NAME = "ids.zk-urls";

    /**
     * 获取机房id
     */
    static String getRoomId() {
        return PropertyUtils.getProperty(ROOM_ID_PROPERTY_NAME, "");
    }

    /**
     * 创建id上下文
     *
     * @param idCode id编码
     * @return id上下文
     */
    static IdContext createIdContext(String idCode) {
        IdContext.InitParams initParams = new IdContext.InitParams();
        initParams.setIdCode(idCode);
        initParams.setServerUrl(PropertyUtils.getRequiredProperty(SERVER_URL_PROPERTY_NAME));
        initParams.setInitAmount(1000);
        initParams.setMinTime(10 * 60 * 1000);
        initParams.setMaxTime(15 * 60 * 1000);

        return new IdContext(initParams);
    }

    /**
     * 创建id生成器
     *
     * @param idCode     id编码
     * @param periodType 周期类型
     * @param maxId      id最大值（不包含）
     * @return id生成器
     */
    static IdGenerator createIdGenerator(String idCode, PeriodType periodType, Long maxId) {
        String filePath = PropertyUtils.getRequiredProperty(HOME_PATH_PROPERTY_NAME) + File.separator + String.format("ids-idGenerator-%s.properties", idCode);
        return new IdGenerator(periodType, 1000, maxId, filePath);
    }

    /**
     * 获取workerId
     *
     * @param maxWorkerId 允许的最大workerId（不包含）
     * @return workerId
     */
    static int getWorkerId(int maxWorkerId) {
        String worker = PropertyUtils.getRequiredProperty(WORKER_PROPERTY_NAME);
        String[] zkUrls = StringUtils.split(PropertyUtils.getRequiredProperty(ZK_URLS_PROPERTY_NAME), ',');
        if (ArrayUtils.isEmpty(zkUrls)) {
            throw new IllegalArgumentException("必须配置zookeeper地址：" + ZK_URLS_PROPERTY_NAME);
        }
        String filePath = PropertyUtils.getRequiredProperty(HOME_PATH_PROPERTY_NAME) + File.separator + "ids-workerId.properties";

        int workerId = WorkerId.getId(worker, zkUrls, "/ids/workerId", filePath);
        if (workerId >= maxWorkerId) {
            throw new IllegalStateException("worker数量超过最大值：" + maxWorkerId);
        }
        return workerId;
    }
}
