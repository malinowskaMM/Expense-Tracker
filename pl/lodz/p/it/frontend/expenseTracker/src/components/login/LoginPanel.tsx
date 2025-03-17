import { useTranslation } from "react-i18next";
import { Button, Link, Stack, AlertTitle, ListItemButton, Box, ListItemText, List, ListItem, TextField, Snackbar, Alert } from '@mui/material';
import "../../i18n";
import { useNavigate } from 'react-router-dom';
import { AccountAuthenticationRequestDto } from '../../types/AccountAuthenticationRequestDto';
import { useForm } from 'react-hook-form';
import { API_URL } from '../../consts';
import axios from 'axios';
import React, { useState } from 'react';

const LoginPanel = () => {
    const { t, i18n } = useTranslation();
    const logo = require("../../assets/logo.svg").default;
    const navigate = useNavigate();
    const { register, handleSubmit } = useForm<AccountAuthenticationRequestDto>();
    const [openLoginFailedAlert, setOpenLoginFailedAlert] = useState(false);
    const [validationErrors, setValidationErrors] = useState<string[]>([]);
    const [alertHeight, setAlertHeight] = useState(0);
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isLoginButtonDisabled, setIsLoginButtonDisabled] = React.useState(true);

    const handleLoginFailedAlertClose = () => {
        setOpenLoginFailedAlert(false);
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
                    case 'password':
                        //updatedErrors[field] = t('registerPanel.passwordValidation');
                        break;
                    default:
                        break;
                }
            } else {
                delete updatedErrors[field];
            }

            if (Object.keys(updatedErrors).length === 0 && email.length !== 0 && password.length !== 0) {
                setIsLoginButtonDisabled(false);
            } else {
                setIsLoginButtonDisabled(true);
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

    const handlePasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setPassword(event.target.value);
        updateValidationErrors('password', event.target.value.length > 0);
    };

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

    const handleRegisterClick = () => {
        navigate('/register');
    }

    const handleForgotPasswordClick = () => {
        navigate('/forgot-password');
    }

    const handleLoginClick = handleSubmit((data: AccountAuthenticationRequestDto) => {

        let config = {
            method: 'POST',
            url: API_URL + '/auth/account/authenticate',
            headers: {
                'Content-Type': 'application/json'
            },
            data: JSON.stringify(data),
        };

        axios.request(config)
            .then((response) => {
                localStorage.setItem('token', response.data.authenticationToken);
                localStorage.setItem('id', response.data.id);
                localStorage.setItem('role', response.data.role);
                i18n.changeLanguage(response.data.language);
                setOpenLoginFailedAlert(false);
                navigate('/');
            })
            .catch((error) => {
                setOpenLoginFailedAlert(true);
            });

    }
    );

    return (
        <div style={containerStyle} className="font-link">
            <Box sx={boxStyle}>
                <div style={imageStyle}>
                    <img src={logo} alt='logo' style={{ width: '25%' }} />
                </div>

                <div style={headerTextStyle}>
                    {t('loginPanel.login')}
                </div>

                <List style={listStyle}>
                    <Box component="form" onSubmit={handleLoginClick}>
                        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                            <ListItem>
                                <div className="font-link">
                                    {t('loginPanel.email')}*
                                </div>
                            </ListItem>
                            <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                <TextField
                                    {...register('email')}
                                    value={email}
                                    required
                                    id="outlined-required-email"
                                    style={{ backgroundColor: 'white', width: '100%' }}
                                    variant='standard'
                                    onChange={handleEmailChange}
                                />
                            </ListItem>

                            <ListItem>
                                <div className="font-link">
                                    {t('loginPanel.password')}*
                                </div>
                            </ListItem>
                            <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                <TextField
                                    {...register('password')}
                                    value={password}
                                    required
                                    id="outlined-required-password"
                                    style={{ backgroundColor: 'white', width: '100%' }}
                                    type='password'
                                    variant='standard'
                                    onChange={handlePasswordChange}
                                />
                            </ListItem>
                        </div>

                        <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                            <Button style={{ textTransform: 'initial' }} onClick={handleForgotPasswordClick}>
                                <Link>
                                    <div className="font-link" >
                                        {t('loginPanel.forgotPassword')}
                                    </div>
                                </Link>
                            </Button>
                        </ListItem>

                        <ListItem>
                            <ListItemButton>
                                <ListItemText>
                                    <div className="font-link" style={buttonTextStyle} onClick={handleRegisterClick} >
                                        {t('loginPanel.register')}
                                    </div>
                                </ListItemText>
                            </ListItemButton>
                            <ListItemButton disabled={isLoginButtonDisabled} onChange={() => console.log(isLoginButtonDisabled)}>
                                <ListItemText>
                                    <div className="font-link" style={buttonTextStyle} onClick={handleLoginClick}>
                                        {t('loginPanel.login')}
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

            <Snackbar open={openLoginFailedAlert} autoHideDuration={6000} onClose={handleLoginFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleLoginFailedAlertClose}>
                    {t('loginPanel.loginFailed')}
                </Alert>
            </Snackbar>
        </div>
    );

}

export default LoginPanel;