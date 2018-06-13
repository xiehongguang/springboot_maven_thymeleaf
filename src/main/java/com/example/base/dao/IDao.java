package com.example.base.dao;

/**
 * Created by songyc on 2017/8/8.
 */

/*import com.hundsun.docsys.base.exception.Exception;
import com.hundsun.docsys.base.utils.Page;*/
import com.example.base.page.Page;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Map;

/**
 * 数据库服务，支持简单数据路由，分库分表处理
 */
public interface IDao {

    /* ******************************************************************* */
    /*  对spring template的简单封装                                        */
    /* ******************************************************************* */
    Map<String, Object> queryForMap(String sql, Object... args) throws Exception;
    Map<String, Object> queryForMap(String dataSourceName, String sql, Object... args) throws Exception;//Exception;
    List<Map<String, Object>> queryForMaps(String sql, Object... args) throws Exception;
    List<Map<String, Object>> queryForMaps(String dataSourceName, String sql, Object... args) throws Exception;
    int update(String sql, Object... args) throws Exception;
    int update(String dataSourceName, String sql, Object... args) throws Exception;
    public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) throws Exception;
    public <T> List<T> queryForList(String dataSourceName, String sql, Class<T> elementType, Object... args) throws Exception;
    Integer queryForInt(String sql, Object... args) throws Exception;
    Integer queryForInt(String dataSourceName, String sql, Object... args) throws Exception;

    /* ******************************************************************* */
    /* 特定数据服务实现                                                    */
    /* ******************************************************************* */
    <T> T mySelectOne(String statement) throws Exception;
    <T> T mySelectOne(String statement, Object parameter) throws Exception;

    /**
     * 根据条件查出所有数据，不分页
     * @param statement mapperxml中对应的id
     * @param parameter 查询的对象信息
     * @param <T> parameter对象对应的类型
     * @return 返回查询结果
     * @throws Exception 数据库操作异常时抛出该异常
     */
    <T> List<T> selectList(String statement, Object parameter)throws Exception;

    /**
     * 获取总记录数
     * @param statement  mapperxml中对应的id
     * @param parameter 查询的对象信息
     * @return 返回符合查询条件的记录数
     * @throws Exception 数据库操作异常时抛出该异常
     */
    long totalPageCount(String statement, Object parameter) throws Exception;
    /**
     * 分页查询
     * @param statement mapperxml中对应的id
     * @param pageNo 页码
     * @param pageSize 每页记录数
     * @param parameter 查询的对象信息
     * @param <T> 查询对象的类型
     * @return 返回分页查询结果
     * @throws Exception 数据库操作异常时抛出该异常
     */
    <T> Page<T> selectByPage(String statement, Integer pageNo, Integer pageSize, Object parameter)throws Exception;

    /**
     * 插入一条记录
     * @param statement mapperxml中对应的id
     * @param parameter 要插入对象的信息
     * @return 返回插入结果，返回值大于0正常插入
     * @throws Exception 数据库操作异常时抛出该异常
     */
    int insert(String statement, Object parameter)throws Exception;

    /**
     * 更新一条记录
     * @param statement mapperxml中对应的id
     * @param parameter 要更新对象的信息
     * @return 返回更新结果，更新值大于0正常更新
     * @throws Exception 数据库操作异常时抛出该异常
     */
    int update(String statement, Object parameter)throws Exception;

    /**
     * 删除一条记录
     * @param statement mapperxml中对应的id
     * @param parameter 要删除对象的信息
     * @return 返回删除结果，返回值大于0正常删除
     * @throws Exception 数据库操作异常时抛出该异常
     */
    int delete(String statement, Object parameter)throws Exception;

    <K, V> Map<K, V> mySelectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) throws Exception;
    <K, V> Map<K, V> mySelectMap(String statement, Object parameter, String mapKey) throws Exception;
    <K, V> Map<K, V> mySelectMap(String statement, String mapKey) throws Exception;
}
