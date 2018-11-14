<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-mapping PUBLIC
        "-//Hibernate/Hibernate Mapping DTD 3.0//EN"
        "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd">
<hibernate-mapping package="tech.ascs.icity.iform.table.service">
    <class entity-name="${dataModel.tableName}" table="if_${dataModel.tableName}">
        <comment>${dataModel.name}</comment>
        <id name="id" type="string" length="32">
            <column name="ID">
                <comment>主键</comment>
            </column>
            <generator class="uuid" />
        </id>

		<#list dataModel.columns as column>
		<property name="${column.columnName}" type="${column.dataType?lower_case}">
            <column name="f${column.columnName}" default="${column.defaultValue!'null'}" not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>32<#else >${column.length}</#if>" precision="<#if !column.precision ?? || column.precision = 0>32<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
                <comment>${column.name}</comment>
            </column>
        </property>
		</#list>

        <property name="PROCESS_ID" type="string">
            <column name="PROCESS_ID" length="64">
                <comment>流程ID</comment>
            </column>
        </property>
        <property name="PROCESS_INSTANCE" type="string">
            <column name="PROCESS_INSTANCE" length="64">
                <comment>流程实例ID</comment>
            </column>
        </property>
        <property name="ACTIVITY_ID" type="string">
            <column name="ACTIVITY_ID" length="255">
                <comment>环节ID</comment>
            </column>
        </property>
        <property name="ACTIVITY_INSTANCE" type="string">
            <column name="ACTIVITY_INSTANCE" length="255">
                <comment>环节实例ID</comment>
            </column>
        </property>
		<#list dataModel.slaverModels as slaver>
		<set name="${slaver.tableName}List" cascade="all">
            <key column="${dataModel.tableName}_id" />
            <one-to-many entity-name="${slaver.tableName}" />
        </set>
		</#list>
    </class>
	<#list dataModel.slaverModels as slaver>
	<class entity-name="${slaver.tableName}" table="if_${slaver.tableName}">
        <comment>${slaver.name}</comment>
        <id name="id" type="string" length="32">
            <generator class="uuid" />
        </id>
        <many-to-one name="${dataModel.tableName}" column="${dataModel.tableName}_id"/>

		<#list slaver.columns as column>
		<property name="${column.columnName}" type="${column.dataType?lower_case}">
            <column name="f${column.columnName}" default="${column.defaultValue!'null'}" not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>32<#else >${column.length}</#if>" precision="<#if !column.precision ?? || column.precision = 0>32<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
                <comment>${column.name}</comment>
            </column>
        </property>
		</#list>
    </class>
	</#list>
	<#list dataModel.childrenModels as child>
	<class entity-name="${child.tableName}" table="if_${child.tableName}">
        <comment>${child.name}</comment>
        <id name="id" type="string" length="32">
            <generator class="uuid" />
        </id>

		<#list child.parentsModel as parent>
			<#if dataModel.tableName = parent.tableName>
        <set name="${dataModel.tableName}" table="${dataModel.tableName}_${child.tableName}_list">
            <key column="${child.tableName}_id"/>
            <many-to-many class="${dataModel.tableName}" column="${dataModel.tableName}_id"/>
        </set>
			</#if>
		</#list>
		<#list child.columns as column>
		<property name="${column.columnName}" type="${column.dataType?lower_case}">
            <column name="f${column.columnName}" default="${column.defaultValue!'null'}" not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>32<#else >${column.length}</#if>" precision="<#if !column.precision ?? || column.precision = 0>32<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
                <comment>${column.name}</comment>
            </column>
        </property>
		</#list>
    </class>
	</#list>
</hibernate-mapping>