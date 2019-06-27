package proj.kedabra.billsnap.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ErrorMessageConfiguration {


    @Bean
    public MessageSource messageSource() {
        var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Please note that if you extend WebMvcConfigurerAdapter, this method will not longer work and you must then override the validator on whatever class just inherited
     * the WebMvcConfigurerAdapter
     *
     * @return validator bean that resolves message properties.
     */
    @Bean
    public LocalValidatorFactoryBean getValidator() {
        var localValidatorBean = new LocalValidatorFactoryBean();
        localValidatorBean.setValidationMessageSource(messageSource());
        return localValidatorBean;
    }
}
