import { useTranslation } from "react-i18next";
import { ListItem, List, ListItemButton, ListItemText, TextField, Box, Stack, Alert, AlertTitle, Snackbar } from '@mui/material';
import "../../i18n";
import { useState } from "react";
import { useForm, SubmitHandler } from "react-hook-form";
import axios from "axios";
import { API_URL, API_CALLBACK_ROUTE } from "../../consts";
import { AccountConfirmPasswordRequestDto } from "../../types/AccountConfirmPasswordRequestDto";
import { useNavigate } from "react-router-dom";
import { useParams } from "react-router-dom";

const AccountForgotPasswordSetPanel = () => {
    const { t, i18n } = useTranslation();
    const logo = require("../../assets/logo.svg").default;
    const { register, handleSubmit } = useForm<AccountConfirmPasswordRequestDto>();
    const [password, setPassword] = useState('');
    const [repeatPassword, setRepeatPassword] = useState('');
    const [validationErrors, setValidationErrors] = useState<string[]>([]);
    const [alertHeight, setAlertHeight] = useState(0);
    const [savePasswordButtonDisabled, setSavePasswordButtonDisabled] = useState(true);
    const [openSetPasswordFailedAlert, setOpenSetPasswordFailedAlert] = useState(false);
    const navigate = useNavigate();
    const token = useParams().token;
    

    const onSubmit = handleSubmit((data: AccountConfirmPasswordRequestDto) => {
        data.token = token;

        let config = {
            method: 'POST',
            url: API_URL + '/auth/account/authenticate/forgot-password/confirm',
            headers: {
                'Content-Type': 'application/json'
            },
            data: JSON.stringify(data),
        };


        axios.request(config)
            .then((response) => {
                setOpenSetPasswordFailedAlert(false);
                navigate('/success-set-password');
            })
            .catch((error) => {
                setOpenSetPasswordFailedAlert(true);
            });

    });

    const handlePasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setPassword(event.target.value);
        validatePassword(event.target.value);
    };

    const handleRepeatPasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setRepeatPassword(event.target.value);
        validateRepeatPassword(password, event.target.value);
    };

    const validatePassword = (password: string) => {
        const re = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$/;
        const isValid = password.length > 8 && re.test(password);
        updateValidationErrors('password', isValid);
        return isValid;
    }

    const validateRepeatPassword = (password: string, repeatPassword: string) => {
        const isValid = password.localeCompare(repeatPassword);
        updateValidationErrors('repeated password', isValid === 0);
        return isValid;
    }

    const handleAlertLoad = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAlertHeight(event.target.clientHeight);
        console.log(event.target.clientHeight);
    };

    const handleSetOpenSetPasswordFailedAlertClose = (event: React.SyntheticEvent | React.MouseEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenSetPasswordFailedAlert(false);
    }

    const updateValidationErrors = (field: string, isValid: boolean) => {
        setValidationErrors((prevErrors) => {
            const updatedErrors = { ...prevErrors };
            if (!isValid) {
                switch (field) {
                    case 'password':
                        updatedErrors[field] = t('adminAccountPasswordSetInitPanel.passwordValidation');
                        break;
                    case 'repeated password':
                        updatedErrors[field] = t('adminAccountPasswordSetInitPanel.repeatPasswordValidation');
                        break;
                    default:
                        break;
                }
            } else {
                delete updatedErrors[field];
            }

            if (Object.keys(updatedErrors).length === 0 && password !== '' && repeatPassword !== '') {
                setSavePasswordButtonDisabled(false);
            } else {
                setSavePasswordButtonDisabled(true);
            }

            return updatedErrors;
        });
    }

    const containerStyle = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        width: '100vw',
    };

    const boxStyle = {
        backgroundColor: 'rgba(153, 237, 197, 0.5)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        width: '40%',
        height: '70%',
        borderRadius: '5vh',
    };

    const listStyle = {
        width: '80%',
        aliginItems: 'center',
        justifyContent: 'center'
    };

    const buttonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 1)',
        borderRadius: '5vh',
        fontSize: '1.5rem',
        color: 'rgba(245, 249, 246, 1)',
    };

    const imageStyle = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center'
    };

    const headerTextStyle = {
        textAlign: 'center' as const,
        fontSize: '3.0rem',
        color: 'rgba(43, 42, 42, 0.8)',

    };


    return (
        <div style={containerStyle} className="font-link">
            <Box sx={boxStyle}>
                <div style={imageStyle}>
                    <img src={logo} alt='logo' style={{ width: '25%' }} />
                </div>

                <div style={headerTextStyle}>
                    {t('accountForgotPasswordSetPanel.accountPasswordSetInit')}
                </div>

                <List style={listStyle}>
                <Box component="form" onSubmit={onSubmit}>
                    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                        <ListItem>
                            <div className="font-link">
                                {t('accountForgotPasswordSetPanel.password')}*
                            </div>
                        </ListItem>
                        <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                            <TextField
                                {...register("password")}
                                required
                                id="outlined-required-email"
                                style={{ backgroundColor: 'white', width: '100%' }}
                                type='password'
                                variant='standard'
                                value={password}
                                onChange={handlePasswordChange}
                            />
                        </ListItem>

                        <ListItem>
                            <div className="font-link">
                                {t('accountForgotPasswordSetPanel.repeatPassword')}*
                            </div>
                        </ListItem>
                        <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                            <TextField
                                {...register("repeatPassword")}
                                required
                                id="outlined-required-password"
                                style={{ backgroundColor: 'white', width: '100%' }}
                                type='password'
                                variant='standard'
                                value={repeatPassword}
                                onChange={handleRepeatPasswordChange}
                            />
                        </ListItem>
                    </div>


                    <ListItem>
                        <ListItemButton disabled={savePasswordButtonDisabled} onClick={onSubmit}>
                            <ListItemText>
                                <div className="font-link" style={buttonTextStyle}>
                                    {t('accountForgotPasswordSetPanel.setPassword')}
                                </div>
                            </ListItemText>
                        </ListItemButton>
                    </ListItem>
                    </Box>
                </List>
            </Box>

            <Stack sx={{
                    width: '20%',
                    position: 'fixed',
                    bottom: 16 + alertHeight,
                    right: 16,
                    zIndex: 1000,
                }} spacing={2}>
                    {Object.values(validationErrors).map((error, index) => (
                        <Alert key={index} severity="error" onLoad={handleAlertLoad}>
                            <AlertTitle>{t('accountForgotPasswordSetPanel.formValidation')}</AlertTitle>
                            {error}
                        </Alert>
                    ))}
                </Stack>

                <Snackbar open={openSetPasswordFailedAlert} autoHideDuration={6000} onClose={handleSetOpenSetPasswordFailedAlertClose}>
                    <Alert severity="error" sx={{ width: '100%' }} onClose={handleSetOpenSetPasswordFailedAlertClose}>
                        {t('accountForgotPasswordSetPanel.setPasswordFailed')}
                    </Alert>
                </Snackbar>
        </div>
    );
}

export default AccountForgotPasswordSetPanel;