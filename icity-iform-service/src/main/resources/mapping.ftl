<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-mapping PUBLIC
        "-//Hibernate/Hibernate Mapping DTD 3.0//EN"
        "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd">
<hibernate-mapping package="tech.ascs.icity.iform.table.service">
    <class entity-name="${dataModel.tableName!''}" table="if_${dataModel.tableName!''}">
        <comment>${dataModel.name!"自定义表"}</comment>
        <id name="id" type="string" length="32">
            <column name="id">
                <comment>主键</comment>
            </column>
            <generator class="uuid" />
        </id>

		<#list dataModel.columns as column>
            <#if column.columnName != 'id' &&  (!column.columnReferences?? || (column.columnReferences?size < 1)) >
                <property name="${column.columnName!''}" type="${column.dataType?lower_case}">
                    <column name="f${column.columnName!''}" default="${column.defaultValue!'null'}" not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>255<#else >${column.length?c}</#if>" precision="<#if !column.precision ?? || column.precision = 0>255<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
					<#if column.name??  && column.name !="" > <comment>${column.name!''}</comment> </#if>
                    </column>
                </property>
            </#if>
            <#list column.columnReferences as reference>
                <#if reference.referenceType.value = "OneToOne">
                    <#if column.columnName != "id">
                        <many-to-one name="${column.columnName!''}" entity-name="${reference.toColumn.dataModel.tableName!''}" column="${column.columnName!''}" cascade="save-update" fetch="select"   unique="true"/>
                    <#else >
                       <one-to-one name="${reference.toColumn.columnName!''}_list" entity-name="${reference.toColumn.dataModel.tableName!''}"  <#if reference.toColumn.columnName != "id"> property-ref="${reference.toColumn.columnName!''}"</#if> constrained="true"/>
                    </#if>
                <#elseif reference.referenceType.value = "ManyToOne">
                        <many-to-one name="${column.columnName!''}" entity-name="${reference.toColumn.dataModel.tableName!''}" column="${column.columnName!''}" cascade="save-update" fetch="select" />
                <#elseif reference.referenceType.value = "OneToMany">
                    <bag name="${reference.toColumn.columnName!''}_list"  inverse="true"  lazy="false">
                        <key column="${reference.toColumn.columnName!''}" />
                        <one-to-many entity-name="${reference.toColumn.dataModel.tableName!''}" />
                    </bag>
                <#else>
                    <bag name="${reference.toColumn.dataModel.tableName!''}_list" <#if reference.referenceMiddleTableName?? && reference.referenceMiddleTableName!=""> table="if_${reference.referenceMiddleTableName}_list" <#else > table="if_${reference.fromColumn.dataModel.tableName}_${reference.toColumn.dataModel.tableName!''}_list" </#if> inverse="false"  lazy="false" fetch="select">
                        <key column="${reference.fromColumn.dataModel.tableName}_id"></key>
                        <many-to-many entity-name="${reference.toColumn.dataModel.tableName!''}" column="${reference.toColumn.dataModel.tableName!''}_id"></many-to-many>
                    </bag>
                </#if>
            </#list>
        </#list>

         <#list dataModel.slaverModels as slaver>
                  <bag name="${slaver.tableName!''}_list" inverse="true" cascade="all" lazy="false">
                      <key column="master_id" />
                      <one-to-many entity-name="${slaver.tableName!''}" />
                  </bag>
         </#list>

        <property name="PROCESS_ID" type="string">
            <column name="PROCESS_ID" length="255">
                <comment>流程ID</comment>
            </column>
        </property>
        <property name="PROCESS_INSTANCE" type="string">
            <column name="PROCESS_INSTANCE" length="255">
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
	 <class entity-name="${referencesData.tableName!''}" table="if_${referencesData.tableName!''}">
         <comment>${referencesData.name!"自定义表"}</comment>
         <id name="id" type="string" length="32">
             <column name="id">
                 <comment>主键</comment>
             </column>
             <generator class="uuid" />
         </id>

       	<#list referencesData.columns as column>
            <#if column.columnName != 'id' &&  (!column.columnReferences?? || (column.columnReferences?size < 1)) >
                <property name="${column.columnName!''}" type="${column.dataType?lower_case}">
                    <column name="f${column.columnName!''}" default="${column.defaultValue!'null'}" not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>255<#else >${column.length?c}</#if>" precision="<#if !column.precision ?? || column.precision = 0>255<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
                        <#if column.name??  && column.name !="" > <comment>${column.name!''}</comment> </#if>
                    </column>
                </property>
            </#if>
            <#list column.columnReferences as reference>
                <#if reference.toColumn.dataModel.tableName = dataModel.tableName>
                    <#if reference.referenceType.value = "OneToOne">
                        <#if column.columnName != "id">
                             <many-to-one name="${column.columnName!''}" entity-name="${reference.toColumn.dataModel.tableName!''}" column="${column.columnName!''}" cascade="save-update" unique="true" fetch="select"/>
                        <#else >
                            <one-to-one name="${reference.toColumn.columnName!''}_list" entity-name="${reference.toColumn.dataModel.tableName!''}" <#if reference.toColumn.columnName != "id">  property-ref="${reference.toColumn.columnName!''}"</#if> constrained="true" />
                        </#if>
                    <#elseif reference.referenceType.value = "ManyToOne">
                        <many-to-one name="${column.columnName!''}" entity-name="${reference.toColumn.dataModel.tableName!''}" column="${column.columnName!''}" cascade="save-update" fetch="select"  />
                    <#elseif reference.referenceType.value = "OneToMany">
                        <bag name="${reference.toColumn.columnName!''}_list" inverse="true" lazy="false" >
                            <key column="${reference.toColumn.columnName!''}" />
                            <one-to-many entity-name="${reference.toColumn.dataModel.tableName!''}" />
                        </bag>
                    <#else>
                        <#if dataModel.tableName = reference.toColumn.dataModel.tableName>
                            <bag name="${reference.toColumn.dataModel.tableName!''}_list" <#if reference.referenceMiddleTableName?? && reference.referenceMiddleTableName!=""> table="if_${reference.referenceMiddleTableName}_list" <#else > table="if_${reference.toColumn.dataModel.tableName!''}_${reference.fromColumn.dataModel.tableName}_list" </#if> inverse="true" lazy="false" fetch="select">
                                <key column="${reference.fromColumn.dataModel.tableName}_id"></key>
                                <many-to-many entity-name="${reference.toColumn.dataModel.tableName!''}" column="${reference.toColumn.dataModel.tableName!''}_id"></many-to-many>
                            </bag>
                        </#if>
                    </#if>
                </#if>
            </#list>
        </#list>

         <property name="PROCESS_ID" type="string">
             <column name="PROCESS_ID" length="255">
                 <comment>流程ID</comment>
             </column>
         </property>
         <property name="PROCESS_INSTANCE" type="string">
             <column name="PROCESS_INSTANCE" length="255">
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
        </#if>
    </#list>

    <#list dataModel.slaverModels as slaver>
            <class entity-name="${slaver.tableName!''}" table="if_${slaver.tableName!''}">
                <comment>${slaver.name!"自定义子表"}</comment>
                <id name="id" type="string" length="32">
                    <column name="id">
                        <comment>主键</comment>
                    </column>
                    <generator class="uuid" />
                </id>

                <#list slaver.columns as column>
                    <#if column.columnName != 'id' &&  (!column.columnReferences?? || (column.columnReferences?size < 1)) >
                        <#if column.columnName = 'master_id' >
                            <many-to-one name="${column.columnName!''}" entity-name="${dataModel.tableName!''}" column="${column.columnName!''}" cascade="save-update" fetch="select"  />
                        <#else>
                            <property name="${column.columnName!''}" type="${column.dataType?lower_case}">
                                <column name="f${column.columnName!''}" default="${column.defaultValue!'null'}" not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>255<#else >${column.length?c}</#if>" precision="<#if !column.precision ?? || column.precision = 0>255<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
                                    <#if column.name?? && column.name !=""> <comment>${column.name!''}</comment> </#if>
                                </column>
                            </property>
                        </#if>
                    </#if>
                </#list>
                <property name="PROCESS_ID" type="string">
                    <column name="PROCESS_ID" length="255">
                        <comment>流程ID</comment>
                    </column>
                </property>
                <property name="PROCESS_INSTANCE" type="string">
                    <column name="PROCESS_INSTANCE" length="255">
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
    </#list>

</hibernate-mapping>