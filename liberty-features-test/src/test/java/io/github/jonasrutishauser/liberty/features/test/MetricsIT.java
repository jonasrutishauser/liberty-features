package io.github.jonasrutishauser.liberty.features.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.util.IOUtils;

class MetricsIT {

    @Test
    void testEndpoint() throws IOException {
        InputStream stream = URI.create("http://localhost:9080/metrics").toURL().openStream();
        String result = IOUtils.toString(stream);
        stream.close();

        assertMetrics(result);
    }

    @Test
    void testEndpoint51() throws IOException {
        InputStream stream = URI.create("http://localhost:9081/metrics").toURL().openStream();
        String result = IOUtils.toString(stream);
        stream.close();

        assertMetrics(result);
    }

    private void assertMetrics(String result) {
        assertThat(result, containsString("classloader_loadedClasses_count{mp_scope=\"base\"} "));
        assertThat(result, containsString("session_create{appname=\"default_host_metrics\",mp_scope=\"vendor\"} "));
        assertThat(result, containsString("threadpool_size{mp_scope=\"vendor\",pool=\"Default_Executor\"} "));
        assertThat(result, containsString("sample_list{key=\"value\"} 0"));
        assertThat(result, containsString("count_me_beans_total{region=\"test\"} 1"));
        assertThat(result, containsString("startup_count_total{class=\"" + StartupBean.class.getName()
                + "\",event=\"Startup\",exception=\"none\",method=\"startup\",result=\"success\",type=\"startup\"} 1"));
        assertThat(result, containsString("# HELP startup_count_total Counts the number of startups"));
        assertThat(result, containsString("startup_time_seconds_count{class=\"" + StartupBean.class.getName()
                + "\",event=\"Startup\",exception=\"none\",method=\"startup\",type=\"startup\"} 1"));
    }

}
