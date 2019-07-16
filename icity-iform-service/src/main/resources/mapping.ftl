<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-mapping PUBLIC
        "-//Hibernate/Hibernate Mapping DTD 3.0//EN"
        "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd">
<hibernate-mapping package="tech.ascs.icity.iform.table.service">
    <class entity-name="${dataModel.tableName!''}" table="${dataModel.prefix!''}${dataModel.tableName!''}">
        <comment>${dataModel.name!"自定义主表"}</comment>
        <id name="id" type="string">
            <column name="id" length="32">
                <comment>主键</comment>
            </column>
            <generator class="uuid" />
        </id>

		<#list dataModel.columns as column>
            <#if column.columnName != 'id' &&  (!column.columnReferences?? || (column.columnReferences?size < 1)) >
                <#if column.columnName = 'master_id' >
                <#else>
                    <property name="${column.columnName!''}" type="${column.dataType?lower_case}">
                        <column name="${column.prefix!''}${column.columnName!''}" <#if column.defaultValue ??> default="${column.defaultValue!'null'}" </#if>  not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>255<#else >${column.length?c}</#if>" precision="<#if !column.precision ?? || column.precision = 0>255<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
                        <#if column.name??  && column.name !="" > <comment>${column.name!''}</comment> </#if>
                        </column>
                    </property>
                </#if>
            </#if>
            <#list column.columnReferences as reference>
                <#if reference.referenceType.value = "OneToOne">
                    <#if column.columnName != "id">
                        <many-to-one name="${column.columnName!''}" entity-name="${reference.toColumn.dataModel.tableName!''}" column="${column.columnName!''}" cascade="save-update" lazy="false" fetch="select"  not-found="ignore"  unique="true"/>
                    <#else >
                       <one-to-one name="${reference.toColumn.dataModel.tableName!''}_${reference.toColumn.columnName!''}_list" entity-name="${reference.toColumn.dataModel.tableName!''}"  <#if reference.toColumn.columnName != "id"> property-ref="${reference.toColumn.columnName!''}"</#if>  lazy="false" fetch="select"  constrained="true"/>
                    </#if>
                <#elseif reference.referenceType.value = "ManyToOne">
                        <many-to-one name="${column.columnName!''}" entity-name="${reference.toColumn.dataModel.tableName!''}" column="${column.columnName!''}" cascade="save-update"  lazy="false" fetch="select"  not-found="ignore"/>
                <#elseif reference.referenceType.value = "OneToMany">
                    <bag name="${reference.toColumn.dataModel.tableName!''}_${reference.toColumn.columnName!''}_list" inverse="true"  lazy="false" fetch="select" >
                        <key column="${reference.toColumn.columnName!''}" />
                        <one-to-many entity-name="${reference.toColumn.dataModel.tableName!''}" />
                    </bag>
                <#else>
                    <bag name="${reference.toColumn.dataModel.tableName!''}_list" <#if reference.referenceMiddleTableName?? && reference.referenceMiddleTableName!=""> table="${reference.referenceMiddleTableName}_list" <#else > table="${reference.fromColumn.dataModel.tableName}_${reference.toColumn.dataModel.tableName!''}_list" </#if> inverse="false"  lazy="false" fetch="select">
                        <key column="${reference.fromColumn.dataModel.tableName}_id"></key>
                        <many-to-many entity-name="${reference.toColumn.dataModel.tableName!''}" column="${reference.toColumn.dataModel.tableName!''}_id"></many-to-many>
                    </bag>
                </#if>
            </#list>
        </#list>

         <#list dataModel.slaverModels as slaver>
                  <bag name="${slaver.tableName!''}_list" inverse="true" cascade="all" lazy="false" fetch="select" >
                      <key column="master_id" />
                      <one-to-many entity-name="${slaver.tableName!''}" />
                  </bag>
         </#list>
    </class>

    <#list dataModel.referencesDataModel as referencesData>
	 <class entity-name="${referencesData.tableName!''}" table="${referencesData.prefix!''}${referencesData.tableName!''}">
         <comment>${referencesData.name!"自定义关联表"}</comment>
         <id name="id" type="string">
             <column name="id" length="32">
                 <comment>主键</comment>
             </column>
             <generator class="uuid" />
         </id>

       	<#list referencesData.columns as column>
            <#if column.columnName != 'id' &&  (!column.columnReferences?? || (column.columnReferences?size < 1)) >
                <#if column.columnName ?? && column.columnName = "master_id">
                <#else >
                    <property name="${column.columnName!''}" type="${column.dataType?lower_case}">
                        <column name="${column.prefix!''}${column.columnName!''}" <#if column.defaultValue ??> default="${column.defaultValue!'null'}" </#if> not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>255<#else >${column.length?c}</#if>" precision="<#if !column.precision ?? || column.precision = 0>255<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
                            <#if column.name??  && column.name !="" > <comment>${column.name!''}</comment> </#if>
                        </column>
                    </property>
                </#if>
            </#if>
            <#list column.columnReferences as reference>
                <#if reference.referenceType.value = "OneToOne">
                    <#if column.columnName != "id">
                         <many-to-one name="${column.columnName!''}" entity-name="${reference.toColumn.dataModel.tableName!''}" column="${column.columnName!''}" cascade="all" unique="true"  lazy="false" fetch="select"  not-found="ignore"/>
                    <#else >
                        <one-to-one name="${reference.toColumn.dataModel.tableName!''}_${reference.toColumn.columnName!''}_list" entity-name="${reference.toColumn.dataModel.tableName!''}" <#if reference.toColumn.columnName != "id">  property-ref="${reference.toColumn.columnName!''}"</#if> constrained="true"  lazy="false" fetch="select"  />
                    </#if>
                <#elseif reference.referenceType.value = "ManyToOne">
                    <many-to-one name="${column.columnName!''}" entity-name="${reference.toColumn.dataModel.tableName!''}" column="${column.columnName!''}" <#if column.columnName ?? && column.columnName="master_id"> <#else >cascade="all" </#if> lazy="false" fetch="select"   not-found="ignore" />
                <#elseif reference.referenceType.value = "OneToMany">
                    <bag name="${reference.toColumn.dataModel.tableName!''}_${reference.toColumn.columnName!''}_list"  lazy="false" fetch="select"  >
                        <key column="${reference.toColumn.columnName!''}" />
                        <one-to-many entity-name="${reference.toColumn.dataModel.tableName!''}" />
                    </bag>
                <#else>
                    <bag name="${reference.toColumn.dataModel.tableName!''}_list" <#if reference.referenceMiddleTableName?? && reference.referenceMiddleTableName!=""> table="${reference.referenceMiddleTableName}_list" <#else > table="${reference.toColumn.dataModel.tableName!''}_${reference.fromColumn.dataModel.tableName}_list" </#if> inverse="true" lazy="false" fetch="select">
                        <key column="${reference.fromColumn.dataModel.tableName}_id"></key>
                        <many-to-many entity-name="${reference.toColumn.dataModel.tableName!''}" column="${reference.toColumn.dataModel.tableName!''}_id"></many-to-many>
                    </bag>
                </#if>
            </#list>
        </#list>
     </class>
    </#list>

    <#list dataModel.slaverModels as slaver>
            <class entity-name="${slaver.tableName!''}" table="${slaver.prefix!''}${slaver.tableName!''}">
                <comment>${slaver.name!"自定义子表"}</comment>
                <id name="id" type="string">
                    <column name="id" length="32">
                        <comment>主键</comment>
                    </column>
                    <generator class="uuid" />
                </id>

                <#list slaver.columns as column>
                    <#if column.columnName != 'id' &&  (!column.columnReferences?? || (column.columnReferences?size < 1)) >
                        <#if column.columnName = 'master_id' >
                            <many-to-one name="${column.columnName!''}" entity-name="${dataModel.tableName!''}" lazy="false" column="${column.columnName!''}" fetch="select" not-found="ignore" />
                        <#else>
                            <property name="${column.columnName!''}" type="${column.dataType?lower_case}">
                                <column name="${column.prefix!''}${column.columnName!''}" <#if column.defaultValue ??> default="${column.defaultValue!'null'}" </#if> not-null="${(column.notNull!false)?c}" length="<#if !column.length ?? || column.length = 0>255<#else >${column.length?c}</#if>" precision="<#if !column.precision ?? || column.precision = 0>255<#else >${column.precision}</#if>" <#if column.dataType?? && column.dataType.value ?? && (column.dataType.value ="Integer" || column.dataType.value = "Long" || column.dataType.value = "Float" || column.dataType.value = "Double")> scale="${column.scale!0}"</#if>>
                                    <#if column.name?? && column.name !=""> <comment>${column.name!''}</comment> </#if>
                                </column>
                            </property>
                        </#if>
                    </#if>
                    <#list column.columnReferences as reference>
                        <#if reference.referenceType.value = "OneToOne">
                            <#if column.columnName != "id">
                                 <many-to-one name="${column.columnName!''}" entity-name="${reference.toColumn.dataModel.tableName!''}" column="${column.columnName!''}" cascade="all" unique="true"  lazy="false" fetch="select"  not-found="ignore"/>
                            <#else >
                                <one-to-one name="${reference.toColumn.dataModel.tableName!''}_${reference.toColumn.columnName!''}_list" entity-name="${reference.toColumn.dataModel.tableName!''}" <#if reference.toColumn.columnName != "id">  property-ref="${reference.toColumn.columnName!''}"</#if> constrained="true"  lazy="false" fetch="select"  />
                            </#if>
                        <#elseif reference.referenceType.value = "ManyToOne">
                            <many-to-one name="${column.columnName!''}" entity-name="${reference.toColumn.dataModel.tableName!''}" column="${column.columnName!''}" <#if column.columnName ?? && column.columnName="master_id"> <#else >cascade="all" </#if> lazy="false" fetch="select"   not-found="ignore" />
                        <#elseif reference.referenceType.value = "OneToMany">
                            <bag name="${reference.toColumn.dataModel.tableName!''}_${reference.toColumn.columnName!''}_list"  lazy="false" fetch="select"  >
                                <key column="${reference.toColumn.columnName!''}" />
                                <one-to-many entity-name="${reference.toColumn.dataModel.tableName!''}" />
                            </bag>
                        <#else>
                            <bag name="${reference.toColumn.dataModel.tableName!''}_list" <#if reference.referenceMiddleTableName?? && reference.referenceMiddleTableName!=""> table="${reference.referenceMiddleTableName}_list" <#else > table="${reference.toColumn.dataModel.tableName!''}_${reference.fromColumn.dataModel.tableName}_list" </#if> inverse="true" lazy="false" fetch="select">
                                <key column="${reference.fromColumn.dataModel.tableName}_id"></key>
                                <many-to-many entity-name="${reference.toColumn.dataModel.tableName!''}" column="${reference.toColumn.dataModel.tableName!''}_id"></many-to-many>
                            </bag>
                        </#if>
                    </#list>
                </#list>
            </class>
    </#list>

</hibernate-mapping>