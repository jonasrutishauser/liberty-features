package io.github.jonasrutishauser.liberty.micrometer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;
import org.osgi.framework.hooks.service.ListenerHook.ListenerInfo;

public class ProxyService<T> implements EventListenerHook, FindHook {

    private final Bundle bundle;
    private final ServiceReference<T> reference;
    private final BundleContext context;
    private ServiceRegistration<?> reg;

    ProxyService(BundleContext context, ServiceReference<T> reference, Bundle bundle) {
        this.context = context;
        this.bundle = bundle;
        this.reference = reference;
    }

    boolean open() throws BundleException {
        boolean active = bundle.getState() == Bundle.ACTIVE;
        if (active) {
            bundle.stop(Bundle.STOP_TRANSIENT);
        }
        reg = context.registerService(new String[] {FindHook.class.getName(), EventListenerHook.class.getName()}, this,
                null);
        if (active) {
            bundle.start(Bundle.START_TRANSIENT);
        }
        return active;
    }

    public void close() {
        reg.unregister();
    }

    @Override
    public void find(BundleContext context, String name, String filter, boolean allServices,
            Collection<ServiceReference<?>> references) {
        if (context.getBundle() == bundle) {
            references.remove(reference);
        } else if (Arrays.asList((String[]) reference.getProperty("objectClass")).contains(name)) {
            references.removeIf(ref -> ref.getBundle() == context.getBundle());
        }
    }

    @Override
    public void event(ServiceEvent event, Map<BundleContext, Collection<ListenerInfo>> listeners) {
        if (event.getServiceReference().equals(reference)) {
            listeners.remove(bundle.getBundleContext());
        }
    }

}
