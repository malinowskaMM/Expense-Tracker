package pl.lodz.p.it.expenseTracker.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@PropertySource({ "classpath:application.properties" })
@EnableJpaRepositories(
        basePackages = "pl.lodz.p.it.expenseTracker.repository.administration",
        entityManagerFactoryRef = "administrationEntityManager",
        transactionManagerRef = "administrationTransactionManager"
)
@Profile("test")
public class PersistenceAdministrationTestConfiguration {

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
    PostgreSQLContainer<?> postgresContainer = SharedPostgresContainer.getInstance();

    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
    dataSource.setUrl(postgresContainer.getJdbcUrl());
    dataSource.setUsername(postgresContainer.getUsername());
    dataSource.setPassword(postgresContainer.getPassword());

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