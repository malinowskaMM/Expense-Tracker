import { useTranslation } from "react-i18next";
import { ListItemButton, Stack, AlertTitle, ListItemText, Snackbar, Alert, List, ListItem, TextField, Box } from '@mui/material';
import "../../i18n";
import { AccountForgotPasswordRequestDto } from '../../types/AccountForgotPasswordRequestDto';
import { set, useForm } from "react-hook-form";
import { API_CALLBACK_ROUTE_FORGOT_PASSWORD, API_URL } from '../../consts';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';

const ForgotPasswordPanel = () => {
    const { t, i18n } = useTranslation();
    const logo = require("../../assets/logo.svg").default;
    const { register, handleSubmit } = useForm<AccountForgotPasswordRequestDto>();
    const navigate = useNavigate();
    const [openForgotPasswordFailedAlert, setOpenForgotPasswordFailedAlert] = useState(false);
    const [validationErrors, setValidationErrors] = useState<string[]>([]);
    const [isForgotPasswordButtonDisabled, setIsForgotPasswordButtonDisabled] = useState(true);
    const [email, setEmail] = useState('');
    const [alertHeight, setAlertHeight] = useState(0);
    const [openForgotPasswordSuccessAlert, setOpenForgotPasswordSuccessAlert] = useState(false);

    const handleForgotPasswordSuccessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenForgotPasswordSuccessAlert(false);
    }

    const handleForgotPasswordFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenForgotPasswordFailedAlert(false);
    };

    const handleAlertLoad = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAlertHeight(event.target.clientHeight);
        console.log(event.target.clientHeight);
    };

    const updateValidationErrors = (field: string, isValid: boolean) => {
        setValidationErrors((prevErrors) => {
            const updatedErrors = { ...prevErrors };
            if (!isValid) {
                switch (field) {
                    case 'email':
                        updatedErrors[field] = t('registerPanel.emailValidation');
                        break;
                    default:
                        break;
                }
            } else {
                delete updatedErrors[field];
            }

            if (Object.keys(updatedErrors).length === 0 && email !== '') {
                setIsForgotPasswordButtonDisabled(false);
            } else {
                setIsForgotPasswordButtonDisabled(true);
            }
            return updatedErrors;
        })
    };

    const validateEmail = (email: string) => {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const isValid = re.test(email);
        updateValidationErrors('email', isValid);
        return isValid;
    };

    const handleEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setEmail(event.target.value);
        validateEmail(event.target.value);
    };


    const onSubmit = handleSubmit((request: AccountForgotPasswordRequestDto) => {
        request.callbackRoute = API_CALLBACK_ROUTE_FORGOT_PASSWORD;
        request.email = email;

        let config = {
            method: 'POST',
            url: API_URL + '/auth/account/authenticate/forgot-password',
            headers: {
                'Content-Type': 'application/json'
            },
            data: JSON.stringify(request),
        };

        console.log(JSON.stringify(request));

        axios(config)
            .then((response) => {
                    setOpenForgotPasswordFailedAlert(false);
                    setOpenForgotPasswordSuccessAlert(true);
                    navigate('/login');
            })
            .catch(function (error) {
                console.log(JSON.stringify(error.data));
                setOpenForgotPasswordSuccessAlert(false);
                setOpenForgotPasswordFailedAlert(true);
            });
    }
    )


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
                    {t('forgotPasswordPanel.forgotPassword')}
                </div>
                <List style={listStyle}>
                    <Box component="form" onSubmit={onSubmit}>
                        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                            <ListItem>
                                <div>
                                    {t('forgotPasswordPanel.forgotPasswordDescription')}
                                </div>
                            </ListItem>
                            <ListItem>
                                <div className="font-link">
                                    {t('forgotPasswordPanel.email')}*
                                </div>
                            </ListItem>
                            <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                <TextField
                                    {...register("email")}
                                    required
                                    id="outlined-required-email"
                                    style={{ backgroundColor: 'white', width: '100%' }}
                                    variant='standard'
                                    onChange={handleEmailChange}
                                />
                            </ListItem>
                        </div>

                        <ListItem>
                            <ListItemButton disabled={isForgotPasswordButtonDisabled}>
                                <ListItemText>
                                    <div className="font-link" style={buttonTextStyle} onClick={onSubmit}>
                                        {t('forgotPasswordPanel.remind')}
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
                        <AlertTitle>{t('registerPanel.formValidation')}</AlertTitle>
                        {error}
                    </Alert>
                ))}
            </Stack>

            <Snackbar open={openForgotPasswordFailedAlert} autoHideDuration={6000} onClose={handleForgotPasswordFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleForgotPasswordFailedAlertClose}>
                    {t('forgotPasswordPanel.forgotPasswordFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openForgotPasswordSuccessAlert} autoHideDuration={6000} onClose={handleForgotPasswordSuccessAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleForgotPasswordSuccessAlertClose}>
                    {t('forgotPasswordPanel.forgotPasswordSuccess')}
                </Alert>
            </Snackbar>
        </div>
    );
}

export default ForgotPasswordPanel;