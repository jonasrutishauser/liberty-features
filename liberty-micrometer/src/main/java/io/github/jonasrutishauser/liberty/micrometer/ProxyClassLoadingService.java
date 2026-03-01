package io.github.jonasrutishauser.liberty.micrometer;

import java.io.File;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.classloading.ClassLoaderConfiguration;
import com.ibm.wsspi.classloading.ClassLoaderIdentity;
import com.ibm.wsspi.classloading.ClassLoadingService;
import com.ibm.wsspi.classloading.ClassTransformer;
import com.ibm.wsspi.classloading.GatewayConfiguration;
import com.ibm.wsspi.library.Library;

import io.micrometer.core.instrument.Metrics;

@Component
public class ProxyClassLoadingService implements ClassLoadingService {

    private ClassLoadingService delegate;

    @Override
    public ClassLoader createBundleAddOnClassLoader(List<File> classPath, ClassLoader gwClassLoader,
            ClassLoaderConfiguration clConfig) {
        return delegate.createBundleAddOnClassLoader(classPath,
                delegate.unify(gwClassLoader, Metrics.class.getClassLoader()), clConfig);
    }

    @Override
    public ClassLoader createChildClassLoader(List<Container> arg0, ClassLoaderConfiguration arg1) {
        return delegate.createChildClassLoader(arg0, arg1);
    }

    @Override
    public ClassLoaderConfiguration createClassLoaderConfiguration() {
        return delegate.createClassLoaderConfiguration();
    }

    @Override
    public GatewayConfiguration createGatewayConfiguration() {
        return delegate.createGatewayConfiguration();
    }

    @Override
    public ClassLoaderIdentity createIdentity(String arg0, String arg1) {
        return delegate.createIdentity(arg0, arg1);
    }

    @Override
    public ClassLoader createThreadContextClassLoader(ClassLoader arg0) {
        return delegate.createThreadContextClassLoader(arg0);
    }

    @Override
    public ClassLoader createTopLevelClassLoader(List<Container> arg0, GatewayConfiguration arg1,
            ClassLoaderConfiguration arg2) {
        return delegate.createTopLevelClassLoader(arg0, arg1, arg2);
    }

    @Override
    public void destroyThreadContextClassLoader(ClassLoader arg0) {
        delegate.destroyThreadContextClassLoader(arg0);
    }

    @Override
    public ClassLoader getShadowClassLoader(ClassLoader arg0) {
        return delegate.getShadowClassLoader(arg0);
    }

    @Override
    public ClassLoader getSharedLibraryClassLoader(Library arg0) {
        return delegate.getSharedLibraryClassLoader(arg0);
    }

    @Override
    public boolean isAppClassLoader(ClassLoader arg0) {
        return delegate.isAppClassLoader(arg0);
    }

    @Override
    public boolean isThreadContextClassLoader(ClassLoader arg0) {
        return delegate.isThreadContextClassLoader(arg0);
    }

    @Override
    public boolean registerTransformer(ClassTransformer arg0, ClassLoader arg1) {
        return delegate.registerTransformer(arg0, arg1);
    }

    @Override
    public void setSharedLibraryProtectionDomains(Map<String, ProtectionDomain> arg0) {
        delegate.setSharedLibraryProtectionDomains(arg0);
    }

    @Override
    public ClassLoader unify(ClassLoader arg0, ClassLoader... arg1) {
        return delegate.unify(arg0, arg1);
    }

    @Override
    public boolean unregisterTransformer(ClassTransformer arg0, ClassLoader arg1) {
        return delegate.unregisterTransformer(arg0, arg1);
    }

    @Reference(name = "classLoadingService", service = ClassLoadingService.class)
    protected void setClassLoadingService(ClassLoadingService ref) {
        delegate = ref;
    }

    protected void unsetClassLoadingService(ClassLoadingService ref) {
        delegate = null;
    }

}
