package com.example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.apache.camel.processor.aggregate.MemoryAggregationRepository;

public class CsvToDbRoute extends RouteBuilder {

    private static String filterPassed = "filterPassed";
    private static String filtered = "filtered";
    private static String processed = "processed";

    @Override
    public void configure() throws Exception {
        CsvDataFormat csv = new CsvDataFormat();
        csv.setUseMaps(true);

        MemoryAggregationRepository repo = new MemoryAggregationRepository();

        from("file:data/inbox?fileName=cardsclients.csv&noop=true")
            .unmarshal(csv)
            .split(body()).streaming()
                .choice()
                    .when(method("filterBean", "filterRecord"))
                        .process(exchange -> {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> row = exchange.getIn().getBody(Map.class);
                        
                            List<String> intFields = Arrays.asList("LIMIT_BAL","AGE","PAY_0","PAY_2","PAY_3","PAY_4","PAY_5","PAY_6",
                                                                "BILL_1","BILL_2","BILL_3","BILL_4","BILL_5","BILL_6",
                                                                "PAY_1","PAY_2","PAY_3","PAY_4","PAY_5","PAY_6");

                            for (String field : intFields) {
                                if (row.containsKey(field)) {
                                    try {
                                        Integer intValue = Integer.parseInt(row.get(field).toString());
                                        row.put(field, intValue);
                                    } catch (NumberFormatException e) {
                                        log.error("Invalid format for field {}: {}", field, row.get(field));
                                    }
                                }
                            }
                        
                            Map<String, Integer> stringFields = Map.of(
                                "SEX", 10,
                                "EDUCATION", 50,
                                "MARRIAGE", 50
                            );
                            stringFields.forEach((field, maxLength) -> {
                                if (row.containsKey(field)) {
                                    String stringValue = row.get(field).toString();
                                    if (stringValue.length() > maxLength) {
                                        log.warn("Truncating field {} to max length {}", field, maxLength);
                                        stringValue = stringValue.substring(0, maxLength);
                                        row.put(field, stringValue);
                                    }
                                }
                            });
                        
                            var field = "default_payment_next_month";
                            if (row.containsKey(field)) {
                                Boolean boolValue = "true".equalsIgnoreCase(row.get(field).toString());
                                row.put(field, boolValue);
                            }
                        
                            exchange.getIn().setBody(row);
                        })                    
                        .to("sql:INSERT INTO clients (limit_bal, sex, education, marriage, age, pay_0, pay_2, pay_3, pay_4, pay_5, pay_6, bill_1, bill_2, bill_3, bill_4, bill_5, bill_6, pay_amt_1, pay_amt_2, pay_amt_3, pay_amt_4, pay_amt_5, pay_amt_6, default_payment_next_month) VALUES (:#LIMIT_BAL, :#SEX, :#EDUCATION, :#MARRIAGE, :#AGE, :#PAY_0, :#PAY_2, :#PAY_3, :#PAY_4, :#PAY_5, :#PAY_6, :#BILL_1, :#BILL_2, :#BILL_3, :#BILL_4, :#BILL_5, :#BILL_6, :#PAY_1, :#PAY_2, :#PAY_3, :#PAY_4, :#PAY_5, :#PAY_6, :#default_payment_next_month)?dataSource=#myDataSource")
                        .setProperty(filterPassed, constant(true))
                    .otherwise()
                        .setProperty(filterPassed, constant(false))
                .end()
                .aggregate(constant(true), new CountAggregationStrategy())
                    .completionTimeout(10000)
                    .aggregationRepository(repo)
                    .process(this::logResults)
                .end();
    }

    private void logResults(Exchange exchange) {
        Integer intProcessed = exchange.getProperty(processed, Integer.class);
        Integer intFiltered = exchange.getProperty(filtered, Integer.class);
        log.info("Total records processed and inserted into database: {}", intProcessed);
        log.info("Total records filtered and not inserted: {}", intFiltered);
    }

    private static class CountAggregationStrategy implements AggregationStrategy {
        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            if (oldExchange == null) {
                newExchange.setProperty(processed, 0);
                newExchange.setProperty(filtered, 0);
                return newExchange;
            }

            Integer intProcessed = oldExchange.getProperty(processed, Integer.class);
            Integer intFiltered = oldExchange.getProperty(filtered, Integer.class);

            if (Boolean.TRUE.equals(newExchange.getProperty(filterPassed, Boolean.class))) {
                intProcessed++;
            } else {
                intFiltered++;
            }

            oldExchange.setProperty(processed, intProcessed);
            oldExchange.setProperty(filtered, intFiltered);

            return oldExchange;
        }
    }
}