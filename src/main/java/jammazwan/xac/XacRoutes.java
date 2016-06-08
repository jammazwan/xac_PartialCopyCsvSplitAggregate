package jammazwan.xac;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class XacRoutes extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("file:../jammazwan.shared/src/main/resources/data/csv/?noop=true&fileName=surname.csv")
				.unmarshal("csv").split(body()).process(new Processor() {
					public void process(Exchange exchange) throws Exception {
						String line = exchange.getIn().getBody(String.class);
						line = line.substring(1, line.indexOf(","));
						exchange.getIn().setBody(line);
						exchange.getIn().setHeader("CamelFileName", "surname.txt");
					}
				}).aggregate(header("CamelFileName"), new MyAggregationStrategy()).completionSize(100)
				.completionTimeout(1000).to("file://src/main/resources/data/txt/")
		;
	}

	public static class MyAggregationStrategy implements AggregationStrategy {

		public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
			if (oldExchange == null) {
				return newExchange;
			}
			String body1 = oldExchange.getIn().getBody(String.class);
			String body2 = newExchange.getIn().getBody(String.class);

			oldExchange.getIn().setBody(body1.trim() + "\n" + body2.trim());
			return oldExchange;
		}
	}
}