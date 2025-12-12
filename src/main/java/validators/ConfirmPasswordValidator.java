package validators;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import beans.CustomerBean;

@FacesValidator("confirmPasswordValidator")
public class ConfirmPasswordValidator implements Validator<Object> {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {

        CustomerBean bean = context.getApplication()
                .evaluateExpressionGet(context, "#{customerBean}", CustomerBean.class);

        String confirmPassword = (String) value;
        String password = bean.getPassword();

        if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
            throw new ValidatorException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Passwords do not match", null));
        }
    }
}
