package io.github.jonasrutishauser.liberty.micrometer;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.ibm.wsspi.classloading.ClassLoadingService;

public class Activator implements BundleActivator {

    private ProxyService<ClassLoadingService> proxyClassLoadingService;

    @Override
    public void start(BundleContext context) throws Exception {
        ServiceReference<ClassLoadingService> serviceReference = context.getServiceReference(ClassLoadingService.class);

        Bundle microprofileMetricsBundle = null;
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getSymbolicName().matches(
                    Pattern.quote("io.openliberty.microprofile.metrics.5.") + "\\d" + Pattern.quote(".internal"))) {
                microprofileMetricsBundle = bundle;
            }
        }

        if (microprofileMetricsBundle != null) {
            Field isSuccessfulActivation = microprofileMetricsBundle.loadClass("io.openliberty.microprofile.metrics50.internal.SmallryeMetricsCDIMetadata").getDeclaredField("isSuccessfulActivation");
            isSuccessfulActivation.setAccessible(true);
            isSuccessfulActivation.set(null, true);
            proxyClassLoadingService = new ProxyService<>(context, serviceReference, microprofileMetricsBundle);
            if (proxyClassLoadingService.open()) {
                Class<?> adapter = microprofileMetricsBundle.loadClass("io.openliberty.smallrye.metrics.adapters.SRSharedMetricRegistriesAdapter");
                Field instance = adapter.getDeclaredField("instance");
                instance.setAccessible(true);
                instance.set(null, null);
            }
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (proxyClassLoadingService != null) {
            proxyClassLoadingService.close();
            proxyClassLoadingService = null;
        }
    }

}
