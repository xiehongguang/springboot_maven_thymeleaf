package com.example.base.dao;

/**
 * Created by songyc on 2017/8/8.
 */

/*import com.hundsun.docsys.base.exception.Exception;
import com.hundsun.docsys.base.utils.Page;*/
import com.example.base.page.Page;
import org.apache.commons.beanutils.BeanUtils;
import com.example.base.IConfigurationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSession.StrictMap;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by songyc on 2017/8/8.
 *
 */
public class Dao implements IDao {
    private static final Log logger = LogFactory.getLog(Dao.class);

    private final Map<String, JdbcTemplate> dsMap = new HashMap<String, JdbcTemplate>();
    private final Map<String, DataSource> myDsMap = new HashMap<String, DataSource>();
    private final Map<String,String> tablePrefixMap=new HashMap<String,String>();
    private final Map<String, SqlSessionFactory> sessionFactoryMap = new HashMap<String, SqlSessionFactory>();

    private final Map<String, String> dmlPattern = new HashMap<String, String>();

    private JdbcTemplate jdbcTemplate;
//	private String tablePrefixs;

    public Dao(IConfigurationService cfgService) {
        dmlPattern.put("select", "from");
        dmlPattern.put("insert", "into");
        dmlPattern.put("replace", "into");
        dmlPattern.put("update", "update");
        dmlPattern.put("delete", "from");
        dmlPattern.put("desc", "desc");
        dmlPattern.put("merge","into");
    }

    public void setTablePrefixs(String dbname,String tablePrefixs) {
        tablePrefixMap.put(dbname,tablePrefixs);
    }


    public void setDataSource(String dbname, DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        dsMap.put(dbname, jdbcTemplate);
        myDsMap.put(dbname, dataSource);
    }

    public void setSessionFactorys(String dbname, SqlSessionFactory sqlSessionFactory){
        sessionFactoryMap.put(dbname,sqlSessionFactory);
    }

    @Override
    public List<Map<String, Object>> queryForMaps(String sql, Object... args) throws Exception {
        JdbcTemplate jdbcTemplate = dispatchDs(sql);
        List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
        try {
            if(jdbcTemplate != null)
                result = jdbcTemplate.queryForList(sql, args);
        } catch(DataAccessException e) {
            throw new Exception("[数据库操作]查询异常：" +getBaseExceptionInfo(e));
        } finally {
            try {
                if(jdbcTemplate != null)
                    closeConnectionSafety(jdbcTemplate.getDataSource().getConnection());
            } catch (SQLException e) {
                throw new Exception("[数据库操作]关闭数据库连接异常：" + getBaseExceptionInfo(e));
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> queryForMaps(String dataSourceName, String sql, Object... args) throws Exception {
        JdbcTemplate jdbcTemplate = dsMap.get(dataSourceName);
        List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
        try {
            if(jdbcTemplate != null)
                result = jdbcTemplate.queryForList(sql, args);
        } catch(DataAccessException e) {
            throw new Exception("[数据库操作]查询异常：" +getBaseExceptionInfo(e));
        } finally {
            try {
                if(jdbcTemplate != null)
                    closeConnectionSafety(jdbcTemplate.getDataSource().getConnection());
            } catch (SQLException e) {
                throw new Exception("[数据库操作]关闭数据库连接异常：" + getBaseExceptionInfo(e));
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object... args) throws Exception {
        JdbcTemplate jdbcTemplate = dispatchDs(sql);
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            if(jdbcTemplate != null) result = dispatchDs(sql).queryForMap(sql, args);
        } catch(DataAccessException e) {
            throw new Exception("[数据库操作]查询异常：" + getBaseExceptionInfo(e));
        } finally {
            try {
                if(jdbcTemplate != null) closeConnectionSafety(jdbcTemplate.getDataSource().getConnection());
            } catch (SQLException e) {
                throw new Exception("[数据库操作]关闭数据库连接异常：" + getBaseExceptionInfo(e));
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> queryForMap(String dataSourceName, String sql, Object... args) throws Exception {
        JdbcTemplate jdbcTemplate =  dsMap.get(dataSourceName);
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            if(jdbcTemplate != null) result = dispatchDs(sql).queryForMap(sql, args);
        } catch(DataAccessException e) {
            throw new Exception("[数据库操作]查询异常：" + getBaseExceptionInfo(e));
        } finally {
            try {
                if(jdbcTemplate != null) closeConnectionSafety(jdbcTemplate.getDataSource().getConnection());
            } catch (SQLException e) {
                throw new Exception("[数据库操作]关闭数据库连接异常：" + getBaseExceptionInfo(e));
            }
        }
        return result;
    }

    @Override
    public int update(String sql, Object... args) throws Exception {
        JdbcTemplate jdbcTemplate = dispatchDs(sql);
        return jdbcTemplate.update(sql, args);
    }

    @Override
    public int update(String dataSourceName, String sql, Object... args) throws Exception {
        JdbcTemplate jdbcTemplate =  dsMap.get(dataSourceName);
        return jdbcTemplate.update(sql, args);
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) throws Exception {
        List<T> ro = new ArrayList<T>();
        List<Map<String, Object>> datas = queryForMaps(sql, args);
        for (Map<String, Object> map : datas) {
            try {
                Map<String, Object> m = new HashMap<String, Object>();
                Object o = elementType.newInstance();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key = entry.getKey();
                    Object val = entry.getValue();
                    m.put(key.toLowerCase(), val);
                }
                BeanUtils. populate(o, m);
                ro.add((T) o);
            } catch (Exception e) {
                throw new Exception("[数据库操作]map转javabean异常：" + e);
            }
        }
        return ro;
    }

    @Override
    public <T> List<T> queryForList(String dataSourceName, String sql, Class<T> elementType, Object... args) throws Exception {
        List<T> ro = new ArrayList<T>();
        List<Map<String, Object>> datas = queryForMaps(dataSourceName,sql, args);
        for (Map<String, Object> map : datas) {
            try {
                Map<String, Object> m = new HashMap<String, Object>();
                Object o = elementType.newInstance();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key = entry.getKey();
                    Object val = entry.getValue();
                    m.put(key.toLowerCase(), val);
                }
                BeanUtils.populate(o, m);
                ro.add((T) o);
            } catch (Exception e) {
                throw new Exception("[数据库操作]map转javabean异常：" + e);
            }
        }
        return ro;
    }

    @Override
    public <T> T mySelectOne(String statement) throws Exception {
        return mySelectOne(statement, null);
    }

    @Override
    public <T> T mySelectOne(String statement, Object parameter) throws Exception {
        T result = null;
        String throwableinfo = null;
        try {
            SqlSessionFactory sqlSessionFactory=getSqlSessionFactory(statement,parameter);
            SqlSession sqlSession = getSqlSession(sqlSessionFactory);
            try{
                result = sqlSession.selectOne(statement, parameter);
            }finally{
                sqlSession.close();
            }
        }  catch (Exception e) {
            throwableinfo = getBaseExceptionInfo(e);
        } catch (Error e) {
            throwableinfo = getBaseExceptionInfo(e);
        }
        if(throwableinfo != null){
            throw new Exception("[数据库操作]查询失败："+throwableinfo);
        }
        return result;
    }

    /**
     * 根据条件查出所有数据，不分页
     * @param statement mapperxml中对应的id
     * @param parameter 查询的对象信息
     * @param <T> parameter对象对应的类型
     * @return 返回查询结果
     * @throws Exception 数据库操作异常时抛出该异常
     */
    @Override
    public <T> List<T> selectList(String statement, Object parameter) throws Exception {
        List<T> res = new ArrayList<T>();
        String throwableinfo = null;

        try {
            SqlSessionFactory sqlSessionFactory=getSqlSessionFactory(statement,parameter);
            SqlSession sqlSession = getSqlSession(sqlSessionFactory);
            try{
                res = sqlSession.selectList(statement, parameter);
            }finally{
                sqlSession.close();
            }
        } catch (Exception e) {
            throwableinfo =  getBaseExceptionInfo(e);
        } catch (Error e) {
            throwableinfo =  getBaseExceptionInfo(e);
        }
        if(throwableinfo != null){
            throw new Exception("[数据库操作]查询失败："+throwableinfo);
        }
        return res;
    }

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
    @Override
    public <T> Page<T> selectByPage(String statement, Integer pageNo, Integer pageSize, Object parameter) throws Exception {
        Long totalPageCount= totalPageCount(statement, parameter);
        if(null==totalPageCount||totalPageCount<1){
            return new Page();
        }
        List<T> tmpres = new ArrayList<T>();
        if(pageNo==null){
            pageNo=1;
        }
        if(pageSize==null){
            pageSize=Page.DEFAULT_PAGE_SIZE;
        }
        int statrtIndex=Page.getStartOfPage(pageNo,pageSize);
        RowBounds rowBounds=new RowBounds(statrtIndex,pageSize);
        String throwableinfo = null;

        try {
            SqlSessionFactory sqlSessionFactory=getSqlSessionFactory(statement,parameter);
            SqlSession sqlSession = getSqlSession(sqlSessionFactory);
            try{
                tmpres= sqlSession.selectList(statement, parameter,rowBounds);
            }finally{
                sqlSession.close();
            }
        } catch (Exception e) {
            throwableinfo =  getBaseExceptionInfo(e);
        } catch (Error e) {
            throwableinfo =  getBaseExceptionInfo(e);
        }
        if(throwableinfo != null){
            throw new Exception("[数据库操作]分页查询失败："+throwableinfo);
        }
        return new Page(statrtIndex, totalPageCount, pageSize, tmpres);
    }

    /**
     * 获取总记录数
     * @param statement  mapperxml中对应的id
     * @param parameter 查询的对象信息
     * @return 返回符合查询条件的记录数
     * @throws Exception 数据库操作异常时抛出该异常
     */
    @Override
    public long totalPageCount(String statement, Object parameter) throws Exception {
        long result = 0L;
        String throwableinfo = null;
        SqlSessionFactory sqlSessionFactory=getSqlSessionFactory(statement,parameter);
        MappedStatement mappedStatement=sqlSessionFactory.getConfiguration().getMappedStatement(statement);
        BoundSql boundSql=mappedStatement.getSqlSource().getBoundSql(wrapCollection(parameter));
        String selectSql = boundSql.getSql();
        selectSql=selectSql.toLowerCase();
        System.out.println(selectSql);
        if(!selectSql.contains("select")||!selectSql.contains("from")){
            throw new Exception("[数据库操作]原始脚本"+selectSql+"存在问题");
        }
        StringBuffer sqlsb=new StringBuffer();
//        sqlsb.append(selectSql.substring(0,selectSql.indexOf("select")+7)).append(" count(*) total_count ").append(selectSql.substring(selectSql.indexOf("from"),selectSql.length()));
        sqlsb.append(selectSql.substring(0,selectSql.indexOf("select")+7)).append(" count(*) total_count from (").append(selectSql+" )");
        String sql=sqlsb.toString();
        DataSource ds = myDispatchDs(statement,sql);
        Connection conn = null;
        PreparedStatement pstmt=null;
        ResultSet rs=null;
        try {
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(sql);
            setParameters(pstmt, mappedStatement, boundSql, parameter,sqlSessionFactory);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                result = rs.getInt("total_count");
            }
        } catch (Exception e) {
            throwableinfo =  getBaseExceptionInfo(e);
        } catch (Error e) {
            throwableinfo =  getBaseExceptionInfo(e);
        } finally {
            closeResultSet(rs);
            closeConnectionSafety(conn);
            closePreparedStatement(pstmt);
        }
        if(throwableinfo != null){
            throw new Exception("[数据库操作]获取总记录数失败："+throwableinfo);
        }
        return result;
    }

    /**
     * 插入一条记录
     * @param statement mapperxml中对应的id
     * @param parameter 要插入对象的信息
     * @return 返回插入结果，返回值大于0正常插入
     * @throws Exception 数据库操作异常时抛出该异常
     */
    @Override
    public int insert(String statement, Object parameter) throws Exception {
        int res=0;
        String throwableinfo = null;
        try {
            SqlSessionFactory sqlSessionFactory=getSqlSessionFactory(statement,parameter);
            SqlSession sqlSession = getSqlSession(sqlSessionFactory);
            try{
                res=sqlSession.insert(statement, parameter);
            }finally{
                sqlSession.close();
            }
        }  catch (Exception e) {
            throwableinfo =  getBaseExceptionInfo(e);
        } catch (Error e) {
            throwableinfo =  getBaseExceptionInfo(e);
        }
        if(throwableinfo != null){
            throw new Exception("[数据库操作]插入数据失败:"+throwableinfo);
        }
        return res;
    }

    /**
     * 更新一条记录
     * @param statement mapperxml中对应的id
     * @param parameter 要更新对象的信息
     * @return 返回更新结果，更新值大于0正常更新
     * @throws Exception 数据库操作异常时抛出该异常
     */
    @Override
    public int update(String statement, Object parameter) throws Exception {
        int res=0;
        String throwableinfo = null;
        try {
            SqlSessionFactory sqlSessionFactory=getSqlSessionFactory(statement,parameter);
            SqlSession sqlSession = getSqlSession(sqlSessionFactory);
            try{
                res=sqlSession.update(statement, parameter);
            }finally{
                sqlSession.close();
            }
        } catch (Exception e) {
            throwableinfo=getBaseExceptionInfo(e);
        } catch (Error e) {
            throwableinfo=getBaseExceptionInfo(e);
        }
        if(throwableinfo != null){
            throw new Exception("[数据库操作]更新数据失败:"+throwableinfo);
        }
        return res;
    }

    /**
     * 删除一条记录
     * @param statement mapperxml中对应的id
     * @param parameter 要删除对象的信息
     * @return 返回删除结果，返回值大于0正常删除
     * @throws Exception 数据库操作异常时抛出该异常
     */
    @Override
    public int delete(String statement, Object parameter) throws Exception {
        int res=0;
        String throwableinfo = null;

        try {
            SqlSessionFactory sqlSessionFactory=getSqlSessionFactory(statement,parameter);
            SqlSession sqlSession = getSqlSession(sqlSessionFactory);
            try{
                res=sqlSession.delete(statement, parameter);
            }finally{
                sqlSession.close();
            }
        }catch (Exception e) {
            throwableinfo=getBaseExceptionInfo(e);
        } catch (Error e) {
            throwableinfo=getBaseExceptionInfo(e);
        }
        if(throwableinfo != null){
            throw new Exception("[数据库操作]删除数据失败:"+throwableinfo);
        }
        return res;
    }

    /**
     * 根据sql路由到对应的数据源
     * @param sql sql语句
     * @return 根据表名前缀得到数据源后返回对应的JdbcTemplate
     */
    public JdbcTemplate dispatchDs(String sql) {
        JdbcTemplate routeJdbcTemplate = null;
        String processedSQL = getProcessSql(sql);
        Set<Entry<String, JdbcTemplate>> ds = dsMap.entrySet();
        for (Entry<String, JdbcTemplate> entry : ds) {
            String db = entry.getKey();
            for(String prefix : tablePrefixMap.get(db).split(",")){
                if(processedSQL.startsWith(prefix.toLowerCase())){
                    routeJdbcTemplate = dsMap.get(db);
                }
            }
        }
        return routeJdbcTemplate;
    }

    private String getProcessSql(String sql) {
        byte step = 0;
        String processedSQL = sql.toLowerCase().trim();
        char[] charArray = processedSQL.toCharArray();
        StringBuilder strBuff = new StringBuilder();
        String replacement = null;
        for(int i=0;i<charArray.length;i++){
            char c = charArray[i];
            if(step == 0){
                if(strBuff.length() > 0){
                    if(Character.isLetter(c)){
                        strBuff.append(c);
                    }else{
                        String sqlType = strBuff.toString();
                        if(dmlPattern.containsKey(sqlType)){
                            step = 1;
                            replacement = dmlPattern.get(sqlType);
                            if(sqlType.equals(replacement)){
                                i = -1;
                            }
                            strBuff.setLength(0);
                        }
                    }
                }else{
                    if(Character.isLetter(c)){
                        strBuff.append(c);
                    }else{
                        continue;
                    }
                }
            }else if(step == 1){
                if(c == replacement.charAt(0)
                        && i + replacement.length() + 1 < charArray.length
                        && processedSQL.substring(i, i + replacement.length()).equals(replacement)){
                    int befPos = i - 1;
                    if(befPos < 0
                            || charArray[befPos] == ' ' || charArray[befPos] == '\n' || charArray[befPos] == '\t' || charArray[befPos] == '`'
                            || charArray[befPos] == '*' || charArray[befPos] == '\'' || charArray[befPos] == '\"' || charArray[befPos] == ')'){
                        int aftPos = i + replacement.length();
                        int parenthesisCnt = 0;
                        while(aftPos<charArray.length && !Character.isLetter(charArray[aftPos]) && charArray[aftPos] != '_'){
                            if(charArray[aftPos] == '('){
                                parenthesisCnt++;
                            }
                            aftPos++;
                        }
                        if(parenthesisCnt > 0 && aftPos + 6 < charArray.length  && "select".equals(processedSQL.substring(aftPos, aftPos + 6))){
                            i = aftPos + 6;
                        }else{
                            i = aftPos;
                            strBuff.append(charArray[aftPos]);
                            step = 2;
                        }
                    }
                }
            }else if(step == 2){
                if(Character.isLetter(c) || c == '_'){
                    strBuff.append(c);
                }else{
                    if(c == '.'){
                        strBuff.setLength(0);
                        continue;
                    }else{
                        break;
                    }
                }
            }
        }
        return strBuff.toString();
    }

    /**
     * 不同数据源对应的表不一样
     * @param statement mapperxml中对应的id
     * @param parameter 操作的对象信息
     * @return 返回sqlsessionfactory
     */
    public  SqlSessionFactory getSqlSessionFactory(String statement, Object parameter) {
        Set<Entry<String, SqlSessionFactory>> sqlSessionFactorys = sessionFactoryMap.entrySet();
        for (Entry<String, SqlSessionFactory> entry : sqlSessionFactorys) {
            String db=entry.getKey().trim();
            SqlSessionFactory factory = entry.getValue();
//            String sql = getMyBatisSQL(factory, statement, parameter);
//            String processedSQL = getProcessSql(sql);
//            for(String prefix : tablePrefixMap.get(db).split(",")){
//                if(processedSQL.startsWith(prefix.toLowerCase())){
//                    return factory;
//                }
//            }
            if(statement.startsWith(db)){
                return factory;
            }
        }
        return null;
    }

    public DataSource myDispatchDs(String statement,String sql) {
        DataSource retDs = null;
        String processedSQL = getProcessSql(sql);
        Set<Entry<String, DataSource>> ds = myDsMap.entrySet();
        for (Entry<String, DataSource> entry : ds) {
            String db=entry.getKey();
            if(tablePrefixMap.size()==0)
                break;
            for(String prefix : tablePrefixMap.get(db).split(",")){
                if(processedSQL.startsWith(prefix.toLowerCase())){
                    retDs = myDsMap.get(db);
                }
            }
        }
        if (retDs==null){
            for (Entry<String, DataSource> entry : ds) {
                String db=entry.getKey().trim();
                if(statement.startsWith(db)){
                    retDs = myDsMap.get(db);
                }
            }
        }
        return retDs;
    }

    public DataSource myDispatchDs(String statement, Object parameter) {
        SqlSessionFactory sqlSessionFactory=getSqlSessionFactory(statement,parameter);
        String sql = getMyBatisSQL(sqlSessionFactory,statement, parameter);
        return myDispatchDs(statement,sql);
    }

    private void closeConnectionSafety(Connection conn){
        if(conn != null){
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closePreparedStatement(PreparedStatement preparedStatement){
        if(preparedStatement != null){
            try {
                preparedStatement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeResultSet(ResultSet resultSet){
        if(resultSet != null){
            try {
                resultSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private SqlSession getSqlSession(SqlSessionFactory sqlSessionFactory){
        return sqlSessionFactory.openSession(true);
    }

    private String getMyBatisSQL(SqlSessionFactory sqlSessionFactory, String statement, Object parameter){
        MappedStatement ms=sqlSessionFactory.getConfiguration().getMappedStatement(statement);
        BoundSql bs=ms.getSqlSource().getBoundSql(wrapCollection(parameter));
        return bs.getSql();
    }

    private Object wrapCollection(final Object object) {
        StrictMap<Object> map = new StrictMap<Object>();
        if (object instanceof List) {
            map.put("list", object);
            return map;
        } else if (object != null && object.getClass().isArray()) {
            map.put("array", object);
            return map;
        }
        return object;
    }

    public void setParameters(PreparedStatement ps,MappedStatement mappedStatement,BoundSql boundSql,Object parameterObject,SqlSessionFactory sqlSessionFactory) throws SQLException {
        TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings != null) {
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else {
                        MetaObject metaObject = sqlSessionFactory.getConfiguration().newMetaObject(parameterObject);
                        value = metaObject.getValue(propertyName);
                    }
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    JdbcType jdbcType = parameterMapping.getJdbcType();
                    if (value == null && jdbcType == null) jdbcType = sqlSessionFactory.getConfiguration().getJdbcTypeForNull();
                    typeHandler.setParameter(ps, i + 1, value, jdbcType);
                }
            }
        }
    }

    @Override
    public <K, V> Map<K, V> mySelectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) throws Exception {
        Map<K, V> result = new HashMap<K, V>();
        String throwableinfo = null;
        try {
            SqlSessionFactory sqlSessionFactory=getSqlSessionFactory(statement,parameter);
            SqlSession sqlSession = getSqlSession(sqlSessionFactory);
            try{
                result = sqlSession.selectMap(statement, parameter, mapKey, rowBounds);
            }finally{
                sqlSession.close();
            }
        } catch (Exception e) {
            throwableinfo = getBaseExceptionInfo(e);
        } catch (Error e) {
            throwableinfo = getBaseExceptionInfo(e);
        }
        if(throwableinfo != null){
            throw new Exception("[数据库操作]查询失败："+throwableinfo);
        }
        return result;
    }

    @Override
    public <K, V> Map<K, V> mySelectMap(String statement, Object parameter, String mapKey) throws Exception {
        return mySelectMap(statement, parameter, mapKey, RowBounds.DEFAULT);
    }

    @Override
    public <K, V> Map<K, V> mySelectMap(String statement, String mapKey) throws Exception {
        return mySelectMap(statement, null, mapKey, RowBounds.DEFAULT);
    }

    private String getBaseExceptionInfo(Throwable e){
        Throwable t=e;
        StringBuffer errmsg=new StringBuffer();
        while(null!=t&&null!=t.getCause()){
            errmsg.append(t.getMessage());
            t=t.getCause();
        }
        logger.error("异常详情:",e);
        return errmsg.toString();
    }

    @Override
    public Integer queryForInt(String sql, Object... args) throws Exception {
        return 0;
    }

    @Override
    public Integer queryForInt(String dataSourceName, String sql, Object... args) throws Exception {
        return null;
    }

}
