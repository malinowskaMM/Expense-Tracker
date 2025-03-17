import { useTranslation } from "react-i18next";
import { ListItemButton, FormControlLabel, FormGroup, Checkbox, ListItemText, Select, MenuItem, SelectChangeEvent, Alert, AlertTitle, Stack, List, ListItem, TextField, Box, Snackbar, Backdrop, CircularProgress } from '@mui/material';
import "../../i18n";
import React, { useState } from 'react';
import axios from 'axios';
import { API_URL, API_CALLBACK_ROUTE } from '../../consts';
import { useForm } from "react-hook-form";
import { AccountRegisterRequestDto } from '../../types/AccountRegisterRequestDto';
import { useNavigate } from 'react-router-dom';

const RegistrationPanel = () => {
    const { t, i18n } = useTranslation();
    const logo = require("../../assets/logo.svg").default;
    const pl = require("../../assets/pl.svg").default;
    const gb = require("../../assets/gb.svg").default;
    const [email, setEmail] = React.useState<string>('');
    const [password, setPassword] = React.useState<string>('');
    const [repeatPassword, setRepeatPassword] = React.useState<string>('');
    const [language, setLanguage] = React.useState<string>('plPL');
    const [validationErrors, setValidationErrors] = useState<string[]>([]);
    const [alertHeight, setAlertHeight] = useState(0);
    const { register, handleSubmit } = useForm<AccountRegisterRequestDto>();
    const navigate = useNavigate();
    const [openRegistrationFailedAlert, setOpenRegistrationFailedAlert] = React.useState(false);
    const [loadingBackdropOpen, setLoadingBackdropOpen] = React.useState(false);
    const [isRegisterButtonDisabled , setIsRegisterButtonDisabled] = React.useState(true);
    const [showPasswords, setShowPasswords] = React.useState(false);

    const handleCheckboxShowPasswordsChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setShowPasswords(!showPasswords);
    };

    const handleRegistrationFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenRegistrationFailedAlert(false);
    };

    const handleLogin = (event: React.SyntheticEvent<unknown>, reason?: string) => {
        navigate('/login');
    }

    const handleRegister = (event: React.SyntheticEvent<unknown>, reason?: string) => {
        onSubmit();
    };

    const onSubmit = handleSubmit((data: AccountRegisterRequestDto) => {
        setLoadingBackdropOpen(true);
        data.callbackRoute = API_CALLBACK_ROUTE.toString();
        data.language_ = language;

        let config = {
            method: 'POST',
            url: API_URL + '/auth/account/register',
            headers: {
                'Content-Type': 'application/json'
            },
            data: JSON.stringify(data),
        };


        axios.request(config)
            .then((response) => {
                setLoadingBackdropOpen(false);
                setOpenRegistrationFailedAlert(false);
                navigate('/success-register');
            })
            .catch((error) => {
                setLoadingBackdropOpen(false);
                setOpenRegistrationFailedAlert(true);
            });

    });

    const handleAlertLoad = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAlertHeight(event.target.clientHeight);
        console.log(event.target.clientHeight);
    };

    const handleEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setEmail(event.target.value);
        validateEmail(event.target.value);
    };

    const handlePasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setPassword(event.target.value);
        validatePassword(event.target.value);
    };

    const handleRepeatPasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newRepeatPassword = event.target.value.trim();
        setRepeatPassword(newRepeatPassword);
        console.log(password);
        console.log(newRepeatPassword);
        validateRepeatPassword(password.trim(), newRepeatPassword);
    }

    const handleLanguageChange = (event: SelectChangeEvent) => {
        setLanguage(event.target.value as string);
        validateLanguage(event.target.value as string);
    };

    const validateLanguage = (language: string) => {
        const isValid = language !== '';
        updateValidationErrors('language', isValid);
        return isValid;
    }

    const validateEmail = (email: string) => {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const isValid = re.test(email);
        updateValidationErrors('email', isValid);
        return isValid;
    }

    const validatePassword = (password: string) => {
        const re = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{9,}$/;
        const isValid = password.length > 8 && re.test(password);
        updateValidationErrors('password', isValid);
        return isValid;
    }

    const validateRepeatPassword = (password: string, repeatPassword: string) => {
        const isValid = password.localeCompare(repeatPassword);
        updateValidationErrors('repeated password', isValid === 0);
        return isValid;
    }

    const updateValidationErrors = (field: string, isValid: boolean) => {
        setValidationErrors((prevErrors) => {
            const updatedErrors = { ...prevErrors };
            if (!isValid) {
                switch (field) {
                    case 'email':
                        updatedErrors[field] = t('registerPanel.emailValidation');
                        break;
                    case 'password':
                        updatedErrors[field] = t('registerPanel.passwordValidation');
                        break;
                    case 'repeated password':
                        updatedErrors[field] = t('registerPanel.repeatPasswordValidation');
                        break;
                    case 'language':
                        updatedErrors[field] = t('registerPanel.languageValidation');
                        break;    
                    default:
                        break;
                }
            } else {
                delete updatedErrors[field];
            }

            if (Object.keys(updatedErrors).length === 0 && email !== '' && password !== '' && repeatPassword !== '' && language !== '') {
                setIsRegisterButtonDisabled(false);
            } else {
                setIsRegisterButtonDisabled(true);
            }

            return updatedErrors;
        });
    }

    const containerStyle = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        width: '100vw'
    };

    const boxStyle = {
        backgroundColor: 'rgba(153, 237, 197, 0.5)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        width: '40%',
        height: '75%',
        borderRadius: '5vh',
        overflow: 'auto',
        paddingTop: '5vh',
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
        alignItems: 'center',
        marginTop: '35vh',
    };

    const headerTextStyle = {
        textAlign: 'center' as const,
        fontSize: '2.8rem',
        color: 'rgba(43, 42, 42, 0.8)',

    };


    return (
        <div className='font-link'>
            <div style={containerStyle} className="font-link">
                <Box sx={boxStyle}>
                    <div style={imageStyle}>
                        <img src={logo} alt='logo' style={{ width: '25%' }} />
                    </div>

                    <div style={headerTextStyle}>
                        {t('registerPanel.register')}
                    </div>

                    <List style={listStyle}>
                        <Box component="form" onSubmit={onSubmit}>
                            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                                <ListItem>
                                    <div className="font-link">
                                        {t('registerPanel.email')}*
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

                                <ListItem>
                                    <div className="font-link">
                                        {t('registerPanel.password')}*
                                    </div>
                                </ListItem>
                                <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                    <TextField
                                        {...register("password")}
                                        required
                                        id="outlined-required-password"
                                        style={{ backgroundColor: 'white', width: '100%' }}
                                        type={showPasswords ? 'text' : 'password'}
                                        variant='standard'
                                        onChange={handlePasswordChange}
                                    />
                                </ListItem>

                                <ListItem>
                                    <div className="font-link">
                                        {t('registerPanel.repeatPassword')}*
                                    </div>
                                </ListItem>
                                <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                    <TextField
                                        {...register("repeatPassword")}
                                        required
                                        id="outlined-required-password"
                                        style={{ backgroundColor: 'white', width: '100%' }}
                                        type={showPasswords ? 'text' : 'password'}
                                        variant='standard'
                                        onChange={handleRepeatPasswordChange}
                                    />
                                </ListItem>


                                <ListItem style={{ display: 'flex' }}>
                                <FormGroup>
                                    <FormControlLabel label={ <div className="font-link"> {t('registerPanel.showPasswords')} </div>} 
                                    control={<Checkbox color="success"
                                                        checked={showPasswords}
                                                        onChange={handleCheckboxShowPasswordsChange} />}/>
                                </FormGroup>
                                </ListItem>

                                <ListItem>
                                    <div className="font-link">
                                        {t('registerPanel.language')}*
                                    </div>
                                </ListItem>
                                <ListItem style={{ display: 'flex' }}>
                                    <Select
                                        labelId="demo-simple-select-label"
                                        id="language-select"
                                        value={language}
                                        label="Language"
                                        onChange={handleLanguageChange}
                                        style={{ width: '50%' }}
                                    >

                                        <MenuItem value={"plPL"}>
                                            <img src={pl} alt="plPL" style={{ width: '10%', height: '10%' }} />
                                        </MenuItem>
                                        <MenuItem value={"enUS"}>
                                            <img src={gb} alt="enUS" style={{ width: '10%', height: '10%' }} />
                                        </MenuItem>
                                    </Select>
                                </ListItem>
                            </div>

                            <ListItem>
                                    <div className="font-link">
                                        {t('registerPanel.requiredInformation')}
                                    </div>
                                </ListItem>

                            <ListItem>
                                <ListItemButton disabled={isRegisterButtonDisabled}>
                                    <ListItemText>
                                        <div className="font-link" style={buttonTextStyle} onClick={handleRegister}>
                                            {t('loginPanel.register')}
                                        </div>
                                    </ListItemText>
                                </ListItemButton>
                            </ListItem>

                            <ListItem>
                                <div>
                                    {t('registerPanel.accountExists')}
                                </div>

                            </ListItem>

                            <ListItem>
                                <ListItemButton>
                                    <ListItemText>
                                        <div className="font-link" style={buttonTextStyle} onClick={handleLogin}>
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

                <Snackbar open={openRegistrationFailedAlert} autoHideDuration={6000} onClose={handleRegistrationFailedAlertClose}>
                    <Alert severity="error" sx={{ width: '100%' }} onClose={handleRegistrationFailedAlertClose}>
                        {t('registerPanel.registrationFailed')}
                    </Alert>
                </Snackbar>

            </div>
            <Backdrop
                sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
                open={loadingBackdropOpen}
            >
                <CircularProgress color="inherit" />
            </Backdrop>
        </div>
    );


}

export default RegistrationPanel;