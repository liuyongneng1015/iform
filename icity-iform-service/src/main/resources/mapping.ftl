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
                        <many-to-one name="${column.columnName}" entity-name="${reference.toColumn.dataModel.tableName}" column="${column.columnName}" cascade="save-update" lazy="false" fetch="join"   unique="true"/>
                    <#else >
                       <one-to-one name="${column.columnName}" entity-name="${reference.toColumn.dataModel.tableName}"  <#if reference.toColumn.columnName != "id"> property-ref="${reference.toColumn.columnName}" </#if> lazy="false" constrained="true"/>
                    </#if>
                <#elseif reference.referenceType.value = "ManyToOne">
                        <many-to-one name="${column.columnName}" entity-name="${reference.toColumn.dataModel.tableName}" column="${column.columnName}" cascade="save-update" lazy="false" fetch="join" />
                <#elseif reference.referenceType.value = "OneToMany">
                    <set name="${reference.toColumn.dataModel.tableName}_list" inverse="false" lazy="false">
                        <key column="${reference.toColumn.columnName}" />
                        <one-to-many entity-name="${reference.toColumn.dataModel.tableName}" />
                    </set>
                <#else>
                    <set name="${reference.toColumn.dataModel.tableName}_list" <#if reference.referenceMiddleTableName?? && reference.referenceMiddleTableName!=""> table="if_${reference.referenceMiddleTableName}_list" <#else > table="if_${reference.fromColumn.dataModel.tableName}_${reference.toColumn.dataModel.tableName}_list" </#if> <#if reference.inverse ?? && reference.inverse="true">inverse="${reference.inverse}"</#if>  lazy="true" fetch="select">
                        <key column="${reference.fromColumn.dataModel.tableName}_id"></key>
                        <many-to-many entity-name="${reference.toColumn.dataModel.tableName}" column="${reference.toColumn.dataModel.tableName}_id"></many-to-many>
                    </set>
                </#if>
            </#list>
		</#list>

         <#list dataModel.slaverModels as slaver>
                  <set name="${slaver.tableName}_list" inverse="false" lazy="false">
                      <key column="master_id" />
                      <one-to-many entity-name="${slaver.tableName}" />
                  </set>
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
    </class>

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
                         <many-to-one name="${column.columnName}" entity-name="${reference.toColumn.dataModel.tableName}" column="${column.columnName}" unique="true" lazy="false" fetch="join"  cascade="save-update"/>
                    <#else >
                        <one-to-one name="${column.columnName}" entity-name="${reference.toColumn.dataModel.tableName}" <#if reference.toColumn.columnName != "id"> property-ref="${reference.toColumn.columnName}" </#if> lazy="false" constrained="true" />
                    </#if>
                <#elseif reference.referenceType.value = "ManyToOne">
                    <many-to-one name="${column.columnName}" entity-name="${reference.toColumn.dataModel.tableName}" column="${column.columnName}" cascade="save-update" lazy="false" fetch="join"  />
                <#elseif reference.referenceType.value = "OneToMany">
                    <set name="${reference.toColumn.dataModel.tableName}_list" inverse="false" lazy="false" >
                        <key column="${reference.toColumn.columnName}" />
                        <one-to-many entity-name="${reference.toColumn.dataModel.tableName}" />
                    </set>
                <#else>
                    <#if dataModel.tableName = reference.toColumn.dataModel.tableName>
                        <set name="${reference.toColumn.dataModel.tableName}_list" <#if reference.referenceMiddleTableName?? && reference.referenceMiddleTableName!=""> table="if_${reference.referenceMiddleTableName}_list" <#else > table="if_${reference.toColumn.dataModel.tableName}_${reference.fromColumn.dataModel.tableName}_list" </#if> <#if reference.inverse ?? && reference.inverse="true" >inverse="${reference.inverse}"</#if> fetch="select" lazy="true">
                            <key column="${reference.fromColumn.dataModel.tableName}_id"></key>
                            <many-to-many entity-name="${reference.toColumn.dataModel.tableName}" column="${reference.toColumn.dataModel.tableName}_id"></many-to-many>
                        </set>
                    </#if>
                </#if>
            </#list>
        </#list>
    </class>
    </#if>
    </#list>

    <#list dataModel.slaverModels as slaver>
            <class entity-name="${slaver.tableName}" table="if_${slaver.tableName}">
                <comment>${slaver.name}</comment>
                <id name="id" type="string" length="32">
                    <column name="id">
                        <comment>主键</comment>
                    </column>
                    <generator class="uuid" />
                </id>

                <#list slaver.columns as column>
                    <#if column.columnName != 'id' &&  (!column.columnReferences?? || (column.columnReferences?size < 1)) >
                        <#if column.columnName = 'master_id' >
                            <many-to-one name="${column.columnName}" entity-name="${dataModel.tableName}" column="${column.columnName}" cascade="save-update" lazy="false" fetch="join"  />
                        <#else>
                            <property name="${column.columnName}" type="${column.dataType?lower_case}">
                                <column name="f${column.columnName}" default="${column.defaultValue!'null'}" not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>32<#else >${column.length}</#if>" precision="<#if !column.precision ?? || column.precision = 0>32<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
                                    <comment>${column.name}</comment>
                                </column>
                            </property>
                        </#if>
                    </#if>
                </#list>
            </class>
    </#list>

</hibernate-mapping>