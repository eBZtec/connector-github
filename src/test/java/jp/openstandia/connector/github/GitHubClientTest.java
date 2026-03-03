package jp.openstandia.connector.github;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import org.identityconnectors.common.security.GuardedString;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

class GitHubClientTest {

    private static class DummyClient implements GitHubClient<AbstractGitHubSchema<AbstractGitHubConfiguration>> {
        @Override public void setInstanceName(String instanceName) {}
        @Override public void test() {}
        @Override public void auth() {}
        @Override public void close() {}
    }

    private AbstractGitHubConfiguration baseConfig(
            long connectMs, long readMs, long writeMs
    ) {
        AbstractGitHubConfiguration cfg = mock(AbstractGitHubConfiguration.class);
        when(cfg.getConnectionTimeoutInMilliseconds()).thenReturn((int) connectMs);
        when(cfg.getReadTimeoutInMilliseconds()).thenReturn((int) readMs);
        when(cfg.getWriteTimeoutInMilliseconds()).thenReturn((int) writeMs);

        when(cfg.getHttpProxyHost()).thenReturn("");
        when(cfg.getHttpProxyPort()).thenReturn(0);
        when(cfg.getHttpProxyUser()).thenReturn("");
        when(cfg.getHttpProxyPassword()).thenReturn(null);

        return cfg;
    }

    @Test
    void createClient_withoutProxy_usesTimeouts_and_noProxy() {
        DummyClient client = new DummyClient();
        AbstractGitHubConfiguration cfg = baseConfig(1234, 5678, 9999);

        OkHttpClient ok = client.createClient(cfg);

        assertEquals(1234, ok.connectTimeoutMillis());
        assertEquals(5678, ok.readTimeoutMillis());
        assertEquals(9999, ok.writeTimeoutMillis());
        assertNull(ok.proxy(), "Não deveria haver proxy quando host está vazio");
    }

    @Test
    void createClient_withProxy_withoutAuth_setsProxyOnly() {
        DummyClient client = new DummyClient();
        AbstractGitHubConfiguration cfg = baseConfig(2000, 3000, 4000);

        when(cfg.getHttpProxyHost()).thenReturn("proxy.local");
        when(cfg.getHttpProxyPort()).thenReturn(8080);
        when(cfg.getHttpProxyUser()).thenReturn("");
        when(cfg.getHttpProxyPassword()).thenReturn(null);

        OkHttpClient ok = client.createClient(cfg);

        Proxy proxy = ok.proxy();
        assertNotNull(proxy, "Proxy deveria estar configurado");
        assertEquals(Proxy.Type.HTTP, proxy.type());
        InetSocketAddress addr = (InetSocketAddress) proxy.address();
        assertEquals("proxy.local", addr.getHostString());
        assertEquals(8080, addr.getPort());

        Authenticator pa = ok.proxyAuthenticator();
        assertSame(Authenticator.NONE, pa, "Não deveria configurar proxyAuthenticator sem user/password");
    }

    @Test
    void createClient_withProxy_andAuth_setsProxyAndAuthenticator() {
        DummyClient client = new DummyClient();
        AbstractGitHubConfiguration cfg = baseConfig(1000, 1000, 1000);

        when(cfg.getHttpProxyHost()).thenReturn("corp-proxy");
        when(cfg.getHttpProxyPort()).thenReturn(3128);
        when(cfg.getHttpProxyUser()).thenReturn("user1");
        when(cfg.getHttpProxyPassword()).thenReturn(new GuardedString("secret".toCharArray()));

        OkHttpClient ok = client.createClient(cfg);

        Proxy proxy = ok.proxy();
        assertNotNull(proxy);
        assertEquals(Proxy.Type.HTTP, proxy.type());

        Authenticator pa = ok.proxyAuthenticator();
        assertNotNull(pa);
        assertNotSame(Authenticator.NONE, pa, "Deveria haver um proxyAuthenticator quando user/senha estão presentes");
    }

    @Test
    void createClient_withProxy_andAuth_coversProxyAuthenticatorLambda() throws IOException {
        DummyClient client = new DummyClient();
        AbstractGitHubConfiguration cfg = baseConfig(1000, 1000, 1000);

        when(cfg.getHttpProxyHost()).thenReturn("proxy.local");
        when(cfg.getHttpProxyPort()).thenReturn(8080);
        when(cfg.getHttpProxyUser()).thenReturn("testuser");
        when(cfg.getHttpProxyPassword()).thenReturn(new GuardedString("testpass".toCharArray()));

        OkHttpClient ok = client.createClient(cfg);

        Authenticator pa = ok.proxyAuthenticator();

        Request request = new Request.Builder()
                .url("http://example.com")
                .build();

        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(407)
                .message("Proxy Authentication Required")
                .body(ResponseBody.create("", MediaType.get("text/plain")))
                .build();

        Request authenticated = pa.authenticate(null, response);

        assertNotNull(authenticated);

        String authHeader = authenticated.header("Proxy-Authorization");
        assertNotNull(authHeader, "Deveria ter header Proxy-Authorization");
        assertTrue(authHeader.startsWith("Basic "), "Deveria começar com 'Basic '");
        assertTrue(authHeader.contains("dGVzdHVzZXI6"), "Deveria conter base64 do username (senha é zerada pelo GuardedString)");
    }
}


