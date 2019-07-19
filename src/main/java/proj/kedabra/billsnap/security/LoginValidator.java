package proj.kedabra.billsnap.security;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import proj.kedabra.billsnap.presentation.resources.LoginResource;

public class LoginValidator implements Validator {

    private final SpringValidatorAdapter validatorAdapter;

    public LoginValidator(SpringValidatorAdapter validatorAdapter) {
        super();
        this.validatorAdapter = validatorAdapter;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return LoginResource.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validatorAdapter.validate(target, errors); // JSR-303 constraint validation

        // more complex validation rules can go here in the future if needed
    }
}
