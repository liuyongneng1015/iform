package tech.ascs.icity.iform.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.apache.commons.io.IOUtils;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import tech.ascs.icity.iform.model.ColumnModelEntity;
import tech.ascs.icity.iform.model.ColumnReferenceEntity;
import tech.ascs.icity.iform.model.DataModelEntity;
import tech.ascs.icity.iform.support.internal.IFormMetadataSources;

@Service
public class IFormSessionFactoryBuilder {

	@PersistenceUnit
	private EntityManagerFactory entityMangerFactory;

	private StandardServiceRegistry serviceRegistry;

	private Map<String, SessionFactory> sessionFactories = new HashMap<String, SessionFactory>();

	public SessionFactory getSessionFactory(DataModelEntity dataModel) throws Exception {
		return getSessionFactory(dataModel, false);
	}

	public SessionFactory getSessionFactory(DataModelEntity dataModel, boolean forceNewInstance) throws Exception {
		if (forceNewInstance || sessionFactories.get(dataModel.getId()) == null) {
			if (sessionFactories.get(dataModel.getId()) != null) {
				invalidSessionFactory(sessionFactories.get(dataModel.getId()));
			}

			SessionFactory sessionFactory = createNewSessionFactory(dataModel);

			sessionFactories.put(dataModel.getId(), sessionFactory);
			for (DataModelEntity referenceDataModel : dataModel.getReferencesDataModel()) {
				sessionFactories.put(referenceDataModel.getId(), sessionFactory);
			}
		}
		
		return sessionFactories.get(dataModel.getId());
	}

	protected void invalidSessionFactory(SessionFactory sessionFactory) {
		sessionFactories.entrySet().removeIf(matches -> sessionFactory.equals(matches.getValue()));
	}

	public SessionFactory createNewSessionFactory(InputStream stream) throws Exception {
		Metadata metadata;
		metadata = new IFormMetadataSources(standardRegistry())
				.addInputStream(stream)
				.getMetadataBuilder().build();
		SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
		return sessionFactoryBuilder.build();
	}

	protected SessionFactory createNewSessionFactory(DataModelEntity dataModel) throws Exception {
		Metadata metadata;
		metadata = new IFormMetadataSources(standardRegistry())
				.addInputStream(IOUtils.toInputStream(generateHibernateMapping(dataModel), "UTF-8"))
				.getMetadataBuilder().build();
		SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
		return sessionFactoryBuilder.build();
	}

	protected StandardServiceRegistry standardRegistry() {
		if (serviceRegistry == null) {
			SessionFactory sessionFactory = entityMangerFactory.unwrap(SessionFactory.class);
			serviceRegistry = new StandardServiceRegistryBuilder()
					.applySetting(AvailableSettings.DATASOURCE, SessionFactoryUtils.getDataSource(sessionFactory))
					.applySetting(AvailableSettings.DIALECT, ((SessionFactoryImplementor) sessionFactory).getServiceRegistry().getService( JdbcServices.class ).getDialect().getClass())
					.applySetting(AvailableSettings.DEFAULT_ENTITY_MODE, EntityMode.MAP.getExternalName())
					.applySetting(AvailableSettings.HBM2DDL_AUTO, "update")
					.applySetting("hibernate.temp.use_jdbc_metadata_defaults", false)
					.build();
		}
		return serviceRegistry;
	}

	public String generateHibernateMapping(DataModelEntity dataModel) throws Exception {
		/*if(dataModel.getSynchronized() == null || !dataModel.getSynchronized()){
			throw new IFormException("数据模型【"+dataModel.getTableName()+"】未同步");
		}*/
		setReferenceDataModel(dataModel);
		return generateHibernateMappingFreeMarker(dataModel);
	}

	private void setReferenceDataModel(DataModelEntity dataModel){
		Set<DataModelEntity> allDataModels = new HashSet<>();
		allDataModels.add(dataModel);
		//子表
		List<DataModelEntity> slaverModels = dataModel.getSlaverModels();
		allDataModels.addAll(slaverModels);

		//关联表
		Set<DataModelEntity> referencesDataModel = new HashSet<>();

		//主表关联
		setReferencesDataModel(allDataModels, referencesDataModel, dataModel);
		for(DataModelEntity slaverDataModel: slaverModels){
			//子表关联
			setReferencesDataModel(allDataModels, referencesDataModel, slaverDataModel);
		}
		dataModel.setReferencesDataModel(new ArrayList<>(referencesDataModel));
	}

	private void setReferencesDataModel(Set<DataModelEntity> allDataModels, Set<DataModelEntity> referencesDataModel, DataModelEntity dataModelEntity1){
		for(ColumnModelEntity columnModelEntity : dataModelEntity1.getColumns()) {
			for (ColumnReferenceEntity entity : columnModelEntity.getColumnReferences()) {
				DataModelEntity dataModelEntity = entity.getToColumn().getDataModel();
                if(dataModelEntity == null){
                    continue;
                }
				if (!referencesDataModel.contains(dataModelEntity) && !allDataModels.contains(dataModelEntity)) {
					referencesDataModel.add(dataModelEntity);
					allDataModels.add(dataModelEntity);
					//关联表的关联
					setReferencesDataModel( allDataModels,  referencesDataModel, dataModelEntity);
				}
			}
		}
	}

	protected String generateHibernateMappingThymeleaf(DataModelEntity dataModel) {
        TemplateEngine templateEngine = new SpringTemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.XML);
        templateEngine.setTemplateResolver(templateResolver);
        Context context = new Context();
        context.setVariable("dataModel", dataModel);
        StringWriter stringWriter = new StringWriter();
        templateEngine.process("mapping.xml", context, stringWriter);
        return stringWriter.toString();
	}



	private Configuration cfg;

	protected String generateHibernateMappingFreeMarker(DataModelEntity dataModel) throws IOException {
		Configuration cfg = getConfiguration();
		Template template = cfg.getTemplate("mapping.ftl");
		Map<String, Object> root = new HashMap<String, Object>();
        root.put("dataModel", dataModel);
        StringWriter stringWriter = new StringWriter();
        try {
			template.process(root, stringWriter);
		} catch (TemplateException e) {
			throw new IOException(e);
		}
        return stringWriter.toString();
	}

	private Configuration getConfiguration() {
		if (cfg == null) {
			cfg = new Configuration(Configuration.VERSION_2_3_28);
			cfg.setClassForTemplateLoading(IFormSessionFactoryBuilder.class, "/");
			cfg.setDefaultEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		}
		return cfg;
	}
}
