package io.github.jonasrutishauser.liberty.micrometer;

import java.util.Set;

import org.osgi.service.component.annotations.Component;

import io.github.jonasrutishauser.liberty.micrometer.cdi.MeterRegistryProvider;
import io.github.jonasrutishauser.liberty.micrometer.cdi.MicrometerExtension;
import io.openliberty.cdi.spi.CDIExtensionMetadata;
import jakarta.enterprise.inject.spi.Extension;

@Component
public class MicrometerCDIMetadata implements CDIExtensionMetadata {

    @Override
    public Set<Class<?>> getBeanClasses() {
        return Set.of(MeterRegistryProvider.class);
    }

    @Override
    public Set<Class<? extends Extension>> getExtensions() {
        return Set.of(MicrometerExtension.class);
    }

}
