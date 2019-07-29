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
                <#elseif column.columnName = 'PROCESS_INSTANCE'>
                    <many-to-one name="processInstance" column="PROCESS_INSTANCE" entity-name="ProcessInstance" lazy="false" fetch="select"/>
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
                <#elseif column.columnName = 'PROCESS_INSTANCE'>
                    <many-to-one name="processInstance" column="PROCESS_INSTANCE" entity-name="ProcessInstance"  lazy="false" fetch="select"/>
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


    <class entity-name="ProcessInstance" subselect="select proc_inst_id_,business_key_,start_time_,end_time_,current_task_,current_handler_ from act_hi_procinst">
        <id name="id" type="string">
            <column name="proc_inst_id_" length="64"/>
        </id>
		<property name="formInstance" type="string">
			<column name="business_key_" not-null="false" length="255"/>
		</property>
		<property name="startTime" type="timestamp">
			<column name="start_time_" not-null="true" sql-type="timestamptz"/>
		</property>
		<property name="endTime" type="timestamp">
			<column name="end_time_" not-null="false" sql-type="timestamptz"/>
		</property>
		<property name="currentTask" type="string">
			<column name="current_task_" not-null="false" length="255"/>
		</property>
		<property name="currentHandler" type="string">
			<column name="current_handler_" not-null="false" length="255"/>
		</property>
		<bag name="workingTasks"   lazy="false" fetch="select">
			<key column="proc_inst_id_" />
			<one-to-many entity-name="WorkingTask"/>
		</bag>
		<bag name="doneTasks" lazy="false" fetch="select">
			<key column="proc_inst_id_" />
			<one-to-many entity-name="DoneTask"/>
		</bag>
    </class>

	<class entity-name="WorkingTask" subselect="select id_,task_def_key_,proc_inst_id_,create_time_,claim_time_,assignee_,prev_task_id_,next_task_id_ from act_ru_task">
        <id name="id" type="string">
            <column name="id_" length="64"/>
        </id>
		<property name="taskDefKey" type="string">
			<column name="task_def_key_" not-null="false" length="255"/>
		</property>
		<many-to-one name="processInstance" column="proc_inst_id_" entity-name="ProcessInstance" lazy="false" fetch="select" />
		<property name="createTime" type="timestamp">
			<column name="create_time_" not-null="false" sql-type="timestamptz"/>
		</property>
		<property name="claimTime" type="timestamp">
			<column name="claim_time_" not-null="false" sql-type="timestamptz"/>
		</property>
		<property name="assignee" type="string">
			<column name="assignee_" not-null="false" length="255"/>
		</property>
		<property name="prevTaskId" type="string">
			<column name="prev_task_id_" not-null="false" length="255"/>
		</property>
		<property name="nextTaskId" type="string">
			<column name="next_task_id_" not-null="false" length="255"/>
		</property>
		<bag name="candidates" lazy="false" fetch="select">
			<key column="task_id_" />
			<one-to-many entity-name="TaskCandidate"/>
		</bag>
	</class>

	<class entity-name="DoneTask" subselect="select id_,task_def_key_,proc_inst_id_,start_time_,claim_time_,end_time_,assignee_ from act_hi_taskinst where end_time_ is not null">
        <id name="id" type="string">
            <column name="id_" length="64"/>
        </id>
		<property name="taskDefKey" type="string">
			<column name="task_def_key_" not-null="false" length="255"/>
		</property>
		<many-to-one name="processInstance" column="proc_inst_id_" entity-name="ProcessInstance" lazy="false" fetch="select"/>
		<property name="startTime" type="timestamp">
			<column name="start_time_" not-null="true" sql-type="timestamptz"/>
		</property>
		<property name="signTime" type="timestamp">
			<column name="claim_time_" not-null="false" sql-type="timestamptz"/>
		</property>
		<property name="endTime" type="timestamp">
			<column name="end_time_" not-null="false" sql-type="timestamptz"/>
		</property>
		<property name="assignee" type="string">
			<column name="assignee_" not-null="false" length="255"/>
		</property>
	</class>

	<class entity-name="TaskCandidate" subselect="select id_,task_id_,group_id_,user_id_ from act_ru_identitylink where type_='candidate'">
        <id name="id" type="string">
            <column name="id_" length="64"/>
        </id>
		<many-to-one name="task" column="task_id_" entity-name="WorkingTask"/>
		<property name="groupId" type="string">
			<column name="group_id_" not-null="false" length="255"/>
		</property>
		<property name="userId" type="string">
			<column name="user_id_" not-null="false" length="255"/>
		</property>
	</class>
</hibernate-mapping>