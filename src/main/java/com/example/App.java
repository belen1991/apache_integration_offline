package com.example;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultRegistry;
import org.apache.commons.dbcp.BasicDataSource;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String... args) throws Exception {
        String URLJDBC = "jdbc:postgresql://localhost:5432/belen_db";
        DataSource dataSource = setupDataSource(URLJDBC);
        
        DefaultRegistry reg = new DefaultRegistry();
        reg.bind("myDataSource",dataSource);

        CamelContext context = new DefaultCamelContext(reg);
        context.addRoutes(new CsvToDbRoute());
        context.getRegistry().bind("filterBean", new FilterBean());
        context.start();
        
        // Keep application running
        synchronized (context) {
            context.wait();
        }
    }

    private static DataSource setupDataSource(String jdbcURL) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUsername("belen");
        ds.setPassword("belen");
        ds.setUrl(jdbcURL);
        return ds;
    }
}
