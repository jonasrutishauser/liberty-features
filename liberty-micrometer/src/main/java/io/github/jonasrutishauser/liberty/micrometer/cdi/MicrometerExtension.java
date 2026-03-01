package io.github.jonasrutishauser.liberty.micrometer.cdi;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.micrometer.common.annotation.ValueExpressionResolver;
import io.micrometer.common.annotation.ValueResolver;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Singleton;

public class MicrometerExtension implements Extension {

    private final Set<Bean<? extends ValueResolver>> valueResolverBeans = new HashSet<>();
    private Bean<? extends ValueExpressionResolver> valueExpressionResolverBean;

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
        event.configureInterceptorBinding(Counted.class).methods()
                .forEach(member -> member.add(Nonbinding.Literal.INSTANCE));
        event.configureInterceptorBinding(Timed.class).methods()
                .forEach(member -> member.add(Nonbinding.Literal.INSTANCE));
        event.addAnnotatedType(MicrometerCountedInterceptor.class, MicrometerCountedInterceptor.class.getName())
                .add(new CountedLiteral());
        event.addAnnotatedType(MicrometerTimedInterceptor.class, MicrometerTimedInterceptor.class.getName());
    }

    <T extends ValueResolver> void discoverValueResolvers(@Observes ProcessBean<T> event) {
        if (event.getBean().getBeanClass() != null
                && ValueResolver.class.isAssignableFrom(event.getBean().getBeanClass())) {
            valueResolverBeans.add(event.getBean());
        }
    }

    <T extends ValueExpressionResolver> void discoverValueExpressionResolver(@Observes ProcessBean<T> event) {
        if (event.getBean().getBeanClass() != null
                && ValueResolver.class.isAssignableFrom(event.getBean().getBeanClass())) {
            if (valueExpressionResolverBean != null) {
                event.addDefinitionError(
                        new AmbiguousResolutionException("Multiple ValueExpressionResolver beans found: "
                                + valueExpressionResolverBean + " and " + event.getBean()));
            }
            valueExpressionResolverBean = event.getBean();
        }
    }

    void registerMeterTagsSupport(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        event.addBean() //
                .beanClass(MeterTagsSupport.class) //
                .addType(MeterTagsSupport.class) //
                .scope(Singleton.class) //
                .createWith(
                        ctx -> new MeterTagsSupport(
                                valueResolverBeans.stream()
                                        .collect(Collectors.toMap(Bean::getBeanClass,
                                                bean -> (ValueResolver) beanManager.getReference(bean,
                                                        ValueResolver.class, ctx))),
                                valueExpressionResolverBean == null ? null
                                        : (ValueExpressionResolver) beanManager.getReference(
                                                valueExpressionResolverBean, ValueExpressionResolver.class, ctx)));
    }

}
