<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-mapping PUBLIC "-//Hibernate/Hibernate Mapping DTD 3.0//EN" "http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd">
<hibernate-mapping package="tech.ascs.icity.iform.table.service">
  <class entity-name="${table}">	
	<id name="id" type="java.lang.String" length="32">
       <column name="id" />
       <generator class="uuid" />
    </id>

      <#list columns as column>
       <#if column.colName!="id">		
			<property name="${column.colName}" type="${column.type}">
                 <column name="${column.colName}" not-null="${column.notnullable}"></column>
            </property>
       </#if>
     </#list>

	</class>
</hibernate-mapping>