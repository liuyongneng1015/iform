<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-mapping PUBLIC
        "-//Hibernate/Hibernate Mapping DTD 3.0//EN"
        "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd">
<hibernate-mapping package="tech.ascs.icity.iform.table.service">
    <class entity-name="${dataModel.tableName}" table="if_${dataModel.tableName}">
        <comment>${dataModel.name}</comment>
        <id name="id" type="string" length="32">
            <column name="id">
                <comment>主键</comment>
            </column>
            <generator class="uuid" />
        </id>

		<#list dataModel.columns as column>
            <#if column.columnName != 'id' &&  (!column.columnReferences?? || (column.columnReferences?size < 1)) >
                <property name="${column.columnName}" type="${column.dataType?lower_case}">
                    <column name="f${column.columnName}" default="${column.defaultValue!'null'}" not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>32<#else >${column.length}</#if>" precision="<#if !column.precision ?? || column.precision = 0>32<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
                        <comment>${column.name}</comment>
                    </column>
                </property>
            </#if>
            <#list column.columnReferences as reference>
                <#if reference.referenceType.value = "OneToOne">
                    <#if column.columnName != "id">
                        <many-to-one name="${column.columnName}" entity-name="${reference.toColumn.dataModel.tableName}" column="${column.columnName}" unique="true"/>
                    <#else >
                       <one-to-one name="${column.columnName}" entity-name="${reference.toColumn.dataModel.tableName}"  <#if reference.toColumn.columnName != "id"> property-ref="${reference.toColumn.columnName}" </#if> constrained="true"/>
                    </#if>
                <#elseif reference.referenceType.value = "ManyToOne">
                        <many-to-one name="${column.columnName}" entity-name="${reference.toColumn.dataModel.tableName}" column="${column.columnName}" unique="true"/>
                <#elseif reference.referenceType.value = "OneToMany">
                    <set name="${reference.toColumn.dataModel.tableName}_list" cascade="all" inverse="true">
                        <key column="id" />
                        <one-to-many entity-name="${reference.toColumn.dataModel.tableName}" />
                    </set>
                <#else>
                    <set name="${reference.toColumn.dataModel.tableName}_list" table="if_${reference.fromColumn.dataModel.tableName}_${reference.toColumn.dataModel.tableName}_list">
                        <key column="${reference.fromColumn.dataModel.tableName}_id"></key>
                        <many-to-many entity-name="${reference.toColumn.dataModel.tableName}" column="${reference.toColumn.dataModel.tableName}_id"></many-to-many>
                    </set>
                </#if>
            </#list>
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
            <set name="${slaver.tableName}_list" cascade="all" inverse="true">
                <key column="id" />
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
        <many-to-one name="${dataModel.tableName}" entity-name="${dataModel.tableName}" column="master_id"/>

		<#list slaver.columns as column>
		<property name="${column.columnName}" type="${column.dataType?lower_case}">
            <column name="f${column.columnName}" default="${column.defaultValue!'null'}" not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>32<#else >${column.length}</#if>" precision="<#if !column.precision ?? || column.precision = 0>32<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
                <comment>${column.name}</comment>
            </column>
        </property>
		</#list>
    </class>
	</#list>

    <#list dataModel.referencesDataModel as referencesData>
    <#if referencesData.id != dataModel.id>
	 <class entity-name="${referencesData.tableName}" table="if_${referencesData.tableName}">
         <comment>${referencesData.name}</comment>
         <id name="id" type="string" length="32">
             <column name="id">
                 <comment>主键</comment>
             </column>
             <generator class="uuid" />
         </id>

       	<#list referencesData.columns as column>
            <#if column.columnName != 'id' &&  (!column.columnReferences?? || (column.columnReferences?size < 1)) >
                <property name="${column.columnName}" type="${column.dataType?lower_case}">
                    <column name="f${column.columnName}" default="${column.defaultValue!'null'}" not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>32<#else >${column.length}</#if>" precision="<#if !column.precision ?? || column.precision = 0>32<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
                        <comment>${column.name}</comment>
                    </column>
                </property>
            </#if>
            <#list column.columnReferences as reference>
                <#if reference.referenceType.value = "OneToOne">
                    <#if column.columnName != "id">
                         <many-to-one name="${column.columnName}" entity-name="${reference.toColumn.dataModel.tableName}" column="${column.columnName}" unique="true"/>
                    <#else >
                        <one-to-one name="${column.columnName}" entity-name="${reference.toColumn.dataModel.tableName}" <#if reference.toColumn.columnName != "id"> property-ref="${reference.toColumn.columnName}" </#if> constrained="true" />
                    </#if>
                <#elseif reference.referenceType.value = "ManyToOne">
                    <many-to-one name="${column.columnName}" entity-name="${reference.toColumn.dataModel.tableName}" column="${column.columnName}" unique="true"/>
                <#elseif reference.referenceType.value = "OneToMany">
                    <set name="${reference.toColumn.dataModel.tableName}_list" cascade="all"  inverse="true">
                        <key column="id" />
                        <one-to-many entity-name="${reference.toColumn.dataModel.tableName}" />
                    </set>
                <#elseif dataModel.tableName != reference.toColumn.dataModel.tableName>
                    <set name="${reference.toColumn.dataModel.tableName}_list" table="if_${reference.fromColumn.dataModel.tableName}_${reference.toColumn.dataModel.tableName}_list">
                        <key column="${reference.fromColumn.dataModel.tableName}_id"></key>
                        <many-to-many entity-name="${reference.toColumn.dataModel.tableName}" column="${reference.toColumn.dataModel.tableName}_id"></many-to-many>
                    </set>
                </#if>
            </#list>
        </#list>
    </class>
    </#if>
    </#list>

</hibernate-mapping>