package pl.lodz.p.it.expenseTracker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@PropertySource({ "classpath:application.properties" })
@EnableJpaRepositories(
        basePackages = "pl.lodz.p.it.expenseTracker.repository.administration",
        entityManagerFactoryRef = "administrationEntityManager",
        transactionManagerRef = "administrationTransactionManager"
)
@Profile("!test")
public class PersistenceAdministrationConfiguration {

  @Autowired
  private Environment env;

  @Bean
  @Primary
  public LocalContainerEntityManagerFactoryBean administrationEntityManager() {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(administrationDataSource());
    em.setPackagesToScan(new String[] { "pl.lodz.p.it.expenseTracker.domain.entity" });

    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter(vendorAdapter);
    HashMap<String, Object> properties = new HashMap<>();
    properties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.properties.hibernate.hbm2ddl.auto"));
    properties.put("hibernate.dialect", env.getProperty("spring.jpa.properties.hibernate.dialect"));
    em.setJpaPropertyMap(properties);
    return em;
  }

  @Primary
  @Bean
  public DataSource administrationDataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
    dataSource.setUrl("jdbc:postgresql://postgres/postgres");
    dataSource.setUsername("expensetrackeradmin");
    dataSource.setPassword("obSjEBGaX");
    return dataSource;
  }

  @Primary
  @Bean
  public PlatformTransactionManager administrationTransactionManager() {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(administrationEntityManager().getObject());
    return transactionManager;
  }
}