package org.optaweb.vehiclerouting.plugin.persistence;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
public class PersistenceConfig {

    private final DataSource dataSource;

    public PersistenceConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /*
     * @Bean
     * public SessionFactory sessionFactoryX(SessionFactory entityManagerFactory) {
     * return entityManagerFactory;
     * }
     */

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("org.optaweb.vehiclerouting.plugin.persistence");
        //        sessionFactory.setHibernateProperties(hibernateProperties());

        return sessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }
}
