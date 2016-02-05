import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.zalando.stups.spring.http.client.ClientHttpRequestFactorySelector;

public class HttpClientSelectorTest {

	@Test
	public void createClientHttpRequestFactory() {
		ClientHttpRequestFactory factory = ClientHttpRequestFactorySelector.getRequestFactory();
		Assert.assertTrue(factory instanceof HttpComponentsClientHttpRequestFactory);
	}

}
