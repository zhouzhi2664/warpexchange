package com.zhoucong.exchange.db;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class Mapper<T> {
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	final Class<T> entityClass;
    final Constructor<T> constructor;
    final String tableName;
    
    // @Id property:
    final AccessibleProperty id;
    
    // all properties including @Id, key is property name
    final List<AccessibleProperty> allProperties;
    
    // property name -> AccessibleProperty
    final Map<String, AccessibleProperty> allPropertiesMap;
    
    final List<AccessibleProperty> insertableProperties;
    final List<AccessibleProperty> updatableProperties;
    
    final ResultSetExtractor<List<T>> resultSetExtractor;
	
	final String selectSQL;
    final String insertSQL;
    final String insertIgnoreSQL;
    final String updateSQL;
    final String deleteSQL;
    
    public T newInstance() throws ReflectiveOperationException {
        return this.constructor.newInstance();
    }
	
	public Mapper(Class<T> clazz) throws Exception {		
		List<AccessibleProperty> all = getProperties(clazz);
		AccessibleProperty[] ids = all.stream().filter(AccessibleProperty::isId).toArray(AccessibleProperty[]::new);
		if (ids.length != 1) {
            throw new RuntimeException("Require exact one @Id for class " + clazz.getName());
        }
		this.id = ids[0];		
		this.allProperties = all;
		this.allPropertiesMap = buildPropertiesMap(this.allProperties);
		this.insertableProperties = all.stream().filter(AccessibleProperty::isInsertable).collect(Collectors.toList());
		this.updatableProperties = all.stream().filter(AccessibleProperty::isUpdatable).collect(Collectors.toList());
		this.entityClass = clazz;
		this.constructor = clazz.getConstructor();
		this.tableName = getTableName(clazz);
		this.selectSQL = "SELECT * FROM " + this.tableName + " WHERE " + this.id.propertyName + " = ?";
		this.updateSQL = "UPDATE" + this.tableName + "SET"
				+ String.join(", ",
						this.updatableProperties.stream().map(p -> p.propertyName + " = ?").toArray(String[]::new))
				+" WHERE " + this.id.propertyName + " = ?";
		this.insertSQL = "INSERT INTO " + this.tableName + " ("
				+ String.join(", ", 
						this.insertableProperties.stream().map(p -> p.propertyName).toArray(String[]::new))
				+ ") VALUES (" + numOfQuestions(this.insertableProperties.size()) + ")";
		this.insertIgnoreSQL = this.insertSQL.replace("INSERT INTO", "INSERT IGNORE INTO");		
		this.deleteSQL = "DELETE FROM " + this.tableName + " WHERE " + this.id.propertyName + " = ?";
		this.resultSetExtractor = new ResultSetExtractor<>() {
			@Override
            public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
				final List<T> results = new ArrayList<>();
				final ResultSetMetaData m = rs.getMetaData();
				final int cols = m.getColumnCount();
				final String[] names = new String[cols];
				for (int i = 0; i < cols; i++) {
                    names[i] = m.getColumnLabel(i + 1);
                }
				try {
					while (rs.next()) {
						T bean = newInstance();
						for (int i = 0; i < cols; i++) {
							String name = names[i];
							AccessibleProperty p = allPropertiesMap.get(name);
							if (p != null) {
                                p.set(bean, rs.getObject(i + 1));
                            }
						}
						results.add(bean);
					}
				} catch(ReflectiveOperationException e){
					throw new RuntimeException(e);
				}
				return results;
			}		
		};		
	}


	private Map<String, AccessibleProperty> buildPropertiesMap(List<AccessibleProperty> props) {
		Map<String, AccessibleProperty> map = new HashMap<>();
		for (AccessibleProperty prop : props) {
            map.put(prop.propertyName, prop);
        }
        return map;
	}
	
	private String numOfQuestions(int n) {
		String[] qs = new String[n];
		return String.join(", ", Arrays.stream(qs).map((s) -> {
			return "?";
		}).toArray(String[]::new));		
	}

	private String getTableName(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private List<AccessibleProperty> getProperties(Class<T> clazz) {
		List<AccessibleProperty> properties = new ArrayList<>();
		for (Field f : clazz.getFields()) {
			
			
		}
		// TODO Auto-generated method stub
		return properties;
	}
    
}
